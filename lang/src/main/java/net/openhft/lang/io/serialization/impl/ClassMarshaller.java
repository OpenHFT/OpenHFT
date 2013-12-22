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

import net.openhft.lang.Compare;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class ClassMarshaller implements CompactBytesMarshaller<Class> {
    private static final int CACHE_SIZE = 1019;
    private static final Map<String, Class> SC_SHORT_NAME = new LinkedHashMap<String, Class>();
    private static final Map<Class, String> CS_SHORT_NAME = new LinkedHashMap<Class, String>();

    static {
        Class[] classes = {Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
                String.class, Class.class, BigInteger.class, BigDecimal.class, Date.class};
        for (Class clazz : classes) {
            String simpleName = clazz.getSimpleName();
            SC_SHORT_NAME.put(simpleName, clazz);
            CS_SHORT_NAME.put(clazz, simpleName);
        }
    }

    private final ClassLoader classLoader;
    private final StringBuilder className = new StringBuilder(40);
    @Nullable
    @SuppressWarnings("unchecked")
    private WeakReference<Class>[] classWeakReference = null;

    public ClassMarshaller(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull Class aClass) {
        String s = CS_SHORT_NAME.get(aClass);
        if (s == null)
            s = aClass.getName();
        bytes.writeUTFΔ(s);
    }

    @Nullable
    @Override
    public Class read(@NotNull Bytes bytes) {
        className.setLength(0);
        bytes.readUTFΔ(className);
        return load(className);
    }

    @Nullable
    private Class load(@NotNull CharSequence name) {
        int hash = (int) (Compare.calcLongHashCode(name) & 0x7ffffff) % CACHE_SIZE;
        if (classWeakReference == null)
            //noinspection unchecked
            classWeakReference = new WeakReference[CACHE_SIZE];
        WeakReference<Class> ref = classWeakReference[hash];
        if (ref != null) {
            Class clazz = ref.get();
            if (clazz != null && clazz.getName().equals(name))
                return clazz;
        }
        try {

            Class<?> clazz = SC_SHORT_NAME.get(name.toString());
            if (clazz != null)
                return clazz;
            clazz = classLoader.loadClass(name.toString());
            classWeakReference[hash] = new WeakReference<Class>(clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public byte code() {
        return 'C' & 31; // control C
    }
}
