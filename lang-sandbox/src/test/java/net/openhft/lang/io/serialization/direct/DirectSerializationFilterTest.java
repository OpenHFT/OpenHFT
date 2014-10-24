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

package net.openhft.lang.io.serialization.direct;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.openhft.lang.io.serialization.direct.DirectSerializationFilter.stopAtFirstIneligibleField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DirectSerializationFilterTest {

    @Test
    public void instancePrimitivesAreEligible() {
        List<Field> field = fieldsNamed("intField");
        assertEquals(field, stopAtFirstIneligibleField(field));
    }

    @Test
    public void instancePrimitiveArraysAreNotEligible() {
        List<Field> field = fieldsNamed("doubleArray");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void instanceReferencesAreNotEligible() {
        List<Field> field = fieldsNamed("stringList");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void instanceReferenceArraysAreNotEligible() {
        List<Field> field = fieldsNamed("objectArray");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void staticPrimitivesAreNotEligible() {
        List<Field> field = fieldsNamed("staticIntField");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void staticPrimitiveArraysAreNotEligible() {
        List<Field> field = fieldsNamed("staticDoubleArray");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void staticReferencesAreNotEligible() {
        List<Field> field = fieldsNamed("staticStringList");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void staticReferenceArraysAreNotEligible() {
        List<Field> field = fieldsNamed("staticObjectArray");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void transientPrimitivesAreNotEligible() {
        List<Field> field = fieldsNamed("transientShort");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void transientReferencsAreNotEligible() {
        List<Field> field = fieldsNamed("transientObject");
        assertTrue(stopAtFirstIneligibleField(field).isEmpty());
    }

    @Test
    public void includesAllEligibleFields() {
        List<Field> fields = fieldsNamed("intField", "shortField", "longField", "byteField", "stringList");
        List<Field> expectedfields = fields.subList(0, 4);

        assertEquals(expectedfields, stopAtFirstIneligibleField(fields));
    }

    private static List<Field> fieldsNamed(String... names) {
        ArrayList<Field> fields = new ArrayList<Field>();

        for (String name : names) {
            try {
                fields.add(TestClasses.MixedFields.class.getDeclaredField(name));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Exception retrieving " + name, e);
            }
        }

        return fields;
    }
}
