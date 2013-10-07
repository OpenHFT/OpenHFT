package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.*;
import java.util.*;

public class DirectSerializationFilter {
    public static Collection<Field> check(Collection<Field> fields) {
        ArrayList<Field> ineligibleFields = new ArrayList<Field>();
        for (Field f : fields) {
            if (!checkEligible(f)) {
                ineligibleFields.add(f);
            }
        }

        return ineligibleFields.isEmpty() ?
                Collections.<Field>emptyList() :
                ineligibleFields;
    }

    private static boolean checkEligible(Field f) {
        return (isPrimitive(f) || isPrimitiveArray(f)) && !isStatic(f);
    }

    private static boolean isPrimitive(Field f) {
        return f.getType().isPrimitive();
    }

    private static boolean isPrimitiveArray(Field f) {
        Class<?> clazz = f.getType();
        return clazz.isArray() && clazz.getComponentType().isPrimitive();
    }

    private static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }
}
