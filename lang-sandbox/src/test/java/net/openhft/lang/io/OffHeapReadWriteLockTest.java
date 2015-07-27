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