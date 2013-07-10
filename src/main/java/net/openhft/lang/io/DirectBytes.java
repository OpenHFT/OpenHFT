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
public class DirectBytes extends NativeBytes {
    private final Cleaner cleaner = Cleaner.create(this, new Runnable() {
        @Override
        public void run() {
            NativeBytes.UNSAFE.freeMemory(startAddr);
            startAddr = positionAddr = limitAddr = 0;
        }
    });

    public static DirectBytes allocate(long size) {
        return new DirectBytes(null, size);
    }

    public DirectBytes(BytesMarshallerFactory bytesMarshallerFactory, long size) {
        super(bytesMarshallerFactory, 0, 0, 0);
        startAddr = positionAddr = NativeBytes.UNSAFE.allocateMemory(size);
        limitAddr = startAddr + size;
    }

    public void resize(long newSize) {
        if (newSize == capacity())
            return;
        long position = position();
        if (position > newSize)
            position = newSize;
        startAddr = NativeBytes.UNSAFE.reallocateMemory(startAddr, newSize);
        positionAddr = startAddr + position;
        limitAddr = startAddr + newSize;
    }

    public void free() {
        cleaner.clean();
    }
}
