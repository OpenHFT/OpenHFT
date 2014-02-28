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
        store = new DirectStore(null, length * size);
        ((Byteable) ref).bytes(store.createSlice());
        recycle(ref);
    }

    private T createRef() {
        T ref = DataValueClasses.newInstance(tClass);
        ((Byteable) ref).bytes(store.createSlice());
        return ref;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public T get(long index) {
        T t = acquire();
        DirectBytes bytes = (DirectBytes) ((Byteable) t).bytes();
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
