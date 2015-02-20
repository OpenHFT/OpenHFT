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
    public Bytes writeUnsignedByte(int i) {
        if (position >= writeLimit)
            bufferOverflowOnWrite();
        bytesStore.writeByte(position++, (byte) i);
        return this;
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
    public Bytes writeByte(long offset, byte i) {
        bytesStore.writeByte(this.offset + offset, i);
        return this;
    }

    @Override
    public Bytes writeOrderedInt(long offset, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readUTFΔ(StringBuilder sb) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readStopBit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte readByte() {
        throw new UnsupportedOperationException();
    }

    @Override
    public short readShort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readUnsignedShort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readInt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readUnsignedInt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float readFloat() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double readDouble() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int peakVolatileInt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTFΔ() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeUnsignedInt(long i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeInt(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeByte(byte i8) {
        bytesStore.writeByte(writeMove(position, 1), i8);
        return this;
    }

    private long writeMove(long position, int adding) {
        if (position + adding > limit)
            return bufferOverflowOnWrite();
        position += adding;
        return position - adding;
    }

    private long bufferOverflowOnWrite() {
        throw new BufferOverflowException();
    }

    @Override
    public Bytes writeShort(short i16) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeUnsignedShort(int u16) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeLong(long i64) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeFloat(float f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes writeDouble(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes write(Bytes bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes skip(long bytesToSkip) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes flip() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toDebugString(long capacity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes append(char ch) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Bytes append(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes append(float f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bytes append(double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void parseUTF(CharSequence sb, StopCharTester stopCharTester) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long parseLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double parseDouble() {
        throw new UnsupportedOperationException();
    }
}
