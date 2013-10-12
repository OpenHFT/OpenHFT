package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.Field;
import java.util.*;

import static net.openhft.lang.io.serialization.direct.FieldMetadata.*;

public final class DirectSerializationFilter {
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
        return isPrimitive(f) &&
                !isStatic(f) &&
                !isTransient(f);
    }
}