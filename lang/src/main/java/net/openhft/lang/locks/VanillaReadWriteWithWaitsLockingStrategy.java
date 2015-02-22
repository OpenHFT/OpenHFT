/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
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

import net.openhft.lang.io.Bytes;

import static java.nio.ByteOrder.nativeOrder;
import static net.openhft.lang.io.NativeBytes.UNSAFE;

public final class VanillaReadWriteWithWaitsLockingStrategy extends AbstractReadWriteLockingStrategy
        implements ReadWriteWithWaitsLockingStrategy {

    private static final ReadWriteWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteWithWaitsLockingStrategy();

    public static ReadWriteWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    private VanillaReadWriteWithWaitsLockingStrategy() {}


    static final int RW_LOCK_LIMIT = 30;
    static final long RW_READ_LOCKED = 1L << 0;
    static final long RW_WRITE_WAITING = 1L << RW_LOCK_LIMIT;
    static final long RW_WRITE_LOCKED = 1L << 2 * RW_LOCK_LIMIT;
    static final int RW_LOCK_MASK = (1 << RW_LOCK_LIMIT) - 1;


    static int rwReadLocked(long lock) {
        return (int) (lock & RW_LOCK_MASK);
    }

    static int rwWriteWaiting(long lock) {
        return (int) ((lock >>> RW_LOCK_LIMIT) & RW_LOCK_MASK);
    }

    static int rwWriteLocked(long lock) {
        return (int) (lock >>> (2 * RW_LOCK_LIMIT));
    }

    static long readVolatileLong(long address) {
        return UNSAFE.getLongVolatile(null, address);
    }

    static long readVolatileLong(Bytes bytes, long offset) {
        long lock = bytes.readVolatileLong(offset);
        if (bytes.byteOrder() != nativeOrder())
            lock = Long.reverseBytes(lock);
        return lock;
    }

    static boolean compareAndSwapLong(long address, long expected, long x) {
        return UNSAFE.compareAndSwapLong(null, address, expected, x);
    }

    static boolean compareAndSwapLong(Bytes bytes, long offset, long expected, long x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Long.reverseBytes(expected);
            x = Long.reverseBytes(x);
        }
        return bytes.compareAndSwapLong(offset, expected, x);
    }

    @Override
    public boolean tryReadLock(long address) {
        long lock = readVolatileLong(address);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        // readers wait for waiting writers
        if (writersLocked <= 0 && writersWaiting <= 0) {
            // increment readers locked.
            int readersLocked = rwReadLocked(lock);
            if (readersLocked >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("readersLocked has reached a limit of " +
                        readersLocked);
            if (compareAndSwapLong(address, lock, lock + RW_READ_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryReadLock(Bytes bytes, long offset) {
        long lock = readVolatileLong(bytes, offset);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        // readers wait for waiting writers
        if (writersLocked <= 0 && writersWaiting <= 0) {
            // increment readers locked.
            int readersLocked = rwReadLocked(lock);
            if (readersLocked >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("readersLocked has reached a limit of " +
                        readersLocked);
            if (compareAndSwapLong(bytes, offset, lock, lock + RW_READ_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryWriteLock(long address) {
        long lock = readVolatileLong(address);
        int readersLocked = rwReadLocked(lock);
        int writersLocked = rwWriteLocked(lock);
        // writers don't wait for waiting readers.
        if (readersLocked <= 0 && writersLocked <= 0) {
            if (compareAndSwapLong(address, lock, lock + RW_WRITE_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryWriteLock(Bytes bytes, long offset) {
        long lock = readVolatileLong(bytes, offset);
        int readersLocked = rwReadLocked(lock);
        int writersLocked = rwWriteLocked(lock);
        // writers don't wait for waiting readers.
        if (readersLocked <= 0 && writersLocked <= 0) {
            if (compareAndSwapLong(bytes, offset, lock, lock + RW_WRITE_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryUpgradeReadToWriteLock(long address) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean tryUpgradeReadToWriteLock(Bytes bytes, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void readUnlock(long address) {
        for (; ; ) {
            long lock = readVolatileLong(address);
            int readersLocked = rwReadLocked(lock);
            if (readersLocked <= 0)
                throw new IllegalMonitorStateException("readerLock underflow");
            if (compareAndSwapLong(address, lock, lock - RW_READ_LOCKED))
                return;
        }
    }

    @Override
    public void readUnlock(Bytes bytes, long offset) {
        for (; ; ) {
            long lock = readVolatileLong(bytes, offset);
            int readersLocked = rwReadLocked(lock);
            if (readersLocked <= 0)
                throw new IllegalMonitorStateException("readerLock underflow");
            if (compareAndSwapLong(bytes, offset, lock, lock - RW_READ_LOCKED))
                return;
        }
    }

    @Override
    public void writeUnlock(long address) {
        for (; ; ) {
            long lock = readVolatileLong(address);
            int writersLocked = rwWriteLocked(lock);
            if (writersLocked != 1)
                throw new IllegalMonitorStateException("writersLock underflow " + writersLocked);
            if (compareAndSwapLong(address, lock, lock - RW_WRITE_LOCKED))
                return;
        }
    }

    @Override
    public void writeUnlock(Bytes bytes, long offset) {
        for (; ; ) {
            long lock = readVolatileLong(bytes, offset);
            int writersLocked = rwWriteLocked(lock);
            if (writersLocked != 1)
                throw new IllegalMonitorStateException("writersLock underflow " + writersLocked);
            if (compareAndSwapLong(bytes, offset, lock, lock - RW_WRITE_LOCKED))
                return;
        }
    }

    @Override
    public void downgradeWriteToReadLock(long address) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void downgradeWriteToReadLock(Bytes bytes, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean isWriteLocked(long state) {
        return rwWriteLocked(state) > 0;
    }

    @Override
    public int readLockCount(long state) {
        return rwReadLocked(state);
    }

    @Override
    public void reset(long address) {
        UNSAFE.putOrderedLong(null, address, 0L);
    }

    @Override
    public void reset(Bytes bytes, long offset) {
        bytes.writeOrderedLong(offset, 0L);
    }

    @Override
    public void resetKeepingWaits(long address) {
        while (true) {
            long lock = readVolatileLong(address);
            long onlyWaits = lock & ((long) RW_LOCK_MASK) << RW_LOCK_LIMIT;
            if (compareAndSwapLong(address, lock, onlyWaits))
                return;
        }
    }

    @Override
    public void resetKeepingWaits(Bytes bytes, long offset) {
        while (true) {
            long lock = readVolatileLong(bytes, offset);
            long onlyWaits = lock & ((long) RW_LOCK_MASK) << RW_LOCK_LIMIT;
            if (compareAndSwapLong(bytes, offset, lock, onlyWaits))
                return;
        }
    }

    @Override
    public void registerWait(long address) {
        for (; ; ) {
            long lock = readVolatileLong(address);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("writersWaiting has reached a limit of " +
                        writersWaiting);
            if (compareAndSwapLong(address, lock, lock + RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public void registerWait(Bytes bytes, long offset) {
        for (; ; ) {
            long lock = readVolatileLong(bytes, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("writersWaiting has reached a limit of " +
                        writersWaiting);
            if (compareAndSwapLong(bytes, offset, lock, lock + RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public void deregisterWait(long address) {
        for (; ; ) {
            long lock = readVolatileLong(address);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            if (compareAndSwapLong(address, lock, lock - RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public void deregisterWait(Bytes bytes, long offset) {
        for (; ; ) {
            long lock = readVolatileLong(bytes, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            if (compareAndSwapLong(bytes, offset, lock, lock - RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public boolean tryWriteLockAndDeregisterWait(long address) {
        long lock = readVolatileLong(address);
        int readersLocked = rwReadLocked(lock);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        if (readersLocked <= 0 && writersLocked <= 0) {
            // increment readers locked.
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // add to the readLock count and decrease the readWaiting count.
            if (compareAndSwapLong(address, lock, lock + RW_WRITE_LOCKED - RW_WRITE_WAITING))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryWriteLockAndDeregisterWait(Bytes bytes, long offset) {
        long lock = readVolatileLong(bytes, offset);
        int readersLocked = rwReadLocked(lock);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        if (readersLocked <= 0 && writersLocked <= 0) {
            // increment readers locked.
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // add to the readLock count and decrease the readWaiting count.
            if (compareAndSwapLong(bytes, offset, lock, lock + RW_WRITE_LOCKED - RW_WRITE_WAITING))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryUpgradeReadToWriteLockAndDeregisterWait(long address) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean tryUpgradeReadToWriteLockAndDeregisterWait(Bytes bytes, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public long resetState() {
        return 0L;
    }

    @Override
    public long getState(long address) {
        return readVolatileLong(address);
    }

    @Override
    public long getState(Bytes bytes, long offset) {
        return readVolatileLong(bytes, offset);
    }

    @Override
    public int waitCount(long state) {
        return rwWriteWaiting(state);
    }

    @Override
    public boolean isLocked(long state) {
        return isReadLocked(state) || isWriteLocked(state);
    }

    @Override
    public int lockCount(long state) {
        return rwReadLocked(state) + rwWriteLocked(state);
    }

    @Override
    public String toString(long state) {
        return "[read locks = " + readLockCount(state) +
                ", write locked = " + isWriteLocked(state) +
                ", waits = " + waitCount(state) + "]";
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }
}
