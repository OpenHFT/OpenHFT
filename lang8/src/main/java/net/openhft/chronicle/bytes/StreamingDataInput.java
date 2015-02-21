package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.Maths;

import java.io.InputStream;
import java.io.ObjectInput;
import java.nio.ByteBuffer;

/**
 * This data input has a a position() and a limit()
 */
public interface StreamingDataInput<S extends StreamingDataInput<S>> extends StreamingCommon<S> {
    UnderflowMode underflowMode();

    default ObjectInput objectInput() {
        throw new UnsupportedOperationException();
    }

    default InputStream inputStream() {
        throw new UnsupportedOperationException();
    }

    default boolean readUTFΔ(StringBuilder sb) throws UTFDataFormatRuntimeException {
        sb.setLength(0);
        long len0 = BytesUtil.readStopBit(this);
        if (len0 == -1)
            return false;
        int len = Maths.toUInt31(len0);
        BytesUtil.parseUTF(this, sb, len);
        return true;
    }

    default long readStopBit() {
        return BytesUtil.readStopBit(this);
    }

    default boolean readBoolean() {
        return readByte() != 0;
    }

    byte readByte();

    default int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    short readShort();

    default int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    int readInt();

    default long readUnsignedInt() {
        return readInt() & 0xFFFFFFFFL;
    }

    long readLong();

    float readFloat();

    double readDouble();

    int peakVolatileInt();

    default String readUTFΔ() {
        return BytesUtil.readUTFΔ(this);
    }

    void read(byte[] bytes);

    void read(ByteBuffer buffer);

    int readVolatileInt();

    long readVolatileLong();
}
