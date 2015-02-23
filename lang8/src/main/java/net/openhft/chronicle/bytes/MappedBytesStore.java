package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.ReferenceCounted;

public class MappedBytesStore extends NativeStore {
    private final long start;
    private final long safeCapacity;

    protected MappedBytesStore(ReferenceCounted owner, long start, long address, long capacity, long safeCapacity) {
        super(address, capacity, new OS.Unmapper(address, capacity, owner));
        this.start = start;
        this.safeCapacity = safeCapacity;
    }

    public static MappedBytesStore of(ReferenceCounted owner, long start, long address, long size, long safeCapacity) {
        return new MappedBytesStore(owner, start, address, size, safeCapacity);
    }

    /**
     * Use this test to determine if an offset is considered safe.
     */
    public boolean inStore(long offset) {
        long offset2 = offset - start();
        return offset2 >= 0 && offset2 < safeCapacity;
    }

    @Override
    public long start() {
        return start;
    }
}
