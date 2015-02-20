package net.openhft.bytes;

import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * Position based access.  Once data has been read, the position() moves.
 * <p/>
 * The use of this instance is single threaded, though the use of the data
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

    S writeUnsignedByte(int i);

    S writeUnsignedInt(long i);

    S writeInt(int i);

    S writeByte(byte i8);

    S writeShort(short i16);

    S writeUnsignedShort(int u16);

    S writeLong(long i64);

    S writeFloat(float f);

    S writeDouble(double d);

    S write(Bytes bytes);
}
