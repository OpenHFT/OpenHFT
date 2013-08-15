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

package net.openhft.lang.io.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.BytesMarshaller;
import net.openhft.lang.io.StopCharTester;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class ClassMarshaller implements BytesMarshaller<Class> {
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
    @SuppressWarnings("unchecked")
    private WeakReference<Class>[] classWeakReference = null;

    public ClassMarshaller(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class<Class> classMarshaled() {
        return Class.class;
    }

    @Override
    public void write(Bytes bytes, Class aClass) {
        String s = CS_SHORT_NAME.get(aClass);
        if (s == null)
            s = aClass.getName();
        bytes.writeUTF(s);
    }

    @Override
    public void append(Bytes bytes, Class aClass) {
        String s = CS_SHORT_NAME.get(aClass);
        if (s == null)
            s = aClass.getName();
        bytes.append(s);
    }

    @Override
    public Class read(Bytes bytes) {
        className.setLength(0);
        bytes.readUTF(className);
        return load(className);
    }

    private Class load(CharSequence name) {
        int hash = (name.hashCode() & 0x7fffffff) % CACHE_SIZE;
        if (classWeakReference == null)
            classWeakReference = new WeakReference[CACHE_SIZE];
        WeakReference<Class> ref = classWeakReference[hash];
        if (ref != null) {
            Class clazz = ref.get();
            if (clazz != null && clazz.getName().equals(name))
                return clazz;
        }
        try {

            Class<?> clazz = SC_SHORT_NAME.get(name);
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
    public Class parse(Bytes bytes, StopCharTester tester) {
        className.setLength(0);
        bytes.parseUTF(className, tester);
        return load(className);
    }
}
