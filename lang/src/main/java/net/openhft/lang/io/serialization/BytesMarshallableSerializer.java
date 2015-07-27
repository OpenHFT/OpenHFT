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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.AbstractBytes;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.impl.NoMarshaller;
import net.openhft.lang.io.serialization.impl.StringBuilderPool;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.pool.EnumInterner;

import java.io.Externalizable;
import java.io.IOException;

/**
 * An extension of built-in Java serialization, featuring special treatment of {@link
 * BytesMarshallable} objects, compact {@link String} encoding and support of pluggable custom
 * serializers for arbitrary classes.
 *
 * <p>{@code BytesMarshallableSerializer} could benefit if objects (either top-level serialized or
 * nested fields) implement {@link BytesMarshallable} interface the same way as built-in
 * serialization benefit if objects implement {@link Externalizable} (of cause, {@code
 * BytesMarshallableSerializer} supports {@code Externalizable} too).
 *
 * <p>{@link CharSequence}s, including {@code String}s (either top-level serialized or nested
 * fields) are serialized in UTF-8 encoding.
 *
 * <p>Custom per-class serializers are held by {@link BytesMarshallerFactory}, which could be
 * passed via constructor or static factory {@link #create create()} method.
 *
 * @see #create(BytesMarshallerFactory, ObjectSerializer)
 */
public class BytesMarshallableSerializer implements ObjectSerializer {
    private static final long serialVersionUID = 0L;

    private static final byte NULL = 'N';
    private static final byte ENUMED = 'E';
    private static final byte SERIALIZED = 'S';
    private static final StringBuilderPool SBP = new StringBuilderPool();

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

            } else if (Enum.class.isAssignableFrom(expectedClass)) {
                bytes.write8bitText(object.toString());
                return;
            }
        }
        writeSerializable2(bytes, object);
    }

    private void writeSerializable2(Bytes bytes, Object object) throws IOException {
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
            this.writeSerializable(bytes, clazz, Class.class);
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

                } else if (Enum.class.isAssignableFrom(expectedClass)) {
                    StringBuilder sb = SBP.acquireStringBuilder();
                    bytes.read8bitText(sb);
                    return (T) EnumInterner.intern((Class<Enum>) expectedClass, sb);
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
                Class clazz = this.readSerializable(bytes, Class.class, null);
                assert clazz != null;
                return (T) bytesMarshallerFactory.acquireMarshaller(clazz, true).read(bytes, object);
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

    public static ObjectSerializer create() {
        return create(new VanillaBytesMarshallerFactory(), JDKZObjectSerializer.INSTANCE);
    }

    public static ObjectSerializer create(BytesMarshallerFactory bytesMarshallerFactory, ObjectSerializer instance) {
        return bytesMarshallerFactory == null ? instance : new BytesMarshallableSerializer(bytesMarshallerFactory, instance);
    }
}
