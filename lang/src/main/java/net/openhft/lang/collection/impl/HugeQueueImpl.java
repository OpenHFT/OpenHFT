/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.collection.impl;

import net.openhft.lang.collection.HugeArray;
import net.openhft.lang.collection.HugeQueue;

/**
 * User: plawrey
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
