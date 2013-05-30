package net.openhft.lang.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public interface BytesCommon {
    /**
     * @return the offset read/written so far
     */
    int position();

    /**
     * @param position to skip to
     */
    void position(int position);

    /**
     * @return space available
     */
    int capacity();

    /**
     * @return space remaining in bytes
     */
    int remaining();

    /**
     * Mark the end of the message if writing and check we are at the end of the message if reading.
     */
    void finish();

    boolean isFinished();

    /**
     * @return Byte order for reading binary
     */
    ByteOrder byteOrder();

    /**
     * @return these Bytes as an InputStream
     */
    InputStream inputStream();

    /**
     * @return these Bytes as an OutputStream
     */
    OutputStream outputStream();

    /**
     * @return the factory for marshallers.
     */
    BytesMarshallerFactory bytesMarshallerFactory();

    /**
     * @throws IndexOutOfBoundsException if the bounds of the Bytes has been exceeded.
     */
    void checkEndOfBuffer() throws IndexOutOfBoundsException;
}
