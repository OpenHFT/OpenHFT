package net.openhft.bytes;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class BytesStoreBytes implements Bytes {
    private static final AtomicBoolean MEMORY_BARRIER = new AtomicBoolean();

    private final BytesStore bytesStore;
    private long offset, position, limit, writeLimit, capacity;
    private UnderflowMode underflowMode = UnderflowMode.BOUNDED;

    public BytesStoreBytes(BytesStore bytesStore) {
        this(bytesStore, 0, bytesStore.capacity());
    }

    public BytesStoreBytes(BytesStore bytesStore, long offset, long capacity) {
        this.bytesStore = bytesStore;
        this.offset = offset;
        this.capacity = capacity;
        assert offset + capacity <= bytesStore.capacity();
        clear();
    }

    @Override
    public void storeFence() {
        MEMORY_BARRIER.lazySet(true);
    }

    @Override
    public void loadFence() {
        MEMORY_BARRIER.get();
    }

    public Bytes clear() {
        position = 0;
        limit = writeLimit = capacity;
        return this;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public long limit() {
        return limit;
    }

    @Override
    public Bytes position(long position) {
        if (position < start()) throw new BufferUnderflowException();
        if (underflowMode == UnderflowMode.BOUNDED && position >= limit())
            throw new BufferOverflowException();
        this.position = position;
        return this;
    }

    @Override
    public Bytes limit(long limit) {
        if (limit < start()) throw new BufferUnderflowException();
        if (underflowMode == UnderflowMode.BOUNDED && limit >= capacity())
            throw new BufferOverflowException();
        writeLimit = Math.min(limit, capacity());
        this.limit = limit;
        return this;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public UnderflowMode underflowMode() {
        return underflowMode;
    }

    @Override
    public Bytes underflowMode(UnderflowMode underflowMode) {
        Objects.requireNonNull(underflowMode);
        this.underflowMode = underflowMode;
        return this;
    }

    @Override
    public void writeUnsignedByte(int i) {
        if (position >= writeLimit)
            throw new BufferOverflowException();
        bytesStore.writeUnsignedByte(position++, i);
    }

    @Override
    public int readUnsignedByte() {
        if (position < limit)
            return bytesStore.readUnsignedByte(position++);
        if (underflowMode.isRemainingOk(remaining(), 1)) {
            position++;
            return 0;
        }
        throw new BufferUnderflowException();
    }

    @Override
    public int refCount() {
        return bytesStore.refCount();
    }

    @Override
    public void reserve() {
        bytesStore.reserve();
    }

    @Override
    public void release() {
        bytesStore.release();
    }

    @Override
    public int readUnsignedByte(long position) {
        if (position >= start() && position < capacity())
            return bytesStore.readUnsignedByte(offset + position);
        else
            throw new BufferUnderflowException();
    }

    @Override
    public void writeUnsignedByte(long position, int i) {
        if (position >= start() && position < capacity())
            bytesStore.writeUnsignedByte(offset + position, i);
        else
            throw new BufferOverflowException();
    }
}
