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

}
