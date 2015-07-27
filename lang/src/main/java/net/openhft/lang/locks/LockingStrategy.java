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

public interface LockingStrategy {

    <T> boolean tryLock(NativeAtomicAccess<T> access, T t, long offset);

    <T> void unlock(NativeAtomicAccess<T> access, T t, long offset);

    <T> void reset(NativeAtomicAccess<T> access, T t, long offset);

    long resetState();

    <T> long getState(NativeAtomicAccess<T> access, T t, long offset);

    boolean isLocked(long state);

    int lockCount(long state);

    String toString(long state);

    int sizeInBytes();
}
