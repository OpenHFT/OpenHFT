/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 * Copyright 2013 Niall Gallagher
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

package net.openhft.lang.locks;


import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static net.openhft.lang.locks.LockingStrategyTest.AccessMethod.ADDRESS;
import static net.openhft.lang.locks.LockingStrategyTest.AccessMethod.BYTES_WITH_OFFSET;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(value = Parameterized.class)
public class LockingStrategyTest {
    
    enum AccessMethod {ADDRESS, BYTES_WITH_OFFSET}

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] {
                {VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), ADDRESS},
                {VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), BYTES_WITH_OFFSET},
                {VanillaReadWriteWithWaitsLockingStrategy.instance(), ADDRESS},
                {VanillaReadWriteWithWaitsLockingStrategy.instance(), BYTES_WITH_OFFSET},
        });
    }

    ExecutorService e1, e2;
    ByteBuffer buffer;
    Bytes bytes;
    long address;
    LockingStrategy lockingStrategy;
    AccessMethod accessMethod;

    public LockingStrategyTest(LockingStrategy lockingStrategy, AccessMethod accessMethod) {
        this.lockingStrategy = lockingStrategy;
        this.accessMethod = accessMethod;
    }

    Callable<Boolean> tryReadLockTask = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return rwls().tryReadLock();
        }
    };

    class TestReadWriteLockState extends AbstractReadWriteLockState {

        private ReadWriteLockingStrategy rwls() {
            return (ReadWriteLockingStrategy) lockingStrategy;
        }

        boolean a() {
            return accessMethod == ADDRESS;
        }

        @Override
        public boolean tryReadLock() {
            return a() ? rwls().tryReadLock(address) : rwls().tryReadLock(bytes, 0L);
        }

        @Override
        public boolean tryWriteLock() {
            return a() ? rwls().tryWriteLock(address) : rwls().tryWriteLock(bytes, 0L);
        }

        @Override
        public boolean tryUpgradeReadToWriteLock() {
            return a() ? rwls().tryUpgradeReadToWriteLock(address) :
                    rwls().tryUpgradeReadToWriteLock(bytes, 0L);
        }

        @Override
        public void readUnlock() {
            if (a()) rwls().readUnlock(address); else rwls().readUnlock(bytes, 0L);
        }

        @Override
        public void writeUnlock() {
            if (a()) rwls().writeUnlock(address); else rwls().writeUnlock(bytes, 0L);
        }

        @Override
        public void downgradeWriteToReadLock() {
            if (a()) rwls().downgradeWriteToReadLock(address);
            else rwls().downgradeWriteToReadLock(bytes, 0L);
        }

        @Override
        public void reset() {
            if (a()) rwls().reset(address); else rwls().reset(bytes, 0L);
        }

        @Override
        public long getState() {
            return a() ? rwls().getState(address) : rwls().getState(bytes, 0L);
        }

        @Override
        public ReadWriteLockingStrategy lockingStrategy() {
            return rwls();
        }
    }
    TestReadWriteLockState rwLockState = new TestReadWriteLockState();

    ReadWriteLockState rwls() {
        return rwLockState;
    }

    class TestReadWriteUpdateLockState extends TestReadWriteLockState
            implements ReadWriteUpdateLockState {

        ReadWriteUpdateLockingStrategy rwuls() {
            return (ReadWriteUpdateLockingStrategy) lockingStrategy;
        }

        @Override
        public boolean tryUpdateLock() {
            return a() ? rwuls().tryUpdateLock(address) : rwuls().tryUpdateLock(bytes, 0L);
        }

        @Override
        public boolean tryUpgradeReadToUpdateLock() {
            return a() ? rwuls().tryUpgradeReadToUpdateLock(address) :
                    rwuls().tryUpgradeReadToUpdateLock(bytes, 0L);
        }

        @Override
        public boolean tryUpgradeUpdateToWriteLock() {
            return a() ? rwuls().tryUpgradeUpdateToWriteLock(address) :
                    rwuls().tryUpgradeUpdateToWriteLock(bytes, 0L);
        }

        @Override
        public void updateUnlock() {
            if (a()) rwuls().updateUnlock(address); else rwuls().updateUnlock(bytes, 0L);
        }

        @Override
        public void downgradeUpdateToReadLock() {
            if (a()) rwuls().downgradeUpdateToReadLock(address);
            else rwuls().downgradeUpdateToReadLock(bytes, 0L);
        }

        @Override
        public void downgradeWriteToUpdateLock() {
            if (a()) rwuls().downgradeWriteToUpdateLock(address);
            else rwuls().downgradeWriteToUpdateLock(bytes, 0L);
        }

        @Override
        public ReadWriteUpdateLockingStrategy lockingStrategy() {
            return rwuls();
        }
    }
    TestReadWriteUpdateLockState rwuLockState = new TestReadWriteUpdateLockState();

    Runnable readUnlockTask = new Runnable() {
        @Override
        public void run() {
            rwls().readUnlock();
        }
    };

    Callable<Boolean> tryUpdateLockTask = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return rwuls().tryUpdateLock();
        }
    };

    ReadWriteUpdateLockState rwuls() {
        return rwuLockState;
    }

    Runnable updateUnlockTask = new Runnable() {
        @Override
        public void run() {
            rwuls().updateUnlock();
        }
    };

    Callable<Boolean> tryWriteLockTask = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            return rwls().tryWriteLock();
        }
    };

    Runnable writeUnlockTask = new Runnable() {
        @Override
        public void run() {
            rwls().writeUnlock();
        }
    };


    @Before
    public void setUp() throws Exception {
        e1 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        e2 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        buffer = ByteBuffer.allocateDirect(8);
        bytes = new ByteBufferBytes(buffer);
        address = ((DirectBuffer) buffer).address();
        rwls().reset();
    }

    @After
    public void tearDown() throws Exception {
        e1.shutdown();
        e2.shutdown();
    }

    @Test
    public void testUpdateLockIsExclusive() throws Exception {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire update lock in thread 2, should fail...
        assertFalse(e2.submit(tryUpdateLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire update lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryUpdateLockTask).get());

        // Release the update lock in thread 2...
        e2.submit(updateUnlockTask).get();
    }

    @Test
    public void testUpdateLockAllowsOtherReaders() throws Exception {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire read lock in thread 2, should succeed...
        assertTrue(e2.submit(tryReadLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
    }

    @Test
    public void testUpdateLockBlocksOtherWriters() throws Exception {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire write lock in thread 2, should fail...
        assertFalse(e2.submit(tryWriteLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire write lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryWriteLockTask).get());

        // Release the write lock in thread 2...
        e2.submit(writeUnlockTask).get();
    }

    @Test
    public void testWriteLockBlocksOtherReaders() throws Exception {
        assumeReadWriteLock();

        // Acquire the write lock in thread 1...
        assertTrue(e1.submit(tryWriteLockTask).get());

        // Try to acquire read lock in thread 2, should fail...
        assertFalse(e2.submit(tryReadLockTask).get());

        // Release the write lock in thread 1...
        e1.submit(writeUnlockTask).get();

        // Try to acquire read lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryReadLockTask).get());

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
    }

    @Test
    public void testUpdateLockUpgradeToWriteLock() throws Exception {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire write lock in thread 1, should succeed...
        assertTrue(e1.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return rwuls().tryUpgradeUpdateToWriteLock();
            }
        }).get());

        // Release the write lock in thread 1...
        e1.submit(new Runnable() {
            @Override
            public void run() {
                rwuls().downgradeWriteToUpdateLock();
            }
        });

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();
    }

    @Test
    public void testReadWriteLockTransitions() {
        assumeReadWriteLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToReadLockForbidden();

        // Read lock is held
        assertTrue(rwls().tryReadLock());
        writeUnlockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwls().readUnlock();
        assertTrue(rwls().tryReadLock());

        // allow upgrade to write lock
        try {
            assertTrue(rwls().tryUpgradeReadToWriteLock());
        } catch (UnsupportedOperationException tolerated) {
            rwls().readUnlock();
            assertTrue(rwls().tryWriteLock());
        }

        // write lock is held
        readUnlockForbidden();
        upgradeReadToWriteLockForbidden();

        // allow unlock
        rwls().writeUnlock();
        assertTrue(rwls().tryWriteLock());

        // allow downgrade to read lock
        try {
            rwls().downgradeWriteToReadLock();
        } catch (UnsupportedOperationException tolerated) {}

        rwls().reset();
    }

    void downgradeWriteToReadLockForbidden() {
        try {
            rwls().downgradeWriteToReadLock();
            fail("downgradeWriteToReadLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void upgradeReadToWriteLockForbidden() {
        try {
            rwls().tryUpgradeReadToWriteLock();
            fail("tryUpgradeReadToWriteLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void writeUnlockForbidden() {
        try {
            rwls().writeUnlock();
            fail("writeUnlock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void readUnlockForbidden() {
        try {
            rwls().readUnlock();
            fail("readUnlock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    @Test
    public void testReadWriteUpgradeLockTransitions() {
        assumeReadWriteUpdateLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // Read lock is held
        assertTrue(rwuls().tryReadLock());
        updateUnlockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // allow upgrade to update lock
        assertTrue(rwuls().tryUpgradeReadToUpdateLock());

        // update lock is held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToUpdateLockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwuls().updateUnlock();
        assertTrue(rwuls().tryUpdateLock());

        // allow upgrade to write lock
        assertTrue(rwuls().tryUpgradeUpdateToWriteLock());

        // write lock is held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();

        // allow downgrade to update lock
        rwuls().downgradeWriteToUpdateLock();

        rwuls().updateUnlock();
    }

    void downgradeWriteToUpdateLockForbidden() {
        try {
            rwuls().downgradeWriteToUpdateLock();
            fail("downgradeWriteToUpdateLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void downgradeUpdateToReadLockForbidden() {
        try {
            rwuls().downgradeUpdateToReadLock();
            fail("downgradeUpdateToReadLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void upgradeUpdateToWriteLockForbidden() {
        try {
            rwuls().tryUpgradeUpdateToWriteLock();
            fail("tryUpgradeUpdateToWriteLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void upgradeReadToUpdateLockForbidden() {
        try {
            rwuls().tryUpgradeReadToUpdateLock();
            fail("tryUpgradeReadToUpdateLock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void updateUnlockForbidden() {
        try {
            rwuls().updateUnlock();
            fail("updateUnlock() should fail");
        } catch (IllegalMonitorStateException e) {
            // expected
        } catch (UnsupportedOperationException e2) {
            // expected
        }
    }

    void assumeReadWriteUpdateLock() {
        assumeTrue(lockingStrategy instanceof ReadWriteUpdateLockingStrategy);
    }

    void assumeReadWriteLock() {
        assumeTrue(lockingStrategy instanceof ReadWriteLockingStrategy);
    }
}