package net.openhft.chronicle.core;

public interface Memory {
    void storeFence();

    void loadFence();

    void setMemory(long address, long size, byte b);

    void freeMemory(long address);

    long allocate(long capacity);

    void writeByte(long address, byte i8);

    void writeByte(Object object, long offset, byte b);

    byte readByte(Object object, long offset);

    byte readByte(long address);

    void writeShort(long address, short i16);

    void writeShort(Object object, long offset, short i16);

    short readShort(long address);

    short readShort(Object object, long offset);

    void writeInt(long address, int i32);

    void writeInt(Object object, long offset, int i32);

    void writeOrderedInt(long offset, int i32);

    void writeOrderedInt(Object object, long offset, int i32);

    int readInt(long address);

    int readInt(Object object, long offset);

    void writeLong(long address, long i64);

    void writeLong(Object object, long offset, long i64);

    long readLong(long address);

    long readLong(Object object, long offset);

    void writeFloat(long address, float f);

    void writeFloat(Object object, long offset, float f);

    float readFloat(long address);

    float readFloat(Object object, long offset);

    void writeDouble(long address, double d);

    void writeDouble(Object object, long offset, double d);

    double readDouble(long address);

    double readDouble(Object object, long offset);

    void copyMemory(byte[] bytes, int offset, long address, int length);

    void copyMemory(long fromAddress, long address, int length);

    void copyMemory(byte[] bytes, int offset, Object obj2, long offset2, int length);

    void copyMemory(long fromAddress, Object obj2, long offset2, int length);

    void writeOrderedLong(long address, long i);

    void writeOrderedLong(Object object, long offset, long i);

    boolean compareAndSwapInt(long address, int expected, int value);

    boolean compareAndSwapInt(Object underlyingObject, long offset, int expected, int value);

    boolean compareAndSwapLong(long address, long expected, long value);

    boolean compareAndSwapLong(Object underlyingObject, long offset, long expected, long value);

    int pageSize();
}
