/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.Jvm;
import net.openhft.lang.Maths;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.io.serialization.impl.NoMarshaller;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;
import net.openhft.lang.pool.StringInterner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author peter.lawrey
 */
@SuppressWarnings("MagicNumber")
public abstract class AbstractBytes implements Bytes {
    private static final int END_OF_BUFFER = Integer.MIN_VALUE;
    private static final long BUSY_LOCK_LIMIT = 10L * 1000 * 1000 * 1000;
    private static final int INT_LOCK_MASK;
    private static final int UNSIGNED_BYTE_MASK = 0xFF;
    private static final int UNSIGNED_SHORT_MASK = 0xFFFF;
    private static final int USHORT_EXTENDED = UNSIGNED_SHORT_MASK;
    public static final long UNSIGNED_INT_MASK = 0xFFFFFFFFL;
    // extra 1 for decimal place.
    private static final int MAX_NUMBER_LENGTH = 1 + (int) Math.ceil(Math.log10(Long.MAX_VALUE));
    private final byte[] numberBuffer = new byte[MAX_NUMBER_LENGTH];
    private static final byte[] RADIX_PARSE = new byte[256];

    static {
        Arrays.fill(RADIX_PARSE, (byte) -1);
        for (int i = 0; i < 10; i++)
            RADIX_PARSE['0' + i] = (byte) i;
        for (int i = 0; i < 26; i++)
            RADIX_PARSE['A' + i] = RADIX_PARSE['a' + i] = (byte) (i + 10);
        INT_LOCK_MASK = 0xFFFFFF;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBytes.class);
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    private static final byte[] MIN_VALUE_TEXT = ("" + Long.MIN_VALUE).getBytes();
    private static final byte[] Infinity = "Infinity".getBytes();
    private static final byte[] NaN = "NaN".getBytes();
    private static final long MAX_VALUE_DIVIDE_5 = Long.MAX_VALUE / 5;
    private static final byte BYTE_MIN_VALUE = Byte.MIN_VALUE;
    private static final byte BYTE_EXTENDED = Byte.MIN_VALUE + 1;
    private static final byte BYTE_MAX_VALUE = Byte.MIN_VALUE + 2;
    private static final short UBYTE_EXTENDED = 0xff;
    private static final short SHORT_MIN_VALUE = Short.MIN_VALUE;
    private static final short SHORT_EXTENDED = Short.MIN_VALUE + 1;
    private static final short SHORT_MAX_VALUE = Short.MIN_VALUE + 2;
    // RandomDataInput
    private static final int INT_MIN_VALUE = Integer.MIN_VALUE;
    private static final int INT_EXTENDED = Integer.MIN_VALUE + 1;
    private static final int INT_MAX_VALUE = Integer.MIN_VALUE + 2;
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;
    private static final byte NULL = 'N';
    private static final byte ENUMED = 'E';
    private static final byte SERIALIZED = 'S';
    private static final byte[] RADIX = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
    private static boolean ID_LIMIT_WARNED = false;
    final AtomicInteger refCount;
    protected boolean finished;
    BytesMarshallerFactory bytesMarshallerFactory;
    private StringInterner stringInterner = null;
    private BytesInputStream inputStream = null;
    private BytesOutputStream outputStream = null;
    private StringBuilder utfReader = null;
    private SimpleDateFormat dateFormat = null;
    private long lastDay = Long.MIN_VALUE;
    @Nullable
    private byte[] lastDateStr = null;
    private Thread currentThread;
    private int shortThreadId = Integer.MIN_VALUE;
    private boolean selfTerminating = false;

    AbstractBytes() {
        this(new VanillaBytesMarshallerFactory(), new AtomicInteger(1));
    }

    AbstractBytes(BytesMarshallerFactory bytesMarshallerFactory, AtomicInteger refCount) {
        this.finished = false;
        this.bytesMarshallerFactory = bytesMarshallerFactory;
        this.refCount = refCount;
    }

    private static boolean equalsCaseIgnore(StringBuilder sb, String s) {
        if (sb.length() != s.length())
            return false;
        for (int i = 0; i < s.length(); i++)
            if (Character.toLowerCase(sb.charAt(i)) != s.charAt(i))
                return false;
        return true;
    }

    private static double asDouble(long value, int exp, boolean negative, int decimalPlaces) {
        if (decimalPlaces > 0 && value < Long.MAX_VALUE / 2) {
            if (value < Long.MAX_VALUE / (1L << 32)) {
                exp -= 32;
                value <<= 32;
            }
            if (value < Long.MAX_VALUE / (1L << 16)) {
                exp -= 16;
                value <<= 16;
            }
            if (value < Long.MAX_VALUE / (1L << 8)) {
                exp -= 8;
                value <<= 8;
            }
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
            }
        }
        for (; decimalPlaces > 0; decimalPlaces--) {
            exp--;
            long mod = value % 5;
            value /= 5;
            int modDiv = 1;
            if (value < Long.MAX_VALUE / (1L << 4)) {
                exp -= 4;
                value <<= 4;
                modDiv <<= 4;
            }
            if (value < Long.MAX_VALUE / (1L << 2)) {
                exp -= 2;
                value <<= 2;
                modDiv <<= 2;
            }
            if (value < Long.MAX_VALUE / (1L << 1)) {
                exp -= 1;
                value <<= 1;
                modDiv <<= 1;
            }
            if (decimalPlaces > 1)
                value += modDiv * mod / 5;
            else
                value += (modDiv * mod + 4) / 5;
        }
        final double d = Math.scalb((double) value, exp);
        return negative ? -d : d;
    }

    private static void warnIdLimit(long id) {
        LOGGER.warn("High thread id may result in collisions id: {}", id);

        ID_LIMIT_WARNED = true;
    }

    @Override
    public long size() {
        return capacity();
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException("Forcing a free() via Bytes is unsafe, try reserve() + release()");
    }

    @Override
    public void reserve() {
        if (refCount.get() < 1) throw new IllegalStateException();
        refCount.incrementAndGet();
    }

    @Override
    public void release() {
        if (refCount.get() < 1) throw new IllegalStateException();
        if (refCount.decrementAndGet() > 0) return;
        cleanup();
    }

    protected abstract void cleanup();

    @Override
    public int refCount() {
        return refCount.get();
    }

    StringInterner stringInterner() {
        if (stringInterner == null)
            stringInterner = new StringInterner(8 * 1024);
        return stringInterner;
    }

    @Override
    public void selfTerminating(boolean selfTerminating) {
        this.selfTerminating = selfTerminating;
    }

    @Override
    public boolean selfTerminating() {
        return selfTerminating;
    }

    @Override
    public int readUnsignedByteOrThrow() throws BufferUnderflowException {
        return readByteOrThrow(selfTerminating);
    }

    public int readByteOrThrow(boolean selfTerminating) throws BufferUnderflowException {
        return remaining() < 1 ? returnOrThrowEndOfBuffer(selfTerminating) : readUnsignedByte();
    }

    static int returnOrThrowEndOfBuffer(boolean selfTerminating) {
        if (selfTerminating) return END_OF_BUFFER;
        throw new BufferUnderflowException();
    }

    @Override
    public Boolean parseBoolean(@NotNull StopCharTester tester) {
        StringBuilder sb = acquireUtfReader();
        parseUTF(sb, tester);
        if (sb.length() == 0)
            return null;
        switch (sb.charAt(0)) {
            case 't':
            case 'T':
                return sb.length() == 1 || equalsCaseIgnore(sb, "true") ? true : null;
            case 'y':
            case 'Y':
                return sb.length() == 1 || equalsCaseIgnore(sb, "yes") ? true : null;
            case '0':
                return sb.length() == 1 ? false : null;
            case '1':
                return sb.length() == 1 ? true : null;
            case 'f':
            case 'F':
                return sb.length() == 1 || equalsCaseIgnore(sb, "false") ? false : null;
            case 'n':
            case 'N':
                return sb.length() == 1 || equalsCaseIgnore(sb, "no") ? false : null;
        }
        return null;
    }

    @Override
    public void readFully(@NotNull byte[] bytes) {
        readFully(bytes, 0, bytes.length);
    }

    @Override
    public int skipBytes(int n) {
        return (int) skip(n);
    }

    @Override
    public boolean readBoolean() {
        return readByteOrThrow(false) != 0;
    }

    @Override
    public boolean readBoolean(long offset) {
        return readByte(offset) != 0;
    }

    @Override
    public int readUnsignedByte() {
        return readByte() & UNSIGNED_BYTE_MASK;
    }

    @Override
    public int readUnsignedByte(long offset) {
        return readByte(offset) & UNSIGNED_BYTE_MASK;
    }

    @Override
    public int readUnsignedShort() {
        return readShort() & UNSIGNED_SHORT_MASK;
    }

    @Override
    public int readUnsignedShort(long offset) {
        return readShort(offset) & UNSIGNED_SHORT_MASK;
    }

    @NotNull
    @Override
    public String readLine() {
        StringBuilder input = acquireUtfReader();
        EOL:
        while (position() < capacity()) {
            int c = readUnsignedByteOrThrow();
            switch (c) {
                case END_OF_BUFFER:
                case '\n':
                    break EOL;
                case '\r':
                    long cur = position();
                    if (cur < capacity() && readByte(cur) == '\n')
                        position(cur + 1);
                    break EOL;
                default:
                    input.append((char) c);
                    break;
            }
        }
        return stringInterner().intern(input);
    }

    // RandomDataOutput

    @Nullable
    @Override
    public String readUTFΔ() {
        if (readUTFΔ(acquireUtfReader()))
            return utfReader.length() == 0 ? "" : stringInterner().intern(utfReader);
        return null;
    }

    @Nullable
    @Override
    public String readUTFΔ(long offset) throws IllegalStateException {
        long position = position();
        try {
            position(offset);
            return readUTFΔ();
        } finally {
            position(position);
        }
    }

    @NotNull
    private StringBuilder acquireUtfReader() {
        if (utfReader == null)
            utfReader = new StringBuilder(128);
        else
            utfReader.setLength(0);
        return utfReader;
    }

    @Override
    public boolean readUTFΔ(@NotNull StringBuilder stringBuilder) {
        try {
            stringBuilder.setLength(0);
            return appendUTF0(stringBuilder);
        } catch (IOException unexpected) {
            throw new IllegalStateException(unexpected);
        }
    }

    @SuppressWarnings("MagicNumber")
    private boolean appendUTF0(@NotNull Appendable appendable) throws IOException {
        long len = readStopBit();
        if (len < -1 || len > Integer.MAX_VALUE)
            throw new StreamCorruptedException("UTF length invalid " + len);
        if (len == -1)
            return false;
        int utflen = (int) len;
        readUTF0(appendable, utflen);
        return true;
    }

    private void readUTF0(@NotNull Appendable appendable, int utflen) throws IOException {
        int count = 0;
        while (count < utflen) {
            int c = readUnsignedByteOrThrow();
            if (c >= 128) {
                position(position() - 1);
                break;
            } else if (c < 0) {

            }
            count++;
            appendable.append((char) c);
        }

        while (count < utflen) {
            int c = readUnsignedByte();
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                /* 0xxxxxxx */
                    count++;
                    appendable.append((char) c);
                    break;
                case 12:
                case 13: {
                /* 110x xxxx 10xx xxxx */
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    int char2 = readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    int c2 = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    appendable.append((char) c2);
                    break;
                }
                case 14: {
                /* 1110 xxxx 10xx xxxx 10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    int char2 = readUnsignedByte();
                    int char3 = readUnsignedByte();

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count - 1));
                    int c3 = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    appendable.append((char) c3);
                    break;
                }
                default:
                /* 10xx xxxx, 1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
    }

    @NotNull
    @Override
    public String parseUTF(@NotNull StopCharTester tester) {
        parseUTF(acquireUtfReader(), tester);
        return stringInterner().intern(utfReader);
    }

    @Override
    public void parseUTF(@NotNull StringBuilder builder, @NotNull StopCharTester tester) {
        builder.setLength(0);
        try {
            readUTF0(builder, tester);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void readUTF0(@NotNull Appendable appendable, @NotNull StopCharTester tester) throws IOException {
        while (true) {
            int c = readUnsignedByteOrThrow();
            if (c >= 128) {
                position(position() - 1);
                break;
            }
            if (tester.isStopChar(c))
                return;
            appendable.append((char) c);
        }

        while (true) {
            int c = readUnsignedByteOrThrow();
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                /* 0xxxxxxx */
                    if (tester.isStopChar(c))
                        return;
                    appendable.append((char) c);
                    break;
                case 12:
                case 13: {
                /* 110x xxxx 10xx xxxx */
                    int char2 = readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte");
                    int c2 = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    if (tester.isStopChar(c2))
                        return;
                    appendable.append((char) c2);
                    break;
                }
                case 14: {
                /* 1110 xxxx 10xx xxxx 10xx xxxx */

                    int char2 = readUnsignedByte();
                    int char3 = readUnsignedByte();

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte ");
                    int c3 = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    if (tester.isStopChar(c3))
                        return;
                    appendable.append((char) c3);
                    break;
                }
                default:
                /* 10xx xxxx, 1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte ");
            }
        }
    }

    @Override
    public boolean stepBackAndSkipTo(@NotNull StopCharTester tester) {
        if (position() > 0)
            position(position() - 1);
        return skipTo(tester);
    }

    @Override
    public boolean skipTo(@NotNull StopCharTester tester) {
        while (remaining() > 0) {
            int ch = readUnsignedByteOrThrow();
            if (tester.isStopChar(ch))
                return true;
        }
        return false;
    }

    @NotNull
    @Override
    public String readUTF() {
        try {
            int len = readUnsignedShort();
            readUTF0(acquireUtfReader(), len);
            return utfReader.length() == 0 ? "" : stringInterner().intern(utfReader);
        } catch (IOException unexpected) {
            throw new AssertionError(unexpected);
        }
    }

    @Override
    public short readCompactShort() {
        byte b = readByte();
        switch (b) {
            case BYTE_MIN_VALUE:
                return Short.MIN_VALUE;
            case BYTE_MAX_VALUE:
                return Short.MAX_VALUE;
            case BYTE_EXTENDED:
                return readShort();
            default:
                return b;
        }
    }

    @Override
    public int readCompactUnsignedShort() {
        int b = readUnsignedByte();
        if (b == UBYTE_EXTENDED)
            return readUnsignedShort();
        return b;
    }

    @Override
    public int readInt24() {
        int b = readUnsignedByte();
        int s = readUnsignedShort();
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((b << 24) + (s << 8)) >> 8;
        // extra shifting to get sign extension.
        return ((b << 8) + (s << 16)) >> 8;
    }

    @Override
    public int readInt24(long offset) {
        int b = readUnsignedByte(offset);
        int s = readUnsignedShort(offset + 1);
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((b << 24) + (s << 8)) >> 8;
        // extra shifting to get sign extension.
        return ((b << 8) + (s << 16)) >> 8;
    }

    @Override
    public long readUnsignedInt() {
        return readInt() & UNSIGNED_INT_MASK;
    }

    @Override
    public long readUnsignedInt(long offset) {
        return readInt(offset) & UNSIGNED_INT_MASK;
    }

    @Override
    public int readCompactInt() {
        short b = readShort();
        switch (b) {
            case SHORT_MIN_VALUE:
                return Integer.MIN_VALUE;
            case SHORT_MAX_VALUE:
                return Integer.MAX_VALUE;
            case SHORT_EXTENDED:
                return readInt();
            default:
                return b;
        }
    }

    @Override
    public long readCompactUnsignedInt() {
        int b = readUnsignedShort();
        if (b == USHORT_EXTENDED)
            return readUnsignedInt();
        return b;
    }

    @Override
    public long readInt48() {
        long s = readUnsignedShort();
        long l = readUnsignedInt();
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((s << 48) + (l << 16)) >> 16;
        // extra shifting to get sign extension.
        return ((s << 16) + (l << 32)) >> 16;
    }

    @Override
    public long readInt48(long offset) {
        long s = readUnsignedShort(offset);
        long l = readUnsignedInt(offset + 2);
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((s << 48) + (l << 16)) >> 16;
        // extra shifting to get sign extension.
        return ((s << 16) + (l << 32)) >> 16;
    }

    @Override
    public long readCompactLong() {
        int b = readInt();
        switch (b) {
            case INT_MIN_VALUE:
                return Long.MIN_VALUE;
            case INT_MAX_VALUE:
                return Long.MAX_VALUE;
            case INT_EXTENDED:
                return readLong();
            default:
                return b;
        }
    }

    @Override
    public long readStopBit() {
        long l;
        if ((l = readByte()) >= 0)
            return l;
        l &= 0x7FL;
        long b;
        int count = 7;
        while ((b = readByte()) < 0) {
            l |= (b & 0x7FL) << count;
            count += 7;
        }
        if (b != 0) {
            if (count > 56)
                throw new IllegalStateException(
                        "Cannot read more than 9 stop bits of positive value");
            return l | (b << count);
        } else {
            if (count > 63)
                throw new IllegalStateException(
                        "Cannot read more than 10 stop bits of negative value");
            return ~l;
        }
    }

    @Override
    public double readCompactDouble() {
        float f = readFloat();
        if (Float.isNaN(f))
            return readDouble();
        return f;
    }

    @Override
    public void read(@NotNull ByteBuffer bb) {
        int len = (int) Math.min(bb.remaining(), remaining());
        if (bb.order() == byteOrder()) {
            while (len >= 8) {
                bb.putLong(readLong());
                len -= 8;
            }
        }
        while (len > 0) {
            bb.put(readByte());
            len--;
        }
    }

    // // RandomOutputStream
    @Override
    public void write(@NotNull byte[] bytes) {
        int length = bytes.length;
        checkWrite(length);
        write(bytes, 0, length);
    }

    private void checkWrite(int length) {
        if (length > remaining())
            throw new IllegalStateException("Cannot write " + length + " only " + remaining() + " remaining");
    }

    @Override
    public void writeBoolean(boolean v) {
        write(v ? -1 : 0);
    }

    @Override
    public void writeBoolean(long offset, boolean v) {
        writeByte(offset, v ? -1 : 0);
    }

    @Override
    public void writeBytes(@NotNull String s) {
        int len = s.length();
        for (int i = 0; i < len; i++)
            write(s.charAt(i));
    }

    @Override
    public void writeChars(@NotNull String s) {
        int len = s.length();
        for (int i = 0; i < len; i++)
            writeChar(s.charAt(i));
    }

    @Override
    public void writeUTF(@NotNull String str) {
        long strlen = str.length();
        long utflen = findUTFLength(str, strlen);
        if (utflen > 65535)
            throw new IllegalStateException("String too long " + utflen + " when encoded, max: 65535");
        writeUnsignedShort((int) utflen);
        writeUTF0(str, strlen);
    }

    @Override
    public void writeUTFΔ(@Nullable CharSequence str) {
        if (str == null) {
            writeStopBit(-1);
            return;
        }
        long strlen = str.length();
        long utflen = findUTFLength(str, strlen);
        writeStopBit(utflen);
        writeUTF0(str, strlen);
    }

    @Override
    public void writeUTFΔ(long offset, int maxSize, @Nullable CharSequence s) throws IllegalStateException {
        assert maxSize > 1;
        if (s == null) {
            position(offset);
            writeStopBit(-1);
            return;
        }
        long strlen = s.length();
        long utflen = findUTFLength(s, strlen);
        long totalSize = IOTools.stopBitLength(utflen) + utflen;
        if (totalSize > maxSize)
            throw new IllegalStateException("Attempted to write " + totalSize + " byte String, when only " + maxSize + " allowed");
        long position = position();
        try {
            position(offset);
            writeStopBit(utflen);
            writeUTF0(s, strlen);
        } finally {
            position(position);
        }
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence str) {
        long strlen = str.length();
        writeUTF0(str, strlen);
        return this;
    }

    private long findUTFLength(@NotNull CharSequence str, long strlen) {
        long utflen = 0, c;/* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0000) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > remaining())
            throw new IllegalArgumentException(
                    "encoded string too long: " + utflen + " bytes, remaining=" + remaining());
        return utflen;
    }

    private void writeUTF0(@NotNull CharSequence str, long strlen) {
        int c;
        int i;
        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0000) && (c <= 0x007F)))
                break;
            write(c);
        }

        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0000) && (c <= 0x007F)) {
                write(c);

            } else if (c > 0x07FF) {
                write((byte) (0xE0 | ((c >> 12) & 0x0F)));
                write((byte) (0x80 | ((c >> 6) & 0x3F)));
                write((byte) (0x80 | (c & 0x3F)));
            } else {
                write((byte) (0xC0 | ((c >> 6) & 0x1F)));
                write((byte) (0x80 | c & 0x3F));
            }
        }
    }

    @Override
    public void writeByte(int v) {
        write(v);
    }

    @Override
    public void writeUnsignedByte(int v) {
        writeByte(v);
    }

    @Override
    public void writeUnsignedByte(long offset, int v) {
        writeByte(offset, v);
    }

    @Override
    public void write(long offset, @NotNull byte[] bytes) {
        checkWrite(bytes.length);
        for (int i = 0; i < bytes.length; i++)
            writeByte(offset + i, bytes[i]);
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        checkWrite(len);

        for (int i = 0; i < len; i++)
            write(bytes[off + i]);
    }

    @Override
    public void writeUnsignedShort(int v) {
        writeShort(v);
    }

    @Override
    public void writeUnsignedShort(long offset, int v) {
        writeShort(offset, v);
    }

    @Override
    public void writeCompactShort(int v) {
        if (v > BYTE_MAX_VALUE && v <= Byte.MAX_VALUE)
            writeByte(v);
        else
            switch (v) {
                case Short.MIN_VALUE:
                    writeByte(BYTE_MIN_VALUE);
                    break;
                case Short.MAX_VALUE:
                    writeByte(BYTE_MAX_VALUE);
                    break;
                default:
                    writeByte(BYTE_EXTENDED);
                    writeShort(v);
                    break;
            }
    }

    @Override
    public void writeCompactUnsignedShort(int v) {
        if (v >= 0 && v < USHORT_EXTENDED) {
            writeByte(v);
        } else {
            writeUnsignedShort(USHORT_EXTENDED);
            writeUnsignedShort(v);
        }
    }

    @Override
    public void writeInt24(int v) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN) {
            writeUnsignedByte(v >>> 16);
            writeUnsignedShort(v);
        } else {
            writeUnsignedByte(v);
            writeUnsignedShort(v >>> 8);
        }
    }

    @Override
    public void writeInt24(long offset, int v) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN) {
            writeUnsignedByte(offset, v >>> 16);
            writeUnsignedShort(offset + 1, v);
        } else {
            writeUnsignedByte(offset, v);
            writeUnsignedShort(offset + 1, v >>> 8);
        }
    }

    @Override
    public void writeUnsignedInt(long v) {
        writeInt((int) v);
    }

    @Override
    public void writeUnsignedInt(long offset, long v) {
        writeInt(offset, (int) v);
    }

    @Override
    public void writeCompactInt(int v) {
        if (v > SHORT_MAX_VALUE && v <= Short.MAX_VALUE)
            writeShort(v);
        else
            switch (v) {
                case Integer.MIN_VALUE:
                    writeShort(SHORT_MIN_VALUE);
                    break;
                case Integer.MAX_VALUE:
                    writeShort(SHORT_MAX_VALUE);
                    break;
                default:
                    writeShort(SHORT_EXTENDED);
                    writeInt(v);
                    break;
            }
    }

    @Override
    public void writeCompactUnsignedInt(long v) {
        if (v >= 0 && v < USHORT_EXTENDED) {
            writeShort((int) v);
        } else {
            writeShort(USHORT_EXTENDED);
            writeUnsignedInt(v);
        }
    }

    @Override
    public void writeInt48(long v) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN) {
            writeUnsignedShort((int) (v >>> 32));
            writeUnsignedInt(v);
        } else {
            writeUnsignedShort((int) v);
            writeUnsignedInt(v >>> 16);
        }
    }

    @Override
    public void writeInt48(long offset, long v) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN) {
            writeUnsignedShort(offset, (int) (v >>> 32));
            writeUnsignedInt(offset + 2, v);
        } else {
            writeUnsignedShort(offset, (int) v);
            writeUnsignedInt(offset + 2, v >>> 16);
        }
    }

    @Override
    public void writeCompactLong(long v) {
        if (v > INT_MAX_VALUE && v <= Integer.MAX_VALUE) {
            writeInt((int) v);

        } else if (v == Long.MIN_VALUE) {
            writeInt(INT_MIN_VALUE);

        } else if (v == Long.MAX_VALUE) {
            writeInt(INT_MAX_VALUE);

        } else {
            writeInt(INT_EXTENDED);
            writeLong(v);

        }
    }

    @Override
    public void writeStopBit(long n) {
        boolean neg = false;
        if (n < 0) {
            neg = true;
            n = ~n;
        }
        long n2;
        while ((n2 = n >>> 7) != 0) {
            writeByte((byte) (0x80L | n));
            n = n2;
        }
        // final byte
        if (!neg) {
            writeByte((byte) n);
        } else {
            writeByte((byte) (0x80L | n));
            writeByte(0);
        }
    }

    @Override
    public void writeCompactDouble(double v) {
        float f = (float) v;
        if (f == v) {
            writeFloat(f);
        } else {
            writeFloat(Float.NaN);
            writeDouble(v);
        }
    }

    @Override
    public void write(@NotNull ByteBuffer bb) {
        if (bb.order() == byteOrder())
            while (bb.remaining() >= 8)
                writeLong(bb.getLong());
        while (bb.remaining() >= 1)
            writeByte(bb.get());
    }

    // ByteStringAppender
    @NotNull
    @Override
    public ByteStringAppender append(@NotNull CharSequence s, int start, int end) {
        for (int i = start, len = Math.min(end, s.length()); i < len; i++)
            writeByte(s.charAt(i));
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender append(@Nullable Enum value) {
        return value == null ? this : append(value.toString());
    }

    @NotNull
    @Override
    public ByteStringAppender append(boolean b) {
        append(b ? "true" : "false");
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender append(char c) {
        writeByte(c);
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender append(int num) {
        return append((long) num);
    }

    @NotNull
    @Override
    public ByteStringAppender append(long num) {
        if (num < 0) {
            if (num == Long.MIN_VALUE) {
                write(MIN_VALUE_TEXT);
                return this;
            }
            writeByte('-');
            num = -num;
        }
        if (num == 0) {
            writeByte('0');

        } else {
            appendLong0(num);
        }
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender append(long num, int base) {
        if (base < 2 || base > Character.MAX_RADIX)
            throw new IllegalArgumentException("Invalid base: " + base);
        if (num < 0) {
            if (num == Long.MIN_VALUE) {
                writeBytes(BigInteger.valueOf(num).toString(base));
                return this;
            }
            writeByte('-');
            num = -num;
        }
        if (num == 0) {
            writeByte('0');

        } else {
            while (num > 0) {
                writeByte(RADIX[((int) (num % base))]);
                num /= base;
            }
        }
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender appendDateMillis(long timeInMS) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        long date = timeInMS / 86400000;
        if (lastDay != date) {
            lastDateStr = dateFormat.format(new Date(timeInMS)).getBytes(ISO_8859_1);
            lastDay = date;
        } else {
            assert lastDateStr != null;
        }
        write(lastDateStr);
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        appendDateMillis(timeInMS);
        writeByte('T');
        appendTimeMillis(timeInMS % 86400000L);
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender appendTimeMillis(long timeInMS) {
        int hours = (int) (timeInMS / (60 * 60 * 1000));
        if (hours > 99) {
            appendLong0(hours); // can have over 24 hours.
        } else {
            writeByte((char) (hours / 10 + '0'));
            writeByte((char) (hours % 10 + '0'));
        }
        writeByte(':');
        int minutes = (int) ((timeInMS / (60 * 1000)) % 60);
        writeByte((char) (minutes / 10 + '0'));
        writeByte((char) (minutes % 10 + '0'));
        writeByte(':');
        int seconds = (int) ((timeInMS / 1000) % 60);
        writeByte((char) (seconds / 10 + '0'));
        writeByte((char) (seconds % 10 + '0'));
        writeByte('.');
        int millis = (int) (timeInMS % 1000);
        writeByte((char) (millis / 100 + '0'));
        writeByte((char) (millis / 10 % 10 + '0'));
        writeByte((char) (millis % 10 + '0'));
        return this;
    }

    @NotNull
    @Override
    public ByteStringAppender append(double d) {
        long val = Double.doubleToRawLongBits(d);
        int sign = (int) (val >>> 63);
        int exp = (int) ((val >>> 52) & 2047);
        long mantissa = val & ((1L << 52) - 1);
        if (sign != 0) {
            writeByte('-');
        }
        if (exp == 0 && mantissa == 0) {
            writeByte('0');
            return this;
        } else if (exp == 2047) {
            if (mantissa == 0) {
                write(Infinity);
            } else {
                write(NaN);
            }
            return this;
        } else if (exp > 0) {
            mantissa += 1L << 52;
        }
        final int shift = (1023 + 52) - exp;
        if (shift > 0) {
            // integer and faction
            if (shift < 53) {
                long intValue = mantissa >> shift;
                appendLong0(intValue);
                mantissa -= intValue << shift;
                if (mantissa > 0) {
                    writeByte('.');
                    mantissa <<= 1;
                    mantissa++;
                    int precision = shift + 1;
                    long error = 1;

                    long value = intValue;
                    int decimalPlaces = 0;
                    while (mantissa > error) {
                        // times 5*2 = 10
                        mantissa *= 5;
                        error *= 5;
                        precision--;
                        long num = (mantissa >> precision);
                        value = value * 10 + num;
                        writeByte((char) ('0' + num));
                        mantissa -= num << precision;

                        final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                        if (parsedValue == d)
                            break;
                    }
                }
                return this;

            } else {
                // faction.
                writeByte('0');
                writeByte('.');
                mantissa <<= 6;
                mantissa += (1 << 5);
                int precision = shift + 6;

                long error = (1 << 5);

                long value = 0;
                int decimalPlaces = 0;
                while (mantissa > error) {
                    while (mantissa > MAX_VALUE_DIVIDE_5) {
                        mantissa >>>= 1;
                        error = (error + 1) >>> 1;
                        precision--;
                    }
                    // times 5*2 = 10
                    mantissa *= 5;
                    error *= 5;
                    precision--;
                    if (precision >= 64) {
                        decimalPlaces++;
                        writeByte('0');
                        continue;
                    }
                    long num = (mantissa >>> precision);
                    value = value * 10 + num;
                    final char c = (char) ('0' + num);
                    assert !(c < '0' || c > '9');
                    writeByte(c);
                    mantissa -= num << precision;
                    final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                    if (parsedValue == d)
                        break;
                }
                return this;
            }
        }
        // large number
        mantissa <<= 10;
        int precision = -10 - shift;
        int digits = 0;
        while ((precision > 53 || mantissa > Long.MAX_VALUE >> precision) && precision > 0) {
            digits++;
            precision--;
            long mod = mantissa % 5;
            mantissa /= 5;
            int modDiv = 1;
            while (mantissa < MAX_VALUE_DIVIDE_5 && precision > 1) {
                precision -= 1;
                mantissa <<= 1;
                modDiv <<= 1;
            }
            mantissa += modDiv * mod / 5;
        }
        long val2 = precision > 0 ? mantissa << precision : mantissa >>> -precision;

        appendLong0(val2);
        for (int i = 0; i < digits; i++)
            writeByte('0');

        return this;
    }

    @Override
    public double parseDouble() {
        long value = 0;
        int exp = 0;
        boolean negative = false;
        int decimalPlaces = Integer.MIN_VALUE;
        while (true) {
            int ch = readUnsignedByteOrThrow();
            if (ch >= '0' && ch <= '9') {
                while (value >= MAX_VALUE_DIVIDE_10) {
                    value >>>= 1;
                    exp++;
                }
                value = value * 10 + (ch - '0');
                decimalPlaces++;
            } else if (ch == '-') {
                negative = true;
            } else if (ch == '.') {
                decimalPlaces = 0;
            } else {
                break;
            }
        }

        return asDouble(value, exp, negative, decimalPlaces);
    }

    @NotNull
    @Override
    public <E> ByteStringAppender append(@NotNull Iterable<E> list, @NotNull CharSequence separator) {
        if (list instanceof RandomAccess && list instanceof List) {
            return append((List<E>) list, separator);
        }
        int i = 0;
        for (E e : list) {
            if (i++ > 0)
                append(separator);
            if (e != null)
                append(e.toString());
        }
        return this;
    }

    @NotNull
    <E> ByteStringAppender append(@NotNull List<E> list, @NotNull CharSequence separator) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                append(separator);
            E e = list.get(i);
            if (e != null)
                append(e.toString());
        }
        return this;
    }

    @NotNull
    @Override
    public MutableDecimal parseDecimal(@NotNull MutableDecimal decimal) {
        long num = 0, scale = Long.MIN_VALUE;
        boolean negative = false;
        while (true) {
            int b = readUnsignedByteOrThrow();
            // if (b >= '0' && b <= '9')
            if ((b - ('0' + Integer.MIN_VALUE)) <= 9 + Integer.MIN_VALUE) {
                num = num * 10 + b - '0';
                scale++;
            } else if (b == '.') {
                scale = 0;
            } else if (b == '-') {
                negative = true;
            } else {
                break;
            }
        }
        if (negative)
            num = -num;
        decimal.set(num, scale > 0 ? (int) scale : 0);
        return decimal;
    }

    @Override
    public long parseLong() {
        long num = 0;
        boolean negative = false;
        while (true) {
            int b = readUnsignedByteOrThrow();
            // if (b >= '0' && b <= '9')
            if ((b - ('0' + Integer.MIN_VALUE)) <= 9 + Integer.MIN_VALUE)
                num = num * 10 + b - '0';
            else if (b == '-')
                negative = true;
            else
                break;
        }
        return negative ? -num : num;
    }

    @Override
    public long parseLong(int base) {
        if (base < 2 || base > Character.MAX_RADIX)
            throw new IllegalArgumentException("Invalid base:" + base);
        long num = 0;
        boolean negative = false;
        while (true) {
            int b = readUnsignedByteOrThrow();
            byte rp = RADIX_PARSE[b];
            if (rp >= 0 && rp < base) {
                num = num * base + rp;
            } else if (b == '-')
                negative = true;
            else
                break;
        }
        return negative ? -num : num;
    }

    private void appendLong0(long num) {
        // Extract digits into the end of the numberBuffer
        int endIndex = appendLong1(num);

        // Bulk copy the digits into the front of the buffer
        write(numberBuffer, endIndex, MAX_NUMBER_LENGTH - endIndex);
    }

    private int appendLong1(long num) {
        numberBuffer[19] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 19;
        numberBuffer[18] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 18;
        numberBuffer[17] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 17;
        numberBuffer[16] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 16;
        numberBuffer[15] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 15;
        numberBuffer[14] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 14;
        numberBuffer[13] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 13;
        numberBuffer[12] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 12;
        numberBuffer[11] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 11;
        numberBuffer[10] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 10;
        numberBuffer[9] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 9;
        numberBuffer[8] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 8;
        numberBuffer[7] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 7;
        numberBuffer[6] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 6;
        numberBuffer[5] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 5;
        numberBuffer[4] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 4;
        numberBuffer[3] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 3;
        numberBuffer[2] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0)
            return 2;
        numberBuffer[1] = (byte) (num % 10L + '0');
        return 1;
    }

    @NotNull
    @Override
    public ByteStringAppender append(double d, int precision) {
        if (precision < 0)
            precision = 0;
        long power10 = Maths.power10(precision);
        if (power10 < 0)
            power10 = 100000000000000000L;
        if (d < 0) {
            d = -d;
            writeByte('-');
        }
        double d2 = d * power10;
        if (d2 > Long.MAX_VALUE || d2 < Long.MIN_VALUE + 1)
            return append(d);
        long val = (long) (d2 + 0.5);
        while (precision > 1 && val % 10 == 0) {
            val /= 10;
            precision--;
        }
        if (precision > 0 && val % 10 == 0) {
            val = (val + 5) / 10;
            precision--;
        }
        if (precision > 0)
            appendDouble0(val, precision);
        else
            appendLong0(val);
        return this;
    }

    private void appendDouble0(long num, int precision) {
        // Extract digits into the end of the numberBuffer
        // Once desired precision is reached, write the '.'
        int endIndex = appendDouble1(num, precision);

        // Bulk copy the digits into the front of the buffer
        // TODO: Can this be avoided with use of correctly offset bulk appends on Excerpt?
        // Uses (numberBufferIdx - 1) because index was advanced one too many times

        write(numberBuffer, endIndex, MAX_NUMBER_LENGTH - endIndex);
    }

    private int appendDouble1(long num, int precision) {
        int endIndex = MAX_NUMBER_LENGTH;
        int maxEnd = MAX_NUMBER_LENGTH - precision - 2;
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 1)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 2)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 3)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 4)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 5)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 6)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 7)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 8)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 9)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 10)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 11)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 12)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 13)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 14)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 15)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 16)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 17)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0 && endIndex <= maxEnd)
            return endIndex;
        if (precision == 18)
            numberBuffer[--endIndex] = (byte) '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        return endIndex;
    }

    @NotNull
    @Override
    public ByteStringAppender append(@NotNull MutableDecimal md) {
        StringBuilder sb = acquireUtfReader();
        md.toString(sb);
        append(sb);
        return this;
    }

    @NotNull
    @Override
    public InputStream inputStream() {
        if (inputStream == null)
            inputStream = new BytesInputStream();
        return inputStream;
    }

    @NotNull
    @Override
    public OutputStream outputStream() {
        if (outputStream == null)
            outputStream = new BytesOutputStream();
        return outputStream;
    }

    @NotNull
    @Override
    public BytesMarshallerFactory bytesMarshallerFactory() {
        return bytesMarshallerFactory == null ? bytesMarshallerFactory = new VanillaBytesMarshallerFactory() : bytesMarshallerFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> void writeEnum(@Nullable E e) {
        Class aClass;
        if (e == null || e instanceof CharSequence)
            aClass = String.class;
        else
            aClass = (Class) e.getClass();
        BytesMarshaller<E> em = bytesMarshallerFactory().acquireMarshaller(aClass, true);
        em.write(this, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E readEnum(@NotNull Class<E> eClass) {
        BytesMarshaller<E> em = bytesMarshallerFactory().acquireMarshaller(eClass, true);
        return em.read(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Enum<E>> E parseEnum(@NotNull Class<E> eClass, @NotNull StopCharTester tester) {
        String text = parseUTF(tester);
        if (text.isEmpty())
            return null;
        return Enum.valueOf(eClass, text);
    }

    @Override
    public <E> void writeList(@NotNull Collection<E> list) {
        writeStopBit(list.size());
        for (E e : list)
            writeEnum(e);
    }

    @Override
    public <K, V> void writeMap(@NotNull Map<K, V> map) {
        writeStopBit(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            writeEnum(entry.getKey());
            writeEnum(entry.getValue());
        }
    }

    @Override
    public <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass) {
        int len = (int) readStopBit();
        list.clear();
        for (int i = 0; i < len; i++) {
            @SuppressWarnings("unchecked")
            E e = readEnum(eClass);
            list.add(e);
        }
    }

    @Override
    @NotNull
    public <K, V> Map<K, V> readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        int len = (int) readStopBit();
        map.clear();
        for (int i = 0; i < len; i++)
            map.put(readEnum(kClass), readEnum(vClass));
        return map;
    }

    @Override
    public int available() {
        return (int) Math.min(Integer.MAX_VALUE, remaining());
    }

    @Override
    public int read() {
        return remaining() > 0 ? readUnsignedByte() : -1;
    }

    @Override
    public int read(@NotNull byte[] bytes) {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public abstract int read(@NotNull byte[] bytes, int off, int len);

    @Override
    public long skip(long n) {
        if (n < 0)
            throw new IllegalArgumentException("Skip bytes out of range, was " + n);
        if (n > remaining())
            n = remaining();
        position(position() + n);
        return n;
    }

    @Override
    public void close() {
        if (!isFinished())
            finish();
    }

    @Override
    public void finish() throws IndexOutOfBoundsException {
        if (remaining() < 0)
            throwOverflow();
        finished = true;
    }

    private void throwOverflow() throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Buffer overflow, capacity: " + capacity() + " position: " + position());
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public AbstractBytes clear() {
        finished = false;
        position(0L);
        limit(capacity());
        return this;
    }

    @Override
    public Bytes flip() {
        limit(position());
        position(0);
        return this;
    }

    @Override
    public void flush() {
        checkEndOfBuffer();
    }

    @Nullable
    @Override
    public Object readObject() {
        int type = readUnsignedByteOrThrow();
        switch (type) {
            case END_OF_BUFFER:
            case NULL:
                return null;
            case ENUMED: {
                Class clazz = readEnum(Class.class);
                assert clazz != null;
                return readEnum(clazz);
            }
            case SERIALIZED: {
                return bytesMarshallerFactory.readSerializable(this);
            }
            default:
                BytesMarshaller<Object> m = bytesMarshallerFactory().getMarshaller((byte) type);
                if (m == null)
                    throw new IllegalStateException("Unknown type " + (char) type);
                return m.read(this);
        }
    }

    @Nullable
    @Override
    public <T> T readObject(Class<T> tClass) throws IllegalStateException {
        Object o = readObject();
        if (o == null || tClass.isInstance(o))
            return (T) o;
        throw new ClassCastException("Cannot convert " + o.getClass().getName() + " to " + tClass.getName() + " was " + o);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T readInstance(@NotNull Class<T> objClass, T obj) {
        try {
            if (BytesMarshallable.class.isAssignableFrom(objClass)) {
                if (obj == null)
                    obj = (T) NativeBytes.UNSAFE.allocateInstance(objClass);
                ((BytesMarshallable) obj).readMarshallable(this);
                return obj;
            } else if (Externalizable.class.isAssignableFrom(objClass)) {
                if (obj == null)
                    obj = (T) NativeBytes.UNSAFE.allocateInstance(objClass);
                ((Externalizable) obj).readExternal(this);
                return obj;
            } else if (CharSequence.class.isAssignableFrom(objClass)) {
                if (obj instanceof StringBuilder) {
                    readUTFΔ((StringBuilder) obj);
                    return obj;
                } else {
                    return (T) readUTFΔ();
                }
            }
            return (T) readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeObject(@Nullable Object obj) {
        if (obj == null) {
            writeByte(NULL);
            return;
        }

        Class<?> clazz = obj.getClass();
        final BytesMarshallerFactory bytesMarshallerFactory = bytesMarshallerFactory();
        BytesMarshaller em = bytesMarshallerFactory.acquireMarshaller(clazz, false);
        if (em == NoMarshaller.INSTANCE && autoGenerateMarshaller(obj))
            em = bytesMarshallerFactory.acquireMarshaller(clazz, true);

        if (em != NoMarshaller.INSTANCE) {
            if (em instanceof CompactBytesMarshaller) {
                writeByte(((CompactBytesMarshaller) em).code());
                em.write(this, obj);
                return;
            }
            writeByte(ENUMED);
            writeEnum(clazz);
            em.write(this, obj);
            return;
        }
        writeByte(SERIALIZED);
        // TODO this is the lame implementation, but it works.
        bytesMarshallerFactory.writeSerializable(this, obj);
        checkEndOfBuffer();
    }

    @Override
    public <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj) {
        try {
            if (BytesMarshallable.class.isAssignableFrom(objClass)) {
                ((BytesMarshallable) obj).writeMarshallable(this);
            } else if (Externalizable.class.isAssignableFrom(objClass)) {
                ((Externalizable) obj).writeExternal(this);
            } else if (CharSequence.class.isAssignableFrom(objClass)) {
                writeUTFΔ((CharSequence) obj);
            } else {
                writeObject(obj);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean autoGenerateMarshaller(Object obj) {
        return (obj instanceof Comparable && obj.getClass().getPackage().getName().startsWith("java"))
                || obj instanceof Externalizable
                || obj instanceof BytesMarshallable;
    }

    @Override
    public boolean tryLockInt(long offset) {
        return tryLockNanos4a(offset);
    }

    @Override
    public boolean tryLockNanosInt(long offset, long nanos) {
        int limit = nanos <= 10000 ? (int) nanos / 10 : 1000;
        for (int i = 0; i < limit; i++)
            if (tryLockNanos4a(offset))
                return true;
        if (nanos <= 10000)
            return false;
        long end = System.nanoTime() + nanos - 10000;
        do {
            if (tryLockNanos4a(offset))
                return true;
        } while (end > System.nanoTime() && !currentThread().isInterrupted());
        return false;
    }

    private boolean tryLockNanos4a(long offset) {
        //lowId = bottom 24 bytes of the thread id
        int lowId = shortThreadId();
        //Use the top 8 bytes as a counter, and the bottom 24 bytes as the thread id
        int firstValue = ((1 << 24) | lowId);
        //If the cas works, it was unlocked and we now atomically have the lock
        if (compareAndSwapInt(offset, 0, firstValue))
            return true;
        //The cas failed so get the value of the current lock
        int currentValue = readInt(offset);
        //if the bottom 24 bytes match our thread id ... 
        // TODO but what if we're in a different process?
        if ((currentValue & INT_LOCK_MASK) == lowId) {
            //then if the counter in the top 8 bytes is 255, throw an exception
            if ((currentValue >>> 24) >= 255)
                throw new IllegalStateException("Reentered 255 times without an unlock - if you are using this to lock across processes, there could be a thread id conflict letting one process 'steal' the lock from another process. To avoid this, call AffinitySupport.setThreadId() during startup which will make all threads have unique ids");
            //otherwise increase the counter in the top 8 bytes by one
            currentValue += 1 << 24;
            //and store it - no other threads can successfully write at this point
            //because their cas will fail (the value is not 0), so no update concurrency
            //conflict, but we do want other threads to read the value we write
            writeOrderedInt(offset, (int) currentValue);
            //we've got the lock - and incremented it, so return true
            return true;
        }
        return false;
    }

    @Override
    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        boolean success = tryLockNanosInt(offset, BUSY_LOCK_LIMIT);
        if (!success)
            if (currentThread().isInterrupted())
                throw new InterruptedException();
            else
                throw new IllegalStateException("Failed to acquire lock after " + BUSY_LOCK_LIMIT / 1e9 + " seconds.");
    }

    @Override
    public void unlockInt(long offset) throws IllegalMonitorStateException {
        int lowId = shortThreadId();
        int firstValue = ((1 << 24) | lowId);
        if (compareAndSwapInt(offset, firstValue, 0))
            return;
        // try to cheek the lowId and the count.
        unlockFailedInt(offset, lowId);
    }

    @Override
    public void resetLockInt(long offset) {
        writeOrderedInt(offset, 0);
    }

    @Override
    public int threadIdForLockInt(long offset) {
        return readVolatileInt(offset) & INT_LOCK_MASK;
    }

    int shortThreadId() {
        return shortThreadId > 0 ? shortThreadId : shortThreadId0();
    }

    int shortThreadId0() {
        final int tid = (int) getId() & INT_LOCK_MASK;
        if (!ID_LIMIT_WARNED && tid > 1 << 24) {
            warnIdLimit(tid);
        }
        return tid;
    }

    public void setCurrentThread() {
        currentThread = Thread.currentThread();
        shortThreadId = shortThreadId0();
    }

    Thread currentThread() {
        return currentThread == null ? Thread.currentThread() : currentThread;
    }

    @Override
    public boolean tryLockLong(long offset) {
        long id = uniqueTid();
        return tryLockNanos8a(offset, id);
    }

    long uniqueTid() {
        return Jvm.getUniqueTid(currentThread());
    }

    @Override
    public boolean tryLockNanosLong(long offset, long nanos) {
        long id = uniqueTid();
        int limit = nanos <= 10000 ? (int) nanos / 10 : 1000;
        for (int i = 0; i < limit; i++)
            if (tryLockNanos8a(offset, id))
                return true;
        if (nanos <= 10000)
            return false;
        long end = System.nanoTime() + nanos - 10000;
        do {
            if (tryLockNanos8a(offset, id))
                return true;
        } while (end > System.nanoTime() && !currentThread().isInterrupted());
        return false;
    }

    private boolean tryLockNanos8a(long offset, long id) {
        long firstValue = (1L << 48) | id;
        if (compareAndSwapLong(offset, 0, firstValue))
            return true;
        long currentValue = readLong(offset);
        if ((currentValue & (1L << 48) - 1) == id) {
            if (currentValue >>> 48 == 65535)
                throw new IllegalStateException("Reentred 65535 times without an unlock");
            currentValue += 1L << 48;
            writeOrderedLong(offset, currentValue);
            return true;
        }
        return false;
    }

    @Override
    public void busyLockLong(long offset) throws InterruptedException, IllegalStateException {
        boolean success = tryLockNanosLong(offset, BUSY_LOCK_LIMIT);
        if (!success)
            if (currentThread().isInterrupted())
                throw new InterruptedException();
            else
                throw new IllegalStateException("Failed to acquire lock after " + BUSY_LOCK_LIMIT / 1e9 + " seconds.");
    }

    @Override
    public void unlockLong(long offset) throws IllegalMonitorStateException {
        long id = Jvm.getUniqueTid();
        long firstValue = (1L << 48) | id;
        if (compareAndSwapLong(offset, firstValue, 0))
            return;
        // try to check the lowId and the count.
        unlockFailedLong(offset, id);
    }

    @Override
    public void resetLockLong(long offset) {
        writeOrderedLong(offset, 0L);
    }

    @Override
    public long threadIdForLockLong(long offset) {
        return readVolatileLong(offset);
    }

    long getId() {
        return currentThread().getId();
    }

    private void unlockFailedInt(long offset, int lowId) throws IllegalMonitorStateException {
        long currentValue = readInt(offset);
        long holderId = currentValue & INT_LOCK_MASK;
        if (holderId == lowId) {
            currentValue -= 1 << 24;
            writeOrderedInt(offset, (int) currentValue);
        } else if (currentValue == 0) {
            LOGGER.warn("No thread holds this lock, threadId: {}", shortThreadId());
        } else {
            throw new IllegalMonitorStateException("Thread " + holderId + " holds this lock, " + (currentValue >>> 24) + " times");
        }
    }

    private void unlockFailedLong(long offset, long id) throws IllegalMonitorStateException {
        long currentValue = readLong(offset);
        long holderId = currentValue & (-1L >>> 16);
        if (holderId == id) {
            currentValue -= 1L << 48;
            writeOrderedLong(offset, currentValue);
        } else if (currentValue == 0) {
            throw new IllegalMonitorStateException("No thread holds this lock");
        } else {
            throw new IllegalMonitorStateException("Process " + ((currentValue >>> 32) & 0xFFFF)
                    + " thread " + (holderId & (-1L >>> 32))
                    + " holds this lock, " + (currentValue >>> 48)
                    + " times, unlock from " + Jvm.getProcessId()
                    + " thread " + currentThread().getId());
        }
    }

    @Override
    public int getAndAdd(long offset, int delta) {
        for (; ; ) {
            int current = readVolatileInt(offset);
            int next = current + delta;
            if (compareAndSwapInt(offset, current, next))
                return current;
        }
    }

    @Override
    public int addAndGetInt(long offset, int delta) {
        for (; ; ) {
            int current = readVolatileInt(offset);
            int next = current + delta;
            if (compareAndSwapInt(offset, current, next))
                return next;
        }
    }

    @Override
    public byte addByte(long offset, byte b) {
        byte b2 = readByte(offset);
        b2 += b;
        writeByte(offset, b2);
        return b2;
    }

    @Override
    public int addUnsignedByte(long offset, int i) {
        int b2 = readUnsignedByte(offset);
        b2 += i;
        writeUnsignedByte(offset, b2);
        return b2 & 0xFF;
    }

    @Override
    public short addShort(long offset, short s) {
        short s2 = readShort(offset);
        s2 += s;
        writeByte(offset, s2);
        return s2;
    }

    @Override
    public int addUnsignedShort(long offset, int i) {
        int b2 = readUnsignedShort(offset);
        b2 += i;
        writeUnsignedShort(offset, b2);
        return b2 & 0xFFFF;
    }

    @Override
    public int addInt(long offset, int i) {
        int b2 = readInt(offset);
        b2 += i;
        writeInt(offset, b2);
        return b2;
    }

    @Override
    public long addUnsignedInt(long offset, long i) {
        long b2 = readUnsignedInt(offset);
        b2 += i;
        writeUnsignedInt(offset, b2);
        return b2 & 0xFFFFFFFFL;
    }

    @Override
    public long addLong(long offset, long i) {
        long b2 = readLong(offset);
        b2 += i;
        writeLong(offset, b2);
        return b2;
    }

    @Override
    public float addFloat(long offset, float f) {
        float b2 = readFloat(offset);
        b2 += f;
        writeFloat(offset, b2);
        return b2;
    }

    @Override
    public double addDouble(long offset, double d) {
        double b2 = readDouble(offset);
        b2 += d;
        writeDouble(offset, b2);
        return b2;
    }

    @Override
    public int addAtomicInt(long offset, int i) {
        return addAndGetInt(offset, i);
    }

    @Override
    public long addAtomicLong(long offset, long delta) {
        for (; ; ) {
            long current = readVolatileLong(offset);
            long next = current + delta;
            if (compareAndSwapLong(offset, current, next))
                return next;
        }
    }

    @Override
    public float addAtomicFloat(long offset, float delta) {
        for (; ; ) {
            int current = readVolatileInt(offset);
            int next = Float.floatToRawIntBits(Float.intBitsToFloat(current) + delta);
            if (compareAndSwapInt(offset, current, next))
                return next;
        }
    }

    @Override
    public double addAtomicDouble(long offset, double delta) {
        for (; ; ) {
            long current = readVolatileLong(offset);
            long next = Double.doubleToRawLongBits(Double.longBitsToDouble(current) + delta);
            if (compareAndSwapLong(offset, current, next))
                return next;
        }
    }

    @Override
    public float readVolatileFloat(long offset) {
        return Float.intBitsToFloat(readVolatileInt(offset));
    }

    @Override
    public double readVolatileDouble(long offset) {
        return Double.longBitsToDouble(readVolatileLong(offset));
    }

    @Override
    public void writeOrderedFloat(long offset, float v) {
        writeOrderedInt(offset, Float.floatToRawIntBits(v));
    }

    @Override
    public void writeOrderedDouble(long offset, double v) {
        writeOrderedLong(offset, Double.doubleToRawLongBits(v));
    }

    @Override
    public int length() {
        return (int) Math.min(Integer.MAX_VALUE, remaining());
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= length())
            throw new IndexOutOfBoundsException();
        return (char) readUnsignedByte(position() + index);
    }

    @Override
    public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
        long len = Math.min(remaining(), in.remaining());
        long inPosition = in.position();
        write(in, inPosition, len);
        in.position(inPosition + len);
    }

    @Override
    public void writeMarshallable(@NotNull Bytes out) {
        out.write(this, position(), remaining());

    }

    @Override
    public void write(RandomDataInput bytes) {
        write(bytes, bytes.position(), bytes.remaining());
    }

    @Override
    public void write(RandomDataInput bytes, long position, long length) {
        if (length > remaining())
            throw new IllegalArgumentException("Attempt to write " + length + " bytes with " + remaining() + " remaining");
        if (bytes.byteOrder() == byteOrder()) {
            while (length >= 8) {
                writeLong(bytes.readLong(position));
                position += 8;
                length -= 8;
            }
        }
        while (length >= 1) {
            writeByte(bytes.readByte(position));
            position++;
            length--;
        }
    }

    public boolean startsWith(RandomDataInput input) {
        long inputRemaining = input.remaining();
        if (remaining() < inputRemaining) return false;
        long pos = position(), inputPos = input.position();

        int i = 0;
        for (; i < inputRemaining - 3; i += 4) {
            if (readInt(pos + i) != input.readInt(inputPos + i))
                return false;
        }
        for (; i < inputRemaining; i++) {
            if (readByte(pos + i) != input.readByte(inputPos + i))
                return false;
        }
        return true;
    }

    class BytesInputStream extends InputStream {
        private long mark = 0;

        @Override
        public int available() {
            return AbstractBytes.this.available();
        }

        @Override
        public void close() {
            finish();
        }

        @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
        @Override
        public void mark(int readLimit) {
            mark = position();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public int read(@NotNull byte[] bytes, int off, int len) {
            return AbstractBytes.this.read(bytes, off, len);
        }

        @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
        @Override
        public void reset() {
            position(mark);
        }

        @Override
        public long skip(long n) {
            return AbstractBytes.this.skip(n);
        }

        @Override
        public int read() {
            return AbstractBytes.this.read();
        }
    }

    private class BytesOutputStream extends OutputStream {
        @Override
        public void close() {
            finish();
        }

        @Override
        public void write(@NotNull byte[] b) {
            AbstractBytes.this.write(b);
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) {
            AbstractBytes.this.write(b, off, len);
        }

        @Override
        public void write(int b) {
            checkWrite(1);
            writeUnsignedByte(b);
        }
    }

    @NotNull
    @Override
    public String toString() {
        char[] chars = new char[(int) remaining()];
        long pos = position();
        for (long i = pos; i < limit(); i++) {
            chars[((int) (i - pos))] = (char) readUnsignedByte(i);
        }
        return new String(chars);
    }

    @NotNull
    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("[pos: ").append(position()).append(", lim: ").append(limit()).append(", cap: ")
                .append(capacity()).append(" ] ");
        toString(sb, position() - 64, position(), position() + 64);

        return sb.toString();
    }

    @Override
    public void toString(Appendable sb, long start, long position, long end) {
        try {
            // before
            if (start < 0) start = 0;
            if (position > start) {
                for (long i = start; i < position; i++) {
                    append(sb, i);
                }
                sb.append('\u2016');
            }
            if (end > limit())
                end = limit();
            // after
            for (long i = position; i < end; i++) {
                append(sb, i);
            }
        } catch (IOException e) {
            try {
                sb.append(e.toString());
            } catch (IOException e1) {
                throw new AssertionError(e);
            }
        }
    }

    private void append(Appendable sb, long i) throws IOException {
        byte b = readByte(i);
        if (b == 0)
            sb.append('\u0660');
        else if (b < 21)
            sb.append((char) (b + 0x2487));
        else
            sb.append((char) b);
    }

    @Override
    public void asString(Appendable appendable) {
        try {
            for (long i = position(); i < limit(); i++)
                append(appendable, i);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

    }

    @Override
    public CharSequence asString() {
        StringBuilder sb = new StringBuilder();
        asString(sb);
        return sb;
    }


    /**
     * display the hex data of a byte buffer from the position() to the limit()
     *
     * @param buffer the buffer you wish to toString()
     * @return hex representation of the buffer, from example [0D ,OA, FF]
     */
    public static String toHex(@org.jetbrains.annotations.NotNull final ByteBuffer buffer) {

        final ByteBuffer slice = buffer.slice();
        final StringBuilder builder = new StringBuilder("[");

        while (slice.hasRemaining()) {
            final byte b = slice.get();
            builder.append(String.format("%02X ", b));
            builder.append(",");
        }

        // remove the last comma
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean compareAndSwapDouble(long offset, double expected, double value) {
        long exp = Double.doubleToRawLongBits(expected);
        long val = Double.doubleToRawLongBits(value);
        return compareAndSwapLong(offset, exp, val);
    }
}
