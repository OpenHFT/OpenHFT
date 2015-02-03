package net.openhft.lang.io;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Rob Austin
 */
public interface NativeBytesI extends Bytes {



    @Override
    NativeBytesI slice();

    @Override
    NativeBytesI slice(long offset, long length);

    @Override
    CharSequence subSequence(int start, int end);

    @Override
    NativeBytesI bytes();

    @Override
    NativeBytesI bytes(long offset, long length);

    @Override
    long address();

    @Override
    Bytes zeroOut();

    @Override
    Bytes zeroOut(long start, long end);

    @Override
    Bytes zeroOut(long start, long end, boolean ifNotZero);

    @Override
    int read(@NotNull byte[] bytes, int off, int len);

    @Override
    byte readByte();

    @Override
    byte readByte(long offset);

    @Override
    void readFully(@NotNull byte[] b, int off, int len);

    @Override
    void readFully(long offset, byte[] bytes, int off, int len);

    @Override
    void readFully(@NotNull char[] data, int off, int len);

    @Override
    short readShort();

    @Override
    short readShort(long offset);

    @Override
    char readChar();

    @Override
    char readChar(long offset);

    @Override
    int readInt();

    @Override
    int readInt(long offset);

    @Override
    int readVolatileInt();

    @Override
    int readVolatileInt(long offset);

    @Override
    long readLong();

    @Override
    long readLong(long offset);

    @Override
    long readVolatileLong();

    @Override
    long readVolatileLong(long offset);

    @Override
    float readFloat();

    @Override
    float readFloat(long offset);

    @Override
    double readDouble();

    @Override
    double readDouble(long offset);

    @Override
    void write(int b);

    @Override
    void writeByte(long offset, int b);

    @Override
    void write(long offset, @NotNull byte[] bytes);

    @Override
    void write(byte[] bytes, int off, int len);

    @Override
    void write(long offset, byte[] bytes, int off, int len);

    @Override
    void writeShort(int v);

    @Override
    void writeShort(long offset, int v);

    @Override
    void writeChar(int v);

    @Override
    void writeChar(long offset, int v);

    @Override
    void writeInt(int v);

    @Override
    void writeInt(long offset, int v);

    @Override
    void writeOrderedInt(int v);

    @Override
    void writeOrderedInt(long offset, int v);

    @Override
    boolean compareAndSwapInt(long offset, int expected, int x);

    @Override
    void writeLong(long v);

    @Override
    void writeLong(long offset, long v);

    @Override
    void writeOrderedLong(long v);

    @Override
    void writeOrderedLong(long offset, long v);

    @Override
    boolean compareAndSwapLong(long offset, long expected, long x);

    @Override
    void writeFloat(float v);

    @Override
    void writeFloat(long offset, float v);

    @Override
    void writeDouble(double v);

    @Override
    void writeDouble(long offset, double v);

    @Override
    void readObject(Object object, int start, int end);

    @Override
    void writeObject(Object object, int start, int end);

    @Override
    boolean compare(long offset, RandomDataInput input, long inputOffset, long len);

    @Override
    long position();

    @Override
    NativeBytesI position(long position);

    NativeBytesI lazyPosition(long position);

    @Override
    void write(RandomDataInput bytes, long position, long length);

    @Override
    long capacity();

    @Override
    long remaining();

    @Override
    long limit();

    @Override
    NativeBytesI limit(long limit);

    @NotNull
    @Override
    ByteOrder byteOrder();

    @Override
    void checkEndOfBuffer() throws IndexOutOfBoundsException;

    long startAddr();

    @Override
    Bytes load();

    void alignPositionAddr(int powerOf2);

    void positionAddr(long positionAddr);

    long positionAddr();

    @Override
    ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse);
}
