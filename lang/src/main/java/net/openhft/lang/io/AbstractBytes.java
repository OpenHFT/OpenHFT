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

import net.openhft.lang.Maths;
import net.openhft.lang.io.impl.NoMarshaller;
import net.openhft.lang.io.impl.VanillaBytesMarshallerFactory;
import net.openhft.lang.pool.StringInterner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author peter.lawrey
 */
@SuppressWarnings("MagicNumber")
public abstract class AbstractBytes implements Bytes {
    public static final long BUSY_LOCK_LIMIT = 10L * 1000 * 1000 * 1000;
    public static final int INT_LOCK_MASK = 0xFFFFFF;
    public static final int UNSIGNED_BYTE_MASK = 0xFF;
    public static final int UNSIGNED_SHORT_MASK = 0xFFFF;
    public static final long UNSIGNED_INT_MASK = 0xFFFFFFFFL;
    // extra 1 for decimal place.
    static final int MAX_NUMBER_LENGTH = 1 + (int) Math.ceil(Math.log10(Long.MAX_VALUE));
    private static final Logger LOGGER = Logger.getLogger(AbstractBytes.class.getName());
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
    private static final int USHORT_EXTENDED = UNSIGNED_SHORT_MASK;
    // RandomDataInput
    private static final int INT_MIN_VALUE = Integer.MIN_VALUE;
    private static final int INT_EXTENDED = Integer.MIN_VALUE + 1;
    private static final int INT_MAX_VALUE = Integer.MIN_VALUE + 2;
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;
    private static final byte NULL = 'N';
    private static final byte ENUMED = 'E';
    private static final byte SERIALIZED = 'S';
    static boolean ID_LIMIT_WARNED = false;
    private final byte[] numberBuffer = new byte[MAX_NUMBER_LENGTH];
    protected boolean finished;
    protected BytesMarshallerFactory bytesMarshallerFactory;
    private StringInterner stringInterner = null;
    private BytesInputStream inputStream = null;
    private BytesOutputStream outputStream = null;
    private StringBuilder utfReader = null;
    private SimpleDateFormat dateFormat = null;
    private long lastDay = Long.MIN_VALUE;
    @Nullable
    private byte[] lastDateStr = null;

    protected AbstractBytes() {
        this(new VanillaBytesMarshallerFactory());
    }

    protected AbstractBytes(BytesMarshallerFactory bytesMarshallerFactory) {
        this.finished = false;
        this.bytesMarshallerFactory = bytesMarshallerFactory;
    }

    protected StringInterner stringInterner() {
        if (stringInterner == null)
            stringInterner = new StringInterner(8 * 1024);
        return stringInterner;
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

    static boolean equalsCaseIgnore(StringBuilder sb, String s) {
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
        LOGGER.log(Level.WARNING, "High thread id may result in collisions id: " + id);

        ID_LIMIT_WARNED = true;
    }

    @Override
    public void readFully(@NotNull byte[] bytes) {
        readFully(bytes, 0, bytes.length);
    }

    @Override
    public int skipBytes(int n) {
        long position = position();
        int n2 = (int) Math.min(n, capacity() - position);
        position(position + n2);
        return n2;
    }

    @Override
    public boolean readBoolean() {
        return readByte() != 0;
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
            int c = readUnsignedByte();
            switch (c) {
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

    @Nullable
    @Override
    public String readUTFΔ() {
        if (readUTFΔ(acquireUtfReader()))
            return utfReader.length() == 0 ? "" : stringInterner().intern(utfReader);
        return null;
    }

    @NotNull
    private StringBuilder acquireUtfReader() {
        if (utfReader == null)
            utfReader = new StringBuilder();
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

    // RandomDataOutput

    private void readUTF0(@NotNull Appendable appendable, int utflen) throws IOException {
        int count = 0;
        while (count < utflen) {
            int c = readByte();
            if (c < 0) {
                position(position() - 1);
                break;
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
        while (remaining() > 0) {
            int c = readByte();
            if (c < 0) {
                position(position() - 1);
                break;
            }
            if (tester.isStopChar(c))
                return;
            appendable.append((char) c);
        }

        while (remaining() > 0) {
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
            int ch = readByte();
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
        long l = 0, b;
        int count = 0;
        while ((b = readByte()) < 0) {
            l |= (b & 0x7FL) << count;
            count += 7;
        }
        if (b == 0 && count > 0)
            return ~l;
        return l | (b << count);
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
        int utflen = findUTFLength(str, strlen);
        writeUnsignedShort(utflen);
        writeUTF0(str, strlen);
    }

    @Override
    public void writeUTFΔ(@Nullable CharSequence str) {
        if (str == null) {
            writeStopBit(-1);
            return;
        }
        long strlen = str.length();
        int utflen = findUTFLength(str, strlen);
        writeStopBit(utflen);
        writeUTF0(str, strlen);
    }

    @NotNull
    public ByteStringAppender append(@NotNull CharSequence str) {
        if (str == null)
            return this;
        long strlen = str.length();
        writeUTF0(str, strlen);
        return this;
    }

    private int findUTFLength(@NotNull CharSequence str, long strlen) {
        int utflen = 0, c;/* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
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
            if (!((c >= 0x0001) && (c <= 0x007F)))
                break;
            write(c);
        }

        for (; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
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
        checkWrite(bytes.length);

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
        while (true) {
            long n2 = n >>> 7;
            if (n2 != 0) {
                writeByte((byte) (0x80 | (n & 0x7F)));
                n = n2;
            } else {
                if (neg) {
                    writeByte((byte) (0x80 | (n & 0x7F)));
                    writeByte(0);
                } else {
                    writeByte((byte) (n & 0x7F));
                }
                break;
            }
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

    // // ByteStringAppender
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
            byte ch = readByte();
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
    public <E> ByteStringAppender append(@NotNull List<E> list, @NotNull CharSequence separator) {
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
            byte b = readByte();
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
            byte b = readByte();
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
        num /= 10;
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
        return bytesMarshallerFactory;
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
            E e = (E) readEnum(eClass);
            list.add(e);
        }
    }

    @Override
    public <K, V> void readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass) {
        int len = (int) readStopBit();
        map.clear();
        for (int i = 0; i < len; i++)
            map.put(readEnum(kClass), readEnum(vClass));
    }

    @Override
    public int available() {
        return (int) Math.min(Integer.MAX_VALUE, remaining());
    }

    @Override
    public int read() {
        return remaining() > 0 ? readByte() : -1;
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
        skipBytes((int) n);
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
    public void reset() {
        finished = false;
        position(0L);
    }

    @Override
    public void flush() {
        checkEndOfBuffer();
    }

    @Nullable
    @Override
    public Object readObject() {
        int type = readByte();
        switch (type) {
            case NULL:
                return null;
            case ENUMED: {
                Class clazz = readEnum(Class.class);
                return readEnum(clazz);
            }
            case SERIALIZED: {
                try {
                    return new ObjectInputStream(this.inputStream()).readObject();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            default:
                throw new IllegalStateException("Unknown type " + (char) type);
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

    @SuppressWarnings("unchecked")
    @Override
    public void writeObject(@Nullable Object obj) {
        if (obj == null) {
            writeByte(NULL);
            return;
        }

        Class<?> clazz = obj.getClass();
        BytesMarshaller em = bytesMarshallerFactory.acquireMarshaller(clazz, false);
        if (em == NoMarshaller.INSTANCE && autoGenerateMarshaller(obj))
            em = bytesMarshallerFactory.acquireMarshaller(clazz, true);

        if (em != NoMarshaller.INSTANCE) {
            writeByte(ENUMED);
            writeEnum(clazz);
            em.write(this, obj);
            return;
        }
        writeByte(SERIALIZED);
        // TODO this is the lame implementation, but it works.
        try {
            ObjectOutputStream oos = new ObjectOutputStream(this.outputStream());
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        checkEndOfBuffer();
    }

    private boolean autoGenerateMarshaller(Object obj) {
        return (obj instanceof Comparable && obj.getClass().getPackage().getName().startsWith("java"))
                || obj instanceof Externalizable
                || obj instanceof BytesMarshallable;
    }

    @Override
    public boolean tryLockInt(long offset) {
        long id = Thread.currentThread().getId();
        if (!ID_LIMIT_WARNED && id > 1 << 24) {
            warnIdLimit(id);
        }
        return tryLockNanos4a(offset, (int) id);
    }

    @Override
    public boolean tryLockNanosInt(long offset, long nanos) {
        long id = Thread.currentThread().getId();
        if (!ID_LIMIT_WARNED && id > 1 << 24) {
            warnIdLimit(id);
        }
        int limit = nanos <= 10000 ? (int) nanos / 10 : 1000;
        for (int i = 0; i < limit; i++)
            if (tryLockNanos4a(offset, (int) id))
                return true;
        if (nanos <= 10000)
            return false;
        long end = System.nanoTime() + nanos - 10000;
        do {
            if (tryLockNanos4a(offset, (int) id))
                return true;
        } while (end > System.nanoTime() && !Thread.currentThread().isInterrupted());
        return false;
    }

    private boolean tryLockNanos4a(long offset, int id) {
        int lowId = id & INT_LOCK_MASK;
        int firstValue = ((1 << 24) | lowId);
        if (compareAndSetInt(offset, 0, firstValue))
            return true;
        long currentValue = readUnsignedInt(offset);
        if ((currentValue & INT_LOCK_MASK) == lowId) {
            if (currentValue >= (255L << 24))
                throw new IllegalStateException("Reentred 255 times without an unlock");
            currentValue += 1 << 24;
            writeOrderedInt(offset, (int) currentValue);
        }
        return false;
    }

    @Override
    public void busyLockInt(long offset) throws InterruptedException, IllegalStateException {
        boolean success = tryLockNanosInt(offset, BUSY_LOCK_LIMIT);
        if (Thread.currentThread().isInterrupted())
            throw new InterruptedException();
        if (!success)
            throw new IllegalStateException("Failed to acquire lock after " + BUSY_LOCK_LIMIT / 1e9 + " seconds.");
    }

    @Override
    public void unlockInt(long offset) throws IllegalMonitorStateException {
        int lowId = (int) Thread.currentThread().getId() & INT_LOCK_MASK;
        int firstValue = ((1 << 24) | lowId);
        if (compareAndSetInt(offset, firstValue, 0))
            return;
        unlockFailed(offset, lowId);
    }

    private void unlockFailed(long offset, int lowId) throws IllegalMonitorStateException {
        long currentValue = readUnsignedInt(offset);
        long holderId = currentValue & INT_LOCK_MASK;
        if (holderId == lowId) {
            currentValue -= 1 << 24;
            writeOrderedInt(offset, (int) currentValue);
        } else if (currentValue == 0) {
            throw new IllegalMonitorStateException("No thread holds this lock");
        } else {
            throw new IllegalMonitorStateException("Thread " + holderId + " holds this lock, " + (currentValue >>> 24) + " times");
        }
    }

    @Override
    public int getAndAdd(long offset, int delta) {
        for (; ; ) {
            int current = readVolatileInt(offset);
            int next = current + delta;
            if (compareAndSetInt(offset, current, next))
                return current;
        }
    }

    @Override
    public int addAndGetInt(long offset, int delta) {
        for (; ; ) {
            int current = readVolatileInt(offset);
            int next = current + delta;
            if (compareAndSetInt(offset, current, next))
                return next;
        }
    }

    protected class BytesInputStream extends InputStream {
        private long mark = 0;

        @Override
        public int available() throws IOException {
            long remaining = remaining();
            return (int) Math.min(Integer.MAX_VALUE, remaining);
        }

        @Override
        public void close() throws IOException {
            finish();
        }

        @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
        @Override
        public void mark(int readlimit) {
            mark = position();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return AbstractBytes.this.read(b, off, len);
        }

        @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
        @Override
        public void reset() throws IOException {
            position(mark);
        }

        @Override
        public long skip(long n) throws IOException {
            if (n > Integer.MAX_VALUE)
                throw new IOException("Skip too large");
            return skipBytes((int) n);
        }

        @Override
        public int read() throws IOException {
            if (remaining() > 0)
                return readUnsignedByte();
            return -1;
        }
    }

    protected class BytesOutputStream extends OutputStream {
        @Override
        public void close() throws IOException {
            finish();
        }

        @Override
        public void write(@NotNull byte[] b) throws IOException {
            AbstractBytes.this.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            AbstractBytes.this.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            checkWrite(1);
            writeUnsignedByte(b);
        }
    }
}
