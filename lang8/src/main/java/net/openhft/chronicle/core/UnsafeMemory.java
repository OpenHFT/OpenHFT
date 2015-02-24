package net.openhft.chronicle.core;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public enum UnsafeMemory implements Memory {
    MEMORY;

    static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void storeFence() {
        UNSAFE.storeFence();
    }

    @Override
    public void loadFence() {
        UNSAFE.loadFence();
    }

    @Override
    public byte readByte(Object object, long offset) {
        return UNSAFE.getByte(object, offset);
    }

    @Override
    public byte readByte(long address) {
        return UNSAFE.getByte(address);
    }

    @Override
    public void writeByte(long address, byte b) {
        UNSAFE.putByte(address, b);
    }

    @Override
    public void writeByte(Object object, long offset, byte b) {
        UNSAFE.putByte(object, offset, b);
    }

    @Override
    public void writeShort(long address, short i16) {
        UNSAFE.putShort(address, i16);
    }

    @Override
    public void writeShort(Object object, long offset, short i16) {
        UNSAFE.putShort(object, offset, i16);
    }

    @Override
    public short readShort(long address) {
        return UNSAFE.getShort(address);
    }

    @Override
    public short readShort(Object object, long offset) {
        return UNSAFE.getShort(object, offset);
    }

    @Override
    public void writeInt(long address, int i32) {
        UNSAFE.putInt(address, i32);
    }

    @Override
    public void writeInt(Object object, long offset, int i32) {
        UNSAFE.putInt(object, offset, i32);
    }

    @Override
    public void writeOrderedInt(long address, int i32) {
        UNSAFE.putOrderedInt(null, address, i32);
    }

    @Override
    public void writeOrderedInt(Object object, long offset, int i32) {
        UNSAFE.putOrderedInt(object, offset, i32);
    }

    @Override
    public int readInt(long address) {
        return UNSAFE.getInt(address);
    }

    @Override
    public int readInt(Object object, long offset) {
        return UNSAFE.getInt(object, offset);
    }

    @Override
    public void writeLong(long address, long i64) {
        UNSAFE.putLong(address, i64);
    }

    @Override
    public void writeLong(Object object, long offset, long i64) {
        UNSAFE.putLong(object, offset, i64);
    }

    @Override
    public long readLong(long address) {
        return UNSAFE.getLong(address);
    }

    @Override
    public long readLong(Object object, long offset) {
        return UNSAFE.getLong(object, offset);
    }

    @Override
    public void writeFloat(long address, float f) {
        UNSAFE.putFloat(address, f);
    }

    @Override
    public void writeFloat(Object object, long offset, float f) {
        UNSAFE.putFloat(object, offset, f);
    }

    @Override
    public float readFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    @Override
    public float readFloat(Object object, long offset) {
        return UNSAFE.getFloat(object, offset);
    }

    @Override
    public void writeDouble(long address, double d) {
        UNSAFE.putDouble(address, d);
    }

    @Override
    public void writeDouble(Object object, long offset, double d) {
        UNSAFE.putDouble(object, offset, d);
    }

    @Override
    public double readDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    @Override
    public double readDouble(Object object, long offset) {
        return UNSAFE.getDouble(object, offset);
    }

    @Override
    public void copyMemory(byte[] bytes, int offset, long address, int length) {
        copyMemory(bytes, offset, null, address, length);
    }

    @Override
    public void copyMemory(long fromAddress, long address, int length) {
        UNSAFE.copyMemory(fromAddress, address, length);
    }

    @Override
    public void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length) {
        UNSAFE.copyMemory(bytes, Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, obj2, offset2, length);
    }

    @Override
    public void copyMemory(long fromAddress, Object obj2, long offset2, int length) {
        UNSAFE.copyMemory(null, fromAddress, obj2, offset2, length);
    }

    @Override
    public void writeOrderedLong(long address, long i) {
        UNSAFE.putOrderedLong(null, address, i);
    }

    @Override
    public void writeOrderedLong(Object object, long offset, long i) {
        UNSAFE.putOrderedLong(object, offset, i);
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) {
        return UNSAFE.compareAndSwapInt(null, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) {
        return UNSAFE.compareAndSwapLong(null, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapInt(Object underlyingObject, long offset, int expected, int value) {
        return UNSAFE.compareAndSwapInt(underlyingObject, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(Object underlyingObject, long offset, long expected, long value) {
        return UNSAFE.compareAndSwapLong(underlyingObject, offset, expected, value);
    }

    @Override
    public int pageSize() {
        return UNSAFE.pageSize();
    }

    @Override
    public void setMemory(long address, long size, byte b) {
        UNSAFE.setMemory(address, size, b);
    }

    @Override
    public void freeMemory(long address) {
        if (address != 0)
            UNSAFE.freeMemory(address);
    }

    @Override
    public long allocate(long capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("Invalid capacity: " + capacity);
        long address = UNSAFE.allocateMemory(capacity);
        if (address == 0)
            throw new OutOfMemoryError("Not enough free native memory, capacity attempted: " + capacity / 1024 + " KiB");
        return address;
    }


}
