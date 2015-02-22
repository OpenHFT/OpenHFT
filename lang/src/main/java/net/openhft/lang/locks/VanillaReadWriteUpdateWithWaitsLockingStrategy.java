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

import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.ByteOrder.nativeOrder;
import static net.openhft.lang.io.AbstractBytes.UNSIGNED_INT_MASK;
import static net.openhft.lang.io.NativeBytes.UNSAFE;

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

    private static int countWordShift(ByteOrder order) {
        return order == LITTLE_ENDIAN ? 0 : 32;
    }

    private static int waitWordShift(ByteOrder order) {
        return order == LITTLE_ENDIAN ? 32 : 0;
    }

    static final int COUNT_WORD_SHIFT = countWordShift(nativeOrder());
    static final int WAIT_WORD_SHIFT = waitWordShift(nativeOrder());

    static final int READ_BITS = 30;
    static final int MAX_READ = (1 << READ_BITS) - 1;
    static final int READ_MASK = MAX_READ;
    static final int READ_PARTY = 1;

    static final int UPDATE_PARTY = 1 << READ_BITS;
    static final int WRITE_LOCKED_COUNT_WORD = UPDATE_PARTY << 1;

    static final int MAX_WAIT = Integer.MAX_VALUE;
    static final int WAIT_PARTY = 1;

    private static long getLockWord(long address) {
        return UNSAFE.getLongVolatile(null, address);
    }

    private static long getLockWord(Bytes bytes, long offset) {
        long lockWord = bytes.readVolatileLong(offset);
        if (bytes.byteOrder() != nativeOrder())
            lockWord = Long.reverseBytes(lockWord);
        return lockWord;
    }

    private static boolean casLockWord(long address, long expected, long x) {
        return UNSAFE.compareAndSwapLong(null, address, expected, x);
    }

    private static boolean casLockWord(Bytes bytes, long offset, long expected, long x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Long.reverseBytes(expected);
            x = Long.reverseBytes(x);
        }
        return bytes.compareAndSwapLong(offset, expected, x);
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

    private static int getCountWord(long address) {
        return UNSAFE.getIntVolatile(null, address + COUNT_WORD_OFFSET);
    }

    private static int getCountWord(Bytes bytes, long offset) {
        int countWord = bytes.readVolatileInt(offset + COUNT_WORD_OFFSET);
        if (bytes.byteOrder() != nativeOrder())
            countWord = Integer.reverseBytes(countWord);
        return countWord;
    }

    private static boolean casCountWord(long address, int expected, int x) {
        return UNSAFE.compareAndSwapInt(null, address + COUNT_WORD_OFFSET, expected, x);
    }

    private static boolean casCountWord(Bytes bytes, long offset, int expected, int x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Integer.reverseBytes(expected);
            x = Integer.reverseBytes(x);
        }
        return bytes.compareAndSwapInt(offset + COUNT_WORD_OFFSET, expected, x);
    }

    private static void putCountWord(long address, int countWord) {
        UNSAFE.putOrderedInt(null, address + COUNT_WORD_OFFSET, countWord);
    }

    private static void putCountWord(Bytes bytes, long offset, int countWord) {
        if (bytes.byteOrder() != nativeOrder())
            countWord = Integer.reverseBytes(countWord);
        bytes.writeOrderedInt(offset + COUNT_WORD_OFFSET, countWord);
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

    private static int getWaitWord(long address) {
        return UNSAFE.getIntVolatile(null, address + WAIT_WORD_OFFSET);
    }

    private static int getWaitWord(Bytes bytes, long offset) {
        int waitWord = bytes.readVolatileInt(offset + WAIT_WORD_OFFSET);
        if (bytes.byteOrder() != nativeOrder())
            waitWord = Integer.reverseBytes(waitWord);
        return waitWord;
    }

    private static boolean casWaitWord(long address, int expected, int x) {
        return UNSAFE.compareAndSwapInt(null, address + WAIT_WORD_OFFSET, expected, x);
    }

    private static boolean casWaitWord(Bytes bytes, long offset, int expected, int x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Integer.reverseBytes(expected);
            x = Integer.reverseBytes(x);
        }
        return bytes.compareAndSwapInt(offset + WAIT_WORD_OFFSET, expected, x);
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
    public void reset(long address) {
        UNSAFE.putOrderedLong(null, address, 0L);
    }

    @Override
    public void reset(Bytes bytes, long offset) {
        bytes.writeOrderedLong(offset, 0L);
    }

    @Override
    public boolean tryReadLock(long address) {
        long lockWord = getLockWord(address);
        int countWord = countWord(lockWord);
        if (!writeLocked(countWord) && waitWord(lockWord) == 0) {
            checkReadCountForIncrement(countWord);
            if (casCountWord(address, countWord, countWord + READ_PARTY))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryReadLock(Bytes bytes, long offset) {
        long lockWord = getLockWord(bytes, offset);
        int countWord = countWord(lockWord);
        if (!writeLocked(countWord) && waitWord(lockWord) == 0) {
            checkReadCountForIncrement(countWord);
            if (casCountWord(bytes, offset, countWord, countWord + READ_PARTY))
                return true;
        }
        return false;
    }

    @Override
         public boolean tryUpgradeReadToUpdateLock(long address) {
        int countWord = getCountWord(address);
        checkReadLocked(countWord);
        return !updateLocked(countWord) &&
                casCountWord(address, countWord, countWord - READ_PARTY + UPDATE_PARTY);
    }

    @Override
    public boolean tryUpgradeReadToUpdateLock(Bytes bytes, long offset) {
        int countWord = getCountWord(bytes, offset);
        checkReadLocked(countWord);
        return !updateLocked(countWord) &&
                casCountWord(bytes, offset, countWord, countWord - READ_PARTY + UPDATE_PARTY);
    }

    @Override
    public boolean tryUpgradeReadToWriteLock(long address) {
        int countWord = getCountWord(address);
        checkReadLocked(countWord);
        return countWord == READ_PARTY &&
                casCountWord(address, READ_PARTY, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public boolean tryUpgradeReadToWriteLock(Bytes bytes, long offset) {
        int countWord = getCountWord(bytes, offset);
        checkReadLocked(countWord);
        return countWord == READ_PARTY &&
                casCountWord(bytes, offset, READ_PARTY, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public boolean tryUpgradeReadToWriteLockAndDeregisterWait(long address) {
        long lockWord = getLockWord(address);
        int countWord = countWord(lockWord);
        checkReadLocked(countWord);
        return countWord == READ_PARTY && tryWriteLockAndDeregisterWait0(address, lockWord);
    }

    @Override
    public boolean tryUpgradeReadToWriteLockAndDeregisterWait(Bytes bytes, long offset) {
        long lockWord = getLockWord(bytes, offset);
        int countWord = countWord(lockWord);
        checkReadLocked(countWord);
        return countWord == READ_PARTY && tryWriteLockAndDeregisterWait0(bytes, offset, lockWord);
    }

    private static boolean tryWriteLockAndDeregisterWait0(long address, long lockWord) {
        int waitWord = waitWord(lockWord);
        checkWaitWordForDecrement(waitWord);
        return casLockWord(address, lockWord,
                lockWord(WRITE_LOCKED_COUNT_WORD, waitWord - WAIT_PARTY));
    }

    private static boolean tryWriteLockAndDeregisterWait0(Bytes bytes, long offset, long lockWord) {
        int waitWord = waitWord(lockWord);
        checkWaitWordForDecrement(waitWord);
        return casLockWord(bytes, offset, lockWord,
                lockWord(WRITE_LOCKED_COUNT_WORD, waitWord - WAIT_PARTY));
    }

    @Override
    public boolean tryUpdateLock(long address) {
        long lockWord = getLockWord(address);
        int countWord = countWord(lockWord);
        if (!updateLocked(countWord) && !writeLocked(countWord) && waitWord(lockWord) == 0) {
            if (casCountWord(address, countWord, countWord + UPDATE_PARTY))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryUpdateLock(Bytes bytes, long offset) {
        long lockWord = getLockWord(bytes, offset);
        int countWord = countWord(lockWord);
        if (!updateLocked(countWord) && !writeLocked(countWord) && waitWord(lockWord) == 0) {
            if (casCountWord(bytes, offset, countWord, countWord + UPDATE_PARTY))
                return true;
        }
        return false;
    }

    @Override
    public boolean tryWriteLock(long address) {
        return getCountWord(address) == 0 && casCountWord(address, 0, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public boolean tryWriteLock(Bytes bytes, long offset) {
        return getCountWord(bytes, offset) == 0 &&
                casCountWord(bytes, offset, 0, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public boolean tryWriteLockAndDeregisterWait(long address) {
        long lockWord = getLockWord(address);
        int countWord = countWord(lockWord);
        return countWord == 0 && tryWriteLockAndDeregisterWait0(address, lockWord);
    }

    @Override
    public boolean tryWriteLockAndDeregisterWait(Bytes bytes, long offset) {
        long lockWord = getLockWord(bytes, offset);
        int countWord = countWord(lockWord);
        return countWord == 0 && tryWriteLockAndDeregisterWait0(bytes, offset, lockWord);
    }

    @Override
    public void registerWait(long address) {
        while (true) {
            int waitWord = getWaitWord(address);
            checkWaitWordForIncrement(waitWord);
            if (casWaitWord(address, waitWord, waitWord + WAIT_PARTY))
                return;
        }
    }

    @Override
    public void registerWait(Bytes bytes, long offset) {
        while (true) {
            int waitWord = getWaitWord(bytes, offset);
            checkWaitWordForIncrement(waitWord);
            if (casWaitWord(bytes, offset, waitWord, waitWord + WAIT_PARTY))
                return;
        }
    }

    @Override
    public void deregisterWait(long address) {
        while (true) {
            int waitWord = getWaitWord(address);
            checkWaitWordForDecrement(waitWord);
            if (casWaitWord(address, waitWord, waitWord - WAIT_PARTY))
                return;
        }
    }

    @Override
    public void deregisterWait(Bytes bytes, long offset) {
        while (true) {
            int waitWord = getWaitWord(bytes, offset);
            checkWaitWordForDecrement(waitWord);
            if (casWaitWord(bytes, offset, waitWord, waitWord - WAIT_PARTY))
                return;
        }
    }

    @Override
    public boolean tryUpgradeUpdateToWriteLock(long address) {
        int countWord = getCountWord(address);
        return checkExclusiveUpdateLocked(countWord) &&
                casCountWord(address, countWord, WRITE_LOCKED_COUNT_WORD);
    }

    @Override
    public boolean tryUpgradeUpdateToWriteLock(Bytes bytes, long offset) {
        int countWord = getCountWord(bytes, offset);
        return checkExclusiveUpdateLocked(countWord) &&
                casCountWord(bytes, offset, countWord, WRITE_LOCKED_COUNT_WORD);
    }

    private static boolean checkExclusiveUpdateLocked(int countWord) {
        checkUpdateLocked(countWord);
        return countWord == UPDATE_PARTY;
    }

    @Override
    public boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(long address) {
        long lockWord = getLockWord(address);
        int countWord = countWord(lockWord);
        return checkExclusiveUpdateLocked(countWord) &&
                tryWriteLockAndDeregisterWait0(address, lockWord);
    }

    @Override
    public boolean tryUpgradeUpdateToWriteLockAndDeregisterWait(Bytes bytes, long offset) {
        long lockWord = getLockWord(bytes, offset);
        int countWord = countWord(lockWord);
        return checkExclusiveUpdateLocked(countWord) &&
                tryWriteLockAndDeregisterWait0(bytes, offset, lockWord);
    }

    @Override
    public void readUnlock(long address) {
        while (true) {
            int countWord = getCountWord(address);
            checkReadLocked(countWord);
            if (casCountWord(address, countWord, countWord - READ_PARTY))
                return;
        }
    }

    @Override
    public void readUnlock(Bytes bytes, long offset) {
        while (true) {
            int countWord = getCountWord(bytes, offset);
            checkReadLocked(countWord);
            if (casCountWord(bytes, offset, countWord, countWord - READ_PARTY))
                return;
        }
    }

    @Override
    public void updateUnlock(long address) {
        while (true) {
            int countWord = getCountWord(address);
            checkUpdateLocked(countWord);
            if (casCountWord(address, countWord, countWord - UPDATE_PARTY)) {
                return;
            }
        }
    }

    @Override
    public void updateUnlock(Bytes bytes, long offset) {
        while (true) {
            int countWord = getCountWord(bytes, offset);
            checkUpdateLocked(countWord);
            if (casCountWord(bytes, offset, countWord, countWord - UPDATE_PARTY)) {
                return;
            }
        }
    }

    @Override
    public void downgradeUpdateToReadLock(long address) {
        while (true) {
            int countWord = getCountWord(address);
            checkUpdateLocked(countWord);
            checkReadCountForIncrement(countWord);
            if (casCountWord(address, countWord, countWord - UPDATE_PARTY + READ_PARTY)) {
                return;
            }
        }
    }

    @Override
    public void downgradeUpdateToReadLock(Bytes bytes, long offset) {
        while (true) {
            int countWord = getCountWord(bytes, offset);
            checkUpdateLocked(countWord);
            checkReadCountForIncrement(countWord);
            if (casCountWord(bytes, offset, countWord, countWord - UPDATE_PARTY + READ_PARTY)) {
                return;
            }
        }
    }

    @Override
    public void writeUnlock(long address) {
        checkWriteLockedAndPut(address, 0);
    }

    private static void checkWriteLockedAndPut(long address, int countWord) {
        checkWriteLocked(getCountWord(address));
        putCountWord(address, countWord);
    }

    @Override
    public void writeUnlock(Bytes bytes, long offset) {
        checkWriteLockedAndPut(bytes, offset, 0);
    }

    private static void checkWriteLockedAndPut(Bytes bytes, long offset, int countWord) {
        checkWriteLocked(getCountWord(bytes, offset));
        putCountWord(bytes, offset, countWord);
    }

    @Override
    public void downgradeWriteToUpdateLock(long address) {
        checkWriteLockedAndPut(address, UPDATE_PARTY);
    }

    @Override
    public void downgradeWriteToUpdateLock(Bytes bytes, long offset) {
        checkWriteLockedAndPut(bytes, offset, UPDATE_PARTY);
    }

    @Override
    public boolean isUpdateLocked(long state) {
        return updateLocked(countWord(state));
    }

    @Override
    public void downgradeWriteToReadLock(long address) {
        checkWriteLockedAndPut(address, READ_PARTY);
    }

    @Override
    public void downgradeWriteToReadLock(Bytes bytes, long offset) {
        checkWriteLockedAndPut(bytes, offset, READ_PARTY);
    }

    @Override
    public long getState(long address) {
        return getLockWord(address);
    }

    @Override
    public long getState(Bytes bytes, long offset) {
        return getLockWord(bytes, offset);
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
    public int sizeInBytes() {
        return 8;
    }
}
