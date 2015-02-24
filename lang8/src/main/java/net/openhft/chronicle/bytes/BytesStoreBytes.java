package net.openhft.chronicle.bytes;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class BytesStoreBytes extends AbstractBytes {
    private long offset, capacity;

    public BytesStoreBytes(BytesStore bytesStore) {
        this(bytesStore, 0, bytesStore.capacity());
        bytesStore.reserve();
    }

    public BytesStoreBytes(BytesStore bytesStore, long offset, long capacity) {
        super(bytesStore);
        this.offset = offset;
        this.capacity = capacity;
        assert offset + capacity <= bytesStore.capacity();
        clear();
    }

    public void setBytesStore(BytesStore bytesStore) {
        this.bytesStore.release();
        this.bytesStore = bytesStore;
        this.offset = 0;
        this.capacity = bytesStore.capacity();
        assert offset + capacity <= bytesStore.capacity();
        clear();
    }

    @Override
    public long capacity() {
        return capacity;
    }

    protected long checkOffset(long offset) {
        if (offset < start()) throw new BufferUnderflowException();
        if (offset > capacity()) throw new BufferOverflowException();
        return this.offset + offset;
    }
}
