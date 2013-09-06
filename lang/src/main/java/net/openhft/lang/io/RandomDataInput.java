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
    void readFully(byte[] bytes);

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
    void readFully(byte[] bytes, int off, int len);

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

    long readCompactLong();

    /**
     * @return a stop bit encoded number as a long.
     */
    long readStopBit();

    @Override
    float readFloat();

    float readFloat(long offset);

    @Override
    double readDouble();

    double readDouble(long offset);

    double readCompactDouble();

    @Override
    String readLine();

    void readBytesΔ(StringBuilder sb);

    void readChars(StringBuilder sb);

    @Override
    String readUTF();

    String readUTFΔ();

    boolean readUTFΔ(StringBuilder stringBuilder);

    String readUTFΔ(long offset);

    void read(ByteBuffer bb);

    <E> E readEnum(Class<E> eClass);

    <E> void readList(Collection<E> list);

    <K, V> Map<K, V> readMap(Class<K> kClass, Class<V> vClass);

    // ObjectInput
    @Override
    Object readObject() throws IllegalStateException;

    @Override
    int read();

    @Override
    int read(byte[] b);

    @Override
    int read(byte[] b, int off, int len);

    @Override
    long skip(long n);

    /**
     * @return remaining() or Integer.MAX_VALUE if larger.
     */
    @Override
    int available();

    @Override
    void close();
}
