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
import net.openhft.lang.io.Bytes;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: plawrey
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
            int cmp = -Integer.compare(o1.getValue().heapSize(), o2.getValue().heapSize());
            return cmp == 0 ? o1.getKey().compareTo(o2.getKey()) : cmp;
        }
    };

    public <T> T heapInstance(Class<T> tClass) {
        try {
            return (T) acquireHeapClass(tClass).newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    private final Map<Class, Class> heapConstructorMap = new ConcurrentHashMap<Class, Class>();

    private <T> Class acquireHeapClass(Class<T> tClass) throws ClassNotFoundException {
        Class heapClass = heapConstructorMap.get(tClass);
        if (heapClass != null)
            return heapClass;
        String actual = new DataValueGenerator().generateHeapObject(tClass);
        CachedCompiler cc = new CachedCompiler(null, null);
        heapClass = cc.loadFromJava(tClass.getClassLoader(), tClass.getName() + "$heap", actual);
        heapConstructorMap.put(tClass, heapClass);
        return heapClass;

    }

    public String generateHeapObject(Class<?> tClass) {
        return generateHeapObject(DataValueModels.acquireModel(tClass));
    }

    public String generateHeapObject(DataValueModel<?> dvmodel) {
        SortedSet<Class> imported = new TreeSet<Class>(COMPARATOR);
        imported.add(Externalizable.class);
        imported.add(ObjectOutput.class);
        imported.add(ObjectInput.class);
        imported.add(IOException.class);

        StringBuilder fieldDeclarations = new StringBuilder();
        StringBuilder getterSetters = new StringBuilder();
        StringBuilder writeExtern = new StringBuilder();
        StringBuilder readExtern = new StringBuilder();
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
            getterSetters.append("    public void ").append(model.setter().getName()).append('(').append(type.getName()).append(" _) {\n");
            getterSetters.append("        _").append(name).append(" = _;\n");
            getterSetters.append("    }\n\n");
            getterSetters.append("    public ").append(type.getName()).append(' ').append(model.getter().getName()).append("() {\n");
            getterSetters.append("        return _").append(name).append(";\n");
            getterSetters.append("    }\n\n");
            writeExtern.append("         out.write").append(writerFor(type)).append("(_").append(name).append(");\n");
            readExtern.append("         _").append(name).append(" = in.read").append(writerFor(type)).append("();\n");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(dvmodel.type().getPackage().getName()).append(";\n\n");
        for (Class aClass : imported) {
            sb.append("import ").append(aClass.getName()).append(";\n");
        }
        sb.append("\npublic class ").append(dvmodel.type().getSimpleName())
                .append("$heap implements ").append(dvmodel.type().getSimpleName())
                .append(", Externalizable {\n");
        sb.append(fieldDeclarations).append('\n');
        sb.append(getterSetters);
        sb.append("    public void writeExternal(ObjectOutput out) throws IOException {\n");
        sb.append(writeExtern);
        sb.append("    }\n");
        sb.append("    public void readExternal(ObjectInput in) throws IOException {\n");
        sb.append(readExtern);
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    public String generateNativeObject(Class<?> tClass) {
        return generateNativeObject(DataValueModels.acquireModel(tClass));
    }

    public String generateNativeObject(DataValueModel<?> dvmodel) {
        SortedSet<Class> imported = new TreeSet<Class>(COMPARATOR);
        imported.add(Externalizable.class);
        imported.add(ObjectOutput.class);
        imported.add(ObjectInput.class);
        imported.add(IOException.class);
        imported.add(Byteable.class);
        imported.add(Bytes.class);

        StringBuilder fieldDeclarations = new StringBuilder();
        StringBuilder getterSetters = new StringBuilder();
        StringBuilder writeExtern = new StringBuilder();
        StringBuilder readExtern = new StringBuilder();
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
            fieldDeclarations.append("    private static final int ").append(name.toUpperCase()).append(" = ").append(offset).append(";\n");
            getterSetters.append("    public void ").append(model.setter().getName()).append('(').append(type.getName()).append(" _) {\n");
            getterSetters.append("        bytes.write").append(writerFor(type)).append("(").append(name.toUpperCase()).append(", _);\n");
            getterSetters.append("    }\n\n");
            getterSetters.append("    public ").append(type.getName()).append(' ').append(model.getter().getName()).append("() {\n");
            getterSetters.append("        return bytes.read").append(writerFor(type)).append("(").append(name.toUpperCase()).append(");\n");
            getterSetters.append("    }\n\n");
            writeExtern.append("         out.write").append(writerFor(type)).append("(")
                    .append(model.getter().getName()).append("());\n");
            readExtern.append("         ").append(model.setter().getName()).append("(in.read").append(writerFor(type)).append("());\n");
            offset += model.nativeSize() >> 3;
        }
        fieldDeclarations.append("\n").append("    private Bytes bytes;\n");
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(dvmodel.type().getPackage().getName()).append(";\n\n");
        for (Class aClass : imported) {
            sb.append("import ").append(aClass.getName()).append(";\n");
        }
        sb.append("\npublic class ").append(dvmodel.type().getSimpleName())
                .append("$native implements ").append(dvmodel.type().getSimpleName())
                .append(", Externalizable, Byteable {\n");
        sb.append(fieldDeclarations).append('\n');
        sb.append(getterSetters);
        sb.append("    public void writeExternal(ObjectOutput out) throws IOException {\n");
        sb.append(writeExtern);
        sb.append("    }\n");
        sb.append("    public void readExternal(ObjectInput in) throws IOException {\n");
        sb.append(readExtern);
        sb.append("    }\n");
        sb.append("    public void bytes(Bytes bytes) {\n");
        sb.append("       this.bytes = bytes;\n");
        sb.append("    }\n");
        sb.append("    public Bytes bytes() {\n");
        sb.append("       return bytes;\n");
        sb.append("    }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private static String writerFor(Class type) {
        return type.isPrimitive() ? Character.toUpperCase(type.getName().charAt(0)) + type.getName().substring(1) : "Object";
    }
}
