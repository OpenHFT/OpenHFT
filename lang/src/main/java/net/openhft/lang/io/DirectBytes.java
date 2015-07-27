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

package net.openhft.lang.io;

import net.openhft.lang.model.constraints.NotNull;

import java.nio.ByteBuffer;
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

        setStartPositionAddress(store.address() + offset);
        capacityAddr = limitAddr = startAddr + size;
    }

    public BytesStore store() {
        return store;
    }

    @Override
    protected void cleanup() {
        store.free();
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, store);
    }
}
