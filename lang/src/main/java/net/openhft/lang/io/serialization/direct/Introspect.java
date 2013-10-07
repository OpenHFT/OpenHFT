package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.Field;
import java.util.*;

public class Introspect {
    public static Collection<Field> fields(Class<?> clazz) {
        ArrayList<Field> fields = new ArrayList<Field>();
        return addToFields(clazz, fields);
    }

    private static Collection<Field> addToFields(Class<?> clazz, ArrayList<Field> accumulator) {
        Collections.addAll(accumulator, clazz.getDeclaredFields());
        Class<?> maybeSuper = clazz.getSuperclass();

        return maybeSuper != null ?
                addToFields(maybeSuper, accumulator) :
                accumulator;
    }
}