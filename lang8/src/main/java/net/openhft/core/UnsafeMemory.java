package net.openhft.core;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public enum UnsafeMemory implements Memory {
    INSTANCE;

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
    public void writeByte(Object object, long offset, byte b) {
        UNSAFE.putByte(object, offset, b);
    }

}
