package net.openhft.bytes;

import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * Position based access.  Once data has been read, the position() moves.
 * <p/>
 * The use of this instance is single threaded, though the use of the data
 */
public interface StreamingDataOutput<S extends StreamingDataOutput<S>> extends StreamingCommon<S> {
    public ObjectOutput objectStream();

    public OutputStream outputStream();
}
