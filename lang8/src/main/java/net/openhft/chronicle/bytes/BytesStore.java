package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.ReferenceCounted;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * A reference to some bytes with fixed extents.  Only offset access within the capacity is possible.
 */
public interface BytesStore<B extends BytesStore<B>> extends RandomDataInput<B>, RandomDataOutput<B>, ReferenceCounted {
    static BytesStore wrap(ByteBuffer bb) {
        return bb.isDirect()
                ? NativeStore.wrap(bb)
                : HeapBytesStore.wrap(bb);
    }

    default Bytes bytes() {
        return bytes(UnderflowMode.BOUNDED);
    }

    default Bytes bytes(UnderflowMode underflowMode) {
        switch (underflowMode) {
            case BOUNDED:
                return new BytesStoreBytes(this);
            case ZERO_EXTEND:
            case PADDED:
                return new ZeroedBytes(this, underflowMode);
            default:
                throw new UnsupportedOperationException("Unknown known mode " + underflowMode);
        }
    }

    /**
     * @return The smallest position allowed in this buffer.
     */
    default long start() {
        return 0L;
    }

    long capacity();

    /**
     * Perform a set of actions with a temporary bounds mode.
     */
    default BytesStore with(long position, long length, Consumer<Bytes> bytesConsumer) {
        if (position + length > capacity())
            throw new BufferUnderflowException();
        BytesStoreBytes bsb = new BytesStoreBytes(this);
        bsb.position(position);
        bsb.limit(position + length);
        bytesConsumer.accept(bsb);
        return this;
    }

    void storeFence();

    void loadFence();
}
