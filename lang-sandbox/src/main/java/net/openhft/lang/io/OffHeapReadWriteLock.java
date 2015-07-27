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

import java.util.concurrent.TimeUnit;

/**
 * Created by peter.lawrey on 03/08/14.
 */
public class OffHeapReadWriteLock {
    private final Bytes bytes;
    private final long offset;
    private final ReadLock readLock;
    private final WriteLock writeLock;

    public OffHeapReadWriteLock(Bytes bytes, long offset) {
        this.bytes = bytes;
        this.offset = offset;
        this.readLock = new ReadLock();
        this.writeLock = new WriteLock();
    }

    long getLock() {
        return bytes.readVolatileLong(offset);
    }

    boolean trySetLock(long lock0, long lock2) {
        return bytes.compareAndSwapLong(offset, lock0, lock2);
    }

    static final String[] RW_MODES = {"none", "read", "write"};

    public ReadLock readLock() {
        return readLock;
    }

    public WriteLock writeLock() {
        return writeLock;
    }
    class ReadLock implements OffHeapLock {
        @Override
        public boolean tryLock() {
            long lock = getLock();
            int rwMode = (int) (lock >>> 56) & 0x3;
            if (rwMode < 2) {
                long lock2 = (lock | (1L << 56)) + (1 << 16);
                return trySetLock(lock, lock2);
            }
            return false;
        }

        @Override
        public void busyLock() {
            busyLock(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        @Override
        public boolean busyLock(long time, TimeUnit timeUnit) {
            if (tryLock()) return true;
            addWaitingReader(+1);
            try {
                long end = System.nanoTime() + timeUnit.convert(time, TimeUnit.NANOSECONDS);
                do {
                    if (tryLock()) return true;
                    Thread.yield();
                } while (System.nanoTime() < end);
            } finally {
                addWaitingReader(-1);
            }
            return false;
        }

        void addWaitingReader(int add) {
            for(;;) {
                long lock = getLock();
                int readerWaiting = (int) lock & 0xFFFF;
                int readerWaiting2 = readerWaiting + add;
                if (readerWaiting2 >= 1 << 16 || readerWaiting2 < 0)
                    // todo error state
                    return;
                if (trySetLock(lock, (lock & ~0xFFFF) + readerWaiting2))
                    return;
            }
        }

        @Override
        public void unlock() {
            for (; ; ) {
                long lock = getLock();
                long lock2 = lock - (1 << 16);
                if (trySetLock(lock, nextMode(lock2)))
                    return;
            }
        }
    }

    public String toString() {
        long lock = getLock();
        int rwMode = (int) (lock >>> 56) & 0x3;
        int writerCount = (int) (lock >>> 48) & 0xFF;
        int writerWaiting = (int) (lock >>> 32) & 0xFFFF;
        int readerCount = (int) (lock >>> 16) & 0xFFFF;
        int readerWaiting = (int) lock & 0xFFFF;
        return RW_MODES[rwMode] + " w:" + writerCount + "/" + writerWaiting + " r:" + readerCount + "/" + readerWaiting;
    }

    private long nextMode(long lock) {
        int rwMode = (int) (lock >>> 56) & 0x3;
//        int writerCount = (int) (lock >>> 48) & 0xFF;
        int writerWaiting = (int) (lock >>> 32) & 0xFFFF;
        int readerCount = (int) (lock >>> 16) & 0xFFFF;
//        int readerWaiting = (int) lock & 0xFFFF;
        if ((rwMode == 1 && readerCount == 0) || (rwMode == 2 && writerWaiting == 0))
            return lock & (~0L >>> 8); // clear mode.
        return lock;
    }

    class WriteLock implements OffHeapLock{
        @Override
        public boolean tryLock() {
            long lock = getLock();
            int rwMode = (int) (lock >>> 56) & 0x3;
            int writerCount = (int) (lock >>> 48) & 0xFF;
            if ((rwMode & 1) == 0 && ( writerCount == 0)) {
                long lock2 = (lock | (2L << 56)) + (1L << 48);
                return trySetLock(lock, lock2);
            }
            return false;
        }

        @Override
        public void busyLock() {
            busyLock(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }

        @Override
        public boolean busyLock(long time, TimeUnit timeUnit) {
            if (tryLock()) return true;
            addWaitingWriter(+1);
            try {
                long end = System.nanoTime() + timeUnit.convert(time, TimeUnit.NANOSECONDS);
                do {
                    if (tryLock()) return true;
                    Thread.yield();
                } while (System.nanoTime() < end);
            } finally {
                addWaitingWriter(-1);
            }
            return false;
        }

        void addWaitingWriter(int add) {
            for(;;) {
                long lock = getLock();
                int writerWaiting = (int) (lock >>> 32) & 0xFFFF;
                int writerWaiting2 = writerWaiting + add;
                if (writerWaiting2 >= 1 << 16 || writerWaiting2 < 0)
                    // todo error state
                    return;
                if (trySetLock(lock, (lock & ~0xFFFF00000000L) + ((long) writerWaiting2 << 32)))
                    return;
            }
        }

        @Override
        public void unlock() {
            for (; ; ) {
                long lock = getLock();
                long lock2 = lock - (1L << 48);
                if (trySetLock(lock, nextMode(lock2)))
                    return;
            }
        }
    }
}
