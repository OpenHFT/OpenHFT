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

/**
 * Logic of read-write-update lock state transitions.
 *
 * Read lock - could be several at the same time.
 * Update lock - doesn't block reads, but couldn't be several update locks at the same time
 * Write lock - exclusive
 */
public interface ReadWriteUpdateLockingStrategy extends ReadWriteLockingStrategy {

    <T> boolean tryUpdateLock(NativeAtomicAccess<T> access, T t, long offset);

    <T> boolean tryUpgradeReadToUpdateLock(NativeAtomicAccess<T> access, T t, long offset);

    <T> boolean tryUpgradeUpdateToWriteLock(NativeAtomicAccess<T> access, T t, long offset);

    <T> void updateUnlock(NativeAtomicAccess<T> access, T t, long offset);

    <T> void downgradeUpdateToReadLock(NativeAtomicAccess<T> access, T t, long offset);

    <T> void downgradeWriteToUpdateLock(NativeAtomicAccess<T> access, T t, long offset);

    boolean isUpdateLocked(long state);
}
