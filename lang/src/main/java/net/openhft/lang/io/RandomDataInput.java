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

import java.io.ObjectInput;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.RandomAccess;

/**
 * @author peter.lawrey
 */
public interface RandomDataInput extends ObjectInput, RandomAccess, BytesCommon {
    /**
     * Reads some bytes from an input stream and stores them into the buffer array <code>b</code>. The number of bytes
     * read is equal to the length of <code>b</code>.
     * <p/>
     * This method blocks until one of the following conditions occurs:<p> <ul> <li><code>b.length</code> bytes of input
     * data are available, in which case a normal return is made.
     * <p/>
     * <li>End of file is detected, in which case an <code>EOFException</code> is thrown.
     * <p/>
     * <li>An I/O error occurs, in which case an <code>IOException</code> other than <code>EOFException</code> is
     * thrown. </ul>
     * <p/>
     * If <code>b</code> is <code>null</code>, a <code>NullPointerException</code> is thrown. If <code>b.length</code>
     * is zero, then no bytes are read. Otherwise, the first byte read is stored into element <code>b[0]</code>, the
     * next one into <code>b[1]</code>, and so on. If an exception is thrown from this method, then it may be that some
     * but not all bytes of <code>b</code> have been updated with data from the input stream.
     *
     * @param bytes the buffer into which the data is read.
     */
    @Override
    void readFully(@NotNull byte[] bytes);

    /**
     * Reads <code>len</code> bytes from an input stream.
     * <p/>
     * This method blocks until one of the following conditions occurs:<p> <ul> <li><code>len</code> bytes of input data
     * are available, in which case a normal return is made.
     * <p/>
     * <li>End of file is detected, in which case an <code>EOFException</code> is thrown.
     * <p/>
     * <li>An I/O error occurs, in which case an <code>IOException</code> other than <code>EOFException</code> is
     * thrown. </ul>
     * <p/>
     * If <code>b</code> is <code>null</code>, a <code>NullPointerException</code> is thrown. If <code>off</code> is
     * negative, or <code>len</code> is negative, or <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is thrown. If <code>len</code> is zero, then no
     * bytes are read. Otherwise, the first byte read is stored into element <code>b[off]</code>, the next one into
     * <code>b[off+1]</code>, and so on. The number of bytes read is, at most, equal to <code>len</code>.
     *
     * @param bytes the buffer into which the data is read.
     * @param off   an int specifying the offset into the data.
     * @param len   an int specifying the number of bytes to read.
     */
    @Override
    void readFully(@NotNull byte[] bytes, int off, int len);

    /**
     * Makes an attempt to skip over <code>n</code> bytes of data from the input stream, discarding the skipped bytes.
     * However, it may skip over some smaller number of bytes, possibly zero. This may result from any of a number of
     * conditions; reaching end of file before <code>n</code> bytes have been skipped is only one possibility. This
     * method never throws an <code>EOFException</code>. The actual number of bytes skipped is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the number of bytes actually skipped.
     */
    @Override
    int skipBytes(int n);

    /**
     * Reads one input byte and returns <code>true</code> if that byte is nonzero, <code>false</code> if that byte is
     * zero. This method is suitable for reading the byte written by the <code>writeBoolean</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return the <code>boolean</code> value read.
     */
    @Override
    boolean readBoolean();

    /**
     * Reads one input byte and returns <code>true</code> if that byte is nonzero, <code>false</code> if that byte is
     * zero. This method is suitable for reading the byte written by the <code>writeBoolean</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset to read byte translated into a boolean
     * @return the <code>boolean</code> value read.
     */
    boolean readBoolean(long offset);

    /**
     * Reads and returns one input byte. The byte is treated as a signed value in the range <code>-128</code> through
     * <code>127</code>, inclusive. This method is suitable for reading the byte written by the <code>writeByte</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return the 8-bit value read.
     */
    @Override
    byte readByte();

    /**
     * Reads and returns one input byte. The byte is treated as a signed value in the range <code>-128</code> through
     * <code>127</code>, inclusive. This method is suitable for reading the byte written by the <code>writeByte</code>
     * method of interface <code>RandomDataOutput</code>.
     *
     * @param offset of byte to read.
     * @return the 8-bit value read.
     */
    byte readByte(long offset);

    /**
     * Reads one input byte, zero-extends it to type <code>int</code>, and returns the result, which is therefore in the
     * range <code>0</code> through <code>255</code>. This method is suitable for reading the byte written by the
     * <code>writeByte</code> method of interface <code>DataOutput</code> if the argument to <code>writeByte</code> was
     * intended to be a value in the range <code>0</code> through <code>255</code>.
     *
     * @return the unsigned 8-bit value read.
     */
    @Override
    int readUnsignedByte();

    /**
     * Reads one input byte, zero-extends it to type <code>int</code>, and returns the result, which is therefore in the
     * range <code>0</code> through <code>255</code>. This method is suitable for reading the byte written by the
     * <code>writeByte</code> method of interface <code>RandomDataOutput</code> if the argument to
     * <code>writeByte</code> was intended to be a value in the range <code>0</code> through <code>255</code>.
     *
     * @param offset of byte to read
     * @return the unsigned 8-bit value read.
     */
    int readUnsignedByte(long offset);

    /**
     * Reads two input bytes and returns a <code>short</code> value. Let <code>a</code> be the first byte read and
     * <code>b</code> be the second byte on big endian machines, and the opposite on little endian machines. The value
     * returned is:
     * <p><pre><code>(short)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading the bytes written by the <code>writeShort</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return the 16-bit value read.
     */
    @Override
    short readShort();

    /**
     * Reads two input bytes and returns a <code>short</code> value. Let <code>a</code> be the first byte read and
     * <code>b</code> be the second byte on big endian machines, and the opposite on little endian machines. The value
     * returned is:
     * <p><pre><code>
     *     (short)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading the bytes written by the <code>writeShort</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset of short to read.
     * @return the 16-bit value read.
     */
    short readShort(long offset);

    /**
     * Reads two input bytes and returns an <code>int</code> value in the range <code>0</code> through
     * <code>65535</code>. Let <code>a</code> be the first byte read and <code>b</code> be the second byte on big endian
     * machines, and the opposite on little endian machines. The value returned is:
     * <p><pre><code>
     *     (((a &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading the bytes written by the <code>writeUnsignedShort</code> method of interface
     * <code>DataOutput</code>  if the argument to <code>writeUnsignedShort</code> was intended to be a value in the
     * range <code>0</code> through <code>65535</code>.
     *
     * @return the unsigned 16-bit value read.
     */
    @Override
    int readUnsignedShort();

    /**
     * Reads two input bytes and returns an <code>int</code> value in the range <code>0</code> through
     * <code>65535</code>. Let <code>a</code> be the first byte read and <code>b</code> be the second byte on big endian
     * machines, and the opposite on little endian machines. The value returned is:
     * <p><pre><code>
     *     (((a &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading the bytes written by the <code>writeShort</code> method of interface
     * <code>RandomDataOutput</code>  if the argument to <code>writeUnsignedShort</code> was intended to be a value in
     * the range <code>0</code> through <code>65535</code>.
     *
     * @param offset of short to read.
     * @return the unsigned 16-bit value read.
     */
    int readUnsignedShort(long offset);

    /**
     * Reads one or three input bytes and returns a <code>short</code> value. Let <code>a</code> be the first byte read.
     * This mapped as follows; Byte.MIN_VALUE => Short.MIN_VALUE, Byte.MAX_VALUE => Short.MAX_VALUE, Byte.MIN_VALUE+2 to
     * Byte.MAX_VALUE-1 => same as short value, Byte.MIN_VALUE+1 => readShort().
     * <p/>
     * This method is suitable for reading the bytes written by the <code>writeCompactShort</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the 16-bit value read.
     */
    short readCompactShort();

    /**
     * Reads one or three input bytes and returns a <code>short</code> value. Let <code>a</code> be the first byte read.
     * This mapped as follows; -1 => readUnsignedShort(), default => (a &amp; 0xFF)
     * <p/>
     * This method is suitable for reading the bytes written by the <code>writeCompactUnsignedShort</code> method of
     * interface <code>RandomDataOutput</code>.
     *
     * @return the unsigned 16-bit value read.
     */
    int readCompactUnsignedShort();

    /**
     * Reads two input bytes and returns a <code>char</code> value. Let <code>a</code> be the first byte read and
     * <code>b</code> be the second byte on big endian machines, and the opposite on little endian machines. The value
     * returned is:
     * <p><pre><code>(char)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeChar</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return the <code>char</code> value read.
     */
    @Override
    char readChar();

    /**
     * Reads two input bytes and returns a <code>char</code> value. Let <code>a</code> be the first byte read and
     * <code>b</code> be the second byte on big endian machines, and the opposite on little endian machines. The value
     * returned is:
     * <p><pre><code>(char)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeChar</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset of the char to read.
     * @return the <code>char</code> value read.
     */
    char readChar(long offset);

    /**
     * Reads three input bytes and returns a 24-bit <code>int</code> value. Let <code>a-c</code> be the first through
     * third bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * ((((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) << 8))) &gt;&gt; 8
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeInt24</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the <code>int</code> value read.
     */
    int readInt24();

    /**
     * Reads three input bytes and returns a 24-bit <code>int</code> value. Let <code>a-c</code> be the first through
     * third bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * ((((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) << 8))) &gt;&gt; 8
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeInt24</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset to read from
     * @return the <code>int</code> value read.
     */
    int readInt24(long offset);

    /**
     * Reads four input bytes and returns an <code>int</code> value. Let <code>a-d</code> be the first through fourth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeInt</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return the <code>int</code> value read.
     */
    @Override
    int readInt();

    /**
     * Reads four input bytes and returns an <code>int</code> value. Let <code>a-d</code> be the first through fourth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeInt</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset to read from
     * @return the <code>int</code> value read.
     */
    int readInt(long offset);

    /**
     * This is the same as readInt() except a read barrier is performed first. </p> Reads four input bytes and returns
     * an <code>int</code> value. Let <code>a-d</code> be the first through fourth bytes read on big endian machines,
     * and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeOrderedInt</code> or
     * <code>writeVolatileInt</code> method of interface <code>RandomDataOutput</code>.
     *
     * @return the <code>int</code> value read.
     */
    int readVolatileInt();

    /**
     * This is the same as readInt() except a read barrier is performed first. </p> Reads four input bytes and returns
     * an <code>int</code> value. Let <code>a-d</code> be the first through fourth bytes read on big endian machines,
     * and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeOrderedInt</code> or
     * <code>writeVolatileInt</code> method of interface <code>RandomDataOutput</code>.
     *
     * @param offset to read from
     * @return the <code>int</code> value read.
     */
    int readVolatileInt(long offset);

    /**
     * Reads four input bytes and returns an <code>int</code> value. Let <code>a-d</code> be the first through fourth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * ((((long) a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp;
     * 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeUnsignedInt</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the unsigned <code>int</code> value read.
     */
    long readUnsignedInt();

    /**
     * Reads four input bytes and returns an <code>int</code> value. Let <code>a-d</code> be the first through fourth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre>
     * <code>
     * ((((long) a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) | ((c &amp; 0xff) &lt;&lt; 8) | (d &amp;
     * 0xff))
     * </code></pre>
     * This method is suitable for reading bytes written by the <code>writeUnsignedInt</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset to read from
     * @return the unsigned <code>int</code> value read.
     */
    long readUnsignedInt(long offset);

    /**
     * Reads two or six input bytes and returns an <code>int</code> value. Let <code>a</code> be the first short read
     * with readShort(). This mapped as follows; Short.MIN_VALUE => Integer.MIN_VALUE, Short.MAX_VALUE =>
     * Integer.MAX_VALUE, Short.MIN_VALUE+2 to Short.MAX_VALUE-1 => same as short value, Short.MIN_VALUE+1 =>
     * readInt().
     * <p/>
     * This method is suitable for reading the bytes written by the <code>writeCompactInt</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the 32-bit value read.
     */
    int readCompactInt();

    /**
     * Reads two or six input bytes and returns an <code>int</code> value. Let <code>a</code> be the first short read
     * with readShort(). This mapped as follows; -1 => readUnsignedInt(), default => (a &amp; 0xFFFF)
     * <p/>
     * This method is suitable for reading the bytes written by the <code>writeCompactUnsignedInt</code> method of
     * interface <code>RandomDataOutput</code>.
     *
     * @return the unsigned 32-bit value read.
     */
    long readCompactUnsignedInt();

    /**
     * Reads eight input bytes and returns a <code>long</code> value. Let <code>a-h</code> be the first through eighth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeLong</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return the <code>long</code> value read.
     */
    @Override
    long readLong();

    /**
     * Reads eight input bytes and returns a <code>long</code> value. Let <code>a-h</code> be the first through eighth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeLong</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset of the long to read
     * @return the <code>long</code> value read.
     */
    long readLong(long offset);

    /**
     * This is the same readLong() except a dread barrier is performed first
     * <p/>
     * Reads eight input bytes and returns a <code>long</code> value. Let <code>a-h</code> be the first through eighth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeOrderedLong</code> or
     * <code>writeVolatileLong</code> method of interface <code>RandomDataOutput</code>.
     *
     * @return the <code>long</code> value read.
     */
    long readVolatileLong();

    /**
     * This is the same readLong() except a dread barrier is performed first
     * <p/>
     * Reads eight input bytes and returns a <code>long</code> value. Let <code>a-h</code> be the first through eighth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeOrderedLong</code> or
     * <code>writeVolatileLong</code> method of interface <code>RandomDataOutput</code>.
     *
     * @param offset of the long to read
     * @return the <code>long</code> value read.
     */
    long readVolatileLong(long offset);

    /**
     * Reads six input bytes and returns a <code>long</code> value. Let <code>a-f</code> be the first through sixth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16)) &gt;&gt; 16
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeInt48</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the <code>long</code> value read.
     */
    long readInt48();

    /**
     * Reads six input bytes and returns a <code>long</code> value. Let <code>a-f</code> be the first through sixth
     * bytes read on big endian machines, and the opposite on little endian machines. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16)) &gt;&gt; 16
     * </code></pre>
     * <p/>
     * This method is suitable for reading bytes written by the <code>writeInt48</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @param offset of the long to read
     * @return the <code>long</code> value read.
     */
    long readInt48(long offset);

    /**
     * Reads four or twelve input bytes and returns a <code>long</code> value. Let <code>a</code> be the first int read
     * with readInt(). This mapped as follows; Integer.MIN_VALUE => Long.MIN_VALUE, Integer.MAX_VALUE => Long.MAX_VALUE,
     * Integer.MIN_VALUE+2 to Integer.MAX_VALUE-1 => same as short value, Integer.MIN_VALUE+1 => readLong().
     * <p/>
     * This method is suitable for reading the bytes written by the <code>writeCompactLong</code> method of interface
     * <code>RandomDataOutput</code>.
     *
     * @return the 64-bit value read.
     */
    long readCompactLong();

    /**
     * Reads between one and ten bytes with are stop encoded with support for negative numbers
     * <p/><pre><code>
     * long l = 0, b;
     * int count = 0;
     * while ((b = readByte()) < 0) {
     *     l |= (b & 0x7FL) << count;
     *     count += 7;
     * }
     * if (b == 0 && count > 0)
     *     return ~l;
     * return l | (b << count);
     * </code></pre
     *
     * @return a stop bit encoded number as a long.
     */
    long readStopBit();

    /**
     * Reads four input bytes and returns a <code>float</code> value. It does this by first constructing an
     * <code>int</code> value in exactly the manner of the <code>readInt</code> method, then converting this
     * <code>int</code> value to a <code>float</code> in exactly the manner of the method
     * <code>Float.intBitsToFloat</code>. This method is suitable for reading bytes written by the
     * <code>writeFloat</code> method of interface <code>DataOutput</code>.
     *
     * @return the <code>float</code> value read.
     */
    @Override
    float readFloat();

    /**
     * Reads four input bytes and returns a <code>float</code> value. It does this by first constructing an
     * <code>int</code> value in exactly the manner of the <code>readInt</code> method, then converting this
     * <code>int</code> value to a <code>float</code> in exactly the manner of the method
     * <code>Float.intBitsToFloat</code>. This method is suitable for reading bytes written by the
     * <code>writeFloat</code> method of interface <code>DataOutput</code>.
     *
     * @param offset to read from
     * @return the <code>float</code> value read.
     */
    float readFloat(long offset);

    /**
     * Reads eight input bytes and returns a <code>double</code> value. It does this by first constructing a
     * <code>long</code> value in exactly the manner of the <code>readLong</code> method, then converting this
     * <code>long</code> value to a <code>double</code> in exactly the manner of the method
     * <code>Double.longBitsToDouble</code>. This method is suitable for reading bytes written by the
     * <code>writeDouble</code> method of interface <code>DataOutput</code>.
     *
     * @return the <code>double</code> value read.
     */
    @Override
    double readDouble();

    /**
     * Reads eight input bytes and returns a <code>double</code> value. It does this by first constructing a
     * <code>long</code> value in exactly the manner of the <code>readLong</code> method, then converting this
     * <code>long</code> value to a <code>double</code> in exactly the manner of the method
     * <code>Double.longBitsToDouble</code>. This method is suitable for reading bytes written by the
     * <code>writeDouble</code> method of interface <code>DataOutput</code>.
     *
     * @param offset to read from
     * @return the <code>double</code> value read.
     */
    double readDouble(long offset);

    /**
     * Reads the first four bytes as readFloat().  If this is Float.NaN, the next eight bytes are read as readDouble()
     *
     * @return the <code>double</code> value read.
     */
    double readCompactDouble();

    /**
     * Reads the next line of text from the input stream. It reads successive bytes, converting each byte separately
     * into a character, until it encounters a line terminator or end of file; the characters read are then returned as
     * a <code>String</code>. Note that because this method processes bytes, it does not support input of the full
     * Unicode character set.
     * <p/>
     * If end of file is encountered before even one byte can be read, then <code>null</code> is returned. Otherwise,
     * each byte that is read is converted to type <code>char</code> by zero-extension. If the character
     * <code>'\n'</code> is encountered, it is discarded and reading ceases. If the character <code>'\r'</code> is
     * encountered, it is discarded and, if the following byte converts &#32;to the character <code>'\n'</code>, then
     * that is discarded also; reading then ceases. If end of file is encountered before either of the characters
     * <code>'\n'</code> and <code>'\r'</code> is encountered, reading ceases. Once reading has ceased, a
     * <code>String</code> is returned that contains all the characters read and not discarded, taken in order. Note
     * that every character in this string will have a value less than <code>&#92;u0100</code>, that is,
     * <code>(char)256</code>.
     *
     * @return the next line of text from the input stream, or <CODE>null</CODE> if the end of file is encountered
     *         before a byte can be read.
     */
    @Override
    @Nullable
    String readLine();

    /**
     * Reads in a string that has been encoded using a <a href="#modified-utf-8">modified UTF-8</a> format. The general
     * contract of <code>readUTF</code> is that it reads a representation of a Unicode character string encoded in
     * modified UTF-8 format; this string of characters is then returned as a <code>String</code>.
     * <p/>
     * First, two bytes are read and used to construct an unsigned 16-bit integer in exactly the manner of the
     * <code>readUnsignedShort</code> method . This integer value is called the <i>UTF length</i> and specifies the
     * number of additional bytes to be read. These bytes are then converted to characters by considering them in
     * groups. The length of each group is computed from the value of the first byte of the group. The byte following a
     * group, if any, is the first byte of the next group.
     * <p/>
     * If the first byte of a group matches the bit pattern <code>0xxxxxxx</code> (where <code>x</code> means "may be
     * <code>0</code> or <code>1</code>"), then the group consists of just that byte. The byte is zero-extended to form
     * a character.
     * <p/>
     * If the first byte of a group matches the bit pattern <code>110xxxxx</code>, then the group consists of that byte
     * <code>a</code> and a second byte <code>b</code>. If there is no byte <code>b</code> (because byte <code>a</code>
     * was the last of the bytes to be read), or if byte <code>b</code> does not match the bit pattern
     * <code>10xxxxxx</code>, then a <code>UTFDataFormatException</code> is thrown. Otherwise, the group is converted to
     * the character:<p>
     * <pre><code>(char)(((a&amp; 0x1F) &lt;&lt; 6) | (b &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group matches the bit pattern <code>1110xxxx</code>, then the group consists of that byte
     * <code>a</code> and two more bytes <code>b</code> and <code>c</code>. If there is no byte <code>c</code> (because
     * byte <code>a</code> was one of the last two of the bytes to be read), or either byte <code>b</code> or byte
     * <code>c</code> does not match the bit pattern <code>10xxxxxx</code>, then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, the group is converted to the character:<p>
     * <pre><code>
     * (char)(((a &amp; 0x0F) &lt;&lt; 12) | ((b &amp; 0x3F) &lt;&lt; 6) | (c &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group matches the pattern <code>1111xxxx</code> or the pattern <code>10xxxxxx</code>, then
     * a <code>UTFDataFormatException</code> is thrown.
     * <p/>
     * If end of file is encountered at any time during this entire process, then an <code>EOFException</code> is
     * thrown.
     * <p/>
     * After every group has been converted to a character by this process, the characters are gathered, in the same
     * order in which their corresponding groups were read from the input stream, to form a <code>String</code>, which
     * is returned.
     * <p/>
     * The <code>writeUTF</code> method of interface <code>DataOutput</code> may be used to write data that is suitable
     * for reading by this method.
     *
     * @return a Unicode string.
     * @throws IllegalStateException if the bytes do not represent a valid modified UTF-8 encoding of a string.
     */
    @Override
    @NotNull
    String readUTF();

    /**
     * The same as readUTF() except the length is stop bit encoded.  This saves one byte for strings shorter than 128
     * chars.  <code>null</code> values are also supported
     *
     * @return a Unicode string or <code>null</code> if <code>writeUTFΔ(null)</code> was called
     */
    @Nullable
    String readUTFΔ();

    /**
     * The same as readUTFΔ() except the chars are copied to a truncated StringBuilder.
     *
     * @return <code>true</code> if there was a String, or <code>false</code> if it was <code>null</code>
     */
    boolean readUTFΔ(@NotNull StringBuilder stringBuilder);

    /**
     * Copy bytes into a ByteBuffer to the minimum of the length <code>remaining()</code> in the ByteBuffer or the
     * Excerpt.
     *
     * @param bb to copy into
     */
    void read(@NotNull ByteBuffer bb);

    /**
     * Read a String with <code>readUTFΔ</code> which is converted to an enumerable type. i.e. where there is a one to
     * one mapping between an object and it's toString().
     * <p/>
     * This is suitable to read an object written using <code>writeEnum()</code> in the <code>RandomDataOutput</code>
     * interface
     *
     * @param eClass to decode the String as
     * @return the decoded value.  <code>null</code> with be return if null was written.
     */
    @Nullable
    <E> E readEnum(@NotNull Class<E> eClass);

    /**
     * Read a stop bit encoded length and populates this Collection after clear()ing it.
     * <p/>
     * This is suitable to reading a list written using <code>writeList()</code> in the <code>RandomDataOutput</code>
     * interface
     *
     * @param list to populate
     */
    <E> void readList(@NotNull Collection<E> list, @NotNull Class<E> eClass);

    /**
     * Read a stop bit encoded length and populates this Map after clear()ing it.
     * <p/>
     * This is suitable to reading a list written using <code>writeMap()</code> in the <code>RandomDataOutput</code>
     * interface
     *
     * @param map to populate
     */
    <K, V> void readMap(@NotNull Map<K, V> map, @NotNull Class<K> kClass, @NotNull Class<V> vClass);

    // ObjectInput

    /**
     * Read and return an object. The class that implements this interface defines where the object is "read" from.
     *
     * @return the object read from the stream
     * @throws IllegalStateException the class of a serialized object cannot be found or any of the usual Input/Output
     *                               related exceptions occur.
     */
    @Nullable
    @Override
    Object readObject() throws IllegalStateException;

    /**
     * Read and return an object. The class that implements this interface defines where the object is "read" from.
     *
     * @return the object read from the stream
     * @throws IllegalStateException the class of a serialized object cannot be found or any of the usual Input/Output
     *                               related exceptions occur.
     * @throws ClassCastException    The class cannot be cast or converted to the type given.
     */
    @Nullable
    <T> T readObject(Class<T> tClass) throws IllegalStateException;

    /**
     * Reads a byte of data. This method will block if no input is available.
     *
     * @return the byte read, or -1 if the end of the stream is reached.
     */
    @Override
    int read();

    /**
     * Reads into an array of bytes.  This method will block until some input is available.
     *
     * @param bytes the buffer into which the data is read
     * @return the actual number of bytes read, -1 is returned when the end of the stream is reached.
     */
    @Override
    int read(@NotNull byte[] bytes);

    /**
     * Reads into an array of bytes.  This method will block until some input is available.
     *
     * @param bytes the buffer into which the data is read
     * @param off   the start offset of the data
     * @param len   the maximum number of bytes read
     * @return the actual number of bytes read, -1 is returned when the end of the stream is reached.
     */
    @Override
    int read(@NotNull byte[] bytes, int off, int len);

    /**
     * Read the object from start to end bytes
     *
     * @param object to read into
     * @param start  byte inclusive
     * @param end    byte exclusive
     */
    void readObject(Object object, int start, int end);

    /**
     * Skips n bytes of input.
     *
     * @param n the number of bytes to be skipped
     * @return the actual number of bytes skipped.
     */
    @Override
    long skip(long n);

    /**
     * @return remaining() or Integer.MAX_VALUE if larger.
     */
    @Override
    int available();

    /**
     * Finishes the excerpt entry if not finished()
     */
    @Override
    void close();
}
