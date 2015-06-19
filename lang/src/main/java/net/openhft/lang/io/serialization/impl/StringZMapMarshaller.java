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
import java.io.StreamCorruptedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by peter.lawrey on 24/10/14.
 */
public enum StringZMapMarshaller implements CompactBytesMarshaller<Map<String, String>> {
    FAST(Deflater.BEST_SPEED),
    COMPACT(Deflater.BEST_COMPRESSION),
    INSTANCE(Deflater.DEFAULT_STRATEGY);

    private final int level;

    private static final long NULL_SIZE = -1;

    StringZMapMarshaller(int level) {
        this.level = level;
    }

    @Override
    public byte code() {
        return STRINGZ_MAP_CODE;
    }

    @Override
    public void write(Bytes bytes, Map<String, String> kvMap) {
        if (kvMap == null) {
            bytes.writeStopBit(NULL_SIZE);
            return;
        }

        bytes.writeStopBit(kvMap.size());
        long position = bytes.position();
        bytes.clear();
        bytes.position(position + 4);
        DataOutputStream dos = new DataOutputStream(
                new DeflaterOutputStream(bytes.outputStream(), new Deflater(level)));
        try {
            for (Map.Entry<String, String> entry : kvMap.entrySet()) {
                dos.writeUTF(entry.getKey());
                dos.writeUTF(entry.getValue());
            }
            dos.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.writeUnsignedInt(position, bytes.position() - position - 4);
    }

    @Override
    public Map<String, String> read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public Map<String, String> read(Bytes bytes, @Nullable Map<String, String> kvMap) {
        long size = bytes.readStopBit();
        if (size == NULL_SIZE)
            return null;
        if (size < 0 || size > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + size);

        long length = bytes.readUnsignedInt();
        if (length < 0 || length > Integer.MAX_VALUE)
            throw new IllegalStateException(new StreamCorruptedException());
        long position = bytes.position();
        long end = position + length;

        long limit = bytes.limit();
        bytes.limit(end);

        DataInputStream dis = new DataInputStream(new InflaterInputStream(bytes.inputStream()));
        if (kvMap == null) {
            kvMap = new LinkedHashMap<String, String>();

        } else {
            kvMap.clear();
        }
        try {
            for (int i = 0; i < size; i++) {
                String key = dis.readUTF();
                String value = dis.readUTF();
                kvMap.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        bytes.position(end);
        bytes.limit(limit);
        return kvMap;
    }
}
