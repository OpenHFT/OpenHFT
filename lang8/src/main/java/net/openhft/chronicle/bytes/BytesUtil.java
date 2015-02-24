package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.pool.StringBuilderPool;
import net.openhft.chronicle.core.pool.StringInterner;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UTFDataFormatException;

public enum BytesUtil {
    ;
    private static final byte[] MIN_VALUE_TEXT = ("" + Long.MIN_VALUE).getBytes();
    private static final StringBuilderPool SBP = new StringBuilderPool();
    private static final StringInterner SI = new StringInterner(1024);
    private static final byte[] Infinity = "Infinity".getBytes();
    private static final byte[] NaN = "NaN".getBytes();
    private static final long MAX_VALUE_DIVIDE_5 = Long.MAX_VALUE / 5;
    private static final ThreadLocal<byte[]> NUMBER_BUFFER = ThreadLocal.withInitial(() -> new byte[20]);
    private static final long MAX_VALUE_DIVIDE_10 = Long.MAX_VALUE / 10;

    public static void parseUTF(StreamingDataInput bytes, StringBuilder appendable, int utflen) throws UTFDataFormatRuntimeException {
        int count = 0;
        while (count < utflen) {
            int c = bytes.readUnsignedByte();
            if (c >= 128) {
                bytes.position(bytes.position() - 1);
                break;
            } else if (c < 0) {
            }
            count++;
            appendable.append((char) c);
        }

        if (utflen > count)
            parseUTF2(bytes, appendable, utflen, count);
    }

    static void parseUTF2(StreamingDataInput bytes, StringBuilder appendable, int utflen, int count) throws UTFDataFormatRuntimeException {
        while (count < utflen) {
            int c = bytes.readUnsignedByte();
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
                        throw new UTFDataFormatRuntimeException(
                                "malformed input: partial character at end");
                    int char2 = bytes.readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatRuntimeException(
                                "malformed input around byte " + count + " was " + char2);
                    int c2 = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    appendable.append((char) c2);
                    break;
                }
                case 14: {
                /* 1110 xxxx 10xx xxxx 10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatRuntimeException(
                                "malformed input: partial character at end");
                    int char2 = bytes.readUnsignedByte();
                    int char3 = bytes.readUnsignedByte();

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatRuntimeException(
                                "malformed input around byte " + (count - 1) + " was " + char2 + " " + char3);
                    int c3 = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            (char3 & 0x3F));
                    appendable.append((char) c3);
                    break;
                }
                // TODO add code point of characters > 0xFFFF support.
                default:
                /* 10xx xxxx, 1111 xxxx */
                    throw new UTFDataFormatRuntimeException(
                            "malformed input around byte " + count);
            }
        }
    }

    public static void writeUTF(StreamingDataOutput bytes, CharSequence str) {
        if (str == null) {
            bytes.writeStopBit(-1);
        } else {
            bytes.writeStopBit(findUTFLength(str));
            appendUTF(bytes, str, 0, str.length());
        }
    }

    public static long findUTFLength(@NotNull CharSequence str) {
        long utflen = 0;/* use charAt instead of copying String to char array */
        for (int i = 0, strlen = str.length(); i < strlen; i++) {
            char c = str.charAt(i);
            if (c <= 0x007F) {
                utflen++;
            } else if (c <= 0x07FF) {
                utflen += 2;
            } else {
                utflen += 3;
            }
        }
        return utflen;
    }

    public static void appendUTF(StreamingDataOutput bytes, @NotNull CharSequence str, int offset, int length) {
        int i;
        for (i = 0; i < length; i++) {
            char c = str.charAt(offset + i);
            if (c > 0x007F)
                break;
            bytes.writeByte((byte) c);
        }

        for (; i < length; i++) {
            char c = str.charAt(offset + i);
            appendUTF(bytes, c);
        }
    }

    public static void appendUTF(StreamingDataOutput bytes, int c) {
        if (c <= 0x007F) {
            bytes.writeByte((byte) c);
        } else if (c <= 0x07FF) {
            bytes.writeByte((byte) (0xC0 | ((c >> 6) & 0x1F)));
            bytes.writeByte((byte) (0x80 | c & 0x3F));
        } else if (c <= 0xFFFF) {
            bytes.writeByte((byte) (0xE0 | ((c >> 12) & 0x0F)));
            bytes.writeByte((byte) (0x80 | ((c >> 6) & 0x3F)));
            bytes.writeByte((byte) (0x80 | (c & 0x3F)));
        } else {
            bytes.writeByte((byte) (0xF0 | ((c >> 18) & 0x07)));
            bytes.writeByte((byte) (0x80 | ((c >> 12) & 0x3F)));
            bytes.writeByte((byte) (0x80 | ((c >> 6) & 0x3F)));
            bytes.writeByte((byte) (0x80 | (c & 0x3F)));
        }
    }

    public static void writeStopBit(StreamingDataOutput out, long n) {
        if ((n & ~0x7F) == 0) {
            out.writeByte((byte) (n & 0x7f));
            return;
        }
        if ((n & ~0x3FFF) == 0) {
            out.writeByte((byte) ((n & 0x7f) | 0x80));
            out.writeByte((byte) (n >> 7));
            return;
        }
        writeStopBit0(out, n);
    }

    static void writeStopBit0(StreamingDataOutput out, long n) {
        boolean neg = false;
        if (n < 0) {
            neg = true;
            n = ~n;
        }

        long n2;
        while ((n2 = n >>> 7) != 0) {
            out.writeByte((byte) (0x80L | n));
            n = n2;
        }
        // final byte
        if (!neg) {
            out.writeByte((byte) n);
        } else {
            out.writeByte((byte) (0x80L | n));
            out.writeByte((byte) 0);
        }
    }

    public static <IN extends RandomDataInput & StreamingCommon> String toDebugString(IN bytes, long maxLength) {
        StringBuilder sb = new StringBuilder(200);
        sb.append("[pos: ").append(bytes.position()).append(", lim: ").append(bytes.limit()).append(", cap: ")
                .append(bytes.maximumLimit()).append(" ] ");
        toString(bytes, sb, bytes.position() - maxLength, bytes.position(), bytes.position() + maxLength);

        return sb.toString();
    }

    public static <IN extends RandomDataInput & StreamingCommon> String toString(IN bytes) {
        StringBuilder sb = new StringBuilder(200);
        toString(bytes, sb);
        return sb.toString();
    }

    public static <IN extends RandomDataInput & StreamingCommon> void toString(IN bytes, Appendable sb, long start, long position, long end) {
        try {
            // before
            if (start < 0) start = 0;
            if (position > start) {
                for (long i = start; i < position; i++) {
                    sb.append(bytes.printable(i));
                }
                sb.append('\u2016');
            }
            if (end > bytes.limit())
                end = bytes.limit();
            // after
            for (long i = position; i < end; i++) {
                sb.append(bytes.printable(i));
            }
        } catch (IOException e) {
            try {
                sb.append(e.toString());
            } catch (IOException e1) {
                throw new AssertionError(e);
            }
        }
    }

    public static <IN extends RandomDataInput & StreamingCommon> void toString(IN bytes, StringBuilder sb) {
        for (long i = bytes.position(); i < bytes.limit(); i++) {
            sb.append((char) bytes.readUnsignedByte(i));
        }
    }

    public static long readStopBit(StreamingDataInput in) {
        long l;
        if ((l = in.readByte()) >= 0)
            return l;
        return readStopBit0(in, l);
    }

    static long readStopBit0(StreamingDataInput in, long l) {
        l &= 0x7FL;
        long b;
        int count = 7;
        while ((b = in.readByte()) < 0) {
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

    public static <S extends StreamingDataOutput & ByteStringAppender> void append(S out, long num) {
        if (num < 0) {
            if (num == Long.MIN_VALUE) {
                out.write(MIN_VALUE_TEXT);
                return;
            }
            out.writeByte((byte) '-');
            num = -num;
        }
        if (num == 0) {
            out.writeByte((byte) '0');
        } else {
            appendLong0(out, num);
        }
    }

    private static void appendLong0(StreamingDataOutput out, long num) {
        byte[] numberBuffer = NUMBER_BUFFER.get();
        // Extract digits into the end of the numberBuffer
        int endIndex = appendLong1(numberBuffer, num);

        // Bulk copy the digits into the front of the buffer
        out.write(numberBuffer, endIndex, numberBuffer.length - endIndex);
    }

    private static int appendLong1(byte[] numberBuffer, long num) {
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

    public static void append(StreamingDataOutput out, double d) {
        long val = Double.doubleToRawLongBits(d);
        int sign = (int) (val >>> 63);
        int exp = (int) ((val >>> 52) & 2047);
        long mantissa = val & ((1L << 52) - 1);
        if (sign != 0) {
            out.writeByte((byte) '-');
        }
        if (exp == 0 && mantissa == 0) {
            out.writeByte((byte) '0');
            return;
        } else if (exp == 2047) {
            if (mantissa == 0) {
                out.write(Infinity);
            } else {
                out.write(NaN);
            }
            return;
        } else if (exp > 0) {
            mantissa += 1L << 52;
        }
        final int shift = (1023 + 52) - exp;
        if (shift > 0) {
            // integer and faction
            if (shift < 53) {
                long intValue = mantissa >> shift;
                appendLong0(out, intValue);
                mantissa -= intValue << shift;
                if (mantissa > 0) {
                    out.writeByte((byte) '.');
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
                        out.writeByte((byte) ('0' + num));
                        mantissa -= num << precision;

                        final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                        if (parsedValue == d)
                            break;
                    }
                }
                return;
            } else {
                // faction.
                out.writeByte((byte) '0');
                out.writeByte((byte) '.');
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
                        out.writeByte((byte) '0');
                        continue;
                    }
                    long num = (mantissa >>> precision);
                    value = value * 10 + num;
                    final char c = (char) ('0' + num);
                    assert !(c < '0' || c > '9');
                    out.writeByte((byte) c);
                    mantissa -= num << precision;
                    final double parsedValue = asDouble(value, 0, sign != 0, ++decimalPlaces);
                    if (parsedValue == d)
                        break;
                }
                return;
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

        appendLong0(out, val2);
        for (int i = 0; i < digits; i++)
            out.writeByte((byte) '0');
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

    public static String readUTFΔ(StreamingDataInput in) {
        StringBuilder sb = SBP.acquireStringBuilder();
        return in.readUTFΔ(sb) ? SI.intern(sb) : null;
    }

    @NotNull
    public static String parseUTF(StreamingDataInput bytes, @NotNull StopCharTester tester) {
        StringBuilder utfReader = SBP.acquireStringBuilder();
        parseUTF(bytes, utfReader, tester);
        return SI.intern(utfReader);
    }

    public static void parseUTF(StreamingDataInput bytes, @NotNull StringBuilder builder, @NotNull StopCharTester tester) {
        builder.setLength(0);
        try {
            readUTF0(bytes, builder, tester);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static void readUTF0(StreamingDataInput bytes, @NotNull Appendable appendable, @NotNull StopCharTester tester) throws IOException {
        while (true) {
            int c = bytes.readUnsignedByte();
            if (c >= 128) {
                bytes.skip(-1);
                break;
            }
            if (tester.isStopChar(c))
                return;
            appendable.append((char) c);
        }

        while (true) {
            int c = bytes.readUnsignedByte();
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
                    int char2 = bytes.readUnsignedByte();
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

                    int char2 = bytes.readUnsignedByte();
                    int char3 = bytes.readUnsignedByte();

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

    public static double parseDouble(StreamingDataInput in) {
        long value = 0;
        int exp = 0;
        boolean negative = false;
        int decimalPlaces = Integer.MIN_VALUE;
        int ch = in.readUnsignedByte();
        switch (ch) {
            case 'N':
                if (compareRest(in, "aN"))
                    return Double.NaN;
                in.skip(-1);
                return Double.NaN;
            case 'I':
                if (compareRest(in, "nfinity"))
                    return Double.POSITIVE_INFINITY;
                in.skip(-1);
                return Double.NaN;
            case '-':
                if (compareRest(in, "Infinity"))
                    return Double.NEGATIVE_INFINITY;
                negative = true;
                ch = in.readUnsignedByte();
                break;
        }
        while (true) {
            if (ch >= '0' && ch <= '9') {
                while (value >= MAX_VALUE_DIVIDE_10) {
                    value >>>= 1;
                    exp++;
                }
                value = value * 10 + (ch - '0');
                decimalPlaces++;
            } else if (ch == '.') {
                decimalPlaces = 0;
            } else {
                break;
            }
            ch = in.readUnsignedByte();
        }

        return asDouble(value, exp, negative, decimalPlaces);
    }

    static boolean compareRest(StreamingDataInput in, String s) {
        if (s.length() > in.remaining())
            return false;
        long position = in.position();
        for (int i = 0; i < s.length(); i++) {
            if (in.readUnsignedByte() != s.charAt(i)) {
                in.position(position);
                return false;
            }
        }
        return true;
    }

    public static long parseLong(StreamingDataInput in) {
        long num = 0;
        boolean negative = false;
        while (true) {
            int b = in.readUnsignedByte();
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

    public static boolean skipTo(ByteStringParser parser, StopCharTester tester) {
        while (parser.remaining() > 0) {
            int ch = parser.readUnsignedByte();
            if (tester.isStopChar(ch))
                return true;
        }
        return false;
    }

    public static int getAndAddInt(RandomDataInput in, long offset, int adding) {
        for (; ; ) {
            int value = in.readVolatileInt(offset);
            if (in.compareAndSwapInt(offset, value, value + adding))
                return value;
        }
    }

    public static long getAndAddLong(RandomDataInput in, long offset, long adding) {
        for (; ; ) {
            long value = in.readVolatileLong(offset);
            if (in.compareAndSwapLong(offset, value, value + adding))
                return value;
        }
    }
}
