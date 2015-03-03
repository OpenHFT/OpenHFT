package net.openhft.lang.io;

import java.nio.ByteBuffer;

/**
 * Created by peter.lawrey on 27/01/15.
 */
public interface IByteBufferBytes extends Bytes {
    /**
     * Obtain the underlying ByteBuffer
     */
    public ByteBuffer buffer();

}
