package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.Field;
import java.util.*;

import static net.openhft.lang.io.NativeBytes.UNSAFE;
import static net.openhft.lang.io.serialization.direct.FieldMetadata.isStatic;

public class Introspect {
    public static List<Field> fields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();

        addToFields(clazz, fields);
        Collections.sort(fields, FieldOffsetComparator.Instance);

        return fields;
    }

    private static List<Field> addToFields(Class<?> clazz, ArrayList<Field> accumulator) {
        Collections.addAll(accumulator, clazz.getDeclaredFields());
        Class<?> maybeSuper = clazz.getSuperclass();

        return maybeSuper != null ?
                addToFields(maybeSuper, accumulator) :
                accumulator;
    }


    private static final class FieldOffsetComparator implements Comparator<Field> {
        public static final FieldOffsetComparator Instance = new FieldOffsetComparator();

        @Override
        public int compare(Field first, Field second) {
            return Long.compare(offset(first), offset(second));
        }

        private static long offset(Field field) {
            return isStatic(field) ?
                    UNSAFE.staticFieldOffset(field) :
                    UNSAFE.objectFieldOffset(field);
        }
    }
}