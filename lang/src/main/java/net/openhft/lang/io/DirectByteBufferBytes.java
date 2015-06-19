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

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectByteBufferBytes extends NativeBytes implements IByteBufferBytes {
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
    public ByteBuffer buffer() {
        return buffer;
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, buffer);
    }

    protected DirectByteBufferBytes resize(int newCapacity, boolean cleanup, boolean preserveData) {
        if(newCapacity != capacity()) {
            final ByteBuffer oldBuffer = this.buffer;
            final long oldAddress = this.startAddr;
            final long oldPosition = position();

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
