package net.openhft.chronicle.core;

import java.util.function.Function;

/**
 * Lambda friendly, ClassLocal value to cache information relating to a class.
 */
public class ClassLocal<V> extends ClassValue<V> {
    private final Function<Class, V> classVFunction;

    ClassLocal(Function<Class, V> classVFunction) {
        this.classVFunction = classVFunction;
    }

    public static <V> ClassLocal<V> withInitial(Function<Class, V> classVFunction) {
        return new ClassLocal<>(classVFunction);
    }

    @Override
    protected V computeValue(Class<?> type) {
        return classVFunction.apply(type);
    }
}
