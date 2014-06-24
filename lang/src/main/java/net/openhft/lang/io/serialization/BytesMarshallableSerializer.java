/*
 * Copyright 2014 Higher Frequency Trading
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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.AbstractBytes;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.impl.NoMarshaller;

import java.io.Externalizable;
import java.io.IOException;

public class BytesMarshallableSerializer implements ObjectSerializer {
    private static final byte NULL = 'N';
    private static final byte ENUMED = 'E';
    private static final byte SERIALIZED = 'S';

    private final BytesMarshallerFactory bytesMarshallerFactory;
    private final ObjectSerializer objectSerializer;

    protected BytesMarshallableSerializer(BytesMarshallerFactory bytesMarshallerFactory, ObjectSerializer objectSerializer) {
        this.bytesMarshallerFactory = bytesMarshallerFactory;
        this.objectSerializer = objectSerializer;
    }

    @Override
    public void writeSerializable(Bytes bytes, Object object) throws IOException {
        if (object == null) {
            bytes.writeByte(NULL);
            return;
        }

        Class<?> clazz = object.getClass();
        BytesMarshaller em = bytesMarshallerFactory.acquireMarshaller(clazz, false);
        if (em == NoMarshaller.INSTANCE && autoGenerateMarshaller(object))
            em = bytesMarshallerFactory.acquireMarshaller(clazz, true);

        if (em != NoMarshaller.INSTANCE) {
            if (em instanceof CompactBytesMarshaller) {
                bytes.writeByte(((CompactBytesMarshaller) em).code());
                em.write(bytes, object);
                return;
            }
            bytes.writeByte(ENUMED);
            bytes.writeEnum(clazz);
            em.write(bytes, object);
            return;
        }
        bytes.writeByte(SERIALIZED);
        // TODO this is the lame implementation, but it works.
        objectSerializer.writeSerializable(bytes, object);

    }


    static boolean autoGenerateMarshaller(Object obj) {
        return (obj instanceof Comparable && obj.getClass().getPackage().getName().startsWith("java"))
                || obj instanceof Externalizable
                || obj instanceof BytesMarshallable;
    }


    @Override
    public Object readSerializable(Bytes bytes) throws IOException, ClassNotFoundException {
        int type = bytes.readUnsignedByteOrThrow();
        switch (type) {
            case AbstractBytes.END_OF_BUFFER:
            case NULL:
                return null;
            case ENUMED: {
                Class clazz = bytes.readEnum(Class.class);
                assert clazz != null;
                return bytesMarshallerFactory.acquireMarshaller(clazz, true).read(bytes);
            }
            case SERIALIZED: {
                return objectSerializer.readSerializable(bytes);
            }
            default:
                BytesMarshaller<Object> m = bytesMarshallerFactory.getMarshaller((byte) type);
                if (m == null)
                    throw new IllegalStateException("Unknown type " + (char) type);
                return m.read(bytes);
        }
    }

    public static ObjectSerializer create(BytesMarshallerFactory bytesMarshallerFactory, ObjectSerializer instance) {
        return bytesMarshallerFactory == null ? instance : new BytesMarshallableSerializer(bytesMarshallerFactory, instance);
    }
}
