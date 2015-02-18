package net.openhft.bytes;

import net.openhft.core.ReferenceCounted;

import java.util.function.Consumer;

/**
 * A reference to some bytes with fixed extents.  Only offset access within the capacity is possible.
 */
public interface BytesStore<B extends BytesStore<B>> extends RandomDataOutput, ReferenceCounted {

    Bytes bytes();

    long capacity();

    /**
     * Perform a set of actions with a temporary bounds mode.
     */
    void with(long position, long length, Consumer<Bytes> bytesConsumer);

    void writeUnsignedByte(long position, short length);

    Object underlyingObject();

    long address();

    long size();

}
