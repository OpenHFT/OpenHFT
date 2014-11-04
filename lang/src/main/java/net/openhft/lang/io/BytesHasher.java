package net.openhft.lang.io;

public interface BytesHasher {
    /**
     * Provide a 64-bit hash for the bytes in Bytes between the bytes.position() and bytes.limit();
     *
     * @param bytes to hash
     * @return 64-bit hash
     */
    public long hash(Bytes bytes);

    /**
     * Provide a 64-bit hash for the bytes between offset and limit
     *
     * @param bytes  to hash
     * @param offset the start inclusive
     * @param limit  the end exclusive
     * @return 64-bit hash.
     */
    public long hash(Bytes bytes, long offset, long limit);
}
