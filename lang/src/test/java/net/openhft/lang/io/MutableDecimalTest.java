package net.openhft.lang.io;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 10:40
 */
public class MutableDecimalTest {
    @Test
    public void testConstructor() {
        MutableDecimal md = new MutableDecimal(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, md.longValue());
        assertEquals("" + Long.MAX_VALUE, md.toString());

        MutableDecimal md2 = new MutableDecimal(Long.MAX_VALUE, 10);
        assertEquals(Long.MAX_VALUE / 1e10, md2.doubleValue(), 0);
        assertEquals("922337203.6854775807", md2.toString());

        MutableDecimal md3 = new MutableDecimal(Math.PI, 6);
        assertEquals(3.141593, md3.doubleValue(), 0);
        assertEquals(3.141593f, md3.floatValue(), 0);
        assertEquals(3, md3.intValue());
        assertEquals("3.141593", md3.toString());

        assertEquals(1, md.compareTo(md2));
        assertEquals(-1, md2.compareTo(md));
        assertEquals(1, md2.compareTo(md3));
        assertEquals(-1, md3.compareTo(md2));

        assertEquals(3141593, md3.value());
        assertEquals(6, md3.scale());
        assertTrue(md3.isSet());
        md3.clear();
        assertFalse(md3.isSet());
    }
}
