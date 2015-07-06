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

public class MultiStoreBytes extends NativeBytes {
    private Bytes underlyingBytes;
    private long underlyingOffset;

    public MultiStoreBytes() {
        super(NO_PAGE, NO_PAGE);
    }

    public void storePositionAndSize(BytesStore store, long offset, long size) {
        if (offset < 0 || size < 0 || offset + size > store.size())
            throw new IllegalArgumentException("offset: " + offset + ", size: " + size + ", store.size: " + store.size());
        setObjectSerializer(store.objectSerializer());

        setStartPositionAddress(store.address() + offset);
        capacityAddr = limitAddr = startAddr + size;
        underlyingBytes = null;
        underlyingOffset = 0;
    }

    public void setBytesOffset(Bytes bytes, long offset) {
        setObjectSerializer(bytes.objectSerializer());

        long bytesAddr = bytes.address();
        setStartPositionAddress(bytesAddr + offset);
        capacityAddr = limitAddr = bytesAddr + bytes.capacity();
        underlyingBytes = bytes;
        underlyingOffset = offset;
    }

    public Bytes underlyingBytes() {
        if (underlyingBytes == null) throw new IllegalStateException();
        return underlyingBytes;
    }

    public long underlyingOffset() {
        return underlyingOffset;
    }
}

