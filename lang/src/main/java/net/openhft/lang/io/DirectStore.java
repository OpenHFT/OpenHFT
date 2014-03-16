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

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;
import net.openhft.lang.model.constraints.NotNull;
import sun.misc.Cleaner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author peter.lawrey
 */
public class DirectStore implements BytesStore {
    private final BytesMarshallerFactory bytesMarshallerFactory;
    private final Cleaner cleaner;
    private long address;
    private long size;
    private final AtomicInteger refCount = new AtomicInteger(1);

    public DirectStore(long size) {
        this(new VanillaBytesMarshallerFactory(), size);
    }

    public DirectStore(BytesMarshallerFactory bytesMarshallerFactory, long size) {
        this(bytesMarshallerFactory, size, true);
    }

    public DirectStore(BytesMarshallerFactory bytesMarshallerFactory, long size, boolean zeroOut) {
        this.bytesMarshallerFactory = bytesMarshallerFactory;
        address = NativeBytes.UNSAFE.allocateMemory(size);

//        System.out.println("old value " + Integer.toHexString(NativeBytes.UNSAFE.getInt(null, address)));
        if (zeroOut) {
            NativeBytes.UNSAFE.setMemory(address, size, (byte) 0);
            NativeBytes.UNSAFE.putLongVolatile(null, address, 0L);
        }

        this.size = size;
        cleaner = Cleaner.create(this, new Runnable() {
            @Override
            public void run() {
                if (address != 0)
                    NativeBytes.UNSAFE.freeMemory(address);
                address = DirectStore.this.size = 0;
            }
        });
    }

    @NotNull
    public static DirectStore allocate(long size) {
        return new DirectStore(null, size);
    }

    @NotNull
    public static DirectStore allocateLazy(long size) {
        return new DirectStore(null, size, false);
    }

/*    public void resize(long newSize) {
        if (newSize == size)
            return;
        address = NativeBytes.UNSAFE.reallocateMemory(address, newSize);
        size = newSize;
    }*/

    @NotNull
    public DirectBytes createSlice() {
        return new DirectBytes(this, refCount);
    }

    @NotNull
    public DirectBytes createSlice(long offset, long length) {
        return new DirectBytes(this, refCount, offset, length);
    }

    @Override
    public long address() {
        return address;
    }

    public void free() {
        cleaner.clean();
    }

    public long size() {
        return size;
    }

    public BytesMarshallerFactory bytesMarshallerFactory() {
        return bytesMarshallerFactory;
    }

}
