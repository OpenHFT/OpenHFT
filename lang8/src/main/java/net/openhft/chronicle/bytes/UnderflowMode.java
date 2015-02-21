package net.openhft.chronicle.bytes;

public enum UnderflowMode {
    /**
     * Throw a BufferedUnderflowException if reading beyond the end of the buffer.
     */
    BOUNDED {
        @Override
        boolean isRemainingOk(long remaining, int needs) {
            return remaining >= needs;
        }
    },
    /**
     * return 0, false, empty string or some default if remaining() == 0 otherwise if remaining() is less than required throw a BufferUnderflowException.
     */
    ZERO_EXTEND {
        @Override
        boolean isRemainingOk(long remaining, int needs) {
            return remaining >= needs || remaining <= 0;
        }
    },
    /**
     * any read beyond the limit should be treated as 0.
     */
    PADDED {
        @Override
        boolean isRemainingOk(long remaining, int needs) {
            return true;
        }
    };

    abstract boolean isRemainingOk(long remaining, int needs);
}
