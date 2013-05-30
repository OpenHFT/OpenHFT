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

package net.openhft.lang.io.impl;

import net.openhft.lang.io.AbstractBytes;
import net.openhft.lang.io.BytesMarshallerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public class ByteBufferBytes extends AbstractBytes {
    protected ByteBuffer byteBuffer;

    public ByteBufferBytes(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteBufferBytes(BytesMarshallerFactory bytesMarshallerFactory, ByteBuffer byteBuffer) {
        super(bytesMarshallerFactory);
        this.byteBuffer = byteBuffer;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (remaining() <= 0) return -1;
        int pos = byteBuffer.position();
        byteBuffer.get(b, off, len);
        return byteBuffer.position() - pos;
    }

    @Override
    public byte readByte() {
        return 0;
    }

    @Override
    public byte readByte(int offset) {
        return 0;
    }

    @Override
    public short readShort() {
        return 0;
    }

    @Override
    public short readShort(int offset) {
        return 0;
    }

    @Override
    public char readChar() {
        return 0;
    }

    @Override
    public char readChar(int offset) {
        return 0;
    }

    @Override
    public int readInt() {
        return 0;
    }

    @Override
    public int readInt(int offset) {
        return 0;
    }

    @Override
    public long readLong() {
        return 0;
    }

    @Override
    public long readLong(int offset) {
        return 0;
    }

    @Override
    public float readFloat() {
        return 0;
    }

    @Override
    public float readFloat(int offset) {
        return 0;
    }

    @Override
    public double readDouble() {
        return 0;
    }

    @Override
    public double readDouble(int offset) {
        return 0;
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void write(int offset, int b) {
    }

    @Override
    public void writeShort(int v) {
    }

    @Override
    public void writeShort(int offset, int v) {
    }

    @Override
    public void writeChar(int v) {
    }

    @Override
    public void writeChar(int offset, int v) {
    }

    @Override
    public void writeInt(int v) {
    }

    @Override
    public void writeInt(int offset, int v) {
    }

    @Override
    public void writeLong(long v) {
    }

    @Override
    public void writeLong(int offset, long v) {
    }

    @Override
    public void writeFloat(float v) {
    }

    @Override
    public void writeFloat(int offset, float v) {
    }

    @Override
    public void writeDouble(double v) {
    }

    @Override
    public void writeDouble(int offset, double v) {
    }

    @Override
    public int position() {
        return 0;
    }

    @Override
    public void position(int position) {
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public int remaining() {
        return 0;
    }

    @Override
    public ByteOrder byteOrder() {
        return null;
    }

    @Override
    public void checkEndOfBuffer() throws IndexOutOfBoundsException {
    }
}
