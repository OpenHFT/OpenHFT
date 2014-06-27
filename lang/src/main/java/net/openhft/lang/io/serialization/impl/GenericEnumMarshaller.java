/*
 * Copyright 2013 Peter Lawrey
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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class GenericEnumMarshaller<E> implements BytesMarshaller<E> {
    @Nullable
    private final Constructor<E> constructor;
    @Nullable
    private final Method valueOf;
    @NotNull
    private final Map<String, E> map;

    public GenericEnumMarshaller(@NotNull Class<E> classMarshaled, final int capacity) {
        Constructor<E> constructor = null;
        Method valueOf = null;
        try {
            valueOf = classMarshaled.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            try {
                constructor = classMarshaled.getConstructor(String.class);
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
                throw new AssertionError(t.getCause());
            }
        return e;
    }
}
