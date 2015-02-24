package net.openhft.chronicle.bytes;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class MappedFileTest {

    @Test
    public void testReferenceCounts() throws IOException {
        File tmp = File.createTempFile("testReferenceCounts", ".bin");
        tmp.deleteOnExit();
        MappedFile mf = MappedFile.mappedFile(tmp.getName(), 4 << 10, 0);
        assertEquals("refCount: 1", mf.referenceCounts());

        MappedBytesStore bs = mf.acquireByteStore(5 << 10);
        assertEquals(4 << 10, bs.start());
        Bytes bytes = bs.bytes();
        assertEquals(4 << 10, bytes.start());
        assertEquals(0L, bs.readLong(5 << 10));
        assertEquals(0L, bytes.readLong(5 << 10));
        assertFalse(bs.inStore(3 << 10));
        assertFalse(bs.inStore((4 << 10) - 1));
        assertTrue(bs.inStore(4 << 10));
        assertTrue(bs.inStore((8 << 10) - 1));
        assertFalse(bs.inStore(8 << 10));
        try {
            bytes.readLong(3 << 10);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            bytes.readLong(9 << 10);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(2, mf.refCount());
        assertEquals(3, bs.refCount());
        assertEquals("refCount: 2, 0, 3", mf.referenceCounts());

        BytesStore bs2 = mf.acquireByteStore(5 << 10);
        assertEquals(4, bs2.refCount());
        assertEquals("refCount: 2, 0, 4", mf.referenceCounts());
        bytes.release();
        assertEquals(3, bs2.refCount());
        assertEquals("refCount: 2, 0, 3", mf.referenceCounts());

        mf.close();
        assertEquals(2, bs.refCount());
        assertEquals("refCount: 1, 0, 2", mf.referenceCounts());
        bs2.release();
        assertEquals(1, mf.refCount());
        assertEquals(1, bs.refCount());
        bs.release();
        assertEquals(0, bs.refCount());
        assertEquals(0, mf.refCount());
        assertEquals("refCount: 0, 0, 0", mf.referenceCounts());
    }
}