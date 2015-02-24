package net.openhft.chronicle.bytes;

import java.io.IOException;

public class IORuntimeException extends RuntimeException {
    public IORuntimeException(String message) {
        super(message);
    }

    public IORuntimeException(IOException e) {
        super(e);
    }
}
