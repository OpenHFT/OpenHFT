package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.OS;
import net.openhft.chronicle.core.ReferenceCounted;

public class MappedByteStore extends NativeStore {
    protected MappedByteStore(ReferenceCounted owner, long address, long capacity) {
        super(address, capacity, new OS.Unmapper(address, capacity, owner));
    }

    public static MappedByteStore of(ReferenceCounted owner, long address, long size) {
        return new MappedByteStore(owner, address, size);
    }
}
