package net.openhft.bytes;

import net.openhft.core.ReferenceCounted;

import java.nio.BufferUnderflowException;
import java.util.function.Consumer;

/**
 * A reference to some bytes with fixed extents.  Only offset access within the capacity is possible.
 */
public interface BytesStore<B extends BytesStore<B>> extends RandomDataInput, RandomDataOutput, ReferenceCounted {

    default Bytes bytes() {
        return new BytesStoreBytes(this);
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
