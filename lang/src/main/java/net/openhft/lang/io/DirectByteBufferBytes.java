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

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectByteBufferBytes extends NativeBytes {
    private ByteBuffer buffer;

    public DirectByteBufferBytes(int capacity) {
        this(ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()), 0, capacity);
    }

    public DirectByteBufferBytes(final ByteBuffer buffer) {
        this(buffer, 0, buffer.capacity());
    }

    public DirectByteBufferBytes(final ByteBuffer buffer, int start, int capacity) {
        super(
                ((DirectBuffer) buffer).address() + start,
                ((DirectBuffer) buffer).address() + capacity
        );

        this.buffer = buffer;
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, buffer);
    }

    protected DirectByteBufferBytes resize(int newCapacity, boolean cleanup, boolean preserveData) {
        if(newCapacity != capacity()) {


            ByteBuffer oldBuffer = this.buffer;
            long oldAddress = this.startAddr;
            long oldPosition = position();

            if(preserveData && (oldPosition > newCapacity)) {
                throw new IllegalArgumentException(
                        "Capacity can't be less than currently used data (size=" + oldPosition
                                + ", capacity=" + newCapacity + ")"
                );
            }

            this.buffer = ByteBuffer.allocateDirect(newCapacity).order(ByteOrder.nativeOrder());
            this.startAddr = ((DirectBuffer) buffer).address();
            this.positionAddr = this.startAddr;
            this.capacityAddr = this.startAddr + newCapacity;
            this.limitAddr = this.capacityAddr;

            if (preserveData && (oldPosition > 0)) {
                UNSAFE.copyMemory(oldAddress, this.startAddr, Math.min(newCapacity, oldPosition));
                this.positionAddr = this.startAddr + Math.min(newCapacity, oldPosition);
            }

            if(cleanup) {
                IOTools.clean(oldBuffer);
            }
        }

        return this;
    }
}
