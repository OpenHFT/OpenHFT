package net.openhft.lang.io;

import net.openhft.lang.Maths;
import org.junit.Before;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static net.openhft.lang.io.StopCharTesters.CONTROL_STOP;
import static net.openhft.lang.io.StopCharTesters.SPACE_STOP;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA. User: peter Date: 17/09/13 Time: 16:09 To change this template use File | Settings | File
 * Templates.
 */
public class NativeBytesTest {
    public static final int SIZE = 128;
    private NativeBytes nativeBytes;
    private ByteBuffer byteBuffer;

    @Before
    public void beforeTest() {
        byteBuffer = ByteBuffer.allocateDirect(SIZE);
        long addr = ((DirectBuffer) byteBuffer).address();
        nativeBytes = new NativeBytes(addr, addr, addr + SIZE);
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
    public void testRead() throws Exception {
        for (int i = 0; i < nativeBytes.capacity(); i++)
            nativeBytes.writeByte(i, i);
        nativeBytes.position(0);
        for (int i = 0; i < nativeBytes.capacity(); i++)
            assertEquals((byte) i, nativeBytes.read());
        for (int i = (int) (nativeBytes.capacity() - 1); i >= 0; i--) {
            assertEquals((byte) i, nativeBytes.readByte(i));
        }

    }

    @Test
    public void testReadFully() throws Exception {
        for (int i = 0; i < nativeBytes.capacity(); i++)
            nativeBytes.write(i);
        nativeBytes.position(0);
        byte[] bytes = new byte[(int) nativeBytes.capacity()];
        nativeBytes.readFully(bytes);
        for (int i = 0; i < nativeBytes.capacity(); i++)
            assertEquals((byte) i, bytes[i]);
    }

    @Test
    public void testCompareAndSetLong() throws Exception {
        assertTrue(nativeBytes.compareAndSetLong(0, 0, 1));
        assertFalse(nativeBytes.compareAndSetLong(0, 0, 1));
        assertTrue(nativeBytes.compareAndSetLong(8, 0, 1));
        assertTrue(nativeBytes.compareAndSetLong(0, 1, 2));

    }

    @Test
    public void testPosition() throws Exception {
        for (int i = 0; i < nativeBytes.capacity(); i++)
            nativeBytes.write(i);
        for (int i = (int) (nativeBytes.capacity() - 1); i >= 0; i--) {
            nativeBytes.position(i);
            assertEquals((byte) i, nativeBytes.read());
        }
    }

    @Test
    public void testCapacity() throws Exception {
        assertEquals(SIZE, nativeBytes.capacity());
        assertEquals(10, new NativeBytes(0, 0, 10).capacity());
    }

    @Test
    public void testRemaining() throws Exception {
        assertEquals(SIZE, nativeBytes.remaining());
        nativeBytes.position(10);
        assertEquals(SIZE - 10, nativeBytes.remaining());
    }

    @Test
    public void testByteOrder() throws Exception {
        assertEquals(ByteOrder.nativeOrder(), nativeBytes.byteOrder());
    }

    @Test
    public void testCheckEndOfBuffer() throws Exception {
        nativeBytes.checkEndOfBuffer();

        nativeBytes.position(SIZE + 2);
        try {
            nativeBytes.checkEndOfBuffer();
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
        nativeBytes.position(0);
        nativeBytes.append(d).append(' ');
        nativeBytes.position(0);
        double d2 = nativeBytes.parseDouble();
        assertEquals(d, d2, 0);
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
        nativeBytes.position(0);
        nativeBytes.append(d, precision).append(' ');
        Thread.yield();
        nativeBytes.position(0);
        String text = nativeBytes.parseUTF(SPACE_STOP);
        nativeBytes.position(0);
        assertEquals(0, nativeBytes.position());
        double d2 = nativeBytes.parseDouble();
        double d3 = (double) Math.round(d * Maths.power10(precision)) / Maths.power10(precision);
//        if (precision >= 14)
//            assertEquals("'" + text + "' p: " + precision + " v: " + new BigDecimal(d), d3, d2, 5e-29 * Maths.power10(precision));
//        else
        assertEquals("'" + text + "' p: " + precision, d3, d2, 0);
    }

    @Test
    public void testWriteReadBytes() {
        byte[] bytes = "Hello World!".getBytes();
        nativeBytes.write(bytes);
        byte[] bytes2 = new byte[bytes.length];
        nativeBytes.position(0);
        nativeBytes.read(bytes2);
        assertTrue(Arrays.equals(bytes, bytes2));

        nativeBytes.write(22, bytes);
        byte[] bytes3 = new byte[bytes.length];
        nativeBytes.skipBytes((int) (22 - nativeBytes.position()));
        assertEquals(bytes3.length, nativeBytes.read(bytes3));
        assertTrue(Arrays.equals(bytes, bytes3));
        nativeBytes.position(nativeBytes.capacity());
        assertEquals(-1, nativeBytes.read(bytes3));
    }

    @Test
    public void testWriteReadUTFΔ() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            nativeBytes.writeUTFΔ(word);
        }
        nativeBytes.writeUTFΔ(null);
        nativeBytes.position(0);
        for (String word : words) {
            assertEquals(word, nativeBytes.readUTFΔ());
        }
        assertEquals(null, nativeBytes.readUTFΔ());
        assertEquals("", nativeBytes.readUTFΔ());
        assertEquals(26, nativeBytes.position()); // check the size

        nativeBytes.position(0);
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            assertTrue(nativeBytes.readUTFΔ(sb));
            assertEquals(word, sb.toString());
        }
        assertFalse(nativeBytes.readUTFΔ(sb));
        assertTrue(nativeBytes.readUTFΔ(sb));
        assertEquals("", sb.toString());
    }

    @Test
    public void testWriteReadUTF() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            nativeBytes.writeUTF(word);
        }
        nativeBytes.writeUTF("");
        assertEquals(28, nativeBytes.position()); // check the size, more bytes for less strings than writeUTFΔ
        nativeBytes.position(0);
        for (String word : words) {
            assertEquals(word, nativeBytes.readUTF());
        }
        assertEquals("", nativeBytes.readUTF());
    }

    @Test
    public void testAppendParseUTF() {
        String[] words = "Hello,World!,Bye£€!".split(",");
        for (String word : words) {
            nativeBytes.append(word).append('\t');
        }
        nativeBytes.append('\t');
        nativeBytes.position(0);
        for (String word : words) {
            assertEquals(word, nativeBytes.parseUTF(CONTROL_STOP));
        }
        assertEquals("", nativeBytes.parseUTF(CONTROL_STOP));

        nativeBytes.position(0);
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            nativeBytes.parseUTF(sb, CONTROL_STOP);
            assertEquals(word, sb.toString());
        }
        nativeBytes.parseUTF(sb, CONTROL_STOP);
        assertEquals("", sb.toString());

        nativeBytes.position(0);
        nativeBytes.skipTo(CONTROL_STOP);
        assertEquals(6, nativeBytes.position());
        nativeBytes.skipTo(CONTROL_STOP);
        assertEquals(13, nativeBytes.position());
        nativeBytes.skipTo(CONTROL_STOP);
        assertEquals(17, nativeBytes.position());
        nativeBytes.skipTo(CONTROL_STOP);
        assertEquals(18, nativeBytes.position());

        nativeBytes.position(0);
        nativeBytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(6, nativeBytes.position());
        nativeBytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(6, nativeBytes.position());
        nativeBytes.position(10);
        nativeBytes.stepBackAndSkipTo(CONTROL_STOP);
        assertEquals(13, nativeBytes.position());

    }

    @Test
    public void testWriteReadLines() {
        byte[] bytes = "Hello\nWorld!\r\nBye".getBytes();
        nativeBytes.write(bytes);
        nativeBytes.position(0);
        assertEquals("Hello", nativeBytes.readLine());
        assertEquals("World!", nativeBytes.readLine());
        assertTrue(nativeBytes.readLine().startsWith("Bye"));
    }

    @Test
    public void testWriteReadByteBuffer() {
        byte[] bytes = "Hello\nWorld!\r\nBye".getBytes();
        nativeBytes.write(ByteBuffer.wrap(bytes));
        nativeBytes.position(0);
        byte[] bytes2 = new byte[bytes.length + 1];
        ByteBuffer bb2 = ByteBuffer.wrap(bytes2);
        nativeBytes.read(bb2);

        assertEquals(bytes2.length, bb2.position());
        assertTrue(Arrays.equals(bytes, Arrays.copyOf(bytes2, bytes.length)));
    }

    @Test
    public void testReadWriteBoolean() {
        for (int i = 0; i < 32; i++)
            nativeBytes.writeBoolean(i, (i & 3) == 0);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i++)
            nativeBytes.writeBoolean((i & 5) == 0);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i++)
            assertEquals((i & 3) == 0, nativeBytes.readBoolean());
        for (int i = 32; i < 64; i++)
            assertEquals((i & 5) == 0, nativeBytes.readBoolean(i));
    }

    @Test
    public void testReadWriteShort() {
        for (int i = 0; i < 32; i += 2)
            nativeBytes.writeShort(i, i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 2)
            nativeBytes.writeShort(i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(i, nativeBytes.readShort());
        for (int i = 32; i < 64; i += 2)
            assertEquals(i, nativeBytes.readShort(i));
    }

    @Test
    public void testReadWriteCompactShort() {
        int[] ints = {Short.MIN_VALUE, Short.MAX_VALUE, -125, 0, 127, -10000, 10000};
        for (int i : ints) {
            nativeBytes.writeCompactShort(i);
//            System.out.println(i + " " + nativeBytes.position());
        }
        assertEquals(5 + 2 * 3, nativeBytes.position());

        nativeBytes.position(0);
        for (int i : ints)
            assertEquals(i, nativeBytes.readCompactShort());
    }

    @Test
    public void testReadWriteCompactInt() {
        int[] ints = {-10000000, Integer.MIN_VALUE, Integer.MAX_VALUE, Short.MIN_VALUE + 3, 0, Short.MAX_VALUE, 10000000};
        for (int i : ints) {
            nativeBytes.writeCompactInt(i);
//            System.out.println(i + " " + nativeBytes.position());
        }
        assertEquals(5 * 2 + 2 * 6, nativeBytes.position());

        nativeBytes.position(0);
        for (int i : ints)
            assertEquals(i, nativeBytes.readCompactInt());
    }

    @Test
    public void testReadWriteCompactLong() {
        long[] ints = {Long.MAX_VALUE, -100000000000L, Long.MIN_VALUE, Integer.MIN_VALUE + 3, 0, Integer.MAX_VALUE, 100000000000L};
        for (long i : ints) {
            nativeBytes.writeCompactLong(i);
//            System.out.println(i + " " + nativeBytes.position());
        }
        assertEquals(5 * 4 + 2 * 12, nativeBytes.position());

        nativeBytes.position(0);
        for (long i : ints)
            assertEquals(i, nativeBytes.readCompactLong());
    }

    @Test
    public void testReadWriteCompactDouble() {
        double[] doubles = {1, 1000, 1000000, -100000000, 0.1f, 0.1, 0.5, 0.51};
        for (double i : doubles) {
            nativeBytes.writeCompactDouble(i);
//            System.out.println(i + " " + nativeBytes.position());
        }
        assertEquals(6 * 4 + 2 * 12, nativeBytes.position());

        nativeBytes.position(0);
        for (double i : doubles)
            assertEquals(i, nativeBytes.readCompactDouble(), 0.0);
    }

    @Test
    public void testReadWriteStop() {
        long[] longs = {Long.MIN_VALUE, Long.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (long i : longs) {
            nativeBytes.writeStopBit(i);
//            System.out.println(i + " " + nativeBytes.position());
        }
        assertEquals(9 + 10, +5 + 6, nativeBytes.position());

        nativeBytes.position(0);
        for (long i : longs)
            assertEquals(i, nativeBytes.readStopBit());
    }

    @Test
    public void testReadWriteChar() {
        for (int i = 0; i < 32; i += 2)
            nativeBytes.writeChar(i, i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 2)
            nativeBytes.writeChar(i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(i, nativeBytes.readChar());
        for (int i = 32; i < 64; i += 2)
            assertEquals(i, nativeBytes.readChar(i));
    }

    @Test
    public void testReadWriteUnsignedShort() {
        for (int i = 0; i < 32; i += 2)
            nativeBytes.writeUnsignedShort(i, ~i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 2)
            nativeBytes.writeUnsignedShort(~i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 2)
            assertEquals(~i & 0xFFFF, nativeBytes.readUnsignedShort());
        for (int i = 32; i < 64; i += 2)
            assertEquals(~i & 0xFFFF, nativeBytes.readUnsignedShort(i));
    }

    @Test
    public void testReadWriteInt() {
        for (int i = 0; i < 32; i += 4)
            nativeBytes.writeInt(i, i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 4)
            nativeBytes.writeInt(i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, nativeBytes.readInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, nativeBytes.readInt(i));
    }

    @Test
    public void testReadWriteThreadeSafeInt() {
        for (int i = 0; i < 32; i += 4)
            nativeBytes.writeOrderedInt(i, i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 4)
            nativeBytes.writeOrderedInt(i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, nativeBytes.readVolatileInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, nativeBytes.readVolatileInt(i));
    }

    @Test
    public void testReadWriteFloat() {
        for (int i = 0; i < 32; i += 4)
            nativeBytes.writeFloat(i, i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 4)
            nativeBytes.writeFloat(i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(i, nativeBytes.readFloat(), 0);
        for (int i = 32; i < 64; i += 4)
            assertEquals(i, nativeBytes.readFloat(i), 0);
    }

    @Test
    public void testReadWriteUnsignedInt() {
        for (int i = 0; i < 32; i += 4)
            nativeBytes.writeUnsignedInt(i, ~i);
        nativeBytes.position(32);
        for (int i = 32; i < 64; i += 4)
            nativeBytes.writeUnsignedInt(~i);
        nativeBytes.position(0);
        for (int i = 0; i < 32; i += 4)
            assertEquals(~i & 0xFFFFFFFFL, nativeBytes.readUnsignedInt());
        for (int i = 32; i < 64; i += 4)
            assertEquals(~i & 0xFFFFFFFFL, nativeBytes.readUnsignedInt(i));
    }

    @Test
    public void testReadWriteInt24() {
        for (int i = 0; i < 30; i += 3)
            nativeBytes.writeInt24(i, ~i & 0x7FFFFF);
        nativeBytes.position(30);
        for (int i = 30; i < 63; i += 3)
            nativeBytes.writeInt24(~i & 0x7FFFFF);
        assertEquals(63, nativeBytes.position());
        nativeBytes.position(0);
        for (int i = 0; i < 30; i += 3)
            assertEquals("i: " + i, ~i & 0x7FFFFFL, nativeBytes.readInt24());
        for (int i = 30; i < 63; i += 3)
            assertEquals("i: " + i, ~i & 0x7FFFFFL, nativeBytes.readInt24(i));
        // now negative
        nativeBytes.position(0);
        for (int i = 0; i < 30; i += 3)
            nativeBytes.writeInt24(i, ~i);
        nativeBytes.position(30);
        for (int i = 30; i < 63; i += 3)
            nativeBytes.writeInt24(~i);
        assertEquals(63, nativeBytes.position());
        nativeBytes.position(0);
        for (int i = 0; i < 30; i += 3)
            assertEquals("i: " + i, ~i << 8 >> 8, nativeBytes.readInt24());
        for (int i = 30; i < 63; i += 3)
            assertEquals("i: " + i, ~i << 8 >> 8, nativeBytes.readInt24(i));
    }

    @Test
    public void testReadWriteInt48() {
        for (long i = 0; i < 30; i += 6)
            nativeBytes.writeInt48(i, ~i & 0x7FFFFFFFFFFFL);
        nativeBytes.position(30);
        for (long i = 30; i < 60; i += 6)
            nativeBytes.writeInt48(~i & 0x7FFFFFFFFFFFL);
        assertEquals(60, nativeBytes.position());
        nativeBytes.position(0);
        for (long i = 0; i < 30; i += 6)
            assertEquals("i: " + i, ~i & 0x7FFFFFFFFFFFL, nativeBytes.readInt48());
        for (long i = 30; i < 60; i += 6)
            assertEquals("i: " + i, ~i & 0x7FFFFFFFFFFFL, nativeBytes.readInt48(i));
        // now negative
        nativeBytes.position(0);
        for (long i = 0; i < 30; i += 6)
            nativeBytes.writeInt48(i, ~i);
        nativeBytes.position(30);
        for (long i = 30; i < 60; i += 6)
            nativeBytes.writeInt48(~i);
        assertEquals(60, nativeBytes.position());
        nativeBytes.position(0);
        for (long i = 0; i < 30; i += 6)
            assertEquals("i: " + i, ~i << 16 >> 16, nativeBytes.readInt48());
        for (long i = 30; i < 60; i += 6)
            assertEquals("i: " + i, ~i << 16 >> 16, nativeBytes.readInt48(i));
    }

    @Test
    public void testDateTimes() {
        long now = System.currentTimeMillis();
        nativeBytes.appendDateTimeMillis(now);
        nativeBytes.append(' ');
        nativeBytes.appendDateMillis(now);
        nativeBytes.append('T');
        nativeBytes.appendTimeMillis(now % 86400000L);
        assertEquals(23 * 2 + 1, nativeBytes.position());
        nativeBytes.position(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String asStr = sdf.format(new Date(now));
        assertEquals(asStr, nativeBytes.parseUTF(SPACE_STOP));
        assertEquals(asStr, nativeBytes.parseUTF(SPACE_STOP));
    }

    @Test
    public void testReadWriteCompactUnsignedShort() {
        for (int i = 0; i < 64; i += 4)
            nativeBytes.writeCompactUnsignedShort(i);
        assertEquals(64 / 4, nativeBytes.position());
        nativeBytes.position(0);
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, nativeBytes.readByte(i / 4));
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, nativeBytes.readCompactUnsignedShort());
    }

    @Test
    public void testReadWriteCompactUnsignedInt() {
        for (int i = 0; i < 64; i += 4)
            nativeBytes.writeCompactUnsignedInt(i);
        assertEquals(64 / 2, nativeBytes.position());
        nativeBytes.position(0);
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, nativeBytes.readShort(i / 2));
        for (int i = 0; i < 64; i += 4)
            assertEquals(i, nativeBytes.readCompactUnsignedInt());
    }

    @Test
    public void testReadWriteLong() {
        for (long i = 0; i < 32; i += 8)
            nativeBytes.writeLong(i, i);
        nativeBytes.position(32);
        for (long i = 32; i < 64; i += 8)
            nativeBytes.writeLong(i);
        nativeBytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, nativeBytes.readLong());
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, nativeBytes.readLong(i));
    }

    @Test
    public void testReadWriteThreadSafeLong() {
        for (long i = 0; i < 32; i += 8)
            nativeBytes.writeOrderedLong(i, i);
        nativeBytes.position(32);
        for (long i = 32; i < 64; i += 8)
            nativeBytes.writeOrderedLong(i);
        nativeBytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, nativeBytes.readVolatileLong());
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, nativeBytes.readVolatileLong(i));
    }

    @Test
    public void testReadWriteDouble() {
        for (long i = 0; i < 32; i += 8)
            nativeBytes.writeDouble(i, i);
        nativeBytes.position(32);
        for (long i = 32; i < 64; i += 8)
            nativeBytes.writeDouble(i);
        nativeBytes.position(0);
        for (long i = 0; i < 32; i += 8)
            assertEquals(i, nativeBytes.readDouble(), 0);
        for (long i = 32; i < 64; i += 8)
            assertEquals(i, nativeBytes.readDouble(i), 0);
    }

    @Test
    public void testAppendSubstring() {
        nativeBytes.append("Hello World", 2, 7).append("\n");
        nativeBytes.position(0);
        assertEquals("Hello World".substring(2, 7), nativeBytes.parseUTF(CONTROL_STOP));
    }

    @Test
    public void testWriteReadEnum() {
        nativeBytes.append(BuySell.Buy).append("\t").append(BuySell.Sell);
        nativeBytes.position(0);
        assertEquals(BuySell.Buy, nativeBytes.parseEnum(BuySell.class, CONTROL_STOP));
        assertEquals(BuySell.Sell, nativeBytes.parseEnum(BuySell.class, CONTROL_STOP));
        assertEquals(null, nativeBytes.parseEnum(BuySell.class, CONTROL_STOP));
    }

    @Test
    public void testAppendParse() {
        nativeBytes.append(false).append(' ');
        nativeBytes.append(true).append(' ');
        nativeBytes.append("what?").append(' ');
        nativeBytes.append("word£€").append(' ');
        nativeBytes.append(BuySell.Buy).append(' ');
        nativeBytes.append(1234).append(' ');
        nativeBytes.append(123456L).append(' ');
        nativeBytes.append(1.2345).append(' ');
        nativeBytes.append(1.5555, 3).append(' ');
        nativeBytes.position(0);
        assertEquals(false, nativeBytes.parseBoolean(SPACE_STOP));
        assertEquals(true, nativeBytes.parseBoolean(SPACE_STOP));
        assertEquals(null, nativeBytes.parseBoolean(SPACE_STOP));
        assertEquals("word£€", nativeBytes.parseUTF(SPACE_STOP));
        assertEquals(BuySell.Buy, nativeBytes.parseEnum(BuySell.class, SPACE_STOP));
        assertEquals(1234, nativeBytes.parseLong());
        assertEquals(123456L, nativeBytes.parseLong());
        assertEquals(1.2345, nativeBytes.parseDouble(), 0);
        assertEquals(1.556, nativeBytes.parseDouble(), 0);

    }

    @Test
    public void testWriteByteChar() throws UnsupportedEncodingException {
        nativeBytes.writeBytes("Hello \u00ff\u01fe\u02fc\n");
        nativeBytes.writeChars("Hello \u00ff\u01fe\u02fc\n");
        byte[] bytes = new byte[(int) nativeBytes.position()];
        nativeBytes.position(0);
        nativeBytes.readFully(bytes);
        assertEquals("Hello \u00ff\u00fe\u00fc\n" +
                "H\u0000e\u0000l\u0000l\u0000o\u0000 \u0000ÿ\u0000þ\u0001ü\u0002\n" +
                "\u0000", new String(bytes, "ISO-8859-1"));
    }

    @Test
    public void testWriteBytes() {
        nativeBytes.write("Hello World\n".getBytes(), 0, 10);
        nativeBytes.write("good bye\n".getBytes(), 4, 4);
        nativeBytes.write(4, "0 w".getBytes());
        nativeBytes.position(0);
        assertEquals("Hell0 worl bye", nativeBytes.parseUTF(CONTROL_STOP));
    }

    @Test
    public void testAppendIterable() {
        nativeBytes.append(Arrays.asList(1, 2, 3, 4, 5), ";").append(' ');
        nativeBytes.append(new TreeSet<Integer>(Arrays.asList(21, 2, 13, 4, 5)), ";");
        nativeBytes.position(0);
        assertEquals("1;2;3;4;5 2;4;5;13;21", nativeBytes.parseUTF(CONTROL_STOP));
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
        nativeBytes.position(0);
        nativeBytes.append(md).append('\n');
        nativeBytes.position(0);
        nativeBytes.parseDecimal(md2);
        nativeBytes.position(0);
        String text = nativeBytes.parseUTF(CONTROL_STOP);
        if (!md.equals(md2))
            assertEquals("n: " + n + ", s: " + j + " t: " + text, md, md2);
    }

    @Test
    public void testStream() throws IOException {
        GZIPOutputStream out = new GZIPOutputStream(nativeBytes.outputStream());
        out.write("Hello world\n".getBytes());
        out.close();
        nativeBytes.position(0);
        GZIPInputStream in = new GZIPInputStream(nativeBytes.inputStream());
        byte[] bytes = new byte[12];
        for (int i = 0; i < 12; i++)
            bytes[i] = (byte) in.read();
        assertEquals(-1, in.read());
        assertEquals("Hello world\n", new String(bytes));
        in.close();
    }

    @Test
    public void testStream2() throws IOException {
        OutputStream out = nativeBytes.outputStream();
        out.write(11);
        out.write(22);
        out.write(33);
        out.write(44);
        out.write(55);

        nativeBytes.position(0);
        InputStream in = nativeBytes.inputStream();
        assertTrue(in.markSupported());
        assertEquals(11, in.read());
        in.mark(1);
        assertEquals(1, nativeBytes.position());
        assertEquals(22, in.read());
        assertEquals(2, nativeBytes.position());

        assertEquals(33, in.read());
        in.reset();

        assertEquals(1, nativeBytes.position());
        assertEquals(22, in.read());

        assertEquals(2, in.skip(2));
        assertEquals(4, nativeBytes.position());
        assertEquals(SIZE - 4, nativeBytes.available());
        assertEquals(55, in.read());
        in.close();
    }

    @Test
    public void testWriteObject() {
        for (Object o : new Object[]{10, 9.9, "string", new Date(), BigDecimal.valueOf(1.1)}) {
            nativeBytes.position(0);
            nativeBytes.writeObject(o);
//            System.out.println(o +" size: "+nativeBytes.position());
            assertTrue(nativeBytes.position() < 21);
            nativeBytes.position(0);
            Object o2 = nativeBytes.readObject();
            nativeBytes.position(0);
            Object o3 = nativeBytes.readObject(o.getClass());
            assertEquals(o, o2);
            assertEquals(o, o3);
        }
    }

    @Test
    public void testWriteSerializable() {
        int capacity = 16 * 1024;
        byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        nativeBytes = new NativeBytes(addr, addr, addr + capacity);
        Calendar cal = Calendar.getInstance();
        nativeBytes.writeObject(cal);
        Dummy d = new Dummy();
        nativeBytes.writeObject(d);
        nativeBytes.position(0);
        Calendar cal2 = nativeBytes.readObject(Calendar.class);
        Dummy d2 = nativeBytes.readObject(Dummy.class);
        assertEquals(cal, cal2);
        assertEquals(d, d2);
    }

    @Test
    public void testAddAndGet() {
        for (int i = 0; i < 10; i++)
            nativeBytes.addAndGetInt(0L, 10);
        assertEquals(100, nativeBytes.readInt(0L));
        assertEquals(0, nativeBytes.readInt(4L));

        for (int i = 0; i < 11; i++)
            nativeBytes.getAndAdd(4L, 11);
        assertEquals(100, nativeBytes.readInt(0L));
        assertEquals(11 * 11, nativeBytes.readInt(4L));
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
}
