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

package net.openhft.lang.model;

import net.openhft.lang.model.constraints.Digits;
import net.openhft.lang.model.constraints.Group;
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
