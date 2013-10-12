package net.openhft.lang.io.serialization.direct;

import java.lang.reflect.*;

public final class FieldMetadata {
    public static boolean isPrimitive(Field f) {
        return f.getType().isPrimitive();
    }

    public static boolean isPrimitiveArray(Field f) {
        Class<?> clazz = f.getType();
        return clazz.isArray() && clazz.getComponentType().isPrimitive();
    }

    public static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    public static boolean isTransient(Field f) {
        return Modifier.isTransient(f.getModifiers());
    }
}
