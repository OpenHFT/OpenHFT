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

package net.openhft.lang.model;

import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.model.constraints.Group;
import net.openhft.lang.model.constraints.Digits;
import net.openhft.lang.model.constraints.MaxSize;
import net.openhft.lang.model.constraints.Range;

import java.io.Externalizable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static net.openhft.lang.MemoryUnit.BITS;
import static net.openhft.lang.MemoryUnit.BYTES;

/**
 * User: peter.lawrey Date: 06/10/13private static final int VALUE Time: 17:23
 */
public class DataValueModelImpl<T> implements DataValueModel<T> {
    private static final Map<Class, Integer> HEAP_SIZE_MAP = new HashMap<Class, Integer>();
    private static final String VOLATILE_GETTER_PREFIX = "volatile";
    private static final java.lang.String ORDERED_SETTER_PREFIX = "ordered";

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

    private final Map<String, FieldModelImpl> fieldModelMap = new TreeMap<String, FieldModelImpl>();
    private final Class<T> type;
    private final Map<Class, DataValueModel> nestedMap = new HashMap<Class, DataValueModel>();

    public DataValueModelImpl(Class<T> type) {
        this.type = type;
        if (!type.isInterface())
            throw new IllegalArgumentException("type must be an interface, was " + type);

        Method[] methods = type.getMethods();
        for (Method method : methods) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class
                    || declaringClass == Externalizable.class
                    || declaringClass == BytesMarshallable.class
                    || declaringClass == Copyable.class
                    || declaringClass == Byteable.class)
                continue;
            String name = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            final Class<?> returnType = method.getReturnType();
            switch (parameterTypes.length) {
                case 0: {
                    String name5 = getUnlock(name);
                    if (name5 != null && returnType == void.class) {
                        FieldModelImpl fm = acquireField(name5);
                        fm.unlock(method);
                        break;
                    }
                    String name4 = getBusyLock(name);
                    if (name4 != null && returnType == void.class) {
                        FieldModelImpl fm = acquireField(name4);
                        fm.busyLock(method);
                        break;
                    }
                    String name3 = getTryLock(name);
                    if (name3 != null && returnType == boolean.class) {
                        FieldModelImpl fm = acquireField(name3);
                        fm.tryLock(method);
                        break;
                    }
                    String name6 = getSizeOf(name);
                    if (name6 != null && returnType == int.class) {
                        FieldModelImpl fm = acquireField(name6);
                        fm.sizeOf(method);
                        break;
                    }
                    if (returnType == void.class)
                        throw new IllegalArgumentException("void () not supported " + method);

                    String name2 = getGetter(name, returnType);
                    if (isVolatileGetter(name2)) {
                        FieldModelImpl fm = acquireField(volatileGetterFieldName(name2));
                        fm.volatileGetter(method);
                        fm.setVolatile(true);
                    } else {
                        FieldModelImpl fm = acquireField(name2);
                        fm.getter(method);
                    }

                    break;
                }
                case 1: {

                    String name7 = getUsing(name, method);
                    if (name7 != null) {
                        FieldModelImpl fm = acquireField(name7);
                        fm.getUsing(method);
                        break;
                    }

                    String name5 = getTryLockNanos(name);
                    if (name5 != null && returnType == boolean.class) {
                        FieldModelImpl fm = acquireField(name5);
                        fm.tryLockNanos(method);
                        break;
                    }

                    String name4 = getAtomicAdder(name);
                    if (name4 != null) {
                        FieldModelImpl fm = acquireField(name4);
                        fm.atomicAdder(method);
                        break;
                    }

                    String name3 = getAdder(name);
                    if (name3 != null) {
                        FieldModelImpl fm = acquireField(name3);
                        fm.adder(method);
                        break;
                    }

                    String name6 = getGetterAt(name, returnType);
                    if (name6 != null && parameterTypes[0] == int.class && returnType != void.class) {
                        if (isVolatileGetter(name6)) {
                            FieldModelImpl fm = acquireField(volatileGetterFieldName(name6));
                            fm.volatileIndexedGetter(method);
                            fm.setVolatile(true);
                        } else {
                            FieldModelImpl fm = acquireField(name6);
                            fm.indexedGetter(method);
                        }
                        break;
                    }

                    if (returnType != void.class)
                        throw new IllegalArgumentException("setter must be void " + method);

                    String name2 = getSetter(name);
                    if (isOrderedSetter(name2)) {
                        FieldModelImpl fm = acquireField(orderedSetterFieldName(name2));
                        fm.orderedSetter(method);
                    } else {
                        FieldModelImpl fm = acquireField(name2);
                        fm.setter(method);
                    }
                    break;
                }
                case 2: {
                    String name2 = getCAS(name);
                    if (name2 != null && returnType == boolean.class) {
                        FieldModelImpl fm = acquireField(name2);
                        fm.cas(method);
                        break;
                    }
                    String name3 = getSetterAt(name);
                    if (name3 != null && parameterTypes[0] == int.class && returnType == void.class) {
                        if (isOrderedSetter(name3)) {
                            FieldModelImpl fm = acquireField(orderedSetterFieldName(name3));
                            fm.orderedIndexedSetter(method);
                        } else {
                            FieldModelImpl fm = acquireField(name3);
                            fm.indexedSetter(method);
                        }
                        break;
                    }
                }
                default: {
                    throw new IllegalArgumentException("method not supported " + method);
                }
            }
        }

        for (Map.Entry<String, FieldModelImpl> entry : fieldModelMap.entrySet()) {
            FieldModelImpl model = entry.getValue();
            if ((model.getter() == null && model.getUsing() == null) || (model.setter() == null && model
                    .getter()
                    .getReturnType()
                    .isPrimitive()))
                if (model.volatileGetter() == null || (model.orderedSetter() == null && model.volatileGetter().getReturnType().isPrimitive()))
                    if (model.indexedGetter() == null || (model.indexedSetter() == null && model.indexedGetter().getReturnType().isPrimitive()))
                        if (model.volatileIndexedGetter() == null || (model.orderedIndexedSetter() == null && model.volatileIndexedGetter().getReturnType().isPrimitive()))
                            if (model.busyLock() == null || model.unlock() == null)
                                throw new IllegalArgumentException("Field " + entry.getKey() + " must have a getter & setter, or getAt & setAt, or busyLock & unlock.");
            if (model.indexedGetter() != null || model.indexedSetter() != null)
                if (model.indexSize() == null)
                    throw new IllegalStateException("You must set a MaxSize for the range of the index for the getter or setter");

            Class ftype = model.type();
            if (!isScalar(ftype) && !nestedMap.containsKey(ftype))
                nestedMap.put(ftype, new DataValueModelImpl(ftype));
        }
    }

    public static int heapSize(Class primitiveType) {
        if (!primitiveType.isPrimitive())
            throw new IllegalArgumentException();
        return (int) BYTES.alignAndConvert(HEAP_SIZE_MAP.get(primitiveType), BITS);
    }

    private static String getCAS(String name) {
        final int len = 14;
        if (name.length() > len && name.startsWith("compareAndSwap") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getSizeOf(String name) {
        final int len = 6;
        if (name.length() > len && name.startsWith("sizeOf") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getAtomicAdder(String name) {
        final int len = 9;
        if (name.length() > len && name.startsWith("addAtomic") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getAdder(String name) {
        final int len = 3;
        if (name.length() > len && name.startsWith("add") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getSetter(String name) {
        final int len = 3;
        if (name.length() > len && name.startsWith("set") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return name;
    }

    private static String getSetterAt(String name) {
        final int len = 3;
        final int len2 = 2;
        if (name.length() > len + len2 && name.startsWith("set") && Character.isUpperCase(
                name.charAt(len)) && name.endsWith("At"))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1, name.length() - len2);
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

    private static String getUsing(String name, Method method) {
        Class<?> returnType = method.getReturnType();
        if (method.getParameterTypes().length != 1)
            return null;

        Class<?> parameter = method.getParameterTypes()[0];

        if ((returnType == StringBuilder.class || returnType == void.class) && parameter ==
                StringBuilder.class &&
                name.length() > "getUsing".length() && name.startsWith
                ("getUsing") && Character.isUpperCase(name.charAt("getUsing".length())))
            return Character.toLowerCase(name.charAt("getUsing".length())) + name.substring("getUsing"
                    .length() + 1);
        return null;
    }

    private static String getGetterAt(String name, Class returnType) {
        final int len = 3;
        final int len2 = 2;
        if (name.length() > len + len2 && name.startsWith("get") && Character.isUpperCase(
                name.charAt(len)) && name.endsWith("At"))
            return Character.toLowerCase(name.charAt(len)) + name.substring(4, name.length() - len2);
        return name;
    }

    private static String getBusyLock(String name) {
        final int len = 8;
        if (name.length() > len && name.startsWith("busyLock") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getUnlock(String name) {
        final int len = 6;
        if (name.length() > len && name.startsWith("unlock") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getTryLockNanos(String name) {
        final int len = 12;
        if (name.length() > len && name.startsWith("tryLockNanos") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private static String getTryLock(String name) {
        final int len = 7;
        if (name.length() > len && name.startsWith("tryLock") && Character.isUpperCase(name.charAt(len)))
            return Character.toLowerCase(name.charAt(len)) + name.substring(len + 1);
        return null;
    }

    private boolean isOrderedSetter(String name2) {
        return name2.startsWith(ORDERED_SETTER_PREFIX) ? true : false;
    }

    private boolean isVolatileGetter(String name2) {
        return name2.startsWith(VOLATILE_GETTER_PREFIX) ? true : false;
    }

    private String volatileGetterFieldName(String name) {
        name = name.substring(VOLATILE_GETTER_PREFIX.length());
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private String orderedSetterFieldName(String name) {
        name = name.substring(ORDERED_SETTER_PREFIX.length());
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private FieldModelImpl acquireField(String name) {
        FieldModelImpl fieldModelImpl = fieldModelMap.get(name);
        if (fieldModelImpl == null)
            fieldModelMap.put(name, fieldModelImpl = new FieldModelImpl(name));

        return fieldModelImpl;
    }

    @Override
    public Map<String, ? extends FieldModel> fieldMap() {
        return fieldModelMap;
    }

    public boolean isScalar(Class type) {
        return type.isPrimitive() || CharSequence.class.isAssignableFrom(type);
    }

    @Override
    public Set<Class> nestedModels() {
        return nestedMap.keySet();
    }

    @Override
    public <N> DataValueModel<N> nestedModel(Class<N> nClass) {
        @SuppressWarnings("unchecked")
        DataValueModel<N> model = (DataValueModel<N>) (nClass == type ? this : nestedMap.get(nClass));
        return model;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    static class FieldModelImpl<T> implements FieldModel<T> {

        private final String name;
        private Method getter, setter;
        private Method volatileGetter;
        private Method orderedSetter;
        private Digits digits;
        private Range range;
        private MaxSize maxSize;
        private Group group;
        private MaxSize indexSize;
        private Method adder;
        private Method atomicAdder;
        private Method getUsing;
        private Method cas;
        private Method tryLockNanos;
        private Method tryLock;
        private Method busyLock;
        private Method unlock;
        private Method getterAt;
        private Method setterAt;
        private Method volatileGetterAt;
        private Method orderedSetterAt;
        private Method sizeOf;
        private boolean isArray = false;
        private boolean isVolatile = false;

        public FieldModelImpl(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public boolean isArray() {
            return isArray;
        }

        @Override
        public boolean isVolatile() {
            return isVolatile;
        }

        @Override
        public void setVolatile(boolean isVolatile) {
            this.isVolatile = isVolatile;
        }

        public void getter(Method getter) {
            this.getter = getter;
        }

        public Method getter() {
            return getter;
        }

        public void setter(Method setter) {
            this.setter = setter;
            for (Annotation a : setter.getParameterAnnotations()[0]) {
                if (a instanceof Digits)
                    digits = (Digits) a;
                if (a instanceof Range)
                    range = (Range) a;
                if (a instanceof MaxSize)
                    maxSize = (MaxSize) a;

            }

            for (Annotation a : setter.getAnnotations()) {
                if (a instanceof Group)
                    group = (Group) a;
            }

        }

        public Method setter() {
            return setter;
        }

        public void volatileGetter(Method volatileGetter) {
            this.volatileGetter = volatileGetter;
        }

        public Method volatileGetter() {
            return volatileGetter;
        }

        public void orderedSetter(Method orderedSetter) {
            this.orderedSetter = orderedSetter;
            for (Annotation a : orderedSetter.getParameterAnnotations()[0]) {
                if (a instanceof Digits)
                    digits = (Digits) a;
                if (a instanceof Range)
                    range = (Range) a;
                if (a instanceof MaxSize)
                    maxSize = (MaxSize) a;
            }

            for (Annotation a : orderedSetter.getAnnotations()) {
                if (a instanceof Group)
                    group = (Group) a;
            }
        }

        public Method orderedSetter() {
            return orderedSetter;
        }

        @Override
        public Class<T> type() {
            return (Class<T>) (getter != null ? getter.getReturnType() :
                    volatileGetter != null ? volatileGetter.getReturnType() :
                            getterAt != null ? getterAt.getReturnType() :
                                    volatileGetterAt != null ? volatileGetterAt.getReturnType() :
                                            unlock != null ? int.class :
                                                    setter != null && setter.getParameterTypes().length == 1 ?
                                                            setter.getParameterTypes()[0] : null);
        }

        public void adder(Method method) {
            adder = method;
        }

        public Method adder() {
            return adder;
        }

        @Override
        public int heapSize() {
            Integer size = HEAP_SIZE_MAP.get(type());
            if (size == null) return -1;
            return size;
        }

        // maxSize in bits.
        @Override
        public int nativeSize() {
            Integer size = HEAP_SIZE_MAP.get(type());
            if (size != null)
                return size;
            return size().value() << 3;
        }

        @Override
        public Digits digits() {
            return digits;
        }

        @Override
        public Range range() {
            return range;
        }

        @Override
        public MaxSize size() {
            if (maxSize == null)
                throw new IllegalStateException("Field " + name + " is missing @MaxSize on the setter");
            return maxSize;
        }

        @Override
        public Group group() {
            return group;
        }

        @Override
        public String toString() {
            return "FieldModel{" +
                    "name='" + name + '\'' +
                    ", getter=" + (getterAt != null ? getterAt : getter) +
                    ", setter=" + (setterAt != null ? setterAt : setter) +
                    (unlock == null ? "" : ", busyLock=" + busyLock + ", tryLock=" + tryLock + ", unlock=" + unlock) +
                    (digits == null ? "" : ", digits= " + digits) +
                    (range == null ? "" : ", range= " + range) +
                    (maxSize == null ? "" : ", size= " + maxSize) +
                    ((getterAt == null && setterAt == null) ? "" : ", indexSize= " + indexSize.toString().replace("@net.openhft.lang.model.constraints.", "")) +
                    '}';
        }

        public void atomicAdder(Method method) {
            atomicAdder = method;
        }

        public void getUsing(Method method) {
            getUsing = method;
        }

        public Method atomicAdder() {
            return atomicAdder;
        }

        public void cas(Method method) {
            cas = method;
        }

        public Method cas() {
            return cas;
        }

        public void sizeOf(Method method) {
            sizeOf = method;
        }

        public Method sizeOf() {
            return sizeOf;
        }

        public void tryLockNanos(Method method) {
            tryLockNanos = method;
        }

        public Method tryLockNanos() {
            return tryLockNanos;
        }

        public void tryLock(Method tryLock) {
            this.tryLock = tryLock;
        }

        public Method tryLock() {
            return tryLock;
        }

        public void busyLock(Method busyLock) {
            this.busyLock = busyLock;
        }

        public Method busyLock() {
            return busyLock;
        }

        public void unlock(Method unlock) {
            this.unlock = unlock;
        }

        public Method unlock() {
            return unlock;
        }

        public void indexSize(MaxSize indexSize) {
            if (indexSize != null)
                this.indexSize = indexSize;
        }

        public MaxSize indexSize() {
            return indexSize;
        }

        public void indexedGetter(Method indexedGetter) {
            isArray = true;
            this.getterAt = indexedGetter;
            indexAnnotations(indexedGetter);
        }

        public Method indexedGetter() {
            return getterAt;
        }

        public void indexedSetter(Method indexedSetter) {
            isArray = true;
            this.setterAt = indexedSetter;
            indexAnnotations(indexedSetter);
        }

        public Method indexedSetter() {
            return setterAt;
        }

        public void indexAnnotations(Method method) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (Annotation a : parameterAnnotations[0]) {
//                if (a instanceof Digits)
//                    digits = (Digits) a;
//                if (a instanceof Range)
//                    range = (Range) a;
                if (a instanceof MaxSize)
                    indexSize = (MaxSize) a;
            }
            if( parameterAnnotations.length > 1 ) {
                for (Annotation a : parameterAnnotations[1]) {
                    if (a instanceof Digits)
                        digits = (Digits) a;
                    if (a instanceof Range)
                        range = (Range) a;
                    if (a instanceof MaxSize)
                        maxSize = (MaxSize) a;
                }
            }
        }

        public void volatileIndexedGetter(Method volatileIndexedGetter) {
            isArray = true;
            this.volatileGetterAt = volatileIndexedGetter;
            indexAnnotations(volatileIndexedGetter);
        }

        public Method volatileIndexedGetter() {
            return volatileGetterAt;
        }

        public void orderedIndexedSetter(Method orderedIndexedSetter) {
            isArray = true;
            this.orderedSetterAt = orderedIndexedSetter;
            indexAnnotations(orderedIndexedSetter);
        }

        public Method orderedIndexedSetter() {
            return orderedSetterAt;
        }

        @Override
        public Method getUsing() {
            return getUsing;
        }
    }
}
