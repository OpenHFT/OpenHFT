package net.openhft.bytes;

import net.openhft.core.Memory;

import java.nio.BufferUnderflowException;
import java.util.function.Consumer;

public interface Bytes extends BytesStore<Bytes>, StreamingDataInput<Bytes>, StreamingDataOutput<Bytes> {

    /**
     * Perform a set of actions with a temporary bounds mode.
     */
    default Bytes with(long length, Consumer<Bytes> bytesConsumer) {
        if (length > remaining())
            throw new BufferUnderflowException();
        long limit0 = limit();
        long limit = position() + length;
        try {
            limit(limit);
            bytesConsumer.accept(this);
        } finally {
            position(limit);
            limit(limit0);
        }
        return this;
    }

    long remaining();

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
        memory().storeFence();

        return this;
    }

    void writeUnsignedByte(int i);

    Memory memory();

    default Bytes readLength8(Consumer<Bytes> reader) {
        memory().loadFence();
        int length = readUnsignedByte() - 1;
        if (length < 0)
            throw new IllegalStateException("Unset length");
        return with(length, reader);
    }

    int readUnsignedByte();

/*
    Bytes writeLength16(Consumer<Bytes> writer);

    Bytes readLength16(Consumer<Bytes> writer);

    Bytes writeLength32(Consumer<Bytes> writer);

    Bytes readLength32(Consumer<Bytes> writer);
*/
}
