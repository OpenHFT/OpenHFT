package net.openhft.lang.io;

import net.openhft.lang.Maths;

public enum VanillaBytesHasher implements BytesHasher {
    INSTANCE;
    private static final long LONG_LEVEL_PRIME_MULTIPLE = 0x9ddfea08eb382d69L;
    private static final short SHORT_LEVEL_PRIME_MULTIPLE = 0x404f;
    private static final byte BYTE_LEVEL_PRIME_MULTIPLE = 0x57;

    public long hash(Bytes bytes) {
        return hash(bytes, bytes.position(), bytes.limit());
    }

    public long hash(Bytes bytes, long offset, long limit) {
        return Maths.hash(limit - offset == 8 ? bytes.readLong(offset) : hash0(bytes, offset, limit));
    }

    private long hash0(Bytes bytes, long offset, long limit) {
        long h = 0;
        long i = offset;
        for (; i < limit - 7; i += 8)
            h = LONG_LEVEL_PRIME_MULTIPLE * h + bytes.readLong(i);
        for (; i < limit - 1; i += 2)
            h = SHORT_LEVEL_PRIME_MULTIPLE * h + bytes.readShort(i);
        if (i < limit)
            h = BYTE_LEVEL_PRIME_MULTIPLE * h + bytes.readByte(i);
        return h;
    }
}
