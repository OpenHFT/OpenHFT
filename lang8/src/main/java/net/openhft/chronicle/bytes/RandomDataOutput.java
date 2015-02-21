package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.Maths;

import java.nio.ByteBuffer;

public interface RandomDataOutput<R extends RandomDataOutput<R>> {
    default R writeByte(long offset, int i) {
        return writeByte(offset, Maths.toInt8(i));
    }

    default R writeUnsignedByte(long offset, int i) {
        return writeByte(offset, (byte) Maths.toUInt8(i));
    }

    default R writeBoolean(long offset, boolean flag) {
        return writeByte(offset, flag ? 'Y' : 0);
    }

    default R writeUnsignedShort(long offset, int i) {
        return writeShort(offset, (short) Maths.toUInt16(i));
    }

    default R writeUnsignedInt(long offset, long i) {
        return writeInt(offset, (int) Maths.toUInt32(i));
    }

    R writeByte(long offset, byte i8);

    R writeShort(long offset, short i);

    R writeInt(long offset, int i);

    R writeOrderedInt(long offset, int i);

    R writeLong(long offset, long i);

    R writeOrderedLong(long offset, long i);

    R writeFloat(long offset, float d);

    R writeDouble(long offset, double d);

    default R write(long offsetInRDO, byte[] bytes) {
        return write(offsetInRDO, bytes, 0, bytes.length);
    }

    R write(long offsetInRDO, byte[] bytes, int offset, int length);

    R write(long offsetInRDO, ByteBuffer bytes, int offset, int length);
}
