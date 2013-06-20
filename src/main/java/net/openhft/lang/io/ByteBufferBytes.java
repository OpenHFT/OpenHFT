package net.openhft.lang.io;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public class ByteBufferBytes extends AbstractBytes {
    protected ByteBuffer buffer;
    protected int start, position, limit;

    @Override
    public int read(byte[] b, int off, int len) {
        if (len < 0 || off < 0 || off + len > b.length)
            throw new IllegalArgumentException();
        int left = remaining();
        if (left <= 0) return -1;
        int len2 = Math.min(left, len);
        for (int i = 0; i < len2; i++)
            b[off + i] = readByte();
        return len2;
    }

    @Override
    public byte readByte() {
        if (position < limit)
            return buffer.get(position++);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public byte readByte(int offset) {
        if (start + offset < limit)
            return buffer.get(start + offset);
        throw new IndexOutOfBoundsException();
    }


    @Override
    public void readFully(byte[] b, int off, int len) {
        if (len < 0 || off < 0 || off + len > b.length)
            throw new IllegalArgumentException();
        int left = remaining();
        if (left <= len)
            throw new IllegalStateException(new EOFException());
        for (int i = 0; i < len; i++)
            b[off + i] = readByte();
    }

    @Override
    public short readShort() {
        if (position + 2 <= limit) {
            short s = buffer.getShort(position);
            position += 2;
            return s;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public short readShort(int offset) {
        if (start + offset + 2 <= limit)
            return buffer.getShort(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public char readChar() {
        if (position + 2 <= limit) {
            char ch = buffer.getChar(position);
            position += 2;
            return ch;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public char readChar(int offset) {
        if (start + offset + 2 <= limit)
            return buffer.getChar(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readInt() {
        if (position + 4 <= limit) {
            int i = buffer.getInt(position);
            position += 4;
            return i;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int readInt(int offset) {
        if (start + offset + 4 <= limit)
            return buffer.getInt(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readLong() {
        if (position + 8 <= limit) {
            long l = buffer.getLong(position);
            position += 8;
            return l;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public long readLong(int offset) {
        if (start + offset + 8 <= limit)
            return buffer.getLong(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public float readFloat() {
        if (position + 4 <= limit) {
            float f = buffer.getFloat(position);
            position += 4;
            return f;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public float readFloat(int offset) {
        if (start + offset + 4 <= limit)
            return buffer.getFloat(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double readDouble() {
        if (position + 8 <= limit) {
            double d = buffer.getDouble(position);
            position += 8;
            return d;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public double readDouble(int offset) {
        if (start + offset + 8 <= limit)
            return buffer.getDouble(start + offset);
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void write(int b) {
        if (position < limit)
            buffer.put(position++, (byte) b);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void write(int offset, int b) {
        if (start + offset < limit)
            buffer.put(start + offset, (byte) b);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeShort(int v) {
        if (position + 2 <= limit) {
            buffer.putShort(position, (short) v);
            position += 2;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeShort(int offset, int v) {
        if (start + offset + 2 <= limit)
            buffer.putShort(start + offset, (short) v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeChar(int v) {
        if (position + 2 <= limit) {
            buffer.putChar(position, (char) v);
            position += 2;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeChar(int offset, int v) {
        if (start + offset + 2 <= limit)
            buffer.putChar(start + offset, (char) v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeInt(int v) {
        if (position + 4 <= limit) {
            buffer.putInt(position, v);
            position += 4;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeInt(int offset, int v) {
        if (start + offset + 4 <= limit)
            buffer.putInt(start + offset, v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeLong(long v) {
        if (position + 8 <= limit) {
            buffer.putLong(position, v);
            position += 8;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeLong(int offset, long v) {
        if (start + offset + 8 <= limit)
            buffer.putLong(start + offset, v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeFloat(float v) {
        if (position + 4 <= limit) {
            buffer.putFloat(position, v);
            position += 4;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeFloat(int offset, float v) {
        if (start + offset + 4 <= limit)
            buffer.putFloat(start + offset, v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public void writeDouble(double v) {
        if (position + 8 <= limit) {
            buffer.putDouble(position, v);
            position += 8;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void writeDouble(int offset, double v) {
        if (start + offset + 8 <= limit)
            buffer.putDouble(start + offset, v);
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public int position() {
        return position - start;
    }

    @Override
    public void position(int position) {
        this.position = start + position;
    }

    @Override
    public int capacity() {
        return limit - start;
    }

    @Override
    public int remaining() {
        return limit - position;
    }

    @Override
    public ByteOrder byteOrder() {
        return buffer.order();
    }

    @Override
    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
        if (position < start || position > limit)
            throw new IndexOutOfBoundsException();
    }
}
