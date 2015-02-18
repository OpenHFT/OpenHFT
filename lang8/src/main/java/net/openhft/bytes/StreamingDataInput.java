package net.openhft.bytes;

import java.io.InputStream;
import java.io.ObjectInput;

/**
 * This data input has a a position() and a limit()
 */
public interface StreamingDataInput<S extends StreamingDataInput<S>> extends StreamingCommon<S> {
    UnderflowMode underflowMode();

    Bytes underflowMode(UnderflowMode underflowMode);

    ObjectInput objectInput();

    InputStream inputStream();
}
