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

public class DirectByteBufferBytes extends NativeBytes implements IByteBufferBytes {
    private final ByteBuffer buffer;

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

    public ByteBuffer buffer() {
        return buffer;
    }

    @Override
    public ByteBuffer sliceAsByteBuffer(ByteBuffer toReuse) {
        return sliceAsByteBuffer(toReuse, buffer);
    }
}
