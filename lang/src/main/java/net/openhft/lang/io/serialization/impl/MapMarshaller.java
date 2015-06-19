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
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by peter.lawrey on 24/10/14.
 */
public class MapMarshaller<K, V> implements CompactBytesMarshaller<Map<K, V>> {
    private final BytesMarshaller<K> kBytesMarshaller;
    private final BytesMarshaller<V> vBytesMarshaller;

    MapMarshaller(BytesMarshaller<K> kBytesMarshaller, BytesMarshaller<V> vBytesMarshaller) {
        this.kBytesMarshaller = kBytesMarshaller;
        this.vBytesMarshaller = vBytesMarshaller;
    }

    @Override
    public byte code() {
        return MAP_CODE;
    }

    @Override
    public void write(Bytes bytes, Map<K, V> kvMap) {
        bytes.writeInt(kvMap.size());
        for (Map.Entry<K, V> entry : kvMap.entrySet()) {
            kBytesMarshaller.write(bytes, entry.getKey());
            vBytesMarshaller.write(bytes, entry.getValue());
        }
    }

    @Override
    public Map<K, V> read(Bytes bytes) {
        return read(bytes, null);
    }

    @Override
    public Map<K, V> read(Bytes bytes, @Nullable Map<K, V> kvMap) {
        if (kvMap == null) {
            kvMap = new LinkedHashMap<K, V>();

        } else {
            kvMap.clear();
        }
        int size = bytes.readInt();
        for (int i = 0; i < size; i++)
            kvMap.put(kBytesMarshaller.read(bytes), vBytesMarshaller.read(bytes));
        return kvMap;
    }

    public static <K, V> BytesMarshaller<Map<K, V>> of(BytesMarshaller<K> keyMarshaller, BytesMarshaller<V> valueMarshaller) {
        return new MapMarshaller<K, V>(keyMarshaller, valueMarshaller);
    }
}
