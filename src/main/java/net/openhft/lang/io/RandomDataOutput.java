/*
 * Copyright 2013 Peter Lawrey
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

import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.RandomAccess;

/**
 * @author peter.lawrey
 */
public interface RandomDataOutput extends ObjectOutput, RandomAccess, BytesCommon {
    @Override
    void write(int b);

    @Override
    public void writeByte(int v);

    public void writeUnsignedByte(int v);

    public void writeByte(long offset, int b);

    public void writeUnsignedByte(long offset, int v);

    @Override
    void write(byte[] b);

    void write(long offset, byte[] b);

    @Override
    void write(byte[] b, int off, int len);

    @Override
    void writeBoolean(boolean v);

    void writeBoolean(long offset, boolean v);

    @Override
    void writeShort(int v);

    void writeShort(long offset, int v);

    void writeUnsignedShort(int v);

    void writeUnsignedShort(long offset, int v);

    void writeCompactShort(int v);

    void writeCompactUnsignedShort(int v);

    @Override
    void writeChar(int v);

    void writeChar(long offset, int v);

    /**
     * @param v 24-bit integer to write
     */
    void writeInt24(int v);

    void writeInt24(long offset, int v);

    @Override
    void writeInt(int v);

    void writeInt(long offset, int v);

    void writeUnsignedInt(long v);

    void writeUnsignedInt(long offset, long v);

    void writeCompactInt(int v);

    void writeCompactUnsignedInt(long v);

    void writeOrderedInt(int v);

    void writeOrderedInt(long offset, int v);

    boolean compareAndSetInt(long offset, int expected, int x);

    /**
     * @param v 48-bit long to write
     */
    void writeInt48(long v);

    void writeInt48(long offset, long v);

    @Override
    void writeLong(long v);

    void writeLong(long offset, long v);

    void writeCompactLong(long v);

    void writeOrderedLong(long v);

    void writeOrderedLong(long offset, long v);

    boolean compareAndSetLong(long offset, long expected, long x);

    /**
     * Stop bit encoding numbers.
     * This will write the same number of bytes whether you used a byte, short or int.
     */
    void writeStopBit(long n);

    @Override
    void writeFloat(float v);

    void writeFloat(long offset, float v);

    @Override
    void writeDouble(double v);

    void writeDouble(long offset, double v);

    void writeCompactDouble(double v);

    @Override
    void writeBytes(String s);

    void writeBytes(CharSequence s);

    @Override
    void writeChars(String s);

    @Override
    void writeUTF(String s);

    void writeUTF(CharSequence s);

    void write(ByteBuffer bb);

    <E> void writeEnum(E e);

    <E> void writeList(Collection<E> list);

    <K, V> void writeMap(Map<K, V> map);

    // ObjectOutput

    @Override
    void writeObject(Object obj);

    @Override
    void flush();

    @Override
    void close();
}
