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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.nio.ByteBuffer;

public enum ByteBufferMarshaller implements CompactBytesMarshaller<ByteBuffer> {
    INSTANCE;

    @Override
    public byte code() {
        return BYTE_BUFFER_CODE;
    }

    @Override
    public void write(Bytes bytes, ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        bytes.writeStopBit(byteBuffer.remaining());
        bytes.write(byteBuffer);

        // reset the position back as we found it
        byteBuffer.position(position);
    }

    @Override
    public ByteBuffer read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public ByteBuffer read(Bytes bytes, @Nullable ByteBuffer byteBuffer) {
        long length = bytes.readStopBit();
        assert length <= Integer.MAX_VALUE;
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid length: " + length);
        }
        if (byteBuffer == null || byteBuffer.capacity() < length) {
            byteBuffer = newByteBuffer((int) length);

        } else {
            byteBuffer.position(0);
            byteBuffer.limit((int) length);
        }

        bytes.read(byteBuffer);
        byteBuffer.flip();
        return byteBuffer;
    }

    protected ByteBuffer newByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }
}
