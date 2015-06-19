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

import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;

import java.io.Externalizable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.openhft.lang.io.serialization.CompactBytesMarshaller.*;

/**
 * @author peter.lawrey
 */
public final class VanillaBytesMarshallerFactory implements BytesMarshallerFactory {
    private static final long serialVersionUID = 1L;

    private transient Map<Class<?>, BytesMarshaller<?>> marshallerMap;
    private transient BytesMarshaller<?>[] compactMarshallerMap;

    private void init() {
        marshallerMap = new LinkedHashMap<Class<?>, BytesMarshaller<?>>();
        compactMarshallerMap = new BytesMarshaller[256];

        BytesMarshaller<String> stringMarshaller = new StringMarshaller(16 * 1024);
        addMarshaller(String.class, stringMarshaller);
        addMarshaller(CharSequence.class, (BytesMarshaller)stringMarshaller);
        addMarshaller(Class.class, new ClassMarshaller(Thread.currentThread().getContextClassLoader()));
        addMarshaller(Date.class, new DateMarshaller(10191));
        addMarshaller(Integer.class, new CompactEnumBytesMarshaller<Integer>(Integer.class, 10191, INT_CODE));
        addMarshaller(Long.class, new CompactEnumBytesMarshaller<Long>(Long.class, 10191, LONG_CODE));
        addMarshaller(Double.class, new CompactEnumBytesMarshaller<Double>(Double.class, 10191, DOUBLE_CODE));
        addMarshaller(ByteBuffer.class, ByteBufferMarshaller.INSTANCE);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public <E> BytesMarshaller<E> acquireMarshaller(@NotNull Class<E> eClass, boolean create) {
        if (marshallerMap == null) {
            init();
        }

        BytesMarshaller em = marshallerMap.get(eClass);
        if (em == null) {
            if (eClass.isEnum()) {
                marshallerMap.put(eClass, em = new EnumBytesMarshaller(eClass, null));

            } else if (BytesMarshallable.class.isAssignableFrom(eClass)) {
                marshallerMap.put(eClass, em = new BytesMarshallableMarshaller((Class) eClass));

            } else if (Externalizable.class.isAssignableFrom(eClass)) {
                marshallerMap.put(eClass, em = new ExternalizableMarshaller((Class) eClass));

            } else if (Throwable.class.isAssignableFrom(eClass)) {
                marshallerMap.put(eClass, em = NoMarshaller.INSTANCE);

            } else {
                try {
                    marshallerMap.put(eClass, em = new GenericEnumMarshaller<E>(eClass, 1000));
                } catch (Exception e) {
                    marshallerMap.put(eClass, em = NoMarshaller.INSTANCE);
                }
            }
        }

        return em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> BytesMarshaller<E> getMarshaller(byte code) {
        if (marshallerMap == null) {
            init();
        }

        return (BytesMarshaller<E>) compactMarshallerMap[code & 0xFF];
    }

    public <E> void addMarshaller(Class<E> eClass, BytesMarshaller<E> marshaller) {
        if (marshallerMap == null) {
            init();
        }

        marshallerMap.put(eClass, marshaller);
        if (marshaller instanceof CompactBytesMarshaller) {
            compactMarshallerMap[((CompactBytesMarshaller) marshaller).code()] = marshaller;
        }
    }
}
