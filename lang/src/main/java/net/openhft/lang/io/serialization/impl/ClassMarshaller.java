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

import net.openhft.lang.Compare;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;
import net.openhft.lang.pool.StringInterner;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class ClassMarshaller extends ImmutableMarshaller<Class>
        implements CompactBytesMarshaller<Class> {
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
    private static final StringBuilderPool sbp = new StringBuilderPool();
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
        StringBuilder sb = sbp.acquireStringBuilder();
        bytes.readUTFΔ(sb);
        return load(sb);
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
            if (clazz != null && StringInterner.isEqual(clazz.getName(), name))
                return clazz;
        }
        try {

            String className = name.toString();
            Class<?> clazz = SC_SHORT_NAME.get(className);
            if (clazz != null)
                return clazz;
            clazz = classLoader.loadClass(className);
            classWeakReference[hash] = new WeakReference<Class>(clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public byte code() {
        return CLASS_CODE; // control C
    }
}
    