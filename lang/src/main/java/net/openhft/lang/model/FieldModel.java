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

package net.openhft.lang.model;

import net.openhft.lang.model.constraints.Group;
import net.openhft.lang.model.constraints.Digits;
import net.openhft.lang.model.constraints.MaxSize;
import net.openhft.lang.model.constraints.Range;

import java.lang.reflect.Method;

/**
 * User: peter.lawrey
 * Date: 06/10/13
 * Time: 18:22
 */
public interface FieldModel<T> {
    String name();

    Method getter();

    Method setter();

    Method indexedGetter();

    Method indexedSetter();

    Method volatileGetter();

    Method orderedSetter();

    Method volatileIndexedGetter();

    Method orderedIndexedSetter();

    Method getUsing();

    Method adder();

    Method atomicAdder();

    Method cas();

    Method tryLockNanos();

    Method tryLock();

    Method busyLock();

    Method unlock();

    Method sizeOf();

    Class<T> type();

    int heapSize();

    int nativeSize();

    Digits digits();

    Range range();

    MaxSize size();

    MaxSize indexSize();

    boolean isArray();

    public boolean isVolatile();

    public void setVolatile(boolean isVolatile);

    Group group();
}
