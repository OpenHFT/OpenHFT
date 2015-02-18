package net.openhft.bytes;

public enum UnderflowMode {
    /**
     * Throw a BufferedUnderflowException if reading beyond the end of the buffer.
     */
    BOUNDED,
    /**
     * return 0, false, empty string or some default if remaining() == 0 otherwise if remaining() is less than required throw a BufferUnderflowException.
     */
    ZERO_EXTEND,
    /**
     * any byte read beyond the limit should be treated as 0.
     */
    PADDED
}
