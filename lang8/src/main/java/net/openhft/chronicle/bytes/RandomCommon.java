package net.openhft.chronicle.bytes;

public interface RandomCommon<S extends RandomCommon<S>> {
    /**
     * @return the highest offset or position allowed for this buffer.
     */
    default long limit() {
        return maximumLimit();
    }

    /**
     * @return the highest limit allowed for this buffer.
     */
    long maximumLimit();

    boolean compareAndSwapInt(long offset, int expected, int value);

    boolean compareAndSwapLong(long offset, long expected, long value);
}
