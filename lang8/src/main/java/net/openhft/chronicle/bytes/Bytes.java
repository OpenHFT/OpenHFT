package net.openhft.chronicle.bytes;

import java.nio.ByteOrder;
import java.util.function.Consumer;

public interface Bytes extends BytesStore<Bytes>,
        StreamingDataInput<Bytes>, StreamingDataOutput<Bytes>,
        ByteStringParser<Bytes>, ByteStringAppender<Bytes> {

    long position();

    Bytes position(long position);

    long limit();

    Bytes limit(long limit);

    default Bytes writeLength8(Consumer<Bytes> writer) {
        long position = position();
        writeUnsignedByte(0);

        writer.accept(this);
        long length = position() - position;
        if (length >= 1 << 8)
            throw new IllegalStateException("Cannot have an 8-bit length of " + length);
        writeUnsignedByte(position, (short) length);
        storeFence();

        return this;
    }


    default Bytes readLength8(Consumer<Bytes> reader) {
        loadFence();
        int length = readUnsignedByte() - 1;
        if (length < 0)
            throw new IllegalStateException("Unset length");
        return withLength(length, reader);
    }

    default ByteOrder byteOrder() {
        return ByteOrder.nativeOrder();
    }

/*
    Bytes writeLength16(Consumer<Bytes> writer);

    Bytes readLength16(Consumer<Bytes> writer);

    Bytes writeLength32(Consumer<Bytes> writer);

    Bytes readLength32(Consumer<Bytes> writer);
*/
}
