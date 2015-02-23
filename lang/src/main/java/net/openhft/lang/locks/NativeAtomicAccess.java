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

public abstract class NativeAtomicAccess<T> {

    public static <T> NativeAtomicAccess<T> unsafe() {
        return UnsafeAtomicAccess.INSTANCE;
    }

    public static NativeAtomicAccess<Bytes> toBytes() {
        return BytesAtomicAccess.INSTANCE;
    }

    protected NativeAtomicAccess() {}

    public abstract long getLongVolatile(T t, long offset);

    public abstract void putOrderedLong(T t, long offset, long value);

    public abstract boolean compareAndSwapLong(T t, long offset, long expected, long x);

    public abstract int getIntVolatile(T t, long offset);

    public abstract void putOrderedInt(T t, long offset, int value);

    public abstract boolean compareAndSwapInt(T t, long offset, int expected, int x);
}
