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
import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.Copyable;
import net.openhft.lang.model.DataValueClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * User: peter.lawrey Date: 08/10/13 Time: 08:11
 */
public class HugeArrayImpl<T> implements HugeArray<T> {
    private static final int MAX_SIZE = 10;
    private final Class<T> tClass;
    private final long length;
    private final int size;
    private final DirectStore store;
    private final List<T> freeList = new ArrayList<T>(MAX_SIZE);

    public HugeArrayImpl(Class<T> tClass, long length) {
        this.tClass = tClass;
        this.length = length;

        T ref = DataValueClasses.newDirectReference(tClass);
        size = ((Byteable) ref).maxSize();
        store = DirectStore.allocate(length * size);
        ((Byteable) ref).bytes(store.bytes(), 0L);
        recycle(ref);
    }

    private T createRef() {
        T ref = DataValueClasses.newDirectReference(tClass);
        ((Byteable) ref).bytes(store.bytes(), 0L);
        return ref;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public T get(long index) {
        T t = acquire();
        Byteable byteable = (Byteable) t;
        DirectBytes bytes = (DirectBytes) byteable.bytes();
        bytes.positionAndSize(index * size, size);
        return t;
    }

    @Override
    public void get(long index, T element) {
        if (tClass.isInstance(element)) {
            DirectBytes bytes = (DirectBytes) ((Byteable) element).bytes();
            bytes.positionAndSize(index * size, size);
            return;
        }
        T t = acquire();
        DirectBytes bytes = (DirectBytes) ((Byteable) t).bytes();
        bytes.positionAndSize(index * size, size);
        ((Copyable) element).copyFrom(t);
        recycle(t);
    }

    private T acquire() {
        int size = freeList.size();
        if (size > 0)
            return freeList.remove(size - 1);
        return createRef();
    }

    @Override
    public void copyTo(long index, T to) {
        T from = get(index);
        ((Copyable<T>) to).copyFrom(from);
        recycle(from);
    }

    @Override
    public void set(long index, T from) {
        T to = get(index);
        ((Copyable<T>) to).copyFrom(from);
        recycle(to);
    }

    @Override
    public void recycle(T t) {
        if (freeList.size() < MAX_SIZE) {
            assert ((DirectBytes) ((Byteable) t).bytes()).store() == store;
            assert !freeList.contains(t) : "recycling object already recycled";
            freeList.add(t);
        }
    }
}
