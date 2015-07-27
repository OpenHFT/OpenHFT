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
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.io.ObjectStreamException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class GenericEnumMarshaller<E> implements BytesMarshaller<E> {
    private final int capacity;
    @Nullable
    private transient final Constructor<E> constructor;
    @Nullable
    private transient final Method valueOf;
    @NotNull
    private final Map<String, E> map;

    //used by the read resolve method
    private  final Class<E> classMarshaled;

    public GenericEnumMarshaller(@NotNull Class<E> classMarshaled, final int capacity) {
        this.classMarshaled = classMarshaled;
        this.capacity = capacity;
        Constructor<E> constructor = null;
        Method valueOf = null;
        try {
            valueOf = classMarshaled.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            try {
                constructor = classMarshaled.getConstructor(String.class);
                constructor.setAccessible(true);
            } catch (NoSuchMethodException e1) {
                throw new IllegalArgumentException(classMarshaled + " doesn't have a valueOf(String) or a Constructor(String)");
            }
        }
        this.constructor = constructor;
        this.valueOf = valueOf;
        map = new LinkedHashMap<String, E>(128, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, E> eldest) {
                return size() > capacity;
            }
        };
    }

    private Object readResolve() throws ObjectStreamException {
        return new GenericEnumMarshaller(classMarshaled, capacity);
    }

    @Override
    public void write(@NotNull Bytes bytes, @Nullable E e) {
        bytes.writeUTFΔ(e == null ? null : e.toString());
    }

    @Nullable
    @Override
    public E read(@NotNull Bytes bytes) {
        String s = bytes.readUTFΔ();
        return s == null ? null : valueOf(s);
    }

    @Nullable
    @Override
    public E read(Bytes bytes, @Nullable E e) {
        return read(bytes);
    }

    private E valueOf(String s) {
        E e = map.get(s);
        if (e == null)
            try {
                if (constructor != null) {
                    map.put(s, e = constructor.newInstance(s));

                } else {
                    @SuppressWarnings("unchecked")
                    E invoke = (E) valueOf.invoke(null, s);
                    map.put(s, e = invoke);
                }
            } catch (Exception t) {
                throw new AssertionError(t);
            }
        return e;
    }
}
