package net.openhft.bytes;

import net.openhft.core.UnsafeMemory;
import sun.misc.Unsafe;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HeapBytesStore implements BytesStore {
    private static final AtomicBoolean MEMORY_BARRIER = new AtomicBoolean();
    private final AtomicInteger refCount = new AtomicInteger();
    private final Object underlyingObject, additional;
    private final int dataOffset, capacity;

    public HeapBytesStore(ByteBuffer byteBuffer) {
        this.additional = byteBuffer;
        this.underlyingObject = byteBuffer.array();
        this.dataOffset = Unsafe.ARRAY_BYTE_BASE_OFFSET;
        this.capacity = byteBuffer.capacity();
    }

    @Override
    public void storeFence() {
        MEMORY_BARRIER.lazySet(true);
    }

    @Override
    public void loadFence() {
        MEMORY_BARRIER.get();
    }

    @Override
    public void reserve() {
        refCount.incrementAndGet();
    }

    @Override
    public void release() {
        refCount.decrementAndGet();
    }

    @Override
    public int refCount() {
        return refCount.get();
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public int readUnsignedByte(long position) {
        if (position >= 0 && position < capacity)
            return UnsafeMemory.INSTANCE.readByte(underlyingObject, dataOffset + position);
        throw new BufferUnderflowException();
    }

    @Override
    public void writeUnsignedByte(long position, int i) {
        if (position >= 0 && position < capacity)
            UnsafeMemory.INSTANCE.writeByte(underlyingObject, dataOffset + position, (byte) i);
        else
            throw new BufferOverflowException();
    }
}
