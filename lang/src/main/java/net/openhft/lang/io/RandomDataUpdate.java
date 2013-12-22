/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
     * Lock across processes
     * <p/>
     * Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @return did it lock or not.
     */
    boolean tryLockLong(long offset);

    /**
     * Lock across processes
     * <p/>
     * Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
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
     * <p/>
     * Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
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
     * <p/>
     * Lock which uses 8 bytes.  It store the lower 32 bits of the Thread Id, 16 bits are the process id and the re-entrant count as 16 bit.  This
     * means if you create more than 16 million threads you can get a collision, and if you try to re-enter 65535 times
     * you will get an ISE
     *
     * @param offset of the start of the 8-byte lock
     * @throws IllegalMonitorStateException if this thread doesn't hold the lock
     */
    void unlockLong(long offset) throws IllegalMonitorStateException;
}
