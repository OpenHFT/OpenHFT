package net.openhft.lang.io;

import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.OffHeapReadWriteLock;
import org.junit.Test;

import static org.junit.Assert.*;

public class OffHeapReadWriteLockTest {
    @Test
    public void readCycle() {
        OffHeapReadWriteLock ohrwl = new OffHeapReadWriteLock(DirectStore.allocate(64).bytes(), 0);
        assertEquals("none w:0/0 r:0/0", ohrwl.toString());
        assertTrue(ohrwl.readLock().tryLock());
        assertEquals("read w:0/0 r:1/0", ohrwl.toString());
        assertTrue(ohrwl.readLock().tryLock());
        assertEquals("read w:0/0 r:2/0", ohrwl.toString());
        ohrwl.readLock().unlock();
        assertEquals("read w:0/0 r:1/0", ohrwl.toString());
        ohrwl.readLock().unlock();
        assertEquals("none w:0/0 r:0/0", ohrwl.toString());
    }

    @Test
    public void writeCycle() {
        OffHeapReadWriteLock ohrwl = new OffHeapReadWriteLock(DirectStore.allocate(64).bytes(), 0);
        assertEquals("none w:0/0 r:0/0", ohrwl.toString());
        assertTrue(ohrwl.writeLock().tryLock());
        assertEquals("write w:1/0 r:0/0", ohrwl.toString());
        assertFalse(ohrwl.writeLock().tryLock());
        ohrwl.writeLock().addWaitingWriter(1);
        assertEquals("write w:1/1 r:0/0", ohrwl.toString());
        ohrwl.writeLock().unlock();
        assertEquals("write w:0/1 r:0/0", ohrwl.toString());
        assertTrue(ohrwl.writeLock().tryLock());
        ohrwl.writeLock().addWaitingWriter(-1);
        assertEquals("write w:1/0 r:0/0", ohrwl.toString());
        ohrwl.writeLock().unlock();
        assertEquals("none w:0/0 r:0/0", ohrwl.toString());
    }

    @Test
    public void waitingCycle() {
        OffHeapReadWriteLock ohrwl = new OffHeapReadWriteLock(DirectStore.allocate(64).bytes(), 0);
        assertEquals("none w:0/0 r:0/0", ohrwl.toString());
        assertTrue(ohrwl.writeLock().tryLock());
        assertEquals("write w:1/0 r:0/0", ohrwl.toString());
        assertFalse(ohrwl.readLock().tryLock());
        ohrwl.readLock().addWaitingReader(2);
        assertEquals("write w:1/0 r:0/2", ohrwl.toString());
        ohrwl.writeLock().unlock();
        assertEquals("none w:0/0 r:0/2", ohrwl.toString());
        assertTrue(ohrwl.readLock().tryLock());
        ohrwl.readLock().addWaitingReader(-1);
        assertEquals("read w:0/0 r:1/1", ohrwl.toString());
        assertTrue(ohrwl.readLock().tryLock());
        ohrwl.readLock().addWaitingReader(-1);
    }

}