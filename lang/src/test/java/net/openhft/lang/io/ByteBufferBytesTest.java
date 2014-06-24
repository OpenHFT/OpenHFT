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

import net.openhft.lang.Maths;
import net.openhft.lang.thread.NamedThreadFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.openhft.lang.io.StopCharTesters.*;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA. User: peter.lawrey Date: 17/09/13 Time: 16:09 To change this template use File | Settings | File
 * Templates.
 */
public class ByteBufferBytesTest {
    public static final int SIZE = 128;
    private ByteBufferBytes bytes;
    private ByteBuffer byteBuffer;

    @Before
    public void beforeTest() {
        byteBuffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        bytes = new ByteBufferBytes(byteBuffer);
    }

    @Test
    public void testLongHash() throws Exception {
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7, 8};
        long h = NativeBytes.longHash(bytes, 0, bytes.length);
        assertFalse(h == 0);
        byte[] bytes2 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        long h2 = NativeBytes.longHash(bytes2, 0, bytes2.length);
        assertFalse(h2 == 0);
        assertFalse(h2 == h);
    }

    @Test
    public void testCAS() {
        Bytes bytes = new ByteBufferBytes(ByteBuffer.allocate(100));
        bytes.compareAndSwapLong(0, 0L, 1L);
        assertEquals(1L, bytes.readLong(0));
    }

    @Test
    public void testRead() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.writeByte(i, i);
        bytes.position(0);
        for (int i = 0; i < bytes.capacity(); i++)
            assertEquals((byte) i, bytes.read());
        for (int i = (int) (bytes.capacity() - 1); i >= 0; i--) {
            assertEquals((byte) i, bytes.readByte(i));
        }

    }

    @Test
    public void testReadFully() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.write(i);
        bytes.position(0);
        byte[] bytes = new byte[(int) this.bytes.capacity()];
        this.bytes.readFully(bytes);
        for (int i = 0; i < this.bytes.capacity(); i++)
            assertEquals((byte) i, bytes[i]);
    }

    @Test
    public void testCompareAndSetLong() throws Exception {
        assertTrue(bytes.compareAndSwapLong(0, 0, 1));
        assertFalse(bytes.compareAndSwapLong(0, 0, 1));
        assertTrue(bytes.compareAndSwapLong(8, 0, 1));
        assertTrue(bytes.compareAndSwapLong(0, 1, 2));

    }

    @Test
    public void testPosition() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.write(i);
        for (int i = (int) (bytes.capacity() - 1); i >= 0; i--) {
            bytes.position(i);
            assertEquals((byte) i, bytes.read());
        }
    }

    @Test
    public void testCapacity() throws Exception {
        assertEquals(SIZE, bytes.capacity());
        assertEquals(10, new NativeBytes(0, 10).capacity());
    }

    @Test
    public void testRemaining() throws Exception {
        assertEquals(SIZE, bytes.remaining());
        bytes.position(10);
        assertEquals(SIZE - 10, bytes.remaining());
    }

    @Test
    public void testByteOrder() throws Exception {
        assertEquals(ByteOrder.nativeOrder(), bytes.byteOrder());
    }

    @Test
    public void testCheckEndOfBuffer() throws Exception {
        bytes.checkEndOfBuffer();

        try {
            bytes.position(SIZE + 2);
            bytes.checkEndOfBuffer();
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendDouble() {
        testAppendDouble0(-6.895305375646115E24);
        Random random = new Random(1);
        for (int i = 0; i < 100000; i++) {
            double d = Math.pow(1e32, random.nextDouble()) / 1e6;
            if (i % 3 == 0) d = -d;
            testAppendDouble0(d);
        }
    }

    private void testAppendDouble0(double d) {
        bytes.clear();
        bytes.append(d).append(' ');
        bytes.flip();
        double d2 = bytes.parseDouble();
        assertEquals(d, d2, 0);

        bytes.selfTerminating(true);
        bytes.clear();
        bytes.append(d);
        bytes.flip();
        double d3 = bytes.parseDouble();
        assertEquals(d, d3, 0);

        bytes.selfTerminating(false);
        bytes.clear();
        bytes.append(d);
        bytes.flip();
        try {
            fail("got " + bytes.parseDouble());
        } catch (BufferUnderflowException expected) {
            // expected
        }
    }

    @Test
    public void testAppendDouble2() {
//        testAppendDouble0(-0.93879148954440506, 14);
//        testAppendDouble0(-0.214980202661, 12);
//        testAppendDouble0(-0.937082148896, 12);
        testAppendDouble0(0.17805, 5);
        Random random = new Random(1);

        for (int j = 0; j < 20000; j++) {
            double d = random.nextDouble();
            if (j % 3 == 0) d = -d;
//            if (j % 5 == 0) d *= 1e6;
            for (int i = 0; i < 4; i++) {
                testAppendDouble0(d, i);
            }
        }
    }

    private void testAppendDouble0(double d, int precision) {
        bytes.position(0);
        bytes.append(d, precision).append(' ');
        bytes.position(0);
        String text = bytes.parseUTF(SPACE_STOP);
        bytes.position(0);
        assertEquals(0, bytes.position());
        double d2 = bytes.parseDouble();
        double d3 = (double) Math.round(d * Maths.power10(precision)) / Maths.power10(precision);
//        if (precision >= 14)
//            assertEquals("'" + text + "' p: " + precision + " v: " + new BigDecimal(d), d3, d2, 5e-29 * Maths.power10(precision));
//        else
        assertEquals("'" + text + "' p: " + precision, d3, d2, 0);
    }

    @Test
    public void testWriteReadBytes() {
        byte[] bytes = "Hello World!".getBytes();
        this.bytes.write(bytes);
        byte[] bytes2 = new byte[bytes.length];
        this.bytes.position(0);
        this.bytes.read(bytes2);
        assertTrue(Arrays.equals(bytes, bytes2));

        this.bytes.write(22, bytes);
        byte[] bytes3 = new byte[bytes.length];
        this.bytes.skipBytes((int) (22 - this.bytes.position()));
        assertEquals(bytes3.length, this.bytes.read(bytes3));
        assertTrue(Arrays.equals(bytes, bytes3));
        this.bytes.position(this.bytes.capacity());
        assertEquals(-1, this.bytes.read(bytes3));
    }

    @Test
    public void testWriteReadUTFΔ() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            bytes.writeUTFΔ(word);
        }
        bytes.writeUTFΔ(null);
        bytes.position(0);
        for (String word : words) {
            assertEquals(word, bytes.readUTFΔ());
        }
        assertEquals(null, bytes.readUTFΔ());
        assertEquals("", bytes.readUTFΔ());
        assertEquals(26, bytes.position()); // check the size

        bytes.position(0);
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            assertTrue(bytes.readUTFΔ(sb));
            assertEquals(word, sb.toString());
        }
        assertFalse(bytes.readUTFΔ(sb));
        assertTrue(bytes.readUTFΔ(sb));
        assertEquals("", sb.toString());
    }

    @Test
    public void testWriteReadUTF() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            bytes.writeUTF(word);
        }
        bytes.writeUTF("");
        assertEquals(28, bytes.position()); // check the size, more bytes for less strings than writeUTFΔ
        bytes.position(0);
        for (String word : words) {
            assertEquals(word, bytes.readUTF());
        }
        assertEquals("", bytes.readUTF());
    }

    @Test
    public void testAppendParseUTF() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            bytes.append(word).append('\t');
        }
        bytes.append('\t');
        bytes.flip();
        for (String word : words) {
            assertEquals(word, bytes.parseUTF(CONTROL_STOP));
        }
        assertEquals("", bytes.parseUTF(CONTROL_STOP));

        bytes.position(0);
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            bytes.parseUTF(sb, CONTROL_STOP);
            assertEquals(word, sb.toString());
        }
        bytes.parseUTF(sb, CONTROL_STOP);
        assertEquals("", sb.toString());

        bytes.position(0);
        bytes.skipTo(CONTROL_STOP);
        assertEquals(6, bytes.position());
        bytes.skipTo(CONTROL_STOP);
        assertEquals(13, bytes.position());
        assertTrue(bytes.skipTo(CONTROL_STOP));
        assertEquals(23, bytes.position());
        assertTrue(bytes.skipTo(CONTROL_STOP));
        assertEquals(24, bytes.position());
        assertFalse(bytes.skipTo(CONTROL_STOP));

        bytes.position(0);
        bytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(6, bytes.position());
        bytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(6, bytes.position());
        bytes.position(10);
        bytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(13, bytes.position());

    }

    @Test
    public void testWriteReadLines() {
        byte[] bytes = "Hello\nWorld!\r\nBye".getBytes();
        this.bytes.write(bytes);
        this.bytes.position(0);
        assertEquals("Hello", this.bytes.readLine());
        assertEquals("World!", this.bytes.readLine());
        assertTrue(this.bytes.readLine().startsWith("Bye"));
    }

    @Test
    public void testWriteReadByteBuffer() {
        byte[] bytes = "Hello\nWorld!\r\nBye".getBytes();
        this.bytes.write(ByteBuffer.wrap(bytes));
        this.bytes.position(0);
        byte[] bytes2 = new byte[bytes.length + 1];
        ByteBuffer bb2 = ByteBuffer.wrap(bytes2);
        this.bytes.read(bb2);

        assertEquals(bytes2.length, bb2.position());
        assertTrue(Arrays.equals(bytes, Arrays.copyOf(bytes2, bytes.length)));
    }

    @Test
    public void testReadWriteBoolean() {
        for (int i = 0; i < 32; i++)
            bytes.writeBoolean(i, (i & 3) == 0);
        bytes.position(32);
        for (int i = 32; i < 64; i++)
            bytes.writeBoolean((i & 5) == 0);
        bytes.position(0);
        for (int i = 0; i < 32; i++)
            assertEquals((i & 3) == 0, bytes.readBoolean());
        for (int i = 32; i < 64; i++)
            assertEquals((i & 5) == 0, bytes.readBoolean(i));
    }

    @Test
    public void testReadWriteShort() {
        for (int i = 0; i < 32; i += 2)
            bytes.writeShort(i, i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 2)
            bytes.writeShort(i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(i, bytes.readShort());
        for (int i = 32; i < 64; i += 2)
            assertEquals(i, bytes.readShort(i));
    }

    @Test
    public void testReadWriteCompactShort() {
        int[] ints = {Short.MIN_VALUE, Short.MAX_VALUE, -125, 0, 127, -10000, 10000};
        for (int i : ints) {
            bytes.writeCompactShort(i);
//            System.out.println(i + " " + bytes.position());
        }
        assertEquals(5 + 2 * 3, bytes.position());

        bytes.position(0);
        for (int i : ints)
            assertEquals(i, bytes.readCompactShort());
    }

    @Test
    public void testReadWriteCompactInt() {
        int[] ints = {-10000000, Integer.MIN_VALUE, Integer.MAX_VALUE, Short.MIN_VALUE + 3, 0, Short.MAX_VALUE, 10000000};
        for (int i : ints) {
            bytes.writeCompactInt(i);
//            System.out.println(i + " " + bytes.position());
        }
        assertEquals(5 * 2 + 2 * 6, bytes.position());

        bytes.position(0);
        for (int i : ints)
            assertEquals(i, bytes.readCompactInt());
    }

    @Test
    public void testReadWriteCompactLong() {
        long[] ints = {Long.MAX_VALUE, -100000000000L, Long.MIN_VALUE, Integer.MIN_VALUE + 3, 0, Integer.MAX_VALUE, 100000000000L};
        for (long i : ints) {
            bytes.writeCompactLong(i);
//            System.out.println(i + " " + bytes.position());
        }
        assertEquals(5 * 4 + 2 * 12, bytes.position());

        bytes.position(0);
        for (long i : ints)
            assertEquals(i, bytes.readCompactLong());
    }

    @Test
    public void testReadWriteCompactDouble() {
        double[] doubles = {1, 1000, 1000000, -100000000, 0.1f, 0.1, 0.5, 0.51};
        for (double i : doubles) {
            bytes.writeCompactDouble(i);
//            System.out.println(i + " " + bytes.position());
        }
        assertEquals(6 * 4 + 2 * 12, bytes.position());

        bytes.position(0);
        for (double i : doubles)
            assertEquals(i, bytes.readCompactDouble(), 0.0);
    }

    @Test
    public void testReadWriteStop() {
        long[] longs = {Long.MIN_VALUE, Long.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (long i : longs) {
            bytes.writeStopBit(i);
//            System.out.println(i + " " + bytes.position());
        }
        assertEquals(9 + 10, +5 + 6, bytes.position());

        bytes.position(0);
        for (long i : longs)
            assertEquals(i, bytes.readStopBit());
    }

    @Test
    public void testReadWriteChar() {
        for (int i = 0; i < 32; i += 2)
            bytes.writeChar(i, i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 2)
            bytes.writeChar(i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(i, bytes.readChar());
        for (int i = 32; i < 64; i += 2)
            assertEquals(i, bytes.readChar(i));
    }

    @Test
    public void testReadWriteUnsignedShort() {
        for (int i = 0; i < 32; i += 2)
            bytes.writeUnsignedShort(i, ~i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 2)
            bytes.writeUnsignedShort(~i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(~i & 0xFFFF, bytes.readUnsignedShort());
        for (int i = 32; i < 64; i += 2)
            assertEquals(~i & 0xFFFF, bytes.readUnsignedShort(i));
    }

    @Test
    public void testReadWriteInt() {
        for (int i = 0; i < 32; i += 4)
            bytes.writeInt(i, i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 4)
            bytes.writeInt(i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, bytes.readInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, bytes.readInt(i));
    }

    @Test
    public void testReadWriteThreadeSafeInt() {
        for (int i = 0; i < 32; i += 4)
            bytes.writeOrderedInt(i, i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 4)
            bytes.writeOrderedInt(i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, bytes.readVolatileInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, bytes.readVolatileInt(i));
    }

    @Test
    public void testReadWriteFloat() {
        for (int i = 0; i < 32; i += 4)
            bytes.writeFloat(i, i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 4)
            bytes.writeFloat(i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, bytes.readFloat(), 0);
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, bytes.readFloat(i), 0);
    }

    @Test
    public void testReadWriteUnsignedInt() {
        for (int i = 0; i < 32; i += 4)
            bytes.writeUnsignedInt(i, ~i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 4)
            bytes.writeUnsignedInt(~i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(~i & 0xFFFFFFFFL, bytes.readUnsignedInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(~i & 0xFFFFFFFFL, bytes.readUnsignedInt(i));
    }

    @Test
    public void testReadWriteInt24() {
        for (int i = 0; i < 30; i += 3)
            bytes.writeInt24(i, ~i & 0x7FFFFF);
        bytes.position(30);
        for (int i = 30; i < 63; i += 3)
            bytes.writeInt24(~i & 0x7FFFFF);
        assertEquals(63, bytes.position());
        bytes.position(0);
        for (int i = 0; i < 30; i += 3)
            assertEquals("i: " + i, ~i & 0x7FFFFFL, bytes.readInt24());
        for (int i = 30; i < 63; i += 3)
            assertEquals("i: " + i, ~i & 0x7FFFFFL, bytes.readInt24(i));
        // now negative
        bytes.position(0);
        for (int i = 0; i < 30; i += 3)
            bytes.writeInt24(i, ~i);
        bytes.position(30);
        for (int i = 30; i < 63; i += 3)
            bytes.writeInt24(~i);
        assertEquals(63, bytes.position());
        bytes.position(0);
        for (int i = 0; i < 30; i += 3)
            assertEquals("i: " + i, ~i << 8 >> 8, bytes.readInt24());
        for (int i = 30; i < 63; i += 3)
            assertEquals("i: " + i, ~i << 8 >> 8, bytes.readInt24(i));
    }

    @Test
    public void testReadWriteInt48() {
        for (long i = 0; i < 30; i += 6)
            bytes.writeInt48(i, ~i & 0x7FFFFFFFFFFFL);
        bytes.position(30);
        for (long i = 30; i < 60; i += 6)
            bytes.writeInt48(~i & 0x7FFFFFFFFFFFL);
        assertEquals(60, bytes.position());
        bytes.position(0);
        for (long i = 0; i < 30; i += 6)
            assertEquals("i: " + i, ~i & 0x7FFFFFFFFFFFL, bytes.readInt48());
        for (long i = 30; i < 60; i += 6)
            assertEquals("i: " + i, ~i & 0x7FFFFFFFFFFFL, bytes.readInt48(i));
        // now negative
        bytes.position(0);
        for (long i = 0; i < 30; i += 6)
            bytes.writeInt48(i, ~i);
        bytes.position(30);
        for (long i = 30; i < 60; i += 6)
            bytes.writeInt48(~i);
        assertEquals(60, bytes.position());
        bytes.position(0);
        for (long i = 0; i < 30; i += 6)
            assertEquals("i: " + i, ~i << 16 >> 16, bytes.readInt48());
        for (long i = 30; i < 60; i += 6)
            assertEquals("i: " + i, ~i << 16 >> 16, bytes.readInt48(i));
    }

    @Test
    public void testDateTimes() {
        long now = System.currentTimeMillis();
        bytes.appendDateTimeMillis(now);
        bytes.append(' ');
        bytes.appendDateMillis(now);
        bytes.append('T');
        bytes.appendTimeMillis(now % 86400000L);
        assertEquals(23 * 2 + 1, bytes.position());
        bytes.position(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String asStr = sdf.format(new Date(now));
        assertEquals(asStr, bytes.parseUTF(SPACE_STOP));
        assertEquals(asStr, bytes.parseUTF(SPACE_STOP));
    }

    @Test
    public void testReadWriteCompactUnsignedShort() {
        for (int i = 0; i < 64; i += 4)
            bytes.writeCompactUnsignedShort(i);
        assertEquals(64 / 4, bytes.position());
        bytes.position(0);
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, bytes.readByte(i / 4));
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, bytes.readCompactUnsignedShort());
    }

    @Test
    public void testReadWriteCompactUnsignedInt() {
        for (int i = 0; i < 64; i += 4)
            bytes.writeCompactUnsignedInt(i);
        assertEquals(64 / 2, bytes.position());
        bytes.position(0);
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, bytes.readShort(i / 2));
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, bytes.readCompactUnsignedInt());
    }

    @Test
    public void testReadWriteLong() {
        for (long i = 0; i < 32; i += 8)
            bytes.writeLong(i, i);
        bytes.position(32);
        for (long i = 32; i < 64; i += 8)
            bytes.writeLong(i);
        bytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, bytes.readLong());
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, bytes.readLong(i));
    }

    @Test
    public void testReadWriteThreadSafeLong() {
        for (long i = 0; i < 32; i += 8)
            bytes.writeOrderedLong(i, i);
        bytes.position(32);
        for (long i = 32; i < 64; i += 8)
            bytes.writeOrderedLong(i);
        bytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, bytes.readVolatileLong());
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, bytes.readVolatileLong(i));
    }

    @Test
    public void testReadWriteDouble() {
        for (long i = 0; i < 32; i += 8)
            bytes.writeDouble(i, i);
        bytes.position(32);
        for (long i = 32; i < 64; i += 8)
            bytes.writeDouble(i);
        bytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, bytes.readDouble(), 0);
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, bytes.readDouble(i), 0);
    }

    @Test
    public void testAppendSubstring() {
        bytes.append("Hello World", 2, 7).append("\n");
        bytes.position(0);
        assertEquals("Hello World".substring(2, 7), bytes.parseUTF(CONTROL_STOP));
    }

    @Test
    public void testWriteReadEnum() {
        bytes.append(BuySell.Buy).append("\t").append(BuySell.Sell);
        bytes.position(0);
        assertEquals(BuySell.Buy, bytes.parseEnum(BuySell.class, CONTROL_STOP));
        assertEquals(BuySell.Sell, bytes.parseEnum(BuySell.class, CONTROL_STOP));
        assertEquals(null, bytes.parseEnum(BuySell.class, CONTROL_STOP));
    }

    @Test
    public void testAppendParse() {
        bytes.append(false).append(' ');
        bytes.append(true).append(' ');
        bytes.append("what?").append(' ');
        bytes.append("word£€").append(' ');
        bytes.append(BuySell.Buy).append(' ');
        bytes.append(1234).append(' ');
        bytes.append(123456L).append(' ');
        bytes.append(1.2345).append(' ');

        bytes.append(1.5555, 3).append(' ');
        bytes.position(0);
        assertEquals(false, bytes.parseBoolean(SPACE_STOP));
        assertEquals(true, bytes.parseBoolean(SPACE_STOP));
        assertEquals(null, bytes.parseBoolean(SPACE_STOP));
        assertEquals("word£€", bytes.parseUTF(SPACE_STOP));
        assertEquals(BuySell.Buy, bytes.parseEnum(BuySell.class, SPACE_STOP));
        assertEquals(1234, bytes.parseLong());
        assertEquals(123456L, bytes.parseLong());
        assertEquals(1.2345, bytes.parseDouble(), 0);
        assertEquals(1.556, bytes.parseDouble(), 0);
    }

    @Test
    public void testSelfTerminating() {
        bytes.limit(0);
        bytes.selfTerminating(true);
        assertEquals(null, bytes.parseBoolean(ALL));
        assertEquals(0L, bytes.parseLong());
        assertEquals(0.0, bytes.parseDouble(), 0.0);
        assertEquals("", bytes.parseUTF(ALL));
        assertEquals(null, bytes.parseEnum(StopCharTesters.class, ALL));

        bytes.selfTerminating(false);
        try {
            fail("got " + bytes.parseBoolean(ALL));
        } catch (BufferUnderflowException ignored) {
        }
        try {
            fail("got " + bytes.parseLong());
        } catch (BufferUnderflowException ignored) {
        }
        try {
            fail("got " + bytes.parseDouble());
        } catch (BufferUnderflowException ignored) {
        }
        try {
            fail("got " + bytes.parseUTF(ALL));
        } catch (BufferUnderflowException ignored) {
        }
        try {
            fail("got " + bytes.parseEnum(StopCharTesters.class, ALL));
        } catch (BufferUnderflowException ignored) {
        }
    }

    @Test
    public void testWriteByteChar() throws UnsupportedEncodingException {
        bytes.writeBytes("Hello \u00ff\u01fe\u02fc\n");
        bytes.writeChars("Hello \u00ff\u01fe\u02fc\n");
        byte[] bytes = new byte[(int) this.bytes.position()];
        this.bytes.position(0);
        this.bytes.readFully(bytes);
        assertEquals("Hello \u00ff\u00fe\u00fc\n" +
                "H\u0000e\u0000l\u0000l\u0000o\u0000 \u0000ÿ\u0000þ\u0001ü\u0002\n" +
                "\u0000", new String(bytes, "ISO-8859-1"));
    }

    @Test
    public void testWriteBytes() {
        bytes.write("Hello World\n".getBytes(), 0, 10);
        bytes.write("good bye\n".getBytes(), 4, 4);
        bytes.write(4, "0 w".getBytes());
        bytes.position(0);
        assertEquals("Hell0 worl bye", bytes.parseUTF(CONTROL_STOP));
    }

    @Test
    public void testAppendIterable() {
        bytes.append(Arrays.asList(1, 2, 3, 4, 5), ";").append(' ');
        bytes.append(new TreeSet<Integer>(Arrays.asList(21, 2, 13, 4, 5)), ";");
        bytes.position(0);
        assertEquals("1;2;3;4;5 2;4;5;13;21", bytes.parseUTF(CONTROL_STOP));
    }

    @Test
    public void readWriteMutableDecimal() {
        Random rand = new Random(2);
        MutableDecimal md = new MutableDecimal();
        MutableDecimal md2 = new MutableDecimal();
//        md.set(1260042744, 0);

        for (int i = 0; i < 20000; i++) {
            int n = rand.nextInt();
            for (int j = 0; j < 6; j++) {
                testDecimal0(md, md2, n, j);
            }
        }
    }

    private void testDecimal0(MutableDecimal md, MutableDecimal md2, int n, int j) {
        md.set(n, j);
        bytes.position(0);
        bytes.append(md).append('\n');
        bytes.position(0);
        bytes.parseDecimal(md2);
        bytes.position(0);
        String text = bytes.parseUTF(CONTROL_STOP);
        if (!md.equals(md2))
            assertEquals("n: " + n + ", s: " + j + " t: " + text, md, md2);
    }

    @Test
    public void testStream() throws IOException {
        bytes = new ByteBufferBytes(ByteBuffer.allocate(1000));
        GZIPOutputStream out = new GZIPOutputStream(bytes.outputStream());
        out.write("Hello world\n".getBytes());
        out.close();
        bytes.position(0);
        GZIPInputStream in = new GZIPInputStream(bytes.inputStream());
        byte[] bytes = new byte[12];
        for (int i = 0; i < 12; i++)
            bytes[i] = (byte) in.read();
        assertEquals(-1, in.read());
        assertEquals("Hello world\n", new String(bytes));
        in.close();
    }

    @Test
    public void testStream2() throws IOException {
        OutputStream out = bytes.outputStream();
        out.write(11);
        out.write(22);
        out.write(33);
        out.write(44);
        out.write(55);

        bytes.position(0);
        InputStream in = bytes.inputStream();
        assertTrue(in.markSupported());
        assertEquals(11, in.read());
        in.mark(1);
        assertEquals(1, bytes.position());
        assertEquals(22, in.read());
        assertEquals(2, bytes.position());

        assertEquals(33, in.read());
        in.reset();

        assertEquals(1, bytes.position());
        assertEquals(22, in.read());

        assertEquals(2, in.skip(2));
        assertEquals(4, bytes.position());
        assertEquals(SIZE - 4, bytes.available());
        assertEquals(55, in.read());
        in.close();
    }

    @Test
    public void testWriteObject() {
        for (Object o : new Object[]{BigDecimal.valueOf(-1.234), 10, 9.9, "string", new Date(), BigDecimal.valueOf(1.1)}) {
            bytes.position(0);
            bytes.writeObject(o);
//            System.out.println(o +" size: "+bytes.position());
            assertTrue(bytes.position() < 21);
            bytes.position(0);
            Object o2 = bytes.readObject();
            bytes.position(0);
            Object o3 = bytes.readObject(o.getClass());
            assertEquals(o, o2);
            assertEquals(o, o3);
        }
    }

    @Test
    public void testWriteSerializable() {
        int capacity = 16 * 1024;
        byteBuffer = ByteBuffer.allocateDirect(capacity);
        bytes = new ByteBufferBytes(byteBuffer);
        Calendar cal = Calendar.getInstance();
        bytes.writeObject(cal);
        Dummy d = new Dummy();
        bytes.writeObject(d);
        bytes.position(0);
        Calendar cal2 = bytes.readObject(Calendar.class);
        Dummy d2 = bytes.readObject(Dummy.class);
        assertEquals(cal, cal2);
        assertEquals(d, d2);
    }

    @Test
    public void testAddAndGet() {
        for (int i = 0; i < 10; i++)
            bytes.addAndGetInt(0L, 10);
        assertEquals(100, bytes.readInt(0L));
        assertEquals(0, bytes.readInt(4L));

        for (int i = 0; i < 11; i++)
            bytes.getAndAdd(4L, 11);
        assertEquals(100, bytes.readInt(0L));
        assertEquals(11 * 11, bytes.readInt(4L));
    }

    enum BuySell {
        Buy, Sell
    }

    static class Dummy implements Serializable {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Dummy;
        }
    }

    @Test
    public void testErrors() {
        int capacity = 1024;
        byteBuffer = ByteBuffer.allocate(capacity);
        // it is actually much bigger than it believes
        bytes = new ByteBufferBytes(byteBuffer, 0, 16);
        bytes.writeLong(8);
        assertFalse(bytes.isFinished());
        bytes.finish();
        assertTrue(bytes.isFinished());
        bytes.flush();
        bytes.writeLong(16);
        bytes.finish();
        bytes.flush();
        try {
            bytes.writeLong(24);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        bytes.finish();
        bytes.flush();

        bytes.clear();
        assertEquals(0, bytes.position());
        assertEquals(8, bytes.skip(8));
        assertEquals(8, bytes.position());
        bytes.writeLong(22);
        bytes.close();
    }

    @Test
    public void testWriteList() {
        List<Integer> ints = Arrays.asList(1, 2, 3, 4);
        bytes.writeList(ints);
        bytes.clear();
        List<Integer> ints2 = new ArrayList<Integer>();
        bytes.readList(ints2, Integer.class);
        assertEquals(ints, ints2);

        bytes.clear();
        List<String> words = Arrays.asList("Hello word byte for now".split(" "));
        bytes.writeList(words);
        bytes.clear();
        List<String> words2 = new ArrayList<String>();
        bytes.readList(words2, String.class);
    }

    @Test
    public void testWriteMap() {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>() {
            {
                put("one", 1);
                put("two", 2);
                put("three", 3);
                put("four", 4);
            }
        };

        bytes.writeMap(map);
        bytes.finish();

        bytes.clear();
        Map<String, Integer> map2 = new LinkedHashMap<String, Integer>();
        bytes.readMap(map2, String.class, Integer.class);
        assertEquals(map, map2);
    }

    @Test
    public void unloadFailed() throws InterruptedException {
        bytes.busyLockInt(0);
        ExecutorService es = Executors.newSingleThreadExecutor(new NamedThreadFactory("unloadFailed"));
        Future<Void> future = es.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                bytes.unlockInt(0);
                return null;
            }
        });
        es.shutdown();
        try {
            future.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(IllegalMonitorStateException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testToString() {
        NativeBytes bytes = new DirectStore(32).bytes();
        assertEquals("[pos: 0, lim: 32, cap: 32 ] ٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(1);
        assertEquals("[pos: 1, lim: 32, cap: 32 ] ⒈‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(2);
        assertEquals("[pos: 2, lim: 32, cap: 32 ] ⒈⒉‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(3);
        assertEquals("[pos: 3, lim: 32, cap: 32 ] ⒈⒉⒊‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(4);
        assertEquals("[pos: 4, lim: 32, cap: 32 ] ⒈⒉⒊⒋‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(5);
        assertEquals("[pos: 5, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(6);
        assertEquals("[pos: 6, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(7);
        assertEquals("[pos: 7, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍⒎‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeByte(8);
        assertEquals("[pos: 8, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍⒎⒏‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
    }
}
