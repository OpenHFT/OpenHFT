package net.openhft.bytes;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UTFDataFormatException;

public enum BytesUtil {
    ;

    public static void parseUTF(StreamingDataInput bytes, StringBuilder appendable, int utflen) throws IOException {
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

        parseUTF2(bytes, appendable, utflen, count);
    }

    static void parseUTF2(StreamingDataInput bytes, StringBuilder appendable, int utflen, int count) throws UTFDataFormatException {
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
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    int char2 = bytes.readUnsignedByte();
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
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
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    int char2 = bytes.readUnsignedByte();
                    int char3 = bytes.readUnsignedByte();

                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
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
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
    }

    public static void writeUTF(StreamingDataOutput bytes, @NotNull CharSequence str) {
        bytes.writeStopBit(findUTFLength(str));
        appendUTF(bytes, str);
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

    public static void appendUTF(StreamingDataOutput bytes, @NotNull CharSequence str) {
        int i, strlen = str.length();
        for (i = 0; i < strlen; i++) {
            char c = str.charAt(i);
            if (c > 0x007F)
                break;
            bytes.writeByte((byte) c);
        }

        for (; i < strlen; i++) {
            char c = str.charAt(i);
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
}
