package net.openhft.lang.io;


import net.openhft.lang.model.constraints.NotNull;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Rob Austin
 */
public class ChronicleUnsafe {

    private final MappedFile mappedFile;
    private MappedMemory mappedMemory = null;
    public static final Unsafe UNSAFE;

    static {
        try {
            @SuppressWarnings("ALL")
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private long chunkSize;
    private long offset;
    private final long mask;
    private long last = -1;

    /**
     * @param mappedFile a until that able to map block of memory to a file
     * @throws IllegalStateException if the block size is not a power of 2
     */
    public ChronicleUnsafe(@NotNull MappedFile mappedFile) {
        long blockSize = mappedFile.blockSize();

        if (((blockSize & -blockSize) != blockSize))
            throw new IllegalStateException("the block size has to be a power of 2");

        this.mappedFile = mappedFile;
        this.chunkSize = mappedFile.blockSize();

        long shift = (int) (Math.log(blockSize) / Math.log(2));

        mask = ~((1L << shift) - 1L);
    }

    public long toAddress(long address) {
        return (mask & address ^ this.last) == 0 ? address + offset : toAddress0(address);
    }

    public long toAddress0(long address) {
        int index = (int) ((address / chunkSize));
        long remainder = address - (((long) index) * chunkSize);

        // index == 0 is the header, so we wont reference count the header
        if (mappedMemory != null && mappedMemory.index() != 0)
            mappedFile.release(mappedMemory);

        try {
            this.mappedMemory = mappedFile.acquire(index);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long result = mappedMemory.bytes().address() + remainder;
        this.offset = result - address;
        this.last = mask & address;
        return result;
    }


    public long toRemainingInChunk(long address) {

        int chunk = (int) ((address / chunkSize));
        long remainder = address - (((long) chunk) * chunkSize);

        return mappedMemory.bytes().capacity() - remainder;

    }

    public int arrayBaseOffset(Class<?> aClass) {
        return UNSAFE.arrayBaseOffset(aClass);
    }

    public int pageSize() {
        throw new UnsupportedOperationException("todo (pageSize)");
    }

    public long allocateMemory(int aVoid) {
        throw new UnsupportedOperationException("todo (allocateMemory)");
    }

    public long getLong(byte[] bytes, long address) {
        return UNSAFE.getLong(bytes, toAddress(address));
    }

    public long getLong(Object object, long address) {
        return UNSAFE.getLong(object, toAddress(address));
    }

    public void setMemory(long startAddress, long len, byte defaultValue) {
        long remaining = len;
        while (remaining > 0) {
            long address = toAddress(startAddress);
            long remainingInChunk = toRemainingInChunk(startAddress);
            if (remainingInChunk > remaining)
                remainingInChunk = remaining;
            UNSAFE.setMemory(address, remainingInChunk, defaultValue);
            startAddress += remainingInChunk;
            remaining -= remainingInChunk;
        }
    }

    public byte getByte(long address) {
        return UNSAFE.getByte(toAddress(address));
    }

    public void putByte(long address, byte value) {
        UNSAFE.putByte(toAddress(address), value);
    }

    public void putLong(long address, long value) {
        UNSAFE.putLong(toAddress(address), value);
    }

    public long getLong(long address) {
        return UNSAFE.getLong(toAddress(address));
    }

    public void copyMemory(Object o, long positionAddr, Object bytes, long i, long len2) {
        throw new UnsupportedOperationException("todo (copyMemory)");
    }

    public short getShort(long address) {
        return UNSAFE.getShort(toAddress(address));
    }

    public char getChar(long address) {
        return UNSAFE.getChar(toAddress(address));
    }

    public int getInt(long address) {
        return UNSAFE.getInt(toAddress(address));
    }

    public int getIntVolatile(Object o, long address) {
        return UNSAFE.getIntVolatile(o, toAddress(address));
    }

    public long getLongVolatile(Object o, long address) {
        return UNSAFE.getLongVolatile(o, toAddress(address));
    }

    public float getFloat(long address) {
        return UNSAFE.getFloat(toAddress(address));
    }

    public double getDouble(long address) {
        return UNSAFE.getDouble(toAddress(address));
    }

    public void putShort(long address, short v) {
        UNSAFE.putShort(toAddress(address), v);
    }

    public void putChar(long address, char v) {
        UNSAFE.putChar(toAddress(address), v);
    }

    public void putInt(long address, int v) {
        UNSAFE.putInt(toAddress(address), v);
    }

    public void putOrderedInt(Object o, long address, int v) {
        UNSAFE.putOrderedInt(o, toAddress(address), v);
    }

    public boolean compareAndSwapInt(Object o, long address, int expected, int v) {
        return UNSAFE.compareAndSwapInt(o, toAddress(address), expected, v);
    }

    public void putOrderedLong(Object o, long address, long v) {
        UNSAFE.putOrderedLong(o, toAddress(address), v);
    }

    public boolean compareAndSwapLong(Object o, long address, long expected, long v) {
        return UNSAFE.compareAndSwapLong(o, toAddress(address), expected, v);
    }

    public void putFloat(long address, float v) {
        UNSAFE.putFloat(toAddress(address), v);
    }

    public void putDouble(long address, double v) {
        UNSAFE.putDouble(toAddress(address), v);
    }

    public void putLong(Object o, long address, long aLong) {
        UNSAFE.putLong(o, toAddress(address), aLong);
    }

    public void putByte(Object o, long address, byte aByte) {
        UNSAFE.putByte(o, toAddress(address), aByte);
    }

    public byte getByte(Object o, long address) {
        return UNSAFE.getByte(o, toAddress(address));
    }

    public void copyMemory(long l, long positionAddr, long length) {
        throw new UnsupportedOperationException("todo (copyMemory)");
    }
}
