package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.Maths;

import java.io.ObjectOutput;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Position based access.  Once data has been read, the position() moves.
 * <p>The use of this instance is single threaded, though the use of the data
 */
public interface StreamingDataOutput<S extends StreamingDataOutput<S>> extends StreamingCommon<S> {
    default public ObjectOutput objectStream() {
        throw new UnsupportedOperationException();
    }

    default public OutputStream outputStream() {
        throw new UnsupportedOperationException();
    }

    default S writeStopBit(long x) {
        BytesUtil.writeStopBit(this, x);
        return (S) this;
    }

    default S writeUTFΔ(CharSequence cs) {
        BytesUtil.writeUTF(this, cs);
        return (S) this;
    }

    S writeByte(byte i8);

    default S writeUnsignedByte(int i) {
        return writeByte((byte) Maths.toUInt8(i));
    }

    S writeShort(short i16);

    default S writeUnsignedShort(int u16) {
        return writeShort((short) Maths.toUInt16(u16));
    }

    S writeInt(int i);

    default S writeUnsignedInt(long i) {
        return writeInt((int) Maths.toUInt32(i));
    }

    S writeLong(long i64);

    S writeFloat(float f);

    S writeDouble(double d);

    S write(Bytes bytes);

    default S write(byte[] bytes) {
        return write(bytes, 0, bytes.length);
    }

    S write(byte[] bytes, int offset, int length);

    S write(ByteBuffer buffer);

    default S writeBoolean(boolean flag) {
        return writeByte(flag ? (byte) 'Y' : 0);
    }

    S writeOrderedInt(int i);

    S writeOrderedLong(long i);
}
