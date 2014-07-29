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
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.impl.NoMarshaller;
import net.openhft.lang.model.constraints.NotNull;

import java.io.Externalizable;
import java.io.IOException;

public class BytesMarshallableSerializer implements ObjectSerializer {
    private static final long serialVersionUID = 0L;

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
    public void writeSerializable(Bytes bytes, Object object, Class expectedClass) throws IOException {
        if (object == null) {
            bytes.writeByte(NULL);
            return;
        }
        if (expectedClass != null) {
            if (BytesMarshallable.class.isAssignableFrom(expectedClass)) {
                ((BytesMarshallable) object).writeMarshallable(bytes);
                return;
            } else if (Externalizable.class.isAssignableFrom(expectedClass)) {
                ((Externalizable) object).writeExternal(bytes);
                return;
            } else if (CharSequence.class.isAssignableFrom(expectedClass)) {
                bytes.writeUTFΔ((CharSequence) object);
                return;
            }
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
        objectSerializer.writeSerializable(bytes, object, null);
    }

    static boolean autoGenerateMarshaller(Object obj) {
        return (obj instanceof Comparable && obj.getClass().getPackage().getName().startsWith("java"))
                || obj instanceof Externalizable
                || obj instanceof BytesMarshallable;
    }

    @Override
    public <T> T readSerializable(@NotNull Bytes bytes, Class<T> expectedClass, T object) throws IOException, ClassNotFoundException {
        if (expectedClass != null) {
            try {
                if (BytesMarshallable.class.isAssignableFrom(expectedClass)) {
                    return readBytesMarshallable(bytes, expectedClass, object);
                } else if (Externalizable.class.isAssignableFrom(expectedClass)) {
                    return readExternalizable(bytes, expectedClass, object);
                } else if (CharSequence.class.isAssignableFrom(expectedClass)) {
                    return readCharSequence(bytes, object);
                }
            } catch (InstantiationException e) {
                throw new IOException("Unable to create " + expectedClass, e);
            }
        }
        int type = bytes.readUnsignedByteOrThrow();
        switch (type) {
            case AbstractBytes.END_OF_BUFFER:
            case NULL:
                return null;
            case ENUMED: {
                Class clazz = bytes.readEnum(Class.class);
                assert clazz != null;
                return (T) bytesMarshallerFactory.acquireMarshaller(clazz, true).read(bytes);
            }
            case SERIALIZED: {
                return objectSerializer.readSerializable(bytes, expectedClass, object);
            }
            default:
                BytesMarshaller<Object> m = bytesMarshallerFactory.getMarshaller((byte) type);
                if (m == null)
                    throw new IllegalStateException("Unknown type " + (char) type);
                return (T) m.read(bytes);
        }
    }

    private <T> T readCharSequence(Bytes bytes, T object) {
        if (object instanceof StringBuilder) {
            bytes.readUTFΔ(((StringBuilder) object));
            return object;
        } else {
            return (T) bytes.readUTFΔ();
        }
    }

    private <T> T readExternalizable(Bytes bytes, Class<T> expectedClass, T object) throws InstantiationException, IOException, ClassNotFoundException {
        if (object == null)
            object = (T) NativeBytes.UNSAFE.allocateInstance(expectedClass);
        ((Externalizable) object).readExternal(bytes);
        return object;
    }

    private <T> T readBytesMarshallable(Bytes bytes, Class<T> expectedClass, T object) throws InstantiationException {
        if (object == null)
            object = (T) NativeBytes.UNSAFE.allocateInstance(expectedClass);
        ((BytesMarshallable) object).readMarshallable(bytes);
        return object;
    }

    public static ObjectSerializer create(BytesMarshallerFactory bytesMarshallerFactory, ObjectSerializer instance) {
        return bytesMarshallerFactory == null ? instance : new BytesMarshallableSerializer(bytesMarshallerFactory, instance);
    }
}
