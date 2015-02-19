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

public interface ReadWriteWithWaitsLockingStrategy extends ReadWriteLockingStrategy {

    void registerWait(long address);
    void registerWait(Bytes bytes, long offset);

    void deregisterWait(long address);
    void deregisterWait(Bytes bytes, long offset);

    boolean tryWriteLockAndDeregisterWait(long address);
    boolean tryWriteLockAndDeregisterWait(Bytes bytes, long offset);

    boolean tryUpgradeReadToWriteLockAndDeregisterWait(long address);
    boolean tryUpgradeReadToWriteLockAndDeregisterWait(Bytes bytes, long offset);

    int waitCount(long state);
}
