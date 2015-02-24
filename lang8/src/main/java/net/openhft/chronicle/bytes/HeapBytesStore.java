package net.openhft.chronicle.bytes;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

public class HeapBytesStore implements BytesStore<HeapBytesStore> {
    private static final AtomicBoolean MEMORY_BARRIER = new AtomicBoolean();
    private final AtomicLong refCount = new AtomicLong(1);
    private final Object underlyingObject, retainedObject;
    private final int dataOffset, capacity;

    private HeapBytesStore(ByteBuffer byteBuffer) {
        this.retainedObject = byteBuffer;
        this.underlyingObject = byteBuffer.array();
        this.dataOffset = Unsafe.ARRAY_BYTE_BASE_OFFSET;
        this.capacity = byteBuffer.capacity();
    }

    static HeapBytesStore wrap(ByteBuffer bb) {
        return new HeapBytesStore(bb);
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
    public long refCount() {
        return refCount.get();
    }

    @Override
    public long maximumLimit() {
        return capacity;
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) {
        return MEMORY.compareAndSwapInt(underlyingObject, dataOffset + offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) {
        return MEMORY.compareAndSwapLong(underlyingObject, dataOffset + offset, expected, value);
    }

    @Override
    public byte readByte(long offset) {
        checkOffset(offset, 1);
        return MEMORY.readByte(underlyingObject, dataOffset + offset);
    }

    @Override
    public short readShort(long offset) {
        checkOffset(offset, 2);
        return MEMORY.readShort(underlyingObject, dataOffset + offset);
    }

    @Override
    public int readInt(long offset) {
        checkOffset(offset, 4);
        return MEMORY.readInt(underlyingObject, dataOffset + offset);
    }

    @Override
    public long readLong(long offset) {
        checkOffset(offset, 8);
        return MEMORY.readLong(underlyingObject, dataOffset + offset);
    }

    @Override
    public float readFloat(long offset) {
        checkOffset(offset, 4);
        return MEMORY.readFloat(underlyingObject, dataOffset + offset);
    }

    @Override
    public double readDouble(long offset) {
        checkOffset(offset, 8);
        return MEMORY.readDouble(underlyingObject, dataOffset + offset);
    }

    @Override
    public HeapBytesStore writeByte(long offset, byte b) {
        checkOffset(offset, 1);
        MEMORY.writeByte(underlyingObject, dataOffset + offset, b);
        return this;
    }

    @Override
    public HeapBytesStore writeShort(long offset, short i16) {
        checkOffset(offset, 2);
        MEMORY.writeShort(underlyingObject, dataOffset + offset, i16);
        return this;
    }

    @Override
    public HeapBytesStore writeInt(long offset, int i32) {
        checkOffset(offset, 4);
        MEMORY.writeInt(underlyingObject, dataOffset + offset, i32);
        return this;
    }

    @Override
    public HeapBytesStore writeLong(long offset, long i64) {
        checkOffset(offset, 8);
        MEMORY.writeLong(underlyingObject, dataOffset + offset, i64);
        return this;
    }

    @Override
    public HeapBytesStore writeOrderedLong(long offset, long i) {
        checkOffset(offset, 8);
        MEMORY.writeOrderedLong(underlyingObject, dataOffset + offset, i);
        return this;
    }

    @Override
    public HeapBytesStore writeFloat(long offset, float f) {
        checkOffset(offset, 4);
        MEMORY.writeFloat(underlyingObject, dataOffset + offset, f);
        return this;
    }

    @Override
    public HeapBytesStore writeDouble(long offset, double d) {
        checkOffset(offset, 8);
        MEMORY.writeDouble(underlyingObject, dataOffset + offset, d);
        return this;
    }

    public void checkOffset(long offset, int size) {
        if (offset < start() || offset + size > capacity) {
            throw new BufferOverflowException();
        }
    }

    @Override
    public HeapBytesStore writeOrderedInt(long offset, int i32) {
        checkOffset(offset, 4);
        MEMORY.writeOrderedInt(underlyingObject, dataOffset + offset, i32);
        return this;
    }

    @Override
    public HeapBytesStore write(long offsetInRDO, byte[] bytes, int offset, int length) {
        checkOffset(offset, length);
        MEMORY.copyMemory(bytes, offset, underlyingObject, this.dataOffset + offsetInRDO, length);
        return this;
    }

    @Override
    public HeapBytesStore write(long offsetInRDO, ByteBuffer bytes, int offset, int length) {
        checkOffset(offset, length);
        if (bytes.isDirect()) {
            MEMORY.copyMemory(((DirectBuffer) bytes).address(), underlyingObject, this.dataOffset + offsetInRDO, length);
        } else {
            MEMORY.copyMemory(bytes.array(), offset, underlyingObject, this.dataOffset + offsetInRDO, length);
        }
        return this;
    }

    public Object getRetainedObject() {
        return retainedObject;
    }
}
