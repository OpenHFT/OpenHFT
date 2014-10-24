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

package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.io.Bytes;

import static net.openhft.lang.io.NativeBytes.UNSAFE;
import static net.openhft.lang.io.serialization.direct.DirectSerializationMetadata.SerializationMetadata;

public final class ObjectMarshaller<T> {
    private final SerializationMetadata metadata;

    public ObjectMarshaller(SerializationMetadata metadata) {
        this.metadata = metadata;
    }

    public void write(Bytes bytes, T tObject) {
        long i = metadata.start;
        long end = metadata.start + metadata.length;

        while (i < end - 7) {
            bytes.writeLong(UNSAFE.getLong(tObject, i));
            i += 8;
        }

        while (i < end) {
            bytes.writeByte(UNSAFE.getByte(tObject, i));
            ++i;
        }
    }

    public T read(Bytes bytes, T tObject) {
        long i = metadata.start;
        long end = metadata.start + metadata.length;

        while (i < end - 7) {
            UNSAFE.putLong(tObject, i, bytes.readLong());
            i += 8;
        }

        while (i < end) {
            UNSAFE.putByte(tObject, i, bytes.readByte());
            ++i;
        }

        return tObject;
    }

    public long length() {
        return metadata.length;
    }
}