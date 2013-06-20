/*
 * Copyright ${YEAR} Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import sun.misc.Unsafe;

import java.io.EOFException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public class NativeBytes extends AbstractBytes {
    protected long startAddr;
    protected long positionAddr;
    protected long limitAddr;

    public NativeBytes(long startAddr, long positionAddr, long limitAddr) {
        this.startAddr = startAddr;
        this.positionAddr = positionAddr;
        this.limitAddr = limitAddr;
    }

    public NativeBytes(BytesMarshallerFactory bytesMarshallerFactory, long startAddr, long positionAddr, long limitAddr) {
        super(bytesMarshallerFactory);
        this.startAddr = startAddr;
        this.positionAddr = positionAddr;
        this.limitAddr = limitAddr;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (len < 0 || off < 0 || off + len > b.length)
            throw new IllegalArgumentException();
        int left = remaining();
        if (left <= 0) return -1;
        int len2 = Math.min(len, left);
        UNSAFE.copyMemory(null, positionAddr, b, BYTES_OFFSET + off, len2);
        positionAddr += len2;
        return len2;
    }

    @Override
    public byte readByte() {
        return UNSAFE.getByte(positionAddr++);
    }

    @Override
    public byte readByte(int offset) {
        return UNSAFE.getByte(startAddr + offset);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        if (len < 0 || off < 0 || off + len > b.length)
            throw new IllegalArgumentException();
        int left = remaining();
        if (left <= len)
            throw new IllegalStateException(new EOFException());
        UNSAFE.copyMemory(null, positionAddr, b, BYTES_OFFSET + off, len);
        positionAddr += len;
    }

    @Override
    public short readShort() {
        short s = UNSAFE.getShort(positionAddr);
        positionAddr += 2;
        return s;
    }

    @Override
    public short readShort(int offset) {
        return UNSAFE.getShort(startAddr + offset);
    }

    @Override
    public char readChar() {
        char ch = UNSAFE.getChar(positionAddr);
        positionAddr += 2;
        return ch;
    }

    @Override
    public char readChar(int offset) {
        return UNSAFE.getChar(startAddr + offset);
    }

    @Override
    public int readInt() {
        int i = UNSAFE.getInt(positionAddr);
        positionAddr += 4;
        return i;
    }

    @Override
    public int readInt(int offset) {
        return UNSAFE.getInt(startAddr + offset);
    }

    @Override
    public long readLong() {
        long l = UNSAFE.getLong(positionAddr);
        positionAddr += 8;
        return l;
    }

    @Override
    public long readLong(int offset) {
        return UNSAFE.getLong(startAddr + offset);
    }

    @Override
    public float readFloat() {
        float f = UNSAFE.getFloat(positionAddr);
        positionAddr += 4;
        return f;
    }

    @Override
    public float readFloat(int offset) {
        return UNSAFE.getFloat(startAddr + offset);
    }

    @Override
    public double readDouble() {
        double d = UNSAFE.getDouble(positionAddr);
        positionAddr += 8;
        return d;
    }

    @Override
    public double readDouble(int offset) {
        return UNSAFE.getDouble(startAddr + offset);
    }

    @Override
    public void write(int b) {
        UNSAFE.putByte(positionAddr++, (byte) b);
    }

    @Override
    public void write(int offset, int b) {
        UNSAFE.putByte(startAddr + offset, (byte) b);
    }

    @Override
    public void write(int offset, byte[] b) {
        UNSAFE.copyMemory(b, BYTES_OFFSET, null, positionAddr, b.length);
        positionAddr += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        UNSAFE.copyMemory(b, BYTES_OFFSET + off, null, positionAddr, len);
        positionAddr += len;
    }

    @Override
    public void writeShort(int v) {
        UNSAFE.putShort(positionAddr, (short) v);
        positionAddr += 2;
    }

    @Override
    public void writeShort(int offset, int v) {
        UNSAFE.putShort(startAddr + offset, (short) v);
    }

    @Override
    public void writeChar(int v) {
        UNSAFE.putChar(positionAddr, (char) v);
        positionAddr += 2;
    }

    @Override
    public void writeChar(int offset, int v) {
        UNSAFE.putChar(startAddr + offset, (char) v);
    }

    @Override
    public void writeInt(int v) {
        UNSAFE.putInt(positionAddr, v);
        positionAddr += 4;
    }

    @Override
    public void writeInt(int offset, int v) {
        UNSAFE.putInt(startAddr + offset, v);
    }

    @Override
    public void writeLong(long v) {
        UNSAFE.putLong(positionAddr, v);
        positionAddr += 8;
    }

    @Override
    public void writeLong(int offset, long v) {
        UNSAFE.putLong(startAddr + offset, v);
    }

    @Override
    public void writeFloat(float v) {
        UNSAFE.putFloat(positionAddr, v);
        positionAddr += 4;
    }

    @Override
    public void writeFloat(int offset, float v) {
        UNSAFE.putFloat(startAddr + offset, v);
    }

    @Override
    public void writeDouble(double v) {
        UNSAFE.putDouble(positionAddr, v);
        positionAddr += 8;
    }

    @Override
    public void writeDouble(int offset, double v) {
        UNSAFE.putDouble(startAddr + offset, v);
    }

    @Override
    public int position() {
        return (int) (positionAddr - startAddr);
    }

    @Override
    public void position(int position) {
        this.positionAddr = startAddr + position;
    }

    @Override
    public int capacity() {
        return (int) (limitAddr - startAddr);
    }

    @Override
    public int remaining() {
        return (int) (limitAddr - positionAddr);
    }

    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.nativeOrder();
    }

    @Override
    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
    }

    /**
     * *** Access the Unsafe class *****
     */
    @SuppressWarnings("ALL")
    protected static final Unsafe UNSAFE;
    private static final int BYTES_OFFSET;

    static {
        try {
            @SuppressWarnings("ALL")
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            BYTES_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
