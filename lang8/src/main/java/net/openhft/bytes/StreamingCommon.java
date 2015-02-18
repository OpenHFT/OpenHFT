package net.openhft.bytes;

public interface StreamingCommon<S extends StreamingCommon<S>> extends RandomCommon<S> {
    /**
     * @return the number of bytes between the position and the limit.
     */
    default long remaining() {
        return limit() - position();
    }

    S position(long position);

    long position();

    S limit(long limit);

    S clear();
}
