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
import net.openhft.lang.io.impl.VanillaBytesMarshallerFactory;

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
public abstract class AbstractBytes implements Bytes {
    public static final long BUSY_LOCK_LIMIT = 10L * 1000 * 1000 * 1000;
    public static final int INT_LOCK_MASK = 0xFFFFFF;
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
    private static final int USHORT_EXTENDED = 0xFFFF;

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
    private BytesInputStream inputStream = null;
    private BytesOutputStream outputStream = null;
    private StringBuilder utfReader = null;
    private SimpleDateFormat dateFormat = null;
    private long lastDay = Long.MIN_VALUE;
    private byte[] lastDateStr = null;

    protected AbstractBytes() {
        this(new VanillaBytesMarshallerFactory());
    }

    protected AbstractBytes(BytesMarshallerFactory bytesMarshallerFactory) {
        this.finished = false;
        this.bytesMarshallerFactory = bytesMarshallerFactory;
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
            value += modDiv * mod / 5;
        }
        final double d = Math.scalb((double) value, exp);
        return negative ? -d : d;
    }

    @Override
    public void readFully(byte[] b) {
        readFully(b, 0, b.length);
    }

    @Override
    public int skipBytes(int n) {
        long position = position();
        int n2 = (int) Math.min(n, capacity() - position);
        position(position + n2);
        return n2;
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        while (len-- > 0)
            b[off++] = readByte();
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
        return readByte() & 0xFF;
    }

    @Override
    public int readUnsignedByte(long offset) {
        return readByte(offset) & 0xFF;
    }

    @Override
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    @Override
    public int readUnsignedShort(long offset) {
        return readShort(offset) & 0xFFFF;
    }

    @Override
    public String readLine() {
        StringBuilder input = new StringBuilder();
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
        return input.toString();
    }

    @Override
    public String readUTF() {
        if (readUTF(acquireUtfReader()))
            return utfReader.toString();
        return null;
    }

    private StringBuilder acquireUtfReader() {
        if (utfReader == null) utfReader = new StringBuilder();
        utfReader.setLength(0);
        return utfReader;
    }

    @Override
    public boolean readUTF(Appendable appendable) {
        return appendUTF(appendable);
    }

    @Override
    public boolean readUTF(StringBuilder stringBuilder) {
        try {
            stringBuilder.setLength(0);
            return appendUTF0(stringBuilder);
        } catch (IOException unexpected) {
            throw new AssertionError(unexpected);
        }
    }

    @Override
    public boolean appendUTF(Appendable appendable) {
        try {
            return appendUTF0(appendable);
        } catch (IOException unexpected) {
            throw new AssertionError(unexpected);
        }
    }

    private boolean appendUTF0(Appendable appendable) throws IOException {
        long len = readStopBit();
        if (len < -1 || len > Integer.MAX_VALUE)
            throw new StreamCorruptedException("UTF length invalid " + len);
        if (len == -1)
            return false;
        int utflen = (int) len;
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
                    /* 0xxxxxxx*/
                    count++;
                    appendable.append((char) c);
                    break;
                case 12:
                case 13: {
                    /* 110x xxxx   10xx xxxx*/
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
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
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
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
        return true;
    }

    @Override
    public String parseUTF(StopCharTester tester) {
        parseUTF(acquireUtfReader(), tester);
        return utfReader.toString();
    }

    @Override
    public void parseUTF(Appendable builder, StopCharTester tester) {
        try {
            readUTF0(builder, tester);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void readUTF0(Appendable appendable, StopCharTester tester) throws IOException {
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
                    /* 0xxxxxxx*/
                    if (tester.isStopChar(c))
                        return;
                    appendable.append((char) c);
                    break;
                case 12:
                case 13: {
                    /* 110x xxxx   10xx xxxx*/
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
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */

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
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte ");
            }
        }
    }

    @Override
    public boolean stepBackAndSkipTo(StopCharTester tester) {
        if (position() > 0)
            position(position() - 1);
        return skipTo(tester);
    }

    @Override
    public boolean skipTo(StopCharTester tester) {
        while (remaining() > 0) {
            int ch = readByte();
            if (tester.isStopChar(ch))
                return true;
        }
        return false;
    }

    @Override
    public String readUTF(long offset) {
        long oldPosition = position();
        position(offset);
        try {
            return readUTF();
        } finally {
            position(oldPosition);
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
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return (readUnsignedByte() << 24 + readUnsignedShort() << 8) >> 8;
        // extra shifting to get sign extension.
        return (readUnsignedByte() << 8 + readUnsignedShort() << 16) >> 8;
    }

    // RandomDataOutput

    @Override
    public int readInt24(long offset) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return (readUnsignedByte(offset) << 24 + readUnsignedShort(offset + 1) << 8) >> 8;
        // extra shifting to get sign extension.
        return (readUnsignedByte(offset) << 8 + readUnsignedShort(offset + 1) << 16) >> 8;
    }

    @Override
    public long readUnsignedInt() {
        return readInt() & 0xFFFFFFFFL;
    }

    @Override
    public long readUnsignedInt(long offset) {
        return readInt(offset) & 0xFFFFFFFFL;
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
        int b = readUnsignedByte();
        if (b == USHORT_EXTENDED)
            return readUnsignedInt();
        return b;
    }

    @Override
    public long readInt48() {
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((long) readUnsignedShort() << 48 + readUnsignedInt() << 16) >> 16;
        // extra shifting to get sign extension.
        return (readUnsignedShort() << 16 + readUnsignedInt() << 32) >> 8;
    }

    @Override
    public long readInt48(long offset) {
        if (byteOrder() == ByteOrder.BIG_ENDIAN)
            return ((long) readUnsignedShort(offset) << 48 + readUnsignedInt(offset + 2) << 16) >> 16;
        // extra shifting to get sign extension.
        return (readUnsignedShort(offset) << 16 + readUnsignedInt(offset + 2) << 32) >> 16;
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
    public void readByteString(StringBuilder sb) {
        sb.setLength(0);
        int len = (int) readStopBit();
        for (int i = 0; i < len; i++)
            sb.append(readByte());
    }

    @Override
    public void readChars(StringBuilder sb) {
        int len = (int) readStopBit();
        sb.setLength(0);
        for (int i = 0; i < len; i++)
            sb.append(readChar());
    }

    @Override
    public void read(ByteBuffer bb) {
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

    //// RandomOutputStream
    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
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
    public void writeBytes(String s) {
        writeBytes((CharSequence) s);
    }

    @Override
    public void writeBytes(CharSequence s) {
        int len = s.length();
        writeStopBit(len);
        for (int i = 0; i < len; i++)
            write(s.charAt(i));
    }

    @Override
    public void writeChars(String s) {
        int len = s.length();
        writeStopBit(len);
        for (int i = 0; i < len; i++)
            writeChar(s.charAt(i));
    }

    @Override
    public void writeUTF(String s) {
        writeUTF((CharSequence) s);
    }

    @Override
    public void writeUTF(CharSequence str) {
        if (str == null) {
            writeStopBit(-1);
            return;
        }
        long strlen = str.length();
        int utflen = findUTFLength(str, strlen);
        writeStopBit(utflen);
        writeUTF0(str, strlen);
    }

    public ByteStringAppender append(CharSequence str) {
        if (str == null)
            return this;
        long strlen = str.length();
        writeUTF0(str, strlen);
        return this;
    }

    private int findUTFLength(CharSequence str, long strlen) {
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

    private void writeUTF0(CharSequence str, long strlen) {
        int c;
        int i;
        for (i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F))) break;
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
    public void write(long offset, byte[] b) {
        for (int i = 0; i < b.length; i++)
            writeByte(offset + i, b[i]);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        for (int i = 0; i < len; i++)
            write(b[off + i]);
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
        else switch (v) {
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
        else switch (v) {
            case Integer.MIN_VALUE:
                writeShort(SHORT_MIN_VALUE);
                break;
            case Integer.MAX_VALUE:
                writeShort(SHORT_MAX_VALUE);
                break;
            default:
                writeShort(BYTE_EXTENDED);
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
            writeInt(BYTE_MIN_VALUE);

        } else if (v == Long.MAX_VALUE) {
            writeInt(BYTE_MAX_VALUE);

        } else {
            writeInt(BYTE_EXTENDED);
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
    public void write(ByteBuffer bb) {
        if (bb.order() == byteOrder())
            while (bb.remaining() >= 8)
                writeLong(bb.getLong());
        while (bb.remaining() >= 1)
            writeByte(bb.get());
    }

    //// ByteStringAppender
    @Override
    public ByteStringAppender append(CharSequence s, int start, int end) {
        for (int i = start, len = Math.min(end, s.length()); i < len; i++)
            writeByte(s.charAt(i));
        return this;
    }

    @Override
    public ByteStringAppender append(Enum value) {
        return append(value.toString());
    }

    @Override
    public ByteStringAppender append(byte[] str) {
        write(str);
        return this;
    }

    @Override
    public ByteStringAppender append(byte[] str, int offset, int len) {
        write(str, offset, len);
        return this;
    }

    @Override
    public ByteStringAppender append(boolean b) {
        append(b ? "true" : "false");
        return this;
    }

    @Override
    public ByteStringAppender append(char c) {
        writeByte(c);
        return this;
    }

    @Override
    public ByteStringAppender append(int num) {
        return append((long) num);
    }

    @Override
    public ByteStringAppender append(long num) {
        if (num < 0) {
            if (num == Long.MIN_VALUE) {
                append(MIN_VALUE_TEXT);
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
        }
        append(lastDateStr);
        return this;
    }

    @Override
    public ByteStringAppender appendDateTimeMillis(long timeInMS) {
        appendDateMillis(timeInMS);
        writeByte('T');
        appendTimeMillis(timeInMS);
        return this;
    }

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

    @Override
    public <E> ByteStringAppender append(E object) {
        return null;
    }

    @Override
    public <E> ByteStringAppender append(Iterable<E> list, CharSequence seperator) {
        if (list instanceof List) {
            return append((List) list, seperator);
        }
        int i = 0;
        for (E e : list) {
            if (i++ > 0)
                append(seperator);
            append(e);
        }
        return this;
    }

    @Override
    public <E> ByteStringAppender append(List<E> list, CharSequence seperator) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                append(seperator);
            append(list.get(i));
        }
        return this;
    }

    @Override
    public MutableDecimal parseDecimal(MutableDecimal decimal) {
        long num = 0, scale = Long.MIN_VALUE;
        boolean negative = false;
        while (true) {
            byte b = readByte();
//            if (b >= '0' && b <= '9')
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
//            if (b >= '0' && b <= '9')
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
        if (num <= 0) return 19;
        numberBuffer[18] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 18;
        numberBuffer[17] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 17;
        numberBuffer[16] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 16;
        numberBuffer[15] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 15;
        numberBuffer[14] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 14;
        numberBuffer[13] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 13;
        numberBuffer[12] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 12;
        numberBuffer[11] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 11;
        numberBuffer[10] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 10;
        numberBuffer[9] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 9;
        numberBuffer[8] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 8;
        numberBuffer[7] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 7;
        numberBuffer[6] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 6;
        numberBuffer[5] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 5;
        numberBuffer[4] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 4;
        numberBuffer[3] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return 3;
        numberBuffer[2] = (byte) (num % 10L + '0');
        num /= 10;
        return 2;
    }

    @Override
    public ByteStringAppender append(double d, int precision) {
        if (precision < 0) precision = 0;
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
        while (precision > 0 && val % 10 == 0) {
            val /= 10;
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

    private int appendDouble1(long num, final int precision) {
        int endIndex = MAX_NUMBER_LENGTH;
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 1)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 2)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 3)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 4)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 5)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 6)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 7)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 8)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 9)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 10)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 11)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 12)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 13)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 14)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 15)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 16)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 17)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        num /= 10;
        if (num <= 0) return endIndex;
        if (precision == 18)
            numberBuffer[--endIndex] = '.';
        numberBuffer[--endIndex] = (byte) (num % 10L + '0');
        return endIndex;
    }

    @Override
    public ByteStringAppender append(MutableDecimal md) {
        StringBuilder sb = acquireUtfReader();
        md.toString(sb);
        append(sb);
        return this;
    }

    @Override
    public InputStream inputStream() {
        if (inputStream == null)
            inputStream = new BytesInputStream();
        return inputStream;
    }

    @Override
    public OutputStream outputStream() {
        if (outputStream == null)
            outputStream = new BytesOutputStream();
        return outputStream;
    }

    @Override
    public BytesMarshallerFactory bytesMarshallerFactory() {
        return bytesMarshallerFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> void writeEnum(E e) {
        Class aClass;
        if (e == null)
            aClass = String.class;
        else
            aClass = (Class) e.getClass();
        BytesMarshaller<E> em = bytesMarshallerFactory().acquireMarshaller(aClass, true);
        em.write(this, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E readEnum(Class<E> eClass) {
        BytesMarshaller<E> em = bytesMarshallerFactory().acquireMarshaller(eClass, true);
        return em.read(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E parseEnum(Class<E> eClass, StopCharTester tester) {
        BytesMarshaller<E> em = bytesMarshallerFactory().acquireMarshaller(eClass, true);
        return em.parse(this, tester);
    }

    @Override
    public <E> void writeList(Collection<E> list) {
        writeInt(list.size());
        for (E e : list)
            writeObject(e);
    }

    @Override
    public <K, V> void writeMap(Map<K, V> map) {
        writeInt(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            writeEnum(entry.getKey());
            writeEnum(entry.getValue());
        }
    }

    @Override
    public <E> void readList(Collection<E> list) {
        int len = (int) readStopBit();
        list.clear();
        for (int i = 0; i < len; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) readObject();
            list.add(e);
        }
    }

    @Override
    public <K, V> Map<K, V> readMap(Class<K> kClass, Class<V> vClass) {
        int len = (int) readStopBit();
        if (len == 0) return Collections.emptyMap();
        Map<K, V> map = new LinkedHashMap<K, V>(len * 10 / 7);
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
        return remaining() > 0 ? readByte() : -1;
    }

    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    @Override
    public abstract int read(byte[] b, int off, int len);

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
    public void finish() {
        if (remaining() < 0)
            throwOverflow();
        finished = true;
    }

    private void throwOverflow() {
        throw new IllegalStateException("Buffer overflow, capacity: " + capacity() + " position: " + position());
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void flush() {
        checkEndOfBuffer();
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public void writeObject(Object obj) {
        if (obj == null) {
            writeByte(NULL);
            return;
        }

        Class<?> clazz = obj.getClass();
        boolean create = obj instanceof Comparable || obj instanceof Externalizable;
        BytesMarshaller em = bytesMarshallerFactory.acquireMarshaller(clazz, create);
        if (em != null) {
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

    private void warnIdLimit(long id) {
        LOGGER.log(Level.WARNING, "High thread id may result in collisions id: " + id);
        ID_LIMIT_WARNED = true;
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
    public void unlockInt(long offset) throws IllegalStateException {
        int lowId = (int) Thread.currentThread().getId() & INT_LOCK_MASK;
        int firstValue = ((1 << 24) | lowId);
        if (compareAndSetInt(offset, firstValue, 0))
            return;
        long currentValue = readUnsignedInt(offset);
        long holderId = currentValue & INT_LOCK_MASK;
        if (holderId == lowId) {
            currentValue -= 1 << 24;
            writeOrderedInt(offset, (int) currentValue);
        } else if (currentValue == 0) {
            throw new IllegalStateException("No thread holds this lock");
        } else {
            throw new IllegalStateException("Thread " + holderId + " holds this lock, " + (currentValue >>> 24) + " times");
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
            AbstractBytes.this.readFully(b, off, len);
            return len;
        }

        @Override
        public void reset() throws IOException {
            position(mark);
        }

        @Override
        public long skip(long n) throws IOException {
            if (n > Integer.MAX_VALUE) throw new IOException("Skip too large");
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
        public void write(byte[] b) throws IOException {
            AbstractBytes.this.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            AbstractBytes.this.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            writeUnsignedByte(b);
        }
    }
}
