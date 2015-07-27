/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.locks;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.ByteOrder.nativeOrder;
import static net.openhft.lang.io.AbstractBytes.UNSIGNED_INT_MASK;

public final class VanillaReadWriteUpdateWithWaitsLockingStrategy
        extends AbstractReadWriteLockingStrategy
        implements ReadWriteUpdateWithWaitsLockingStrategy {

    private static final ReadWriteUpdateWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteUpdateWithWaitsLockingStrategy();

    public static ReadWriteUpdateWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    private VanillaReadWriteUpdateWithWaitsLockingStrategy() {}

    static final long COUNT_WORD_OFFSET = 0L;
    static final long WAIT_WORD_OFFSET = COUNT_WORD_OFFSET + 4L;

    static final int COUNT_WORD_SHIFT = nativeOrder() == LITTLE_ENDIAN ? 0 : 32;
    static final int WAIT_WORD_SHIFT = nativeOrder() == LITTLE_ENDIAN ? 32 : 0;

    static final int READ_BITS = 30;
    static final int MAX_READ = (1 << READ_BITS) - 1;
    static final int READ_MASK = MAX_READ;
    static final int READ_PARTY = 1;

    static final int UPDATE_PARTY = 1 << READ_BITS;
    static final int WRITE_LOCKED_COUNT_WORD = UPDATE_PARTY << 1;

    static final int MAX_WAIT = Integer.MAX_VALUE;
    static final int WAIT_PARTY = 1;

    private static <T> long getLockWord(NativeAtomicAccess<T> access, T t, long offset) {
        return access.getLongVolatile(t, offset);
    }

    private static <T> boolean casLockWord(
            NativeAtomicAccess<T> access, T t, long offset, long expected, long x) {
        return access.compareAndSwapLong(t, offset, expected, x);
    }

    private static int countWord(long lockWord) {
        return (int) (lockWord >> COUNT_WORD_SHIFT);
    }

    private static int waitWord(long lockWord) {
        return (int) (lockWord >> WAIT_WORD_SHIFT);
    }

    private static long lockWord(int countWord, int waitWord) {
        return ((((long) countWord) & UNSIGNED_INT_MASK) << COUNT_WORD_SHIFT) |
                ((((long) waitWord) & UNSIGNED_INT_MASK) << WAIT_WORD_SHIFT);
    }

    private static <T> int getCountWord(NativeAtomicAccess<T> access, T t, long offset) {
        return access.getIntVolatile(t, offset + COUNT_WORD_OFFSET);
    }

    private static <T> boolean casCountWord(
            NativeAtomicAccess<T> access, T t, long offset, int expected, int x) {
        return access.compareAndSwapInt(t, offset + COUNT_WORD_OFFSET, expected, x);
    }

    private static <T> void putCountWord(
            NativeAtomicAccess<T> access, T t, long offset, int countWord) {
        access.putOrderedInt(t, offset + COUNT_WORD_OFFSET, countWord);
    }

    private static boolean writeLocked(int countWord) {
        return countWord == WRITE_LOCKED_COUNT_WORD;
    }

    private static void checkWriteLocked(int countWord) {
        if (countWord != WRITE_LOCKED_COUNT_WORD)
            throw new IllegalMonitorStateException("Expected write lock");
    }

    private static boolean updateLocked(int countWord) {
        return (countWord & UPDATE_PARTY) != 0;
    }

    private static void checkUpdateLocked(int countWord) {
        if (!updateLocked(countWord))
            throw new IllegalMonitorStateException("Expected update lock");
    }

    private static int readCount(int countWord) {
        return countWord & READ_MASK;
    }

    private static void checkReadLocked(int countWord) {
        if (readCount(countWord) <= 0)
            throw new IllegalMonitorStateException("Expected read lock");
    }

    private static void checkReadCountForIncrement(int countWord) {
        if (readCount(countWord) == MAX_READ) {
            throw new IllegalMonitorStateException(
                    "Lock count reached the limit of " + MAX_READ);
        }
    }

    private static <T> int getWaitWord(NativeAtomicAccess<T> access, T t, long offset) {
        return access.getIntVolatile(t, offset + WAIT_WORD_OFFSET);
    }

    private static <T> boolean casWaitWord(
            NativeAtomicAccess<T> access, T t, long offset, int expected, int x) {
        return access.compareAndSwapInt(t, offset + WAIT_WORD_OFFSET, expected, x);
    }

    private static void checkWaitWordForIncrement(int waitWord) {
        if (waitWord == MAX_WAIT) {
            throw new IllegalMonitorStateException(
                    "Wait count reached the limit of " + MAX_WAIT);
        }
    }

    private static void checkWaitWordForDecrement(int waitWord) {
        if (waitWord == 0) {
            throw new IllegalMonitorStateException(
                    "Wait count underflowed");
        }
    }

    @Override
    public long resetState() {
        return 0L;
    }

    @Override
    public <T> void reset(NativeAtomicAccess<T> access, T t, long offset) {
        access.putOrderedLong(t, offset, 0L);
    }

    @Override
    public <T> void resetKeepingWaits(NativeAtomicAccess<T> access, T t, long offset) {
        putCountWord(access, t, offset, 0);
    }

    @Override
    public <T> boolean tryReadLock(NativeAtomicAccess<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset);
        int countWord = countWord(lockWord);
        if (!writeLocked(countWord) && waitWord(lockWord) == 0) {
            checkReadCountForIncrement(countWord);
            if (casCountWord(access, t, offset, countWord, countWord + READ_PARTY))
                return true;
        }
        return false;
    }

    @Override
    public <T> boolean tryUpgradeReadToUpdateLock(NativeAtomicAccess<T> access, T t, long offset) {
        int countWord = getCountWord(access, t, offset);
        checkReadLocked(countWord);
        return !updateLocked(countWord) &&
                casCountWord(access, t, offset, countWord, countWord - READ_PARTY + UPDATE_PARTY);
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLock(NativeAtomicAccess<T> access, T t, long offset) {
        int countWord = getCountWord(access, t, offset);
        checkReadLocked(countWord);
        return countWord == READ_PARTY &&
                casCountWord(access, t, offset, READ_PARTY, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            NativeAtomicAccess<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset);
        int countWord = countWord(lockWord);
        checkReadLocked(countWord);
        return countWord == READ_PARTY &&
                tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    private static <T> boolean tryWriteLockAndDeregisterWait0(
            NativeAtomicAccess<T> access, T t, long offset, long lockWord) {
        int waitWord = waitWord(lockWord);
        checkWaitWordForDecrement(waitWord);
        return casLockWord(access, t, offset, lockWord,
                lockWord(WRITE_LOCKED_COUNT_WORD, waitWord - WAIT_PARTY));
    }

    @Override
    public <T> boolean tryUpdateLock(NativeAtomicAccess<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset);
        int countWord = countWord(lockWord);
        if (!updateLocked(countWord) && !writeLocked(countWord) && waitWord(lockWord) == 0) {
            if (casCountWord(access, t, offset, countWord, countWord + UPDATE_PARTY))
                return true;
        }
        return false;
    }

    @Override
    public <T> boolean tryWriteLock(NativeAtomicAccess<T> access, T t, long offset) {
        return getCountWord(access, t, offset) == 0 &&
                casCountWord(access, t, offset, 0, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public <T> boolean tryWriteLockAndDeregisterWait(
            NativeAtomicAccess<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset);
        int countWord = countWord(lockWord);
        return countWord == 0 && tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    @Override
    public <T> void registerWait(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            int waitWord = getWaitWord(access, t, offset);
            checkWaitWordForIncrement(waitWord);
            if (casWaitWord(access, t, offset, waitWord, waitWord + WAIT_PARTY))
                return;
        }
    }

    @Override
    public <T> void deregisterWait(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            int waitWord = getWaitWord(access, t, offset);
            checkWaitWordForDecrement(waitWord);
            if (casWaitWord(access, t, offset, waitWord, waitWord - WAIT_PARTY))
                return;
        }
    }

    @Override
    public <T> boolean tryUpgradeUpdateToWriteLock(NativeAtomicAccess<T> access, T t, long offset) {
        int countWord = getCountWord(access, t, offset);
        return checkExclusiveUpdateLocked(countWord) &&
                casCountWord(access, t, offset, countWord, WRITE_LOCKED_COUNT_WORD);
    }

    private static boolean checkExclusiveUpdateLocked(int countWord) {
        checkUpdateLocked(countWord);
        return countWord == UPDATE_PARTY;
    }

    @Override
    public <T> boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(
            NativeAtomicAccess<T> access, T t, long offset) {
        long lockWord = getLockWord(access, t, offset);
        int countWord = countWord(lockWord);
        return checkExclusiveUpdateLocked(countWord) &&
                tryWriteLockAndDeregisterWait0(access, t, offset, lockWord);
    }

    @Override
    public <T> void readUnlock(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset);
            checkReadLocked(countWord);
            if (casCountWord(access, t, offset, countWord, countWord - READ_PARTY))
                return;
        }
    }

    @Override
    public <T> void updateUnlock(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset);
            checkUpdateLocked(countWord);
            if (casCountWord(access, t, offset, countWord, countWord - UPDATE_PARTY)) {
                return;
            }
        }
    }

    @Override
    public <T> void downgradeUpdateToReadLock(NativeAtomicAccess<T> access, T t, long offset) {
        while (true) {
            int countWord = getCountWord(access, t, offset);
            checkUpdateLocked(countWord);
            checkReadCountForIncrement(countWord);
            if (casCountWord(access, t, offset, countWord, countWord - UPDATE_PARTY + READ_PARTY)) {
                return;
            }
        }
    }

    @Override
    public <T> void writeUnlock(NativeAtomicAccess<T> access, T t, long offset) {
        checkWriteLockedAndPut(access, t, offset, 0);
    }

    private static <T> void checkWriteLockedAndPut(
            NativeAtomicAccess<T> access, T t, long offset, int countWord) {
        checkWriteLocked(getCountWord(access, t, offset));
        putCountWord(access, t, offset, countWord);
    }

    @Override
    public <T> void downgradeWriteToUpdateLock(NativeAtomicAccess<T> access, T t, long offset) {
        checkWriteLockedAndPut(access, t, offset, UPDATE_PARTY);
    }

    @Override
    public boolean isUpdateLocked(long state) {
        return updateLocked(countWord(state));
    }

    @Override
    public <T> void downgradeWriteToReadLock(NativeAtomicAccess<T> access, T t, long offset) {
        checkWriteLockedAndPut(access, t, offset, READ_PARTY);
    }

    @Override
    public <T> long getState(NativeAtomicAccess<T> access, T t, long offset) {
        return getLockWord(access, t, offset);
    }

    @Override
    public int readLockCount(long state) {
        return readCount(countWord(state));
    }

    @Override
    public boolean isWriteLocked(long state) {
        return writeLocked(countWord(state));
    }

    @Override
    public int waitCount(long state) {
        return waitWord(state);
    }

    @Override
    public boolean isLocked(long state) {
        return countWord(state) != 0;
    }

    @Override
    public int lockCount(long state) {
        int countWord = countWord(state);
        int lockCount = readCount(countWord);
        if (lockCount > 0) {
            return lockCount + (updateLocked(countWord) ? 1 : 0);

        } else {
            return writeLocked(countWord) ? 1 : 0;
        }
    }

    @Override
    public String toString(long state) {
        return "[read locks = " + readLockCount(state) +
                ", update locked = " + isUpdateLocked(state) +
                ", write locked = " + isWriteLocked(state) +
                ", waits = " + waitCount(state) + "]";
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }
}
