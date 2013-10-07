package net.openhft.lang.io.serialization.direct;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static net.openhft.lang.io.serialization.direct.DirectSerializationFilter.check;
import static net.openhft.lang.io.serialization.direct.TestClasses.DirectSerializationFilterFields;
import static org.junit.Assert.*;

public class DirectSerializationFilterTest {

    @Test
    public void instancePrimitivesAreEligible() {
        assertTrue(check(fieldNamed("intField")).isEmpty());
    }

    @Test
    public void instancePrimitiveArraysAreEligible() {
        assertTrue(check(fieldNamed("doubleArray")).isEmpty());
    }

    @Test
    public void instanceReferencesAreNotEligible() {
        Collection<Field> field = fieldNamed("stringList");
        assertEquals(field, check(field));
    }

    @Test
    public void instanceReferenceArraysAreNotEligible() {
        Collection<Field> field = fieldNamed("objectArray");
        assertEquals(field, check(field));
    }

    @Test
    public void staticPrimitivesAreNotEligible() {
        Collection<Field> field = fieldNamed("staticIntField");
        assertEquals(field, check(field));
    }

    @Test
    public void staticPrimitiveArraysAreNotEligible() {
        Collection<Field> field = fieldNamed("staticDoubleArray");
        assertEquals(field, check(field));
    }

    @Test
    public void staticReferencesAreNotEligible() {
        Collection<Field> field = fieldNamed("staticStringList");
        assertEquals(field, check(field));
    }

    @Test
    public void staticReferenceArraysAreNotEligible() {
        Collection<Field> field = fieldNamed("staticObjectArray");
        assertEquals(field, check(field));
    }

    private static Collection<Field> fieldNamed(String name) {
        try {
            ArrayList<Field> field = new ArrayList<Field>();
            field.add(DirectSerializationFilterFields.class.getDeclaredField(name));
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Exception retrieving " + name, e);
        }
    }
}
