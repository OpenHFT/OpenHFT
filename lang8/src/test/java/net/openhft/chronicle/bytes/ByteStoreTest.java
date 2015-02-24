/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.bytes;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.openhft.chronicle.bytes.StopCharTesters.CONTROL_STOP;
import static net.openhft.chronicle.bytes.StopCharTesters.SPACE_STOP;
import static org.junit.Assert.*;

/**
 * User: peter.lawrey
 */
public class ByteStoreTest {
    public static final int SIZE = 128;
    private Bytes bytes;
    private ByteBuffer byteBuffer;

    @Before
    public void beforeTest() {
        byteBuffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        bytes = BytesStore.wrap(byteBuffer).bytes();
    }

    @Test
    public void testCAS() {
        Bytes bytes = BytesStore.wrap(ByteBuffer.allocate(100)).bytes();
        bytes.compareAndSwapLong(0, 0L, 1L);
        assertEquals(1L, bytes.readLong(0));
    }

    @Test
    public void testRead() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.writeByte(i, i);
        bytes.position(0);
        for (int i = 0; i < bytes.capacity(); i++)
            assertEquals((byte) i, bytes.readByte());
        for (int i = (int) (bytes.capacity() - 1); i >= 0; i--) {
            assertEquals((byte) i, bytes.readByte(i));
        }
    }

    @Test
    public void testReadFully() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.writeByte((byte) i);
        bytes.position(0);
        byte[] bytes = new byte[(int) this.bytes.capacity()];
        this.bytes.read(bytes);
        for (int i = 0; i < this.bytes.capacity(); i++)
            assertEquals((byte) i, bytes[i]);
    }

    @Test
    public void testCompareAndSetLong() throws Exception {
        assertTrue(bytes.compareAndSwapLong(0L, 0L, 1L));
        assertFalse(bytes.compareAndSwapLong(0L, 0L, 1L));
        assertTrue(bytes.compareAndSwapLong(8L, 0L, 1L));
        assertTrue(bytes.compareAndSwapLong(0L, 1L, 2L));
    }

    @Test
    public void testPosition() throws Exception {
        for (int i = 0; i < bytes.capacity(); i++)
            bytes.writeByte((byte) i);
        for (int i = (int) (bytes.capacity() - 1); i >= 0; i--) {
            bytes.position(i);
            assertEquals((byte) i, bytes.readByte());
        }
    }

    @Test
    public void testCapacity() throws Exception {
        assertEquals(SIZE, bytes.capacity());
        assertEquals(10, NativeStore.nativeStore(10).capacity());
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

/*        bytes.selfTerminating(true);
        bytes.clear();
        bytes.append(d);
        bytes.flip();
        double d3 = bytes.parseDouble();
        assertEquals(d, d3, 0);

        bytes.selfTerminating(false);*/
        bytes.clear();
        bytes.append(d);
        bytes.flip();
        try {
            fail("got " + bytes.parseDouble());
        } catch (BufferUnderflowException expected) {
            // expected
        }
    }

/*    @Test
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
    }*/

    @Test
    public void testWriteReadUTFΔ() {
        bytes.writeUTFΔ(null);
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            bytes.writeUTFΔ(word);
        }
        bytes.position(0);
        assertEquals(null, bytes.readUTFΔ());
        for (String word : words) {
            assertEquals(word, bytes.readUTFΔ());
        }
        assertEquals("", bytes.readUTFΔ());
        assertEquals(26, bytes.position()); // check the size

        bytes.position(0);
        StringBuilder sb = new StringBuilder();
        assertFalse(bytes.readUTFΔ(sb));
        for (String word : words) {
            assertTrue(bytes.readUTFΔ(sb));
            assertEquals(word, sb.toString());
        }
        assertTrue(bytes.readUTFΔ(sb));
        assertEquals("", sb.toString());
    }

    @Test
    public void testWriteReadUTF() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            bytes.writeUTFΔ(word);
        }
        bytes.writeUTFΔ("");
        assertEquals(24, bytes.position()); // check the size, more bytes for less strings than writeUTFΔ
        bytes.position(0);
        for (String word : words) {
            assertEquals(word, bytes.readUTFΔ());
        }
        assertEquals("", bytes.readUTFΔ());
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

    }


    @Test
    public void testWriteReadByteBuffer() {
        byte[] bytes = "Hello\nWorld!\r\nBye".getBytes();
        this.bytes.write(ByteBuffer.wrap(bytes));
        this.bytes.flip();
        byte[] bytes2 = new byte[bytes.length + 1];
        ByteBuffer bb2 = ByteBuffer.wrap(bytes2);
        this.bytes.read(bb2);

        assertEquals(bytes.length, bb2.position());
        byte[] bytes2b = Arrays.copyOf(bytes2, bytes.length);
        assertTrue(Arrays.equals(bytes, bytes2b));
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
            bytes.writeShort(i, (short) i);
        bytes.position(32);
        for (int i = 32; i < 64; i += 2)
            bytes.writeShort((short) i);
        bytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(i, bytes.readShort());
        for (int i = 32; i < 64; i += 2)
            assertEquals(i, bytes.readShort(i));
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
    public void testReadWriteUnsignedShort() {
        for (int i = 0; i < 32; i += 2)
            bytes.writeUnsignedShort(i, (~i) & 0xFFFF);
        bytes.position(32);
        for (int i = 32; i < 64; i += 2)
            bytes.writeUnsignedShort(~i & 0xFFFF);
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
            bytes.writeUnsignedInt(i, ~i & 0xFFFF);
        bytes.position(32);
        for (int i = 32; i < 64; i += 4)
            bytes.writeUnsignedInt(~i & 0xFFFF);
        bytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(~i & 0xFFFFL, bytes.readUnsignedInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(~i & 0xFFFFL, bytes.readUnsignedInt(i));
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
//        System.out.println(bytes.bytes().toDebugString());
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
    public void testAppendParse() {
        bytes.append("word£€)").append(' ');
        bytes.append(1234).append(' ');
        bytes.append(123456L).append(' ');
        bytes.append(1.2345).append(' ');

        bytes.position(0);
        assertEquals("word£€)", bytes.parseUTF(SPACE_STOP));
        assertEquals(1234, bytes.parseLong());
        assertEquals(123456L, bytes.parseLong());
        assertEquals(1.2345, bytes.parseDouble(), 0);
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
    @Ignore
    public void testStream() throws IOException {
        bytes = BytesStore.wrap(ByteBuffer.allocate(1000)).bytes();
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
    @Ignore
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
        assertEquals(SIZE - 4, bytes.remaining());
        assertEquals(55, in.read());
        in.close();
    }

    @Test
    public void testAddAndGet() {
        for (int i = 0; i < 10; i++)
            bytes.addAndGetInt(0L, 10);
        assertEquals(100, bytes.readInt(0L));
        assertEquals(0, bytes.readInt(4L));

        for (int i = 0; i < 11; i++)
            bytes.getAndAddInt(4L, 11);
        assertEquals(100, bytes.readInt(0L));
        assertEquals(11 * 11, bytes.readInt(4L));
    }

    @Test
    public void testToString() {
        Bytes bytes = NativeStore.nativeStore(32).bytes();
        assertEquals("[pos: 0, lim: 32, cap: 32 ] ٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(1);
        assertEquals("[pos: 1, lim: 32, cap: 32 ] ⒈‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(2);
        assertEquals("[pos: 2, lim: 32, cap: 32 ] ⒈⒉‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(3);
        assertEquals("[pos: 3, lim: 32, cap: 32 ] ⒈⒉⒊‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(4);
        assertEquals("[pos: 4, lim: 32, cap: 32 ] ⒈⒉⒊⒋‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(5);
        assertEquals("[pos: 5, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(6);
        assertEquals("[pos: 6, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(7);
        assertEquals("[pos: 7, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍⒎‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
        bytes.writeUnsignedByte(8);
        assertEquals("[pos: 8, lim: 32, cap: 32 ] ⒈⒉⒊⒋⒌⒍⒎⒏‖٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠٠", bytes.toDebugString());
    }
}
