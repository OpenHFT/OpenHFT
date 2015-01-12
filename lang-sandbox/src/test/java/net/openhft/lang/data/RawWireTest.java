package net.openhft.lang.data;

import net.openhft.lang.data.attic.RawWire;
import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Before;
import org.junit.Test;

import static net.openhft.lang.data.TestField.BOOL1;
import static net.openhft.lang.data.TestField.BOOL2;
import static org.junit.Assert.assertEquals;

public class RawWireTest {

    private DirectBytes bytes;
    private RawWire wire;

    @Before
    public void setUp() throws Exception {
        bytes = new DirectStore(128).bytes();
        wire = new RawWire();
        wire.setBytes(bytes);
    }

    @Test
    public void testWriteCompactFloat() throws Exception {
        float[] floats = {0.0f, 1.0f, 0.1f, 0.01f, 0.0111f, 0.010101f, 0.01111111f};
        String[] strs = {"[pos: 0, lim: 3, cap: 128 ] \u0080\u0080\u0080",
                "[pos: 0, lim: 6, cap: 128 ] ٠⒈٠⒈٠⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒈⒈⒈⒈⒈⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒉⒈⒉⒈⒉⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒋o⒋o⒋o",
                "[pos: 0, lim: 9, cap: 128 ] ⒍õN⒍õN⒍õN",
                "[pos: 0, lim: 13, cap: 128 ] \u0098`⒒6<\u0098`⒒6<⒍çV"};
        for (int i = 0; i < floats.length; i++) {
            bytes.clear();
            wire.writeCompactFloat(EncodeMode.COMPACT, bytes, floats[i]);
            wire.writeCompactFloat(EncodeMode.COMPRESS, bytes, floats[i]);
            wire.writeCompactFloat(EncodeMode.ROUND6, bytes, floats[i]);
            bytes.flip();
            assertEquals(strs[i], bytes.toDebugString());
        }
    }

    @Test
    public void testWriteCompactDouble() throws Exception {
        double[] floats = {0.0, 1.0, 0.1, 0.01, 0.0111, 0.010101, 0.01111111};
        String[] strs = {"[pos: 0, lim: 3, cap: 128 ] \u0080\u0080\u0080",
                "[pos: 0, lim: 6, cap: 128 ] ٠⒈٠⒈٠⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒈⒈⒈⒈⒈⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒉⒈⒉⒈⒉⒈",
                "[pos: 0, lim: 6, cap: 128 ] ⒋o⒋o⒋o",
                "[pos: 0, lim: 9, cap: 128 ] ⒍õN⒍õN⒍õN",
                "[pos: 0, lim: 13, cap: 128 ] \u0098`⒒6<\u0098`⒒6<⒍çV"};
        for (int i = 0; i < floats.length; i++) {
            bytes.clear();
            wire.writeCompactDouble(EncodeMode.COMPACT, bytes, floats[i]);
            wire.writeCompactDouble(EncodeMode.COMPRESS, bytes, floats[i]);
            wire.writeCompactDouble(EncodeMode.ROUND6, bytes, floats[i]);
            bytes.flip();
            assertEquals("i: "+i, strs[i], bytes.toDebugString());
        }
    }


    @Test
    public void testWriteBoolean() throws Exception {
        wire.writeBoolean(BOOL1, true);
        wire.writeBoolean(BOOL2, false);
        bytes.flip();
        assertEquals("[pos: 0, lim: 2, cap: 128 ] Y٠", bytes.toDebugString());
    }

    @Test
    public void testReadBoolean() throws Exception {

    }

    @Test
    public void testWriteByte() throws Exception {

    }

    @Test
    public void testReadByte() throws Exception {

    }

    @Test
    public void testWriteUnsignedByte() throws Exception {

    }

    @Test
    public void testReadUnsignedByte() throws Exception {

    }

    @Test
    public void testWriteShort() throws Exception {

    }

    @Test
    public void testReadShort() throws Exception {

    }

    @Test
    public void testWriteUnsignedShort() throws Exception {

    }

    @Test
    public void testReadUnsignedShort() throws Exception {

    }

    @Test
    public void testWriteInt() throws Exception {

    }

    @Test
    public void testReadInt() throws Exception {

    }

    @Test
    public void testWriteUnsignedInt() throws Exception {

    }

    @Test
    public void testReadUnsignedInt() throws Exception {

    }

    @Test
    public void testWriteLong() throws Exception {

    }

    @Test
    public void testReadLong() throws Exception {

    }

    @Test
    public void testWriteFloat() throws Exception {

    }

    @Test
    public void testReadFloat() throws Exception {

    }

    @Test
    public void testWriteDouble() throws Exception {

    }

    @Test
    public void testReadDouble() throws Exception {

    }

    @Test
    public void testWriteText() throws Exception {

    }

    @Test
    public void testReadText() throws Exception {

    }

    @Test
    public void testReadText1() throws Exception {

    }

    @Test
    public void testWriteEnum() throws Exception {

    }

    @Test
    public void testReadEnum() throws Exception {

    }

    @Test
    public void testReadEnum1() throws Exception {

    }
}