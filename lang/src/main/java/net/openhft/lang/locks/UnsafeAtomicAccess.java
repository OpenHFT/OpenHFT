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
