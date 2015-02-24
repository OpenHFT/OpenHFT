package net.openhft.chronicle.bytes;

import java.nio.ByteBuffer;

/**
 * Created by peter.lawrey on 24/02/15.
 */
public enum NoBytesStore implements BytesStore {
    NO_BYTES_STORE;

    @Override
    public void reserve() throws IllegalStateException {
    }

    @Override
    public void release() throws IllegalStateException {
    }

    @Override
    public long refCount() {
        return 1L;
    }


    @Override
    public RandomDataOutput writeByte(long offset, byte i8) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeShort(long offset, short i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeInt(long offset, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeOrderedInt(long offset, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeLong(long offset, long i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeOrderedLong(long offset, long i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeFloat(long offset, float d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput writeDouble(long offset, double d) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput write(long offsetInRDO, byte[] bytes, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RandomDataOutput write(long offsetInRDO, ByteBuffer bytes, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte readByte(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short readShort(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readInt(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long readLong(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float readFloat(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double readDouble(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long maximumLimit() {
        return 0;
    }

    @Override
    public boolean inStore(long offset) {
        return false;
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void storeFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyTo(BytesStore store) {
        // nothing to copy.
    }
}
