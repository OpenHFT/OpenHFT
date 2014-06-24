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

public class MultiStoreBytes extends NativeBytes {
    public MultiStoreBytes() {
        super(NO_PAGE, NO_PAGE);
    }

    public void storePositionAndSize(BytesStore store, long offset, long size) {
        if (offset < 0 || size < 0 || offset + size > store.size())
            throw new IllegalArgumentException("offset: " + offset + ", size: " + size + ", store.size: " + store.size());
        this.objectSerializer = store.objectSerializer();
        startAddr = positionAddr = store.address() + offset;
        capacityAddr = limitAddr = startAddr + size;
    }
}

