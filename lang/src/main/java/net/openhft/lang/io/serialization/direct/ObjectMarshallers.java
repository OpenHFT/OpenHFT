package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.io.NativeBytes;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static net.openhft.lang.io.serialization.direct.DirectSerializationMetadata.SerializationMetadata;

public final class ObjectMarshallers {
    private static final Logger Log = Logger.getLogger(ObjectMarshallers.class.getName());

    private static final Map<Class, ObjectMarshaller> metadata = new HashMap<Class, ObjectMarshaller>();

    @SuppressWarnings("unchecked")
    public static <T> ObjectMarshaller<T> forClass(Class<T> clazz) {
        ObjectMarshaller om = metadata.get(clazz);
        if (om == null) {
            List<Field> fields = Introspect.fields(clazz);
            List<Field> eligibleFields = DirectSerializationFilter.stopAtFirstIneligibleField(fields);

            SerializationMetadata serializationMetadata;

            if (hasIneligibleFields(fields, eligibleFields)) {
                WarnAboutIneligibleFields.apply(clazz, fields, eligibleFields);
                serializationMetadata = DirectSerializationMetadata.extractMetadataForPartialCopy(eligibleFields);
            } else {
                serializationMetadata = DirectSerializationMetadata.extractMetadata(eligibleFields);
            }

            om = new ObjectMarshaller<T>(serializationMetadata);
            Log.log(WARNING, String.format("Class %s has metadata %s", clazz.getName(), serializationMetadata));
            metadata.put(clazz, om);
        }

        return (ObjectMarshaller<T>) om;
    }

    private static boolean hasIneligibleFields(List<Field> allFields, List<Field> eligibleFields) {
        return allFields.size() != eligibleFields.size();
    }

    private static class WarnAboutIneligibleFields {
        static void apply(Class clazz, List<Field> allFields, List<Field> eligibleFields) {
            List<Field> ineligibleFields = allFields.subList(eligibleFields.size(), allFields.size());
            Log.log(WARNING, String.format(
                    "The following fields in Class %s will not be copied by ObjectMarshaller:\n%s",
                    clazz.getName(),
                    commaSeparate(ineligibleFields)
            ));
        }

        private static String commaSeparate(Collection<Field> fields) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Field field : fields) {
                if (first) {
                    sb.append("\t");
                    sb.append(field.getName());
                    first = false;
                } else {
                    sb.append("\n\t");
                    sb.append(field.getName());
                }
            }

            return sb.toString();
        }
    }
}
