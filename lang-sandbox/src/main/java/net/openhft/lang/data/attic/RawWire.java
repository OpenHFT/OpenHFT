package net.openhft.lang.data.attic;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.Nullable;

import java.nio.BufferUnderflowException;

/**
 * Created by peter on 1/10/15.
 */
public class RawWire implements Wire {
    public static final int FLOAT_LITERAL = -104;
    public static final int DOUBLE_LITERAL = -108;
    public static final int ZERO_LITERAL = -128;
    public static final int SCALE = 1000000;
    public static final long LONG_MAX_VALUE_DIV_SCALE = Long.MAX_VALUE / SCALE;
    private static long[] TENS = {1, 10, 100, 1000, 10000, 100000, 100000};
    private Bytes bytes;

    private static byte byteDefault(WireKey key) {
        return ((Number) key.defaultValue()).byteValue();
    }

    private static short shortDefault(WireKey key) {
        return ((Number) key.defaultValue()).shortValue();
    }

    private static int intDefault(WireKey key) {
        return ((Number) key.defaultValue()).intValue();
    }

    private static long longDefault(WireKey key) {
        return ((Number) key.defaultValue()).longValue();
    }

    public static void writeCompactFloat(EncodeMode encodeMode, Bytes bytes, float value) {
        if (value >= -LONG_MAX_VALUE_DIV_SCALE && value <= LONG_MAX_VALUE_DIV_SCALE) {
            long num = Math.round(value * SCALE);
            if (encodeMode.compare(num / 1e6f, value)) {
                writeCompactDouble6(bytes, num);
                return;
            }
        }
        writeFloat0(bytes, value);
    }

    private static void writeFloat0(Bytes bytes, float value) {
        bytes.writeByte(FLOAT_LITERAL);
        bytes.writeFloat(value);
    }

    public static void writeCompactDouble(EncodeMode encodeMode, Bytes bytes, double value) {
        if (value >= -LONG_MAX_VALUE_DIV_SCALE && value <= LONG_MAX_VALUE_DIV_SCALE) {
            long num = Math.round(value * SCALE);
            if (encodeMode.compare(num / 1e6, value)) {
                writeCompactDouble6(bytes, num);
                return;
            }
        }
        float f = (float) value;
        if (f == value) {
            writeFloat0(bytes, f);
        } else {
            writeDouble0(bytes, value);
        }
    }

    private static void writeDouble0(Bytes bytes, double value) {
        bytes.writeByte(DOUBLE_LITERAL);
        bytes.writeDouble(value);
    }

    private static void writeCompactDouble6(Bytes bytes, long num) {
        if (num == 0) {
            bytes.writeByte(ZERO_LITERAL);
            return;
        }
        int scale = 6;
        maxScale:
        {
            while (num % 100 == 0) {
                num /= 100;
                scale -= 2;
                if (scale <= -6)
                    break maxScale;
            }
            if (num % 10 == 0) {
                num /= 10;
                scale--;
            }
        }
        bytes.writeByte(scale);
        bytes.writeStopBit(num);
    }

    public Bytes getBytes() {
        return bytes;
    }

    public void setBytes(Bytes bytes) {
        this.bytes = bytes;
    }

    @Override
    public void writeBoolean(WireKey key, boolean value) {
        bytes.writeBoolean(value);
    }

    @Override
    public boolean readBoolean(WireKey key) {
        return bytes.remaining() >= 1 ? bytes.readBoolean() : booleanDefault(key);
    }

    private boolean booleanDefault(WireKey key) {
        return (Boolean) key.defaultValue();
    }

    @Override
    public void writeByte(WireKey key, int value) {
        bytes.writeByte(value);
    }

    @Override
    public byte readByte(WireKey key) {
        return bytes.remaining() >= 1
                ? bytes.readByte()
                : byteDefault(key);
    }

    @Override
    public void writeUnsignedByte(WireKey key, int value) {
        bytes.writeUnsignedShort(value);
    }

    @Override
    public short readUnsignedByte(WireKey key) {
        return bytes.remaining() >= 1
                ? (short) bytes.readUnsignedByte()
                : shortDefault(key);
    }

    @Override
    public void writeShort(WireKey key, int value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeShort(value);
        } else {
            bytes.writeStopBit(value);
        }
    }

    @Override
    public short readShort(WireKey key) {
        return key.encodeMode() != EncodeMode.LITERAL
                ? (short) bytes.readStopBit()
                : bytes.readShort();
    }

    @Override
    public void writeUnsignedShort(WireKey key, int value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeUnsignedShort(value);
        } else {
            bytes.writeStopBit(value);
        }
    }

    @Override
    public int readUnsignedShort(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 2 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readUnsignedShort()
                        : (int) bytes.readStopBit();
            } catch (BufferUnderflowException ignored) {
            }
        return intDefault(key);
    }

    @Override
    public void writeInt(WireKey key, int value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeInt(value);
        } else {
            bytes.writeStopBit(value);
        }
    }

    @Override
    public int readInt(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 4 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readInt()
                        : (int) bytes.readStopBit();
            } catch (BufferUnderflowException ignored) {
            }
        return intDefault(key);
    }

    @Override
    public void writeUnsignedInt(WireKey key, long value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeUnsignedInt(value);
        } else {
            bytes.writeStopBit(value);
        }
    }

    @Override
    public long readUnsignedInt(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 4 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readUnsignedInt()
                        : (int) bytes.readStopBit();
            } catch (BufferUnderflowException ignored) {
            }
        return intDefault(key);
    }

    @Override
    public void writeLong(WireKey key, long value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeLong(value);
        } else {
            bytes.writeStopBit(value);
        }
    }

    @Override
    public long readLong(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 8 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readLong()
                        : bytes.readStopBit();
            } catch (BufferUnderflowException ignored) {
            }
        return longDefault(key);
    }

    @Override
    public void writeFloat(WireKey key, float value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeFloat(value);
        } else {
            writeCompactFloat(key.encodeMode(), bytes, value);
        }
    }

    @Override
    public float readFloat(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 4 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readFloat()
                        : (float) readCompactDouble(bytes);
            } catch (BufferUnderflowException ignored) {
            }
        return longDefault(key);
    }

    private double readCompactDouble(Bytes bytes) {
        byte scale = bytes.readByte();
        switch (scale) {
            case ZERO_LITERAL:
                return 0;
            case DOUBLE_LITERAL:
                return bytes.readDouble();
            case FLOAT_LITERAL:
                return bytes.readFloat();
        }
        double unscaled = (double) bytes.readStopBit();
        if (scale == 0) return unscaled;
        return scale < 0
                ? unscaled * TENS[-scale]
                : unscaled / TENS[scale];
    }

    @Override
    public void writeDouble(WireKey key, double value) {
        if (key.encodeMode() == EncodeMode.LITERAL) {
            bytes.writeDouble(value);
        } else {
            writeCompactDouble(key.encodeMode(), bytes, value);
        }
    }

    @Override
    public double readDouble(WireKey key) {
        boolean isLiteral = key.encodeMode() == EncodeMode.LITERAL;
        int min = isLiteral ? 8 : 1;
        if (bytes.remaining() >= min)
            try {
                return isLiteral
                        ? bytes.readDouble()
                        : readCompactDouble(bytes);
            } catch (BufferUnderflowException ignored) {
            }
        return longDefault(key);
    }

    @Override
    public void writeText(WireKey key, CharSequence value) {

    }

    @Override
    public String readText(WireKey key) {
        return null;
    }

    @Override
    public boolean readText(WireKey key, StringBuilder sb) {
        return false;
    }

    @Override
    public void writeEnum(WireKey key, Enum value) {

    }

    @Override
    public Enum readEnum(WireKey key) {
        return null;
    }

    @Override
    public Enum readEnum(WireKey key, @Nullable StringBuilder value) {
        return null;
    }
}
