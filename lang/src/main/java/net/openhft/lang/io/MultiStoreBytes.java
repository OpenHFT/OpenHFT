/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        this.objectSerializer = store.objectSerializer();
        assert checkSingleThread();
        startAddr = positionAddr = store.address() + offset;
        capacityAddr = limitAddr = startAddr + size;
        underlyingBytes = null;
        underlyingOffset = 0;
    }

    public void setBytesOffset(Bytes bytes, long offset) {
        this.objectSerializer = bytes.objectSerializer();
        assert checkSingleThread();
        long bytesAddr = bytes.address();
        startAddr = positionAddr = bytesAddr + offset;
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

