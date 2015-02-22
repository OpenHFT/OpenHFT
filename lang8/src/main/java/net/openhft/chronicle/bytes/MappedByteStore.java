package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.ReferenceCounted;

public class MappedByteStore extends NativeStore {
    private final long start;

    protected MappedByteStore(ReferenceCounted owner, long start, long address, long capacity) {
        super(address, capacity, new OS.Unmapper(address, capacity, owner));
        this.start = start;
    }

    public static MappedByteStore of(ReferenceCounted owner, long start, long address, long size) {
        return new MappedByteStore(owner, start, address, size);
    }

    @Override
    public long start() {
        return start;
    }
}
