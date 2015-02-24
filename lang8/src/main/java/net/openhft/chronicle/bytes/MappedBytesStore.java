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

    @Override
    public long start() {
        return start;
    }

    @Override
    public long safeCapacity() {
        return safeCapacity;
    }
}
