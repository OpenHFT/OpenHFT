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

import sun.misc.Cleaner;

/**
 * @author peter.lawrey
 */
public class DirectStore {
    private final Cleaner cleaner;
    protected final BytesMarshallerFactory bytesMarshallerFactory;
    protected long address;
    protected long size;

    public static DirectStore allocate(long size) {
        return new DirectStore(null, size);
    }

    public DirectStore(BytesMarshallerFactory bytesMarshallerFactory, long size) {
        this.bytesMarshallerFactory = bytesMarshallerFactory;
        address = NativeBytes.UNSAFE.allocateMemory(size);
//        System.out.println("old value " + Integer.toHexString(NativeBytes.UNSAFE.getInt(null, address)));
        NativeBytes.UNSAFE.setMemory(address, size, (byte) 0);
        NativeBytes.UNSAFE.putLongVolatile(null, address, 0L);

        this.size = size;
        cleaner = Cleaner.create(this, new Runnable() {
            @Override
            public void run() {
                if (address > 0)
                    NativeBytes.UNSAFE.freeMemory(address);
                address = DirectStore.this.size = 0;
            }
        });
    }

    public void resize(long newSize) {
        if (newSize == size)
            return;
        address = NativeBytes.UNSAFE.reallocateMemory(address, newSize);
        size = newSize;
    }

    public DirectBytes createSlice() {
        return new DirectBytes(this);
    }

    public void free() {
        cleaner.clean();
    }
}
