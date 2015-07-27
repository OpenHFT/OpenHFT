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
