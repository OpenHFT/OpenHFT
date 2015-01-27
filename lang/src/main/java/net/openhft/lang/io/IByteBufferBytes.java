package net.openhft.lang.io;

import java.nio.ByteBuffer;

/**
 * Created by peter on 27/01/15.
 */
public interface IByteBufferBytes extends Bytes {
    /**
     * Obtain the underlying ByteBuffer
     */
    public ByteBuffer buffer();

    /**
     * Needed if the buffer is created in one thread and used in another.
     */
    void clearThreadAssociation();
}
