package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.Field;
import java.util.*;

import static net.openhft.lang.io.serialization.direct.FieldMetadata.*;

public final class DirectSerializationFilter {
    public static List<Field> stopAtFirstIneligibleField(List<Field> fields) {
        ArrayList<Field> eligibleFields = new ArrayList<Field>();
        for (Field f : fields) {
            if (checkEligible(f)) {
                eligibleFields.add(f);
            } else {
                break;
            }
        }

        return eligibleFields.isEmpty() ?
                Collections.<Field>emptyList() :
                eligibleFields;
    }

    private static boolean checkEligible(Field f) {
        return isPrimitive(f) &&
                !isStatic(f) &&
                !isTransient(f);
    }
}