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

/**
 * This class supports updates which are a companion to RandomDataInput and RandomDataOutput
 */
public interface RandomDataUpdate {

    byte addByte(long offset, byte b);

    int addUnsignedByte(long offset, int i);

    short addShort(long offset, short s);

    int addUnsignedShort(long offset, int i);

    int addInt(long offset, int i);

    long addUnsignedInt(long offset, long i);

    long addLong(long offset, long i);

    float addFloat(long offset, float f);

    double addDouble(long offset, double d);

    int addAtomicInt(long offset, int i);

    long addAtomicLong(long offset, long l);

    float addAtomicFloat(long offset, float f);

    double addAtomicDouble(long offset, double d);

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @return did it lock or not.
     */
    boolean tryLockInt(long offset);

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @param nanos  to try to lock for
     * @return did it lock or not.
     */
    boolean tryLockNanosInt(long offset, long nanos);

    /**
     * Lock which uses 4 bytes.  It store the lower 24 bits of the Thread Id and the re-entrant count as 8 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 255 times
     * you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @throws InterruptedException  if interrupted
     * @throws IllegalStateException if the thread tries to lock it 255 nested time (without an unlock)
     */
    void busyLockInt(long offset) throws InterruptedException, IllegalStateException;

    /**
     * Lock which uses 4 bytes.  Unlock this It store the lower 24 bits of the Thread Id and the re-entrant count as 8
     * bit.  This means if you create more than 16 million threads you can get a collision, and if you try to re-enter
     * 255 times you will get an ISE
     *
     * @param offset of the start of the 4-byte lock
     * @throws IllegalMonitorStateException if this thread doesn't hold the lock
     */
    void unlockInt(long offset) throws IllegalMonitorStateException;

    /**
     * Lock which uses 4 bytes.  Reset forces the lock to be cleared. Use this only when the program believes the
     * locked thread is dead.
     *
     * @param offset of the start of the 4-byte lock
     */
    void resetLockInt(long offset);

    /**
     * Lock which uses 4 bytes.  This returns the lower bytes which contain the threadId.
     *
     * @param offset of the start of the 4-byte lock
     * @return the threadId or 0 if no thread holds the lock.
     */
    int threadIdForLockInt(long offset);

    /**
     * Lock across processes
     *
     * <p>Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @return did it lock or not.
     */
    boolean tryLockLong(long offset);

    /**
     * Lock across processes
     *
     * <p>Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @param nanos  to try to lock for
     * @return did it lock or not.
     */
    boolean tryLockNanosLong(long offset, long nanos);

    /**
     * Lock across processes
     *
     * <p>Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @throws InterruptedException  if interrupted
     * @throws IllegalStateException if the thread tries to lock it 65535 nested time (without an unlock)
     */
    void busyLockLong(long offset) throws InterruptedException, IllegalStateException;

    /**
     * Lock across processes
     *
     * <p>Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @throws IllegalMonitorStateException if this thread doesn't hold the lock
     */
    void unlockLong(long offset) throws IllegalMonitorStateException;

    /**
     * Lock which uses 8 bytes.  Reset forces the lock to be cleared. Use this only when the program believes the
     * locked thread is dead.
     *
     * @param offset of the start of the 8-byte lock
     */
    void resetLockLong(long offset);

    /**
     * Lock which uses 8 bytes.  This returns the lower bytes which contain the threadId.
     *
     * @param offset of the start of the 8-byte lock
     * @return the threadId or 0 if no thread holds the lock.
     */
    long threadIdForLockLong(long offset);

    /**
     * Uses the 64-bit long at the offset as a non-reentrant read/write lock.
     * There can be up to 2^20-1 threads reading, or waiting to read on a lock.
     *
     * @param offset    of the long monitor
     * @param timeOutNS length of time to busy wait for the lock.
     * @return if the lock could be obtained in time.
     * @throws java.lang.IllegalStateException if the monitor is in an illegal state
     */
    public boolean tryRWReadLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException;

    /**
     * Uses the 64-bit long at the offset as a non-reentrant read/write lock.
     * There can be up to 2^20-1 threads reading, or waiting to read on a lock.
     *
     * @param offset    of the long monitor
     * @param timeOutNS length of time to busy wait for the lock.
     * @return if the lock could be obtained in time.
     * @throws java.lang.IllegalStateException if the monitor is in an illegal state
     */
    public boolean tryRWWriteLock(long offset, long timeOutNS) throws IllegalStateException, InterruptedException;

    /**
     * Uses the 64-bit long at the offset as a non-reentrant read/write lock.
     * There can be up to 2^20-1 threads reading, or waiting to read on a lock.
     *
     * @param offset of the long monitor
     * @throws java.lang.IllegalStateException if the monitor is in an illegal state
     */
    public void unlockRWReadLock(long offset) throws IllegalStateException;

    /**
     * Uses the 64-bit long at the offset as a non-reentrant read/write lock.
     * There can be up to 2^20-1 threads reading, or waiting to read on a lock.
     *
     * @param offset of the long monitor
     * @throws java.lang.IllegalStateException if the monitor is in an illegal state
     */
    public void unlockRWWriteLock(long offset) throws IllegalStateException;

}
