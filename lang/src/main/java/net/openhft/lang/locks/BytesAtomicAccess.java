/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.locks;

import net.openhft.lang.io.Bytes;

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
