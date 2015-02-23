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

import java.nio.ByteOrder;

import static net.openhft.lang.io.NativeBytes.UNSAFE;

final class UnsafeAtomicAccess extends NativeAtomicAccess {
    static final UnsafeAtomicAccess INSTANCE = new UnsafeAtomicAccess();

    @Override
    public long getLongVolatile(Object o, long offset) {
        return UNSAFE.getLongVolatile(o, offset);
    }

    @Override
    public void putOrderedLong(Object o, long offset, long value) {
        UNSAFE.putOrderedLong(o, offset, value);
    }

    @Override
    public boolean compareAndSwapLong(Object o, long offset, long expected, long x) {
        return UNSAFE.compareAndSwapLong(o, offset, expected, x);
    }

    @Override
    public int getIntVolatile(Object o, long offset) {
        return UNSAFE.getInt(o, offset);
    }

    @Override
    public void putOrderedInt(Object o, long offset, int value) {
        UNSAFE.putOrderedInt(o, offset, value);
    }

    @Override
    public boolean compareAndSwapInt(Object o, long offset, int expected, int x) {
        return UNSAFE.compareAndSwapInt(o, offset, expected, x);
    }
}
