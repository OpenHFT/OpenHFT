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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Copy from one Bytes to another, moves the position by length
     *
     * @param bytes to copy
     */
    void write(RandomDataInput bytes);

    /**
     * Copy from one Bytes to another, moves the position by length
     *
     * @param bytes    to copy
     * @param position to copy from
     * @param length   to copy
     */
    void write(RandomDataInput bytes, long position, long length);

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
     * Writes one or three bytes as follows; Short.MIN_VALUE =&gt; Byte.MIN_VALUE, Short.MAX_VALUE =&gt; Byte.MAX_VALUE,
     * Short.MIN_VALUE+2 to Short.MAX_VALUE-1 =&gt; writeByte(x), default =&gt; writeByte(Byte.MIN_VALUE+1;
     * writeShort(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactShort</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>short</code> equal to <code>(short)v</code>.
     *
     * @param v the <code>short</code> value to be written.
     */
    void writeCompactShort(int v);

    /**
     * Writes one or three bytes as follows; 0 to 254 =&gt; writeByte(x); otherwise writeByte(255); writeByteShort(x);
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
     * Writes two or six bytes as follows; Integer.MIN_VALUE =&gt; Short.MIN_VALUE, Integer.MAX_VALUE =&gt;
     * Short.MAX_VALUE, Short.MIN_VALUE+2 to Short.MAX_VALUE-1 =&gt; writeShort(x), default =&gt;
     * writeShort(Short.MIN_VALUE+1; writeInt(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactInt</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>int</code> equal to <code>v</code>.
     *
     * @param v the <code>int</code> value to be written.
     */
    void writeCompactInt(int v);

    /**
     * Writes two or six bytes as follows; 0 to (1 &lt;&lt; 16) - 2 =&gt; writeInt(x), otherwise writeShort(-1);
     * writeInt(x)
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
    boolean compareAndSwapInt(long offset, int expected, int x);

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
     * Writes four or twelve bytes as follows Long.MIN_VALUE =&gt; Integer.MIN_VALUE, Long.MAX_VALUE =&gt;
     * Integer.MAX_VALUE, Integer.MIN_VALUE+2 to Integer.MAX_VALUE-1 =&gt; writeInt(x), default =&gt;
     * writeInt(Integer.MIN_VALUE+1; writeLong(x)
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactLong</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>long</code> equal to <code>v</code>.
     *
     * @param v the <code>long</code> value to be written.
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
    boolean compareAndSwapLong(long offset, long expected, long x);

    /**
     * Stop bit encoding numbers. This will write the same number of bytes whether you used a byte, short or int.
     */
    void writeStopBit(long n);

    /**
     * Writes a <code>float</code> value, which is comprised of four bytes, to the output stream. It does this as if it
     * first converts this <code>float</code> value to an <code>int</code> in exactly the manner of the
     * <code>Float.floatToIntBits</code> method  and then writes the <code>int</code> value in exactly the manner of the
     * <code>writeInt</code> method.  The bytes written by this method may be read by the <code>readFloat</code> method
     * of interface <code>DataInput</code>, which will then return a <code>float</code> equal to <code>v</code>.
     *
     * @param v the <code>float</code> value to be written.
     */
    @Override
    void writeFloat(float v);

    /**
     * Writes a <code>float</code> value, which is comprised of four bytes, to the output stream. It does this as if it
     * first converts this <code>float</code> value to an <code>int</code> in exactly the manner of the
     * <code>Float.floatToIntBits</code> method  and then writes the <code>int</code> value in exactly the manner of the
     * <code>writeInt</code> method.  The bytes written by this method may be read by the <code>readFloat</code> method
     * of interface <code>DataInput</code>, which will then return a <code>float</code> equal to <code>v</code>.
     *
     * @param offset to write to
     * @param v      the <code>float</code> value to be written.
     */
    void writeFloat(long offset, float v);

    /**
     * Same as writeFloat but include an ordered write barrier.  This means all writes will be visible on a read barrier
     * if this write is visible. This might not be visible to be same thread for some clock cycles so an immediate read
     * could see an old value
     * <p/>
     * This is much faster than a volatile write which stalls the pipeline.  The data is visible to other threads at the
     * same time.
     *
     * @param v value to write
     */
    void writeOrderedFloat(long offset, float v);

    /**
     * Writes a <code>double</code> value, which is comprised of eight bytes, to the output stream. It does this as if
     * it first converts this <code>double</code> value to a <code>long</code> in exactly the manner of the
     * <code>Double.doubleToLongBits</code> method  and then writes the <code>long</code> value in exactly the manner of
     * the  <code>writeLong</code> method. The bytes written by this method may be read by the <code>readDouble</code>
     * method of interface <code>DataInput</code>, which will then return a <code>double</code> equal to
     * <code>v</code>.
     *
     * @param v the <code>double</code> value to be written.
     */
    @Override
    void writeDouble(double v);

    /**
     * Writes a <code>double</code> value, which is comprised of eight bytes, to the output stream. It does this as if
     * it first converts this <code>double</code> value to a <code>long</code> in exactly the manner of the
     * <code>Double.doubleToLongBits</code> method  and then writes the <code>long</code> value in exactly the manner of
     * the  <code>writeLong</code> method. The bytes written by this method may be read by the <code>readDouble</code>
     * method of interface <code>DataInput</code>, which will then return a <code>double</code> equal to
     * <code>v</code>.
     *
     * @param offset to write to
     * @param v      the <code>double</code> value to be written.
     */
    void writeDouble(long offset, double v);

    /**
     * Writes four or twelve bytes as follow;
     * <p><pre><code>
     * if ((float) d == d) {
     *     writeFloat((float) d);
     * } else {
     *     writeFloat(Float.NaN);
     *     writeDouble(d);
     * }
     * <p/>
     * The bytes written by this method may be read by the <code>readCompactDouble</code> method of interface
     * <code>RandomDataInput</code> , which will then return a <code>double</code> equal to <code>v</code>.
     *
     * @param v the <code>double</code> value to be written.
     */
    void writeCompactDouble(double v);

    /**
     * Same as writeDouble but include an ordered write barrier.  This means all writes will be visible on a read
     * barrier if this write is visible. This might not be visible to be same thread for some clock cycles so an
     * immediate read could see an old value
     * <p/>
     * This is much faster than a volatile write which stalls the pipeline.  The data is visible to other threads at the
     * same time.
     *
     * @param v value to write
     */
    void writeOrderedDouble(long offset, double v);

    /**
     * Writes a string to the output stream. For every character in the string <code>s</code>,  taken in order, one byte
     * is written to the output stream.  If <code>s</code> is <code>null</code>, a <code>NullPointerException</code> is
     * thrown.<p>  If <code>s.length</code> is zero, then no bytes are written. Otherwise, the character
     * <code>s[0]</code> is written first, then <code>s[1]</code>, and so on; the last character written is
     * <code>s[s.length-1]</code>. For each character, one byte is written, the low-order byte, in exactly the manner of
     * the <code>writeByte</code> method . The high-order eight bits of each character in the string are ignored.
     *
     * @param s the string of bytes to be written. Cannot be null.
     */
    @Override
    void writeBytes(@NotNull String s);

    /**
     * Writes every character in the string <code>s</code>, to the output stream, in order, two bytes per character. If
     * <code>s</code> is <code>null</code>, a <code>NullPointerException</code> is thrown.  If <code>s.length</code> is
     * zero, then no characters are written. Otherwise, the character <code>s[0]</code> is written first, then
     * <code>s[1]</code>, and so on; the last character written is <code>s[s.length-1]</code>. For each character, two
     * bytes are actually written, high-order byte first, in exactly the manner of the <code>writeChar</code> method.
     *
     * @param s the string value to be written. Cannot be null.
     */
    @Override
    void writeChars(@NotNull String s);

    /**
     * Writes two bytes of length information to the output stream, followed by the <a
     * href="DataInput.html#modified-utf-8">modified UTF-8</a> representation of  every character in the string
     * <code>s</code>. If <code>s</code> is <code>null</code>, a <code>NullPointerException</code> is thrown. Each
     * character in the string <code>s</code> is converted to a group of one, two, or three bytes, depending on the
     * value of the character.<p> If a character <code>c</code> is in the range <code>&#92;u0001</code> through
     * <code>&#92;u007f</code>, it is represented by one byte:<p>
     * <pre>(byte)c </pre>  <p>
     * If a character <code>c</code> is <code>&#92;u0000</code> or is in the range <code>&#92;u0080</code> through
     * <code>&#92;u07ff</code>, then it is represented by two bytes, to be written
     * in the order shown:<p> <pre><code>
     * (byte)(0xc0 | (0x1f &amp; (c &gt;&gt; 6)))
     * (byte)(0x80 | (0x3f &amp; c))
     *  </code></pre>  <p> If a character
     * <code>c</code> is in the range <code>&#92;u0800</code> through <code>uffff</code>, then it is represented by
     * three bytes, to be written
     * in the order shown:<p> <pre><code>
     * (byte)(0xe0 | (0x0f &amp; (c &gt;&gt; 12)))
     * (byte)(0x80 | (0x3f &amp; (c &gt;&gt;  6)))
     * (byte)(0x80 | (0x3f &amp; c))
     *  </code></pre>  <p> First,
     * the total number of bytes needed to represent all the characters of <code>s</code> is calculated. If this number
     * is larger than <code>65535</code>, then a <code>UTFDataFormatException</code> is thrown. Otherwise, this length
     * is written to the output stream in exactly the manner of the <code>writeShort</code> method; after this, the
     * one-, two-, or three-byte representation of each character in the string <code>s</code> is written.<p>  The bytes
     * written by this method may be read by the <code>readUTF</code> method of interface <code>DataInput</code> , which
     * will then return a <code>String</code> equal to <code>s</code>.
     *
     * @param s the string value to be written. Cannot be null
     */
    @Override
    void writeUTF(@NotNull String s);

    /**
     * Write the same encoding as <code>writeUTF</code> with the following changes.  1) The length is stop bit encoded
     * i.e. one byte longer for short strings, but is not limited in length. 2) The string can be null.
     *
     * @param s the string value to be written. Can be null.
     */
    void writeUTFΔ(@Nullable CharSequence s);


    /**
     * Write the same encoding as <code>writeUTF</code> with the following changes.  1) The length is stop bit encoded
     * i.e. one byte longer for short strings, but is not limited in length. 2) The string can be null.
     *
     * @param offset  to write to
     * @param maxSize maximum number of bytes to use
     * @param s       the string value to be written. Can be null.
     * @throws IllegalStateException if the size is too large.
     */
    void writeUTFΔ(long offset, int maxSize, @Nullable CharSequence s) throws IllegalStateException;

    /**
     * Copies the contents of a ByteBuffer from the potision ot the limit.
     *
     * @param bb to copy.
     */
    void write(@NotNull ByteBuffer bb);

    /**
     * Write the object in a form which can be uniquely recreated by readEnum.  This type of "enumerable objects" has
     * the following constraints; 1) each object must have a one to one mapping with a toString() representation, 2) be
     * immutable, 3) ideally appears more than once, 4) Must have a constructor which takes a single String or a
     * <code>valueOf(String)</code> method.
     *
     * @param e   to enumerate
     * @param <E> element class
     */
    <E> void writeEnum(@Nullable E e);

    /**
     * Write an ordered collection of "enumerable objects" (See writeEnum).  This writes the stop bit encoded length,
     * followed by multiple calls to <code>writeEnum</code>  All the elements must be of the same type.
     * <p/>
     * This can be read by the <code>readList</code> method of <code>RandomInputStream</code> and the reader must know
     * the type of each element.  You can send the class first by using <code>writeEnum</code> of the element class
     *
     * @param list to be written
     */
    <E> void writeList(@NotNull Collection<E> list);

    /**
     * Write the keys and values of a Map of "enumerable objects" (See writeEnum). This writes the stop bit encoded
     * length, followed by multiple calls to <code>writeEnum</code> for each key and value.  All the keys must be of the
     * same type. All values must be of the same type.
     *
     * @param map to write out
     */

    <K, V> void writeMap(@NotNull Map<K, V> map);

    // ObjectOutput

    /**
     * Write an object as either an "enumerable object" or a Serializable/Externalizable object using Java
     * Serialization. Java Serialization is <i>much</i> slower but sometimes more convenient than using
     * BytesMarshallable.
     *
     * @param object to write
     */
    @Override
    void writeObject(@Nullable Object object);

    /**
     * Write an object with the assumption that the objClass will be provided when the class is read.
     *
     * @param objClass class to write
     * @param obj      to write
     */
    <OBJ> void writeInstance(@NotNull Class<OBJ> objClass, @NotNull OBJ obj);

    /**
     * Copy data from an Object from bytes start to end.
     *
     * @param object to copy from
     * @param start  first byte inclusive
     * @param end    last byte exclusive.
     */
    void writeObject(Object object, int start, int end);

    /**
     * fill the Bytes with zeros, and clear the position.
     *
     * @return this
     */
    Bytes zeroOut();

    /**
     * fill the Bytes with zeros, and clear the position.
     *
     * @return this
     */
    Bytes zeroOut(long start, long end);


    /**
     * Check the end of the stream has not overflowed.  Otherwise this doesn't do anything.
     */
    @Override
    void flush();

    /**
     * The same as calling finish();
     */
    @Override
    void close();
}
