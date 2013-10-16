package net.openhft.lang.io.serialization.direct;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

import static net.openhft.lang.io.serialization.direct.TestClasses.*;
import static org.junit.Assert.assertEquals;

public class IntrospectTest {

    @Test
    public void returnsFieldsRegardlessOfVisibility() {
        List<Field> fields = asFieldList(InstanceOnlyNoStaticFields.class);

        assertEquals(3, fields.size());
    }

    @Test
    public void returnsStaticAsWellAsInstanceFields() {
        List<Field> fields = asFieldList(HasInstanceAndStaticFields.class);

        assertEquals(6, fields.size());
    }

    @Test
    public void walksClassHierarchyForAllFields() {
        List<Field> fields = asFieldList(LevelTwoDerivedClass.class);

        assertEquals(6, fields.size());
    }

    private static List<Field> asFieldList(Class<?> clazz) {
        return new ArrayList<Field>(Introspect.fields(clazz));
    }
}

