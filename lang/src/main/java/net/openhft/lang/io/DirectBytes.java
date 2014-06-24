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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author peter.lawrey
 */
public class DirectBytes extends NativeBytes {
    @NotNull
    private final BytesStore store;

    public DirectBytes(@NotNull BytesStore store, AtomicInteger refCount) {
        super(store.objectSerializer(), store.address(), store.address() + store.size(), refCount);
        this.store = store;
    }

    public DirectBytes(@NotNull BytesStore store, AtomicInteger refCount, long offset, long length) {
        super(store.objectSerializer(), store.address() + offset, store.address() + offset + length, refCount);
        this.store = store;
    }

    public void positionAndSize(long offset, long size) {
        if (offset < 0 || size < 0 || offset + size > store.size())
            throw new IllegalArgumentException();
        startAddr = positionAddr = store.address() + offset;
        capacityAddr = limitAddr = startAddr + size;
    }

    public BytesStore store() {
        return store;
    }

    @Override
    protected void cleanup() {
        store.free();
    }
}
