package net.openhft.bytes;

public interface RandomCommon<S extends RandomCommon<S>> {
    /**
     * @return The smallest position allowed in this buffer.
     */
    long start();

    /**
     * @return the highest offset or position allowed for this buffer.
     */
    long limit();

    /**
     * @return the highest limit allowed for this buffer.
     */
    long capacity();
}
