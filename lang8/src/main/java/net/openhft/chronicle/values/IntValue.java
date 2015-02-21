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

package net.openhft.chronicle.values;

/**
 * User: peter.lawrey
 * Date: 10/10/13
 * Time: 07:15
 */
public interface IntValue {
    int getValue();

    void setValue(int value);

    int getVolatileValue();

    void setOrderedValue(int value);

    int addValue(int delta);

    int addAtomicValue(int delta);

    boolean compareAndSwapValue(int expected, int value);

    boolean tryLockValue();

    boolean tryLockNanosValue(long nanos);

    void busyLockValue() throws InterruptedException, IllegalStateException;

    void unlockValue() throws IllegalMonitorStateException;

}
