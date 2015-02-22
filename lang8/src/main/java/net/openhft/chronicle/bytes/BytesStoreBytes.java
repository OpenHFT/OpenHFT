package net.openhft.chronicle.bytes;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class BytesStoreBytes implements Bytes {
    private static final AtomicBoolean MEMORY_BARRIER = new AtomicBoolean();
    protected final BytesStore bytesStore;
    private long offset, position, limit, capacity;

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
        limit = capacity;
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
        if (position >= limit())
            throw new BufferOverflowException();
        this.position = position;
        return this;
    }

    @Override
    public Bytes limit(long limit) {
        if (limit < start()) throw new BufferUnderflowException();
        if (limit >= capacity())
            throw new BufferOverflowException();
        this.limit = limit;
        return this;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public UnderflowMode underflowMode() {
        return UnderflowMode.BOUNDED;
    }

    @Override
    public Bytes writeUnsignedByte(int i) {
        if (position >= limit)
            bufferOverflowOnWrite();
        bytesStore.writeByte(this.offset + position++, (byte) i);
        return this;
    }

    @Override
    public long refCount() {
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
    public Bytes writeByte(long offset, byte i) {
        bytesStore.writeByte(this.offset + offset, i);
        return this;
    }

    @Override
    public byte readByte(long offset) {
        return bytesStore.readByte(this.offset + offset);
    }

    @Override
    public Bytes writeShort(long offset, short i) {
        bytesStore.writeShort(this.offset + offset, i);
        return this;
    }

    @Override
    public short readShort(long offset) {
        return bytesStore.readShort(this.offset + offset);
    }

    @Override
    public Bytes writeInt(long offset, int i) {
        bytesStore.writeInt(this.offset + offset, i);
        return this;
    }

    @Override
    public Bytes writeOrderedInt(long offset, int i) {
        bytesStore.writeOrderedInt(this.offset + offset, i);
        return this;
    }

    @Override
    public int readInt(long offset) {
        return bytesStore.readInt(this.offset + offset);
    }

    @Override
    public Bytes writeLong(long offset, long i) {
        bytesStore.writeLong(this.offset + offset, i);
        return this;
    }

    @Override
    public long readLong(long offset) {
        return bytesStore.readLong(this.offset + offset);
    }

    @Override
    public float readFloat(long offset) {
        return bytesStore.readFloat(this.offset + offset);
    }

    @Override
    public double readDouble(long offset) {
        return bytesStore.readDouble(this.offset + offset);
    }

    @Override
    public Bytes writeFloat(long offset, float d) {
        bytesStore.writeFloat(this.offset + offset, d);
        return this;
    }

    @Override
    public Bytes writeDouble(long offset, double d) {
        bytesStore.writeDouble(this.offset + offset, d);
        return this;
    }

    @Override
    public byte readByte() {
        return bytesStore.readByte(readOffsetPositionMoved(1));
    }

    @Override
    public short readShort() {
        return bytesStore.readShort(readOffsetPositionMoved(2));
    }

    @Override
    public int readInt() {
        return bytesStore.readInt(readOffsetPositionMoved(4));
    }

    @Override
    public long readLong() {
        return bytesStore.readLong(readOffsetPositionMoved(8));
    }

    @Override
    public float readFloat() {
        return bytesStore.readFloat(readOffsetPositionMoved(4));
    }

    @Override
    public double readDouble() {
        return bytesStore.readDouble(readOffsetPositionMoved(8));
    }

    @Override
    public int peakVolatileInt() {
        return bytesStore.readVolatileInt(offset + position);
    }


    private long writeOffsetPositionMoved(long adding) {
        if (position + adding > limit)
            return bufferOverflowOnWrite();
        position += adding;
        return offset + position - adding;
    }

    private long readOffsetPositionMoved(long adding) {
        if (position + adding > limit)
            return bufferUnderflowOnRead();
        position += adding;
        return offset + position - adding;
    }

    @Override
    public Bytes writeByte(byte i8) {
        bytesStore.writeByte(writeOffsetPositionMoved(1), i8);
        return this;
    }

    private long bufferOverflowOnWrite() {
        throw new BufferOverflowException();
    }

    private long bufferUnderflowOnRead() {
        throw new BufferUnderflowException();
    }

    @Override
    public Bytes writeShort(short i16) {
        bytesStore.writeShort(writeOffsetPositionMoved(2), i16);
        return this;
    }

    @Override
    public Bytes writeInt(int i) {
        bytesStore.writeInt(writeOffsetPositionMoved(4), i);
        return this;
    }

    @Override
    public Bytes writeLong(long i64) {
        bytesStore.writeLong(writeOffsetPositionMoved(8), i64);
        return this;
    }

    @Override
    public Bytes writeFloat(float f) {
        bytesStore.writeFloat(writeOffsetPositionMoved(4), f);
        return this;
    }

    @Override
    public Bytes writeDouble(double d) {
        bytesStore.writeDouble(writeOffsetPositionMoved(8), d);
        return this;
    }

    @Override
    public Bytes write(Bytes bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes skip(long bytesToSkip) {
        readOffsetPositionMoved(bytesToSkip);
        return this;
    }

    @Override
    public Bytes flip() {
        limit = position;
        position = start();
        return this;
    }

    @Override
    public Bytes append(float f) {
        BytesUtil.append(this, f);
        return this;
    }

    @Override
    public Bytes append(double d) {
        BytesUtil.append(this, d);
        return this;
    }

    public String toString() {
        return BytesUtil.toString(this);
    }

    @Override
    public Bytes write(byte[] bytes, int offset, int length) {
        bytesStore.write(writeOffsetPositionMoved(length), bytes, offset, length);
        return this;
    }

    @Override
    public Bytes write(long offsetInRDO, byte[] bytes, int offset, int length) {
        bytesStore.write(this.offset + offsetInRDO, bytes, offset, length);
        return this;
    }

    @Override
    public Bytes write(long offsetInRDO, ByteBuffer bytes, int offset, int length) {
        bytesStore.write(offsetInRDO, bytes, offset, length);
        return this;
    }

    @Override
    public Bytes writeOrderedLong(long offset, long i) {
        bytesStore.writeOrderedLong(this.offset + offset, i);
        return this;
    }

    @Override
    public void read(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = readByte();
    }

    @Override
    public void read(ByteBuffer buffer) {
        while (remaining() > 0 && buffer.remaining() > 0)
            buffer.put(readByte());
    }

    @Override
    public int readVolatileInt() {
        return bytesStore.readVolatileInt(readOffsetPositionMoved(4));
    }

    @Override
    public long readVolatileLong() {
        return bytesStore.readVolatileLong(readOffsetPositionMoved(8));
    }

    @Override
    public Bytes write(ByteBuffer buffer) {
        bytesStore.write(position, buffer, buffer.position(), buffer.limit());
        position += buffer.remaining();
        return this;
    }

    @Override
    public Bytes writeOrderedInt(int i) {
        bytesStore.writeOrderedInt(writeOffsetPositionMoved(4), i);
        return this;
    }

    @Override
    public Bytes writeOrderedLong(long i) {
        bytesStore.writeOrderedLong(writeOffsetPositionMoved(8), i);
        return this;
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) {
        return bytesStore.compareAndSwapInt(this.offset + offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) {
        return bytesStore.compareAndSwapLong(this.offset + offset, expected, value);
    }
}
