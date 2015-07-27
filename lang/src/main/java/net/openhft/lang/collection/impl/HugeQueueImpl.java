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

package net.openhft.lang.collection.impl;

import net.openhft.lang.collection.HugeArray;
import net.openhft.lang.collection.HugeQueue;

/**
 * User: peter.lawrey
 * Date: 08/10/13
 * Time: 13:31
 */
public class HugeQueueImpl<T> implements HugeQueue<T> {
    private final HugeArray<T> array;
    private final long size;
    private long start, end;

    public HugeQueueImpl(HugeArray<T> tHugeArray, long size) {
        array = tHugeArray;
        this.size = size;
    }

    public boolean isFull() {
        return next(end) == start;
    }

    private long next(long end) {
        return (end + 1) % size;
    }

    public boolean isEmpty() {
        return end == start;
    }

    @Override
    public boolean offer(T element) {
        if (isFull())
            return false;
        array.set(end % size, element);
        end = next(end);
        return true;
    }

    @Override
    public T take() {
        if (isEmpty())
            return null;
        long pos = start % size;
        start = next(start);
        return array.get(pos);
    }

    @Override
    public boolean takeCopy(T element) {
        if (isEmpty())
            return false;
        array.get(start % size, element);
        start = next(start);
        return true;
    }

    @Override
    public T offer() {
        if (isFull())
            return null;
        T t = array.get(end % size);
        end = next(end);
        return t;
    }

    @Override
    public void recycle(T element) {
        array.recycle(element);
    }
}
