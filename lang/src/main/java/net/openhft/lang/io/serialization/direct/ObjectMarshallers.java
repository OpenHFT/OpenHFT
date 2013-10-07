package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.Field;
import java.util.*;

public final class ObjectMarshallers {
    private static final Map<Class, ObjectMarshaller> metadata = new HashMap<Class, ObjectMarshaller>();

    @SuppressWarnings("unchecked")
    public static <T> ObjectMarshaller<T> forClass(Class<T> clazz) {
        ObjectMarshaller om = metadata.get(clazz);
        if (om == null) {
            Collection<Field> fields = Introspect.fields(clazz);
            Collection<Field> ineligibleFields = DirectSerializationFilter.check(fields);

            if (!ineligibleFields.isEmpty()) {
                blowUp(clazz, ineligibleFields);
            }

            om = new ObjectMarshaller<T>(DirectSerializationMetadata.extractMetadata(fields));
            metadata.put(clazz, om);
        }

        return (ObjectMarshaller<T>) om;
    }

    private static void blowUp(Class clazz, Collection<Field> fields) {
        throw new IllegalArgumentException(
                String.format("ObjectMarshaller only supports classes comprised entirely of primitives. Class %s has the following non-primitive fields: [%s]",
                        clazz.getName(),
                        commaSeparate(fields)
                ));
    }

    private static String commaSeparate(Collection<Field> fields) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Field field : fields) {
            if (first) {
                sb.append(field.getName());
                first = false;
            } else {
                sb.append(", ");
                sb.append(field.getName());
            }
        }

        return sb.toString();
    }
}
