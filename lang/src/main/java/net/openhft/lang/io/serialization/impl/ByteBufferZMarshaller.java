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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public enum ByteBufferZMarshaller implements CompactBytesMarshaller<ByteBuffer> {
    INSTANCE;

    @Override
    public byte code() {
        return BYTE_BUFFER_CODE;
    }

    @Override
    public void write(Bytes bytes, ByteBuffer byteBuffer) {
        bytes.writeStopBit(byteBuffer.remaining());
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        DataOutputStream dos = new DataOutputStream(new DeflaterOutputStream(bytes.outputStream()));
        try {
            while (byteBuffer.remaining() >= 8)
                dos.writeLong(byteBuffer.getLong());
            while (byteBuffer.remaining() > 0)
                dos.write(byteBuffer.get());
            dos.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.writeUnsignedInt(position, bytes.position() - position - 4);

        bytes.write(byteBuffer);
    }

    @Override
    public ByteBuffer read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public ByteBuffer read(Bytes bytes, @Nullable ByteBuffer byteBuffer) {
        long length = bytes.readStopBit();
        if (length < 0 || length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid length: " + length);
        }
        if (byteBuffer == null || byteBuffer.capacity() < length) {
            byteBuffer = newByteBuffer((int) length);

        } else {
            byteBuffer.clear();
        }
        byteBuffer.limit((int) length);

        long position = bytes.position();
        long end = position + length;

        long limit = bytes.limit();
        bytes.limit(end);

        DataInputStream dis = new DataInputStream(new InflaterInputStream(bytes.inputStream()));
        try {
            while (byteBuffer.remaining() >= 8)
                byteBuffer.putLong(dis.readLong());
            while (byteBuffer.remaining() >= 0)
                byteBuffer.put(dis.readByte());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.position(end);
        bytes.limit(limit);

        byteBuffer.flip();
        return byteBuffer;
    }

    protected ByteBuffer newByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }
}
