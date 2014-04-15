/*
 * Copyright 2014 Peter Lawrey
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
import sun.nio.ch.DirectBuffer;

import java.nio.MappedByteBuffer;

public class VanillaMappedBuffer extends NativeBytes {
    private final MappedByteBuffer buffer;
    private final long index;
    private boolean unmapped;

    public VanillaMappedBuffer(final MappedByteBuffer buffer) {
        this(buffer,-1);
    }

    public VanillaMappedBuffer(final MappedByteBuffer buffer, long index) {
        super(
            ((DirectBuffer)buffer).address(),
            ((DirectBuffer)buffer).address() + buffer.capacity()
        );

        this.buffer   = buffer;
        this.unmapped = false;
        this.index    = index;
    }

    public long index() {
        return this.index;
    }

    public boolean unmapped() {
        return this.unmapped;
    }

    @Override
    public synchronized void cleanup() {
        if(!this.unmapped) {
            Cleaner cl = ((DirectBuffer)this.buffer).cleaner();
            if (cl != null) {
                cl.clean();
            }

            this.unmapped = true;
        }

        super.cleanup();
    }

    public void force() {
        this.buffer.force();
    }
}
