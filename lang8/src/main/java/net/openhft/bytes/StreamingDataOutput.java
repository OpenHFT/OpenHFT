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
}
