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

package net.openhft.lang.model;

import java.io.Externalizable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: plawrey
 * Date: 06/10/13
 * Time: 17:23
 */
public class DataValueModelImpl<T> implements DataValueModel<T> {
    private final Map<String, FieldModelImpl> fieldModelMap = new TreeMap<String, FieldModelImpl>();
    private final Class<T> type;

    public DataValueModelImpl(Class<T> type) {
        this.type = type;
        if (!type.isInterface())
            throw new IllegalArgumentException("type must be an interface, was " + type);
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class
                    || declaringClass == Externalizable.class
                    || declaringClass == Byteable.class)
                continue;
            String name = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            switch (parameterTypes.length) {
                case 0: {
                    if (returnType == void.class)
                        throw new IllegalArgumentException("void () not supported " + method);
                    String name2 = getGetter(name, returnType);
                    FieldModelImpl fm = acquireField(name2);
                    fm.getter(method);
                    break;
                }
                case 1: {
                    if (returnType != void.class)
                        throw new IllegalArgumentException("setter must be void " + method);
                    String name2 = getSetter(name);
                    FieldModelImpl fm = acquireField(name2);
                    fm.setter(method);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("method not supported " + method);
                }
            }
        }
        for (Map.Entry<String, FieldModelImpl> entry : fieldModelMap.entrySet()) {
            FieldModelImpl model = entry.getValue();
            if (model.getter == null || model.setter == null)
                throw new IllegalArgumentException("Field " + entry.getKey() + " must have a getter and setter.");
        }
    }

    private FieldModelImpl acquireField(String name) {
        FieldModelImpl fieldModelImpl = fieldModelMap.get(name);
        if (fieldModelImpl == null)
            fieldModelMap.put(name, fieldModelImpl = new FieldModelImpl(name));

        return fieldModelImpl;
    }

    private static String getSetter(String name) {
        if (name.length() > 3 && name.startsWith("set") && Character.isUpperCase(name.charAt(3)))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        return name;
    }

    private static String getGetter(String name, Class returnType) {
        if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3)))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        if ((returnType == boolean.class || returnType == Boolean.class)
                && name.length() > 2 && name.startsWith("is") && Character.isUpperCase(name.charAt(2)))
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        return name;
    }

    @Override
    public Map<String, ? extends FieldModel> fieldMap() {
        return fieldModelMap;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    static class FieldModelImpl<T> implements FieldModel<T> {
        static final Map<Class, Integer> HEAP_SIZE_MAP = new HashMap<Class, Integer>();

        static {
            HEAP_SIZE_MAP.put(boolean.class, 1);
            HEAP_SIZE_MAP.put(byte.class, 8);
            HEAP_SIZE_MAP.put(char.class, 16);
            HEAP_SIZE_MAP.put(short.class, 16);
            HEAP_SIZE_MAP.put(int.class, 32);
            HEAP_SIZE_MAP.put(float.class, 32);
            HEAP_SIZE_MAP.put(long.class, 64);
            HEAP_SIZE_MAP.put(double.class, 64);
        }

        private final String name;
        Method getter, setter;

        public FieldModelImpl(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public void getter(Method getter) {
            this.getter = getter;
        }

        public Method getter() {
            return getter;
        }

        public void setter(Method setter) {
            this.setter = setter;
        }

        public Method setter() {
            return setter;
        }

        @Override
        public Class<T> type() {
            return (Class<T>) getter.getReturnType();
        }

        @Override
        public int heapSize() {
            Integer size = HEAP_SIZE_MAP.get(type());
            if (size == null) return -1;
            return size;
        }

        @Override
        public int nativeSize() {
            Integer size = HEAP_SIZE_MAP.get(type());
            if (size == null) throw new AssertionError(type() + " not supported for native types");
            return size;
        }

        @Override
        public String toString() {
            return "FieldModel{" +
                    "name='" + name + '\'' +
                    ", getter=" + getter +
                    ", setter=" + setter +
                    '}';
        }
    }
}
