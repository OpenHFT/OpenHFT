package net.openhft.bytes;

import net.openhft.core.Maths;

public interface RandomDataOutput<R extends RandomDataOutput<R>> {
    R writeOrderedInt(long offset, int i);

    default R writeByte(long offset, int i) {
        return writeByte(offset, Maths.toInt8(i));
    }

    default R writeUnsignedByte(long offset, int i) {
        return writeByte(offset, (byte) Maths.toUInt8(i));
    }

    R writeByte(long offset, byte i8);
}
