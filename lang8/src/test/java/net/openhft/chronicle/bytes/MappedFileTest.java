package net.openhft.chronicle.bytes;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MappedFileTest {

    @Test
    public void testReferenceCounts() throws IOException {
        File tmp = File.createTempFile("testReferenceCounts", ".bin");
        tmp.deleteOnExit();
        MappedFile mf = MappedFile.of(tmp.getName(), 4 << 10, 0);
        assertEquals("refCount: 1", mf.referenceCounts());
        BytesStore bs = mf.acquire(5 << 10);
        assertEquals(2, mf.refCount());
        assertEquals(2, bs.refCount());
        assertEquals("refCount: 2, 0, 2", mf.referenceCounts());

        BytesStore bs2 = mf.acquire(5 << 10);
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