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
    /**
     * Writes to the output stream the eight low-order bits of the argument <code>b</code>. The 24 high-order  bits of
     * <code>b</code> are ignored.
     *
     * @param b the byte to be written.
     */
    @Override
    void write(int b);

    /**
     * Writes to the output stream the eight low- order bits of the argument <code>v</code>. The 24 high-order bits of
     * <code>v</code> are ignored. (This means  that <code>writeByte</code> does exactly the same thing as
     * <code>write</code> for an integer argument.) The byte written by this method may be read by the
     * <code>readByte</code> method of interface <code>DataInput</code>, which will then return a <code>byte</code>
     * equal to <code>(byte)v</code>.
     *
     * @param v the byte value to be written.
     */
    @Override
    void writeByte(int v);

    /**
     * Writes to the output stream the eight low- order bits of the argument <code>v</code>. The 24 high-order bits of
     * <code>v</code> are ignored. (This means  that <code>writeByte</code> does exactly the same thing as
     * <code>write</code> for an integer argument.) The byte written by this method may be read by the
     * <code>readUnsignedByte</code> method of interface <code>DataInput</code>, which will then return a
     * <code>byte</code> equal to <code>(byte)v</code>.
     *
     * @param v the byte value to be written.
     */
    void writeUnsignedByte(int v);

    /**
     * Writes to the output stream the eight low-order bits of the argument <code>b</code>. The 24 high-order  bits of
     * <code>b</code> are ignored.
     *
     * @param offset to write byte
     * @param b      the byte to be written.
     */
    void writeByte(long offset, int b);

    /**
     * Writes to the output stream the eight low- order bits of the argument <code>v</code>. The 24 high-order bits of
     * <code>v</code> are ignored. (This means  that <code>writeByte</code> does exactly the same thing as
     * <code>write</code> for an integer argument.) The byte written by this method may be read by the
     * <code>readUnsignedByte</code> method of interface <code>DataInput</code>, which will then return a
     * <code>byte</code> equal to <code>v &amp; 0xFF</code>.
     *
     * @param offset to write byte
     * @param v      the unsigned byte value to be written.
     */
    void writeUnsignedByte(long offset, int v);

    /**
     * Writes to the output stream all the bytes in array <code>bytes</code>. If <code>bytes</code> is
     * <code>null</code>, a <code>NullPointerException</code> is thrown. If <code>bytes.length</code> is zero, then no
     * bytes are written. Otherwise, the byte <code>bytes[0]</code> is written first, then <code>bytes[1]</code>, and so
     * on; the last byte written is <code>bytes[bytes.length-1]</code>.
     *
     * @param bytes the data.
     */
    @Override
    void write(byte[] bytes);

    /**
     * Writes to the output stream all the bytes in array <code>bytes</code>. If <code>bytes</code> is
     * <code>null</code>, a <code>NullPointerException</code> is thrown. If <code>bytes.length</code> is zero, then no
     * bytes are written. Otherwise, the byte <code>bytes[0]</code> is written first, then <code>bytes[1]</code>, and so
     * on; the last byte written is <code>bytes[bytes.length-1]</code>.
     *
     * @param offset to be written
     * @param bytes  the data.
     */
    void write(long offset, byte[] bytes);

    /**
     * Writes <code>len</code> bytes from array <code>bytes</code>, in order,  to the output stream.  If
     * <code>bytes</code> is <code>null</code>, a <code>NullPointerException</code> is thrown.  If <code>off</code> is
     * negative, or <code>len</code> is negative, or <code>off+len</code> is greater than the length of the array
     * <code>bytes</code>, then an <code>IndexOutOfBoundsException</code> is thrown.  If <code>len</code> is zero, then
     * no bytes are written. Otherwise, the byte <code>bytes[off]</code> is written first, then
     * <code>bytes[off+1]</code>, and so on; the last byte written is <code>bytes[off+len-1]</code>.
     *
     * @param bytes the data.
     * @param off   the start offset in the data.
     * @param len   the number of bytes to write.
     */
    @Override
    void write(byte[] bytes, int off, int len);

    /**
     * Writes a <code>boolean</code> value to this output stream. If the argument <code>v</code> is <code>true</code>,
     * the value <code>(byte)1</code> is written; if <code>v</code> is <code>false</code>, the  value
     * <code>(byte)0</code> is written. The byte written by this method may be read by the <code>readBoolean</code>
     * method of interface <code>DataInput</code>, which will then return a <code>boolean</code> equal to
     * <code>v</code>.
     *
     * @param v the boolean to be written.
     */
    @Override
    void writeBoolean(boolean v);

    /**
     * Writes a <code>boolean</code> value to this output stream. If the argument <code>v</code> is <code>true</code>,
     * the value <code>(byte)1</code> is written; if <code>v</code> is <code>false</code>, the  value
     * <code>(byte)0</code> is written. The byte written by this method may be read by the <code>readBoolean</code>
     * method of interface <code>DataInput</code>, which will then return a <code>boolean</code> equal to
     * <code>v</code>.
     *
     * @param offset to write boolean
     * @param v      the boolean to be written.
     */

    void writeBoolean(long offset, boolean v);

    /**
     * Writes two bytes to the output stream to represent the value of the argument. The byte values to be written, in
     * the  order shown for big endian machines and the opposite for little endian, are: <p>
     * <pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code> </pre> <p>
     * The bytes written by this method may be read by the <code>readShort</code> method of interface
     * <code>DataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    @Override
    void writeShort(int v);

    /**
     * Writes two bytes to the output stream to represent the value of the argument. The byte values to be written, in
     * the  order shown for big endian machines and the opposite for little endian, are: <p>
     * <pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code> </pre> <p>
     * The bytes written by this method may be read by the <code>readShort</code> method of interface
     * <code>DataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param offset to be written to
     * @param v      the <code>short</code> value to be written.
     */
    void writeShort(long offset, int v);

    /**
     * Writes two bytes to the output stream to represent the value of the argument. The byte values to be written, in
     * the  order shown for big endian machines and the opposite for little endian, are: <p>
     * <pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code> </pre> <p>
     * The bytes written by this method may be read by the <code>readUnsignedShort</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param v the unsigned <code>short</code> value to be written.
     */
    void writeUnsignedShort(int v);

    /**
     * Writes two bytes to the output stream to represent the value of the argument. The byte values to be written, in
     * the  order shown for big endian machines and the opposite for little endian, are: <p>
     * <pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code> </pre> <p>
     * The bytes written by this method may be read by the <code>readShort</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param offset to be written to
     * @param v      the unsigned <code>short</code> value to be written.
     */
    void writeUnsignedShort(long offset, int v);

    /**
     * Writes one or three bytes as follows; Short.MIN_VALUE => Byte.MIN_VALUE, Short.MAX_VALUE => Byte.MAX_VALUE,
     * Short.MIN_VALUE+2 to Short.MAX_VALUE-1 => writeByte(x), default => writeByte(Byte.MIN_VALUE+1; writeShort(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactShort</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    void writeCompactShort(int v);

    /**
     * Writes one or three bytes as follows; 0 to 254 => writeByte(x); otherwise writeByte(255); writeByteShort(x);
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactUnsignedShort</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>short</code> equal to <code>v &amp; 0xFFFF</code>.
     *
     * @param v the unsigned <code>short</code> value to be written.
     */
    void writeCompactUnsignedShort(int v);

    /**
     * Writes a <code>char</code> value, which is comprised of two bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readChar</code> method of interface
     * <code>DataInput</code> , which will then return a <code>char</code> equal to <code>(char)v</code>.
     *
     * @param v the <code>char</code> value to be written.
     */
    @Override
    void writeChar(int v);

    /**
     * Writes a <code>char</code> value, which is comprised of two bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readChar</code> method of interface
     * <code>DataInput</code> , which will then return a <code>char</code> equal to <code>(char)v</code>.
     *
     * @param offset to be written to
     * @param v      the <code>char</code> value to be written.
     */
    void writeChar(long offset, int v);

    /**
     * Writes an <code>int</code> value, which is comprised of three bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt24</code> method of interface
     * <code>RandomDataInput</code> , which will then return an <code>int</code> equal to <code>v</code>.
     *
     * @param v the <code>int</code> value to be written.
     */
    void writeInt24(int v);

    /**
     * Writes an <code>int</code> value, which is comprised of three bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt24</code> method of interface
     * <code>RandomDataInput</code> , which will then return an <code>int</code> equal to <code>v</code>.
     *
     * @param offset to be written to
     * @param v      the <code>int</code> value to be written.
     */
    void writeInt24(long offset, int v);

    /**
     * Writes an <code>int</code> value, which is comprised of four bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt</code> method of interface
     * <code>DataInput</code> , which will then return an <code>int</code> equal to <code>v</code>.
     *
     * @param v the <code>int</code> value to be written.
     */
    @Override
    void writeInt(int v);

    /**
     * Writes an <code>int</code> value, which is comprised of four bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt</code> method of interface
     * <code>DataInput</code> , which will then return an <code>int</code> equal to <code>v</code>.
     *
     * @param offset to be written to
     * @param v      the <code>int</code> value to be written.
     */
    void writeInt(long offset, int v);

    /**
     * Writes an <code>int</code> value, which is comprised of four bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readUnsignedInt</code> method of interface
     * <code>RandomDataInput</code> , which will then return an <code>long</code> equal to <code>v &amp;
     * 0xFFFFFFFF</code>.
     *
     * @param v the <code>int</code> value to be written.
     */
    void writeUnsignedInt(long v);

    /**
     * Writes an <code>int</code> value, which is comprised of four bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readUnsignedInt</code> method of interface
     * <code>RandomDataInput</code> , which will then return an <code>long</code> equal to <code>v &amp;
     * 0xFFFFFFFF</code>.
     *
     * @param offset to be written to
     * @param v      the <code>int</code> value to be written.
     */
    void writeUnsignedInt(long offset, long v);

    /**
     * Writes two or six bytes as follows; Integer.MIN_VALUE => Short.MIN_VALUE, Integer.MAX_VALUE => Short.MAX_VALUE,
     * Short.MIN_VALUE+2 to Short.MAX_VALUE-1 => writeShort(x), default => writeShort(Short.MIN_VALUE+1; writeInt(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactInt</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>int</code> equal to <code>v</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    void writeCompactInt(int v);

    /**
     * Writes two or six bytes as follows; 0 to (1 << 16) - 2 => writeInt(x), otherwise writeShort(-1); writeInt(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactUnsignedInt</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>int</code> equal to <code>v &amp;
     * 0xFFFFFFFF</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    void writeCompactUnsignedInt(long v);

    /**
     * Same as writeInt but include an ordered write barrier.  This means all writes will be visible on a read barrier
     * if this write is visible. This might not be visible to be same thread for some clock cycles so an immediate read
     * could see an old value
     * <p/>
     * This is much faster than a volatile write which stalls the pipeline.  The data is visible to other threads at the
     * same time.
     *
     * @param v value to write
     */
    void writeOrderedInt(int v);

    /**
     * Same as writeInt but include an ordered write barrier.  This means all writes will be visible on a read barrier
     * if this write is visible. This might not be visible to be same thread for some clock cycles so an immediate read
     * could see an old value
     * <p/>
     * This is much faster than <code>writeVolatileInt</code> as the volatile write stalls the pipeline.  The data is
     * visible to other threads at the same time.
     *
     * @param offset to write to
     * @param v      value to write
     */
    void writeOrderedInt(long offset, int v);

    /**
     * Perform a compare and set operation.  The value is set to <code>x</code> provided the <code>expected</code> value
     * is set already.  This operation is atomic.
     *
     * @param offset   to write to.
     * @param expected to expect
     * @param x        to set if expected was found
     * @return true if set, false if the value was not expected
     */
    boolean compareAndSetInt(long offset, int expected, int x);

    /**
     * Atomically adds the given value to the current value.
     *
     * @param offset of the int value to use.
     * @param delta  the value to add
     * @return the previous value
     */
    int getAndAdd(long offset, int delta);

    /**
     * Atomically adds the given value to the current value.
     *
     * @param offset of the int value to use.
     * @param delta  the value to add
     * @return the updated value
     */
    int addAndGetInt(long offset, int delta);

    /**
     * Writes a <code>long</code> value, which is comprised of eight bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 40))
     * (byte)(0xff &amp; (v &gt;&gt; 32))
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt;  8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt48</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>long</code> equal to <code>v &amp; ((1L &lt;&lt 48)
     * - 1)</code>.
     *
     * @param v the <code>long</code> value to be written.
     */
    void writeInt48(long v);

    /**
     * Writes a <code>long</code> value, which is comprised of eight bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 40))
     * (byte)(0xff &amp; (v &gt;&gt; 32))
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt;  8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readInt48</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>long</code> equal to <code>v &amp; ((1L &lt;&lt 48)
     * - 1)</code>.
     *
     * @param offset to be written to
     * @param v      the <code>long</code> value to be written.
     */
    void writeInt48(long offset, long v);

    /**
     * Writes a <code>long</code> value, which is comprised of eight bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 56))
     * (byte)(0xff &amp; (v &gt;&gt; 48))
     * (byte)(0xff &amp; (v &gt;&gt; 40))
     * (byte)(0xff &amp; (v &gt;&gt; 32))
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt;  8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readLong</code> method of interface
     * <code>DataInput</code> , which will then return a <code>long</code> equal to <code>v</code>.
     *
     * @param v the <code>long</code> value to be written.
     */
    @Override
    void writeLong(long v);

    /**
     * Writes a <code>long</code> value, which is comprised of eight bytes, to the output stream. The byte values to be
     * written, in the  order shown for big endian machines and the opposite for little endian, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 56))
     * (byte)(0xff &amp; (v &gt;&gt; 48))
     * (byte)(0xff &amp; (v &gt;&gt; 40))
     * (byte)(0xff &amp; (v &gt;&gt; 32))
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt;  8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read by the <code>readLong</code> method of interface
     * <code>DataInput</code> , which will then return a <code>long</code> equal to <code>v</code>.
     *
     * @param offset to be written to
     * @param v      the <code>long</code> value to be written.
     */
    void writeLong(long offset, long v);

    /**
     * Writes four or twelve bytes as follows Long.MIN_VALUE => Integer.MIN_VALUE, Long.MAX_VALUE => Integer.MAX_VALUE,
     * Integer.MIN_VALUE+2 to Integer.MAX_VALUE-1 => writeInt(x), default => writeInt(Integer.MIN_VALUE+1; writeLong(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactLong</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>long</code> equal to <code>v</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    void writeCompactLong(long v);


    /**
     * Same as writeLong but include an ordered write barrier.  This means all writes will be visible on a read barrier
     * if this write is visible. This might not be visible to be same thread for some clock cycles so an immediate read
     * could see an old value
     * <p/>
     * This is much faster than a volatile write which stalls the pipeline.  The data is visible to other threads at the
     * same time.
     *
     * @param v value to write
     */
    void writeOrderedLong(long v);

    /**
     * Same as writeLong but include an ordered write barrier.  This means all writes will be visible on a read barrier
     * if this write is visible. This might not be visible to be same thread for some clock cycles so an immediate read
     * could see an old value
     * <p/>
     * This is much faster than a volatile write which stalls the pipeline.  The data is visible to other threads at the
     * same time.
     *
     * @param offset to be written to
     * @param v      value to write
     */
    void writeOrderedLong(long offset, long v);

    /**
     * Perform a compare and set operation.  The value is set to <code>x</code> provided the <code>expected</code> value
     * is set already.  This operation is atomic.
     *
     * @param offset   to write to.
     * @param expected to expect
     * @param x        to set if expected was found
     * @return true if set, false if the value was not expected
     */
    boolean compareAndSetLong(long offset, long expected, long x);

    /**
     * Stop bit encoding numbers. This will write the same number of bytes whether you used a byte, short or int.
     * <p/>
     * <code>
     * <p/>
     * </code>
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

    void writeBytesΔ(CharSequence s);

    @Override
    void writeChars(String s);

    @Override
    void writeUTF(String s);

    void writeUTFΔ(CharSequence s);

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
