package net.openhft.lang.io;

import org.junit.Before;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

import static net.openhft.lang.io.StopCharTesters.CONTROL_STOP;
import static net.openhft.lang.io.StopCharTesters.SPACE_STOP;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA. User: peter Date: 17/09/13 Time: 16:09 To change this template use File | Settings | File
 * Templates.
 */
public class NativeBytesTest {
    private NativeBytes nativeBytes;
    private ByteBuffer byteBuffer;

    @Before
    public void beforeTest() {
        byteBuffer = ByteBuffer.allocateDirect(64);
        long addr = ((DirectBuffer) byteBuffer).address();
        nativeBytes = new NativeBytes(addr, addr, addr + 64);
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
        assertEquals(64, nativeBytes.capacity());
        assertEquals(10, new NativeBytes(0, 0, 10).capacity());
    }

    @Test
    public void testRemaining() throws Exception {
        assertEquals(64, nativeBytes.remaining());
        nativeBytes.position(10);
        assertEquals(64 - 10, nativeBytes.remaining());
    }

    @Test
    public void testByteOrder() throws Exception {
        assertEquals(ByteOrder.nativeOrder(), nativeBytes.byteOrder());
    }

    @Test
    public void testCheckEndOfBuffer() throws Exception {
        nativeBytes.checkEndOfBuffer();

        nativeBytes.position(66);
        try {
            nativeBytes.checkEndOfBuffer();
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void appendDouble() {
        Random random = new Random(1);
        for (int i = 0; i < 100000; i++) {
            double d = Math.pow(1e12, random.nextDouble());
            if (i % 3 == 0) d = -d;
            nativeBytes.position(0);
            nativeBytes.append(d).append(' ');
            nativeBytes.position(0);
            double d2 = nativeBytes.parseDouble();
            assertEquals(d2, d, 0);
        }
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

    enum BuySell {
        Buy, Sell
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


}
