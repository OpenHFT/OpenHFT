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

package net.openhft.lang.values;

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
