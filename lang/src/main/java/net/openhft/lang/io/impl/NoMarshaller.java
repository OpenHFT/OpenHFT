package net.openhft.lang.io.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.BytesMarshaller;

/**
 * Created with IntelliJ IDEA. User: peter Date: 19/09/13 Time: 18:26 To change this template use File | Settings | File
 * Templates.
 */
public enum NoMarshaller implements BytesMarshaller<Void> {
    INSTANCE;

    @Override
    public void write(Bytes bytes, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void read(Bytes bytes) {
        throw new UnsupportedOperationException();
    }
}
