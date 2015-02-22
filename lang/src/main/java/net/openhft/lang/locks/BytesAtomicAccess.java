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

import static java.nio.ByteOrder.nativeOrder;

final class BytesAtomicAccess extends NativeAtomicAccess<Bytes> {
    static final BytesAtomicAccess INSTANCE = new BytesAtomicAccess();

    @Override
    public long getLongVolatile(Bytes bytes, long offset) {
        long value = bytes.readVolatileLong(offset);
        if (bytes.byteOrder() != nativeOrder())
            value = Long.reverseBytes(value);
        return value;
    }

    @Override
    public void putOrderedLong(Bytes bytes, long offset, long value) {
        if (bytes.byteOrder() != nativeOrder())
            value = Long.reverseBytes(value);
        bytes.writeOrderedLong(offset, value);
    }

    @Override
    public boolean compareAndSwapLong(Bytes bytes, long offset, long expected, long x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Long.reverseBytes(expected);
            x = Long.reverseBytes(x);
        }
        return bytes.compareAndSwapLong(offset, expected, x);
    }

    @Override
    public int getIntVolatile(Bytes bytes, long offset) {
        int value = bytes.readVolatileInt(offset);
        if (bytes.byteOrder() != nativeOrder())
            value = Integer.reverseBytes(value);
        return value;
    }

    @Override
    public void putOrderedInt(Bytes bytes, long offset, int value) {
        if (bytes.byteOrder() != nativeOrder())
            value = Integer.reverseBytes(value);
        bytes.writeOrderedInt(offset, value);
    }

    @Override
    public boolean compareAndSwapInt(Bytes bytes, long offset, int expected, int x) {
        if (bytes.byteOrder() != nativeOrder()) {
            expected = Integer.reverseBytes(expected);
            x = Integer.reverseBytes(x);
        }
        return bytes.compareAndSwapInt(offset, expected, x);
    }
}
