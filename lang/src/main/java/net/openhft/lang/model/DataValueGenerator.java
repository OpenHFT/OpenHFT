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

import net.openhft.compiler.CachedCompiler;
import net.openhft.lang.Compare;
import net.openhft.lang.Maths;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshallable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * User: peter.lawrey
 * Date: 06/10/13
 * Time: 19:17
 */
public class DataValueGenerator {
    public static final Comparator<Class> COMPARATOR = new Comparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    public static final Comparator<Map.Entry<String, FieldModel>> COMPARE_BY_HEAP_SIZE = new Comparator<Map.Entry<String, FieldModel>>() {
        @Override
        public int compare(Map.Entry<String, FieldModel> o1, Map.Entry<String, FieldModel> o2) {
            // descending
            int cmp = -Maths.compare(o1.getValue().heapSize(), o2.getValue().heapSize());
            return cmp == 0 ? o1.getKey().compareTo(o2.getKey()) : cmp;
        }
    };
    private static final Logger LOGGER = Logger.getLogger(DataValueGenerator.class.getName());
    final CachedCompiler cc = new CachedCompiler(null, null);
    private final Map<Class, Class> heapClassMap = new ConcurrentHashMap<Class, Class>();
    private final Map<Class, Class> nativeClassMap = new ConcurrentHashMap<Class, Class>();
    private boolean dumpCode = false;

    private static String bytesType(Class type) {
        if (type.isPrimitive())
            return Character.toUpperCase(type.getName().charAt(0)) + type.getName().substring(1);
        if (CharSequence.class.isAssignableFrom(type))
            return "UTFΔ";
        return "Object";
    }

    public <T> T heapInstance(Class<T> tClass) {
        try {
            return (T) acquireHeapClass(tClass).newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public <T> Class acquireHeapClass(Class<T> tClass) {
        Class heapClass = heapClassMap.get(tClass);
        if (heapClass != null)
            return heapClass;
        String actual = new DataValueGenerator().generateHeapObject(tClass);
        if (dumpCode)
            LOGGER.info(actual);
        ClassLoader classLoader = tClass.getClassLoader();
        String className = tClass.getName() + "£heap";
        try {
            heapClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException ignored) {
            try {
                heapClass = cc.loadFromJava(classLoader, className, actual);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        heapClassMap.put(tClass, heapClass);
        return heapClass;
    }

    public String generateHeapObject(Class<?> tClass) {
        return generateHeapObject(DataValueModels.acquireModel(tClass));
    }

    public String generateHeapObject(DataValueModel<?> dvmodel) {
        SortedSet<Class> imported = new TreeSet<Class>(COMPARATOR);
        imported.add(BytesMarshallable.class);
        imported.add(Bytes.class);
        imported.add(IOException.class);
        imported.add(Copyable.class);
        imported.add(dvmodel.type());

        StringBuilder fieldDeclarations = new StringBuilder();
        StringBuilder getterSetters = new StringBuilder();
        StringBuilder writeMarshal = new StringBuilder();
        StringBuilder readMarshal = new StringBuilder();
        StringBuilder copy = new StringBuilder();
        Map<String, ? extends FieldModel> fieldMap = dvmodel.fieldMap();
        Map.Entry<String, FieldModel>[] entries = fieldMap.entrySet().toArray(new Map.Entry[fieldMap.size()]);
        Arrays.sort(entries, COMPARE_BY_HEAP_SIZE);
        for (Map.Entry<String, ? extends FieldModel> entry : entries) {
            String name = entry.getKey();
            FieldModel model = entry.getValue();
            Class type = model.type();
            if (!type.isPrimitive() && !type.getPackage().getName().equals("java.lang"))
                imported.add(type);
            fieldDeclarations.append("    private ").append(type.getName()).append(" _").append(name).append(";\n");

            final Method setter = model.setter();
            final Method getter = model.getter();
            if (setter == null) {
                copy.append("        ((Copyable) ").append(getter.getName()).append("()).copyFrom(from.").append(getter.getName()).append("());\n");
            } else {
                copy.append("        ").append(setter.getName()).append("(from.").append(getter.getName()).append("());\n");
                Class<?> setterType = setter.getParameterTypes()[0];
                getterSetters.append("    public void ").append(setter.getName()).append('(').append(setterType.getName()).append(" _) {\n");
                if (type == String.class && setterType != String.class)
                    getterSetters.append("        _").append(name).append(" = _.toString();\n");
                else
                    getterSetters.append("        _").append(name).append(" = _;\n");
                getterSetters.append("    }\n\n");
            }
            getterSetters.append("    public ").append(type.getName()).append(' ').append(getter.getName()).append("() {\n");
            getterSetters.append("        return _").append(name).append(";\n");
            getterSetters.append("    }\n\n");
            Method adder = model.adder();
            if (adder != null) {
                getterSetters.append("    public ").append(type.getName()).append(' ').append(adder.getName())
                        .append("(").append(adder.getParameterTypes()[0].getName()).append(" _) {\n")
                        .append("        return _").append(name).append(" += _;\n")
                        .append("    }");
            }
            Method atomicAdder = model.atomicAdder();
            if (atomicAdder != null) {
                getterSetters.append("    public synchronized ").append(type.getName()).append(' ').append(atomicAdder.getName())
                        .append("(").append(adder.getParameterTypes()[0].getName()).append(" _) {\n")
                        .append("        return _").append(name).append(" += _;\n")
                        .append("    }");
            }
            Method cas = model.cas();
            if (cas != null) {
                getterSetters.append("    public synchronized boolean ").append(cas.getName()).append("(")
                        .append(type.getName()).append(" _1, ")
                        .append(type.getName()).append(" _2) {\n")
                        .append("        if (_").append(name).append(" == _1) {\n")
                        .append("            _").append(name).append(" = _2;\n")
                        .append("            return true;\n")
                        .append("        }\n")
                        .append("        return false;\n")
                        .append("    }\n");
            }
            Method tryLockNanos = model.tryLockNanos();
            if (tryLockNanos != null) {
                getterSetters.append("    public boolean ").append(tryLockNanos.getName()).append("(long nanos) {\n")
                        .append("        throw new UnsupportedOperationException();\n")
                        .append("    }");
            }
            Method tryLock = model.tryLock();
            if (tryLock != null) {
                getterSetters.append("    public boolean ").append(tryLock.getName()).append("() {\n")
                        .append("        throw new UnsupportedOperationException();\n")
                        .append("    }");
            }
            Method unlock = model.unlock();
            if (unlock != null) {
                getterSetters.append("    public void ").append(unlock.getName()).append("() {\n")
                        .append("        throw new UnsupportedOperationException();\n")
                        .append("    }");
            }
            Method busyLock = model.busyLock();
            if (busyLock != null) {
                getterSetters.append("    public void ").append(busyLock.getName()).append("() {\n")
                        .append("        throw new UnsupportedOperationException();\n")
                        .append("    }");
            }
            writeMarshal.append("         out.write").append(bytesType(type)).append("(_").append(name).append(");\n");
            readMarshal.append("         _").append(name).append(" = in.read").append(bytesType(type)).append("(");
            if ("Object".equals(bytesType(type)))
                readMarshal.append(type.getName()).append(".class");
            readMarshal.append(");\n");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(dvmodel.type().getPackage().getName()).append(";\n\n");
        sb.append("import static ").append(Compare.class.getName()).append(".*;\n");
        for (Class aClass : imported) {
            sb.append("import ").append(aClass.getName()).append(";\n");
        }
        sb.append("\npublic class ").append(dvmodel.type().getSimpleName())
                .append("£heap implements ").append(dvmodel.type().getSimpleName())
                .append(", BytesMarshallable, Copyable<").append(dvmodel.type().getName()).append(">  {\n");
        sb.append(fieldDeclarations).append('\n');
        sb.append(getterSetters);
        sb.append("        @SuppressWarnings(\"unchecked\")\n" +
                "        public void copyFrom(").append(dvmodel.type().getName()).append(" from) {\n");
        sb.append(copy);
        sb.append("    }\n\n");
        sb.append("    public void writeMarshallable(Bytes out) {\n");
        sb.append(writeMarshal);
        sb.append("    }\n");
        sb.append("    public void readMarshallable(Bytes in) {\n");
        sb.append(readMarshal);
        sb.append("    }\n");
        if (Byteable.class.isAssignableFrom(dvmodel.type())) {
            sb.append("    public void bytes(Bytes bytes) {\n");
            sb.append("       throw new UnsupportedOperationException();\n");
            sb.append("    }\n");
            sb.append("    public void bytes(Bytes bytes, long l) {\n");
            sb.append("       throw new UnsupportedOperationException();\n");
            sb.append("    }\n");
            sb.append("    public Bytes bytes() {\n");
            sb.append("       return null;\n");
            sb.append("    }\n");
            sb.append("    public int maxSize() {\n");
            sb.append("       throw new UnsupportedOperationException();\n");
            sb.append("    }\n");
        }
        generateObjectMethods(sb, dvmodel, entries);
        sb.append("}\n");
//        System.out.println(sb);
        return sb.toString();
    }

    private static void generateObjectMethods(StringBuilder sb, DataValueModel<?> dvmodel, Map.Entry<String, FieldModel>[] entries) {
        int count = 0;
        StringBuilder hashCode = new StringBuilder();
        StringBuilder equals = new StringBuilder();
        StringBuilder toString = new StringBuilder();
        for (Map.Entry<String, FieldModel> entry : entries) {
            String name = entry.getKey();
            FieldModel model = entry.getValue();
            Class type = model.type();
            if (count > 0)
                hashCode.append(") * 10191 +\n            ");
            String getterName = model.getter().getName();
            hashCode.append("calcLongHashCode(").append(getterName).append("())");
            equals.append("        if(!isEqual(").append(getterName).append("(), that.").append(getterName).append("())) return false;\n");
            toString.append("            \", ").append(name).append("= \" + ").append(getterName).append("() +\n");
            count++;
        }
        sb.append("    public int hashCode() {\n" +
                "        long lhc = longHashCode();\n" +
                "        return (int) ((lhc >>> 32) ^ lhc);\n" +
                "    }\n" +
                "\n" +
                "    public long longHashCode() {\n" +
                "        return ");
        for (int i = 1; i < count; i++)
            sb.append('(');
        sb.append(hashCode);
        String simpleName = dvmodel.type().getSimpleName();
        sb.append(";\n")
                .append("    }\n")
                .append("\n")
                .append("    public boolean equals(Object o) {\n")
                .append("        if (this == o) return true;\n")
                .append("        if (!(o instanceof ").append(simpleName).append(")) return false;\n")
                .append("        ").append(simpleName).append(" that = (").append(simpleName).append(") o;\n")
                .append("\n")
                .append(equals)
                .append("        return true;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String toString() {\n" +
                        "        return \"").append(simpleName).append(" {\" +\n")
                .append(toString.substring(0, toString.length() - 3)).append(";\n")
                .append("    }");


    }

    public <T> T nativeInstance(Class<T> tClass) {
        try {
            return (T) acquireNativeClass(tClass).newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public <T> Class acquireNativeClass(Class<T> tClass) {
        Class nativeClass = nativeClassMap.get(tClass);
        if (nativeClass != null)
            return nativeClass;
        DataValueModel<T> dvmodel = DataValueModels.acquireModel(tClass);
        for (Class clazz : dvmodel.nestedModels()) {
            // touch them to make sure they are loaded.
            Class clazz2 = acquireNativeClass(clazz);
        }
        String actual = new DataValueGenerator().generateNativeObject(dvmodel);
        if (dumpCode)
            LOGGER.info(actual);
        ClassLoader classLoader = tClass.getClassLoader();
        String className = tClass.getName() + "£native";
        try {
            nativeClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException ignored) {
            try {
                nativeClass = cc.loadFromJava(classLoader, className, actual);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        nativeClassMap.put(tClass, nativeClass);
        return nativeClass;
    }

    public String generateNativeObject(Class<?> tClass) {
        return generateNativeObject(DataValueModels.acquireModel(tClass));
    }

    public String generateNativeObject(DataValueModel<?> dvmodel) {
        SortedSet<Class> imported = new TreeSet<Class>(COMPARATOR);
        imported.add(BytesMarshallable.class);
        imported.add(ObjectOutput.class);
        imported.add(ObjectInput.class);
        imported.add(IOException.class);
        imported.add(Copyable.class);
        imported.add(Byteable.class);
        imported.add(Bytes.class);

        StringBuilder staticFieldDeclarations = new StringBuilder();
        StringBuilder fieldDeclarations = new StringBuilder();
        StringBuilder getterSetters = new StringBuilder();
        StringBuilder writeMarshal = new StringBuilder();
        StringBuilder readMarshal = new StringBuilder();
        StringBuilder copy = new StringBuilder();
        StringBuilder nestedBytes = new StringBuilder();
        Map<String, ? extends FieldModel> fieldMap = dvmodel.fieldMap();
        Map.Entry<String, FieldModel>[] entries = fieldMap.entrySet().toArray(new Map.Entry[fieldMap.size()]);
        Arrays.sort(entries, COMPARE_BY_HEAP_SIZE);
        int offset = 0;
        for (Map.Entry<String, ? extends FieldModel> entry : entries) {
            String name = entry.getKey();
            FieldModel model = entry.getValue();
            Class type = model.type();
            if (!type.isPrimitive() && !type.getPackage().getName().equals("java.lang"))
                imported.add(type);
            String NAME = "_offset + " + name.toUpperCase();
            final Method setter = model.setter();
            final Method getter = model.getter();
            if (dvmodel.isScalar(type)) {
                staticFieldDeclarations.append("    private static final int ").append(name.toUpperCase()).append(" = ").append(offset).append(";\n");
                copy.append("        ").append(setter.getName()).append("(from.").append(getter.getName()).append("());\n");
                Class<?> setterType = setter.getParameterTypes()[0];
                getterSetters.append("    public void ").append(setter.getName()).append('(').append(setterType.getName()).append(" _) {\n");
                getterSetters.append("        _bytes.write").append(bytesType(type)).append("(").append(NAME).append(", ");
                if (CharSequence.class.isAssignableFrom(type))
                    getterSetters.append(model.size().value()).append(", ");
                getterSetters.append("_);\n");
                getterSetters.append("    }\n\n");
                getterSetters.append("    public ").append(type.getName()).append(' ').append(getter.getName()).append("() {\n");
                getterSetters.append("        return _bytes.read").append(bytesType(type)).append("(").append(NAME).append(");\n");
                getterSetters.append("    }\n\n");
                Method adder = model.adder();
                if (adder != null) {
                    getterSetters.append("    public ").append(type.getName()).append(' ').append(adder.getName())
                            .append("(").append(adder.getParameterTypes()[0].getName()).append(" _) {\n")
                            .append("        return _bytes.add").append(bytesType(type)).append("(").append(NAME).append(", _);\n")
                            .append("    }");
                }
                Method atomicAdder = model.atomicAdder();
                if (atomicAdder != null) {
                    getterSetters.append("    public ").append(type.getName()).append(' ').append(atomicAdder.getName())
                            .append("(").append(adder.getParameterTypes()[0].getName()).append(" _) {\n")
                            .append("        return _bytes.addAtomic").append(bytesType(type)).append("(").append(NAME).append(", _);\n")
                            .append("    }");
                }
                Method cas = model.cas();
                if (cas != null) {
                    getterSetters.append("    public boolean ").append(cas.getName()).append("(")
                            .append(type.getName()).append(" _1, ")
                            .append(type.getName()).append(" _2) {\n")
                            .append("        return _bytes.compareAndSwap").append(bytesType(type)).append('(').append(NAME).append(", _1, _2);\n")
                            .append("    }");
                }
                Method tryLockNanos = model.tryLockNanos();
                if (tryLockNanos != null) {
                    getterSetters.append("    public boolean ").append(tryLockNanos.getName()).append("(long nanos) {\n")
                            .append("        return _bytes.tryLockNanos").append(bytesType(type)).append('(').append(NAME).append(", nanos);\n")
                            .append("    }");
                }
                Method tryLock = model.tryLock();
                if (tryLock != null) {
                    getterSetters.append("    public boolean ").append(tryLock.getName()).append("() {\n")
                            .append("        return _bytes.tryLock").append(bytesType(type)).append('(').append(NAME).append(");\n")
                            .append("    }");
                }
                Method unlock = model.unlock();
                if (unlock != null) {
                    getterSetters.append("    public void ").append(unlock.getName()).append("() {\n")
                            .append("         _bytes.unlock").append(bytesType(type)).append('(').append(NAME).append(");\n")
                            .append("    }");
                }
                Method busyLock = model.busyLock();
                if (busyLock != null) {
                    getterSetters.append("    public void ").append(busyLock.getName()).append("() throws InterruptedException {\n")
                            .append("         _bytes.busyLock").append(bytesType(type)).append('(').append(NAME).append(");\n")
                            .append("    }");
                }
                writeMarshal.append("         out.write").append(bytesType(type)).append("(")
                        .append(getter.getName()).append("());\n");
                readMarshal.append("         ").append(setter.getName()).append("(in.read").append(bytesType(type)).append("());\n");
                offset += (model.nativeSize() + 7) >> 3;
            } else {
                staticFieldDeclarations.append("    private static final int ").append(name.toUpperCase()).append(" = ").append(offset).append(";\n");
                fieldDeclarations.append("    private final ").append(type.getName()).append("£native _").append(name).append(" = new ").append(type.getName()).append("£native();\n");
                if (setter == null) {
                    copy.append("        _").append(name).append(".copyFrom(from.").append(getter.getName()).append("());\n");
                } else {
                    copy.append("        ").append(setter.getName()).append("(from.").append(getter.getName()).append("());\n");
                    Class<?> setterType = setter.getParameterTypes()[0];
                    getterSetters.append("    public void ").append(setter.getName()).append('(').append(setterType.getName()).append(" _) {\n");
                    if (type == String.class && setterType != String.class)
                        getterSetters.append("        _").append(name).append(" = _.toString();\n");
                    else
                        getterSetters.append("        _").append(name).append(".copyFrom(_);\n");
                    getterSetters.append("    }\n\n");
                }

                getterSetters.append("    public ").append(type.getName()).append(' ').append(getter.getName()).append("() {\n");
                getterSetters.append("        return _").append(name).append(";\n");
                getterSetters.append("    }\n\n");

                writeMarshal.append("         _").append(name).append(".writeMarshallable(out);\n");
                readMarshal.append("         _").append(name).append(".readMarshallable(in);\n");

                nestedBytes.append("        ((Byteable) _").append(name).append(").bytes(bytes, ").append(NAME).append(");\n");
                DataValueModel dvmodel2 = dvmodel.nestedModel(type);
                Map<String, ? extends FieldModel> fieldMap2 = dvmodel2.fieldMap();
                Map.Entry<String, FieldModel>[] entries2 = fieldMap2.entrySet().toArray(new Map.Entry[fieldMap2.size()]);
                Arrays.sort(entries2, COMPARE_BY_HEAP_SIZE);
                for (Map.Entry<String, ? extends FieldModel> entry2 : entries2) {
                    FieldModel model2 = entry2.getValue();
                    offset += (model2.nativeSize() + 7) >> 3;
                }
            }
        }
        fieldDeclarations.append("\n")
                .append("    private Bytes _bytes;\n")
                .append("    private long _offset;\n");
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(dvmodel.type().getPackage().getName()).append(";\n\n");
        sb.append("import static ").append(Compare.class.getName()).append(".*;\n");
        for (Class aClass : imported) {
            sb.append("import ").append(aClass.getName()).append(";\n");
        }
        sb.append("\npublic class ").append(dvmodel.type().getSimpleName())
                .append("£native implements ").append(dvmodel.type().getSimpleName())
                .append(", BytesMarshallable, Byteable, Copyable<").append(dvmodel.type().getName()).append("> {\n");
        sb.append(staticFieldDeclarations).append('\n');
        sb.append(fieldDeclarations).append('\n');
        sb.append(getterSetters);
        sb.append("    public void copyFrom(").append(dvmodel.type().getName()).append(" from) {\n");
        sb.append(copy);
        sb.append("    }\n\n");
        sb.append("    public void writeMarshallable(Bytes out) {\n");
        sb.append(writeMarshal);
        sb.append("    }\n");
        sb.append("    public void readMarshallable(Bytes in) {\n");
        sb.append(readMarshal);
        sb.append("    }\n");
        sb.append("    public void bytes(Bytes bytes) {\n");
        sb.append("       bytes(bytes, 0L);\n");
        sb.append("    }\n");
        sb.append("    public void bytes(Bytes bytes, long offset) {\n");
        sb.append("       this._bytes = bytes;\n");
        sb.append("       this._offset = offset;\n");
        sb.append(nestedBytes);
        sb.append("    }\n");
        sb.append("    public Bytes bytes() {\n");
        sb.append("       return _bytes;\n");
        sb.append("    }\n");
        sb.append("    public int maxSize() {\n");
        sb.append("       return ").append(offset).append(";\n");
        sb.append("    }\n");
        generateObjectMethods(sb, dvmodel, entries);
        sb.append("}\n");
//        System.out.println(sb);
        return sb.toString();
    }

    public boolean isDumpCode() {
        return dumpCode;
    }

    public void setDumpCode(boolean dumpCode) {
        this.dumpCode = dumpCode;
    }
}
