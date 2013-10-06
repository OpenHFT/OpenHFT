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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

    public String generateJavaCode(Class<?> tClass) {
        return generateJavaCode(DataValueModels.acquireModel(tClass));
    }

    public String generateJavaCode(DataValueModel<?> dvmodel) {
        SortedSet<Class> imported = new TreeSet<Class>(COMPARATOR);
        imported.add(Externalizable.class);
        imported.add(ObjectOutput.class);
        imported.add(ObjectInput.class);
        imported.add(IOException.class);

        StringBuilder fieldDeclarations = new StringBuilder();
        StringBuilder getterSetters = new StringBuilder();
        StringBuilder writeExtern = new StringBuilder();
        StringBuilder readExtern = new StringBuilder();
        for (Map.Entry<String, ? extends FieldModel> entry : dvmodel.fieldMap().entrySet()) {
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
                .append("_ implements ").append(dvmodel.type().getSimpleName())
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

    private static String writerFor(Class type) {
        return type.isPrimitive() ? Character.toUpperCase(type.getName().charAt(0)) + type.getName().substring(1) : "Object";
    }
}
