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

import net.openhft.lang.ReferenceCounted;
import net.openhft.lang.io.serialization.ObjectSerializer;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class MappedMemory implements ReferenceCounted, BytesStore {
    private final MappedByteBuffer buffer;
    private final DirectByteBufferBytes bytes;
    private final long index;
    private final AtomicInteger refCount = new AtomicInteger(0);
    private volatile boolean unmapped = false;

    public MappedMemory(MappedByteBuffer buffer, long index) {
        this.buffer = buffer;
        this.index = index;
        bytes = new DirectByteBufferBytes(buffer);
    }

    public long index() {
        return index;
    }

    @Override
    public void reserve() {
        if (unmapped) throw new IllegalStateException();
        refCount.incrementAndGet();
    }

    /**
     * @return true if release cause the ref count to drop to zero and the resource was freed
     */
    @Override
    public boolean release() {
        if (unmapped) throw new IllegalStateException();
        if (refCount.decrementAndGet() > 0) return false;
        close();
        return true;
    }

    @Override
    public int refCount() {
        return refCount.get();
    }

    public MappedByteBuffer buffer() {
        return buffer;
    }

    public long address() {
        return ((DirectBuffer) buffer).address();
    }

    @Override
    public long size() {
        return bytes.capacity();
    }

    @Override
    public void free() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSerializer objectSerializer() {
        return null;
    }

    @Override
    public File file() {
        throw new UnsupportedOperationException();
    }

    public Bytes bytes() {
        return bytes;
    }

    @Override
    public Bytes bytes(long offset, long length) {
        throw new UnsupportedOperationException();
    }

    public static void release(MappedMemory mapmem) {
        if (mapmem != null)
            mapmem.release();
    }

    public void force() {
        if (!unmapped)
            buffer.force();
    }

    public void close() {
        IOTools.clean(buffer);
        unmapped = true;
    }
}
