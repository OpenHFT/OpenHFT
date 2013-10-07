package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.Jvm;

import java.lang.reflect.Field;
import java.util.Collection;

import static net.openhft.lang.io.NativeBytes.UNSAFE;

public class DirectSerializationMetadata {
    private static final int OBJECT_ALIGNMENT = 8;
    private static final int OBJECT_ALIGNMENT_MASK = OBJECT_ALIGNMENT - 1;

    static final long NATIVE_WORD_SIZE = Jvm.is64Bit() ? 8 : 4;
    static final long OOP_SIZE = UNSAFE.arrayIndexScale(Object[].class);
    static final long OBJECT_HEADER_SIZE = NATIVE_WORD_SIZE + OOP_SIZE; // Object header has a native sized mark word + variable sized oop to klass meta object

    public static final SerializationMetadata EmptyObjectMetadata = new SerializationMetadata(0, 0);

    public static final class SerializationMetadata {
        final long start;
        final long length;

        SerializationMetadata(long start, long length) {
            this.start = start;
            this.length = length;
        }
    }

    public static SerializationMetadata extractMetadata(Collection<Field> fields) {
        long minOffset = Long.MAX_VALUE;
        long maxOffset = Long.MIN_VALUE;

        for (Field field : fields) {
            long offset = UNSAFE.objectFieldOffset(field);

            if (offset < minOffset) {
                minOffset = offset;
                maxOffset = offset;
            } else if (offset > maxOffset) {
                maxOffset = offset;
            }
        }

        if (maxOffset == Long.MIN_VALUE) {
            return EmptyObjectMetadata;
        } else {
            long totalSize = OBJECT_HEADER_SIZE + maxOffset - minOffset + 1;
            return new SerializationMetadata(minOffset, padToObjectAlignment(totalSize) - OBJECT_HEADER_SIZE);
        }
    }

    static long padToObjectAlignment(long length) {
        if ((length & OBJECT_ALIGNMENT_MASK) != 0) {
            long padding = OBJECT_ALIGNMENT - (length & OBJECT_ALIGNMENT_MASK);
            length += padding;
        }

        return length;
    }
}