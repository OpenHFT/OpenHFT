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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by peter on 24/10/14.
 */
public class MapMarshaller<K, V> implements CompactBytesMarshaller<Map<K, V>> {
    private final BytesMarshaller<K> kBytesMarshaller;
    private final BytesMarshaller<V> vBytesMarshaller;

    public MapMarshaller(BytesMarshaller<K> kBytesMarshaller, BytesMarshaller<V> vBytesMarshaller) {
        this.kBytesMarshaller = kBytesMarshaller;
        this.vBytesMarshaller = vBytesMarshaller;
    }

    @Override
    public byte code() {
        return 'M' & 31;
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
}
