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

public interface ReadWriteLockingStrategy extends LockingStrategy {

    boolean tryReadLock(long address);
    boolean tryReadLock(Bytes bytes, long offset);

    boolean tryWriteLock(long address);
    boolean tryWriteLock(Bytes bytes, long offset);

    boolean tryUpgradeReadToWriteLock(long address);
    boolean tryUpgradeReadToWriteLock(Bytes bytes, long offset);

    void readUnlock(long address);
    void readUnlock(Bytes bytes, long offset);

    void writeUnlock(long address);
    void writeUnlock(Bytes bytes, long offset);

    void downgradeWriteToReadLock(long address);
    void downgradeWriteToReadLock(Bytes bytes, long offset);

    boolean isReadLocked(long state);

    boolean isWriteLocked(long state);

    int readLockCount(long state);
}
