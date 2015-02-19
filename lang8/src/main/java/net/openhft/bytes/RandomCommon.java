package net.openhft.bytes;

public interface RandomCommon<S extends RandomCommon<S>> {
    /**
     * @return The smallest position allowed in this buffer.
     */
    default long start() {
        return 0L;
    }

    /**
     * @return the highest offset or position allowed for this buffer.
     */
    default long limit() {
        return capacity();
    }

    /**
     * @return the highest limit allowed for this buffer.
     */
    long capacity();
}
