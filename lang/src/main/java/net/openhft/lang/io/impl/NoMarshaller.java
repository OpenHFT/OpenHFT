package net.openhft.lang.io.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.BytesMarshaller;
import net.openhft.lang.io.StopCharTester;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA. User: peter Date: 19/09/13 Time: 18:26 To change this template use File | Settings | File
 * Templates.
 */
public enum NoMarshaller implements BytesMarshaller<Void> {
    INSTANCE;

    @NotNull
    @Override
    public Class<Void> classMarshaled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(Bytes bytes, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void read(Bytes bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void append(Bytes bytes, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void parse(Bytes bytes, StopCharTester tester) {
        throw new UnsupportedOperationException();
    }
}
