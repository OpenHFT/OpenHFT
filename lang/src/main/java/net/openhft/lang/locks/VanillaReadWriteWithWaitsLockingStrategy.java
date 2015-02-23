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

public final class VanillaReadWriteWithWaitsLockingStrategy extends AbstractReadWriteLockingStrategy
        implements ReadWriteWithWaitsLockingStrategy {

    private static final ReadWriteWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteWithWaitsLockingStrategy();

    public static ReadWriteWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    private VanillaReadWriteWithWaitsLockingStrategy() {}


    static final int RW_LOCK_LIMIT = 30;
    static final long RW_READ_LOCKED = 1L;
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

    static <T> long read(NativeAtomicAccess<T> access, T t, long offset) {
        return access.getLongVolatile(t, offset);
    }

    static <T> boolean cas(NativeAtomicAccess<T> access, T t, long offset, long expected, long x) {
        return access.compareAndSwapLong(t, offset, expected, x);
    }

    @Override
    public <T> boolean tryReadLock(NativeAtomicAccess<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        // readers wait for waiting writers
        if (writersLocked <= 0 && writersWaiting <= 0) {
            // increment readers locked.
            int readersLocked = rwReadLocked(lock);
            if (readersLocked >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("readersLocked has reached a limit of " +
                        readersLocked);
            if (cas(access, t, offset, lock, lock + RW_READ_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public <T> boolean tryWriteLock(NativeAtomicAccess<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int readersLocked = rwReadLocked(lock);
        int writersLocked = rwWriteLocked(lock);
        // writers don't wait for waiting readers.
        if (readersLocked <= 0 && writersLocked <= 0) {
            if (cas(access, t, offset, lock, lock + RW_WRITE_LOCKED))
                return true;
        }
        return false;
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLock(NativeAtomicAccess<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public <T> void readUnlock(NativeAtomicAccess<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int readersLocked = rwReadLocked(lock);
            if (readersLocked <= 0)
                throw new IllegalMonitorStateException("readerLock underflow");
            if (cas(access, t, offset, lock, lock - RW_READ_LOCKED))
                return;
        }
    }

    @Override
    public <T> void writeUnlock(NativeAtomicAccess<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersLocked = rwWriteLocked(lock);
            if (writersLocked != 1)
                throw new IllegalMonitorStateException("writersLock underflow " + writersLocked);
            if (cas(access, t, offset, lock, lock - RW_WRITE_LOCKED))
                return;
        }
    }

    @Override
    public <T> void downgradeWriteToReadLock(NativeAtomicAccess<T> access, T t, long offset) {
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
    public <T> void reset(NativeAtomicAccess<T> access, T t, long offset) {
        access.putOrderedLong(t, offset, 0L);
    }

    @Override
    public <T> void resetKeepingWaits(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            long lock = read(access, t, offset);
            long onlyWaits = lock & ((long) RW_LOCK_MASK) << RW_LOCK_LIMIT;
            if (cas(access, t, offset, lock, onlyWaits))
                return;
        }
    }

    @Override
    public <T> void registerWait(NativeAtomicAccess<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("writersWaiting has reached a limit of " +
                        writersWaiting);
            if (cas(access, t, offset, lock, lock + RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public <T> void deregisterWait(NativeAtomicAccess<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            if (cas(access, t, offset, lock, lock - RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public <T> boolean tryWriteLockAndDeregisterWait(
            NativeAtomicAccess<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int readersLocked = rwReadLocked(lock);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        if (readersLocked <= 0 && writersLocked <= 0) {
            // increment readers locked.
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // add to the readLock count and decrease the readWaiting count.
            if (cas(access, t, offset, lock, lock + RW_WRITE_LOCKED - RW_WRITE_WAITING))
                return true;
        }
        return false;
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            NativeAtomicAccess<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public long resetState() {
        return 0L;
    }

    @Override
    public <T> long getState(NativeAtomicAccess<T> access, T t, long offset) {
        return read(access, t, offset);
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
