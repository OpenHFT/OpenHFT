/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.Jvm;

import java.lang.reflect.Field;
import java.util.List;

import static net.openhft.lang.io.NativeBytes.UNSAFE;

public class DirectSerializationMetadata {
    private static final int OBJECT_ALIGNMENT = 8;
    private static final int OBJECT_ALIGNMENT_MASK = OBJECT_ALIGNMENT - 1;

    static final long NATIVE_WORD_SIZE = Jvm.is64Bit() ? 8 : 4;
    private static final long OOP_SIZE = UNSAFE.arrayIndexScale(Object[].class);
    static final long OBJECT_HEADER_SIZE = NATIVE_WORD_SIZE + OOP_SIZE; // Object header has a native sized mark word + variable sized oop to klass meta object

    private static final SerializationMetadata EmptyObjectMetadata = new SerializationMetadata(0, 0);

    public static final class SerializationMetadata {
        final long start;
        final long length;

        SerializationMetadata(long start, long length) {
            this.start = start;
            this.length = length;
        }

        @Override
        public String toString() {
            return String.format("SerializationMetadata: Start %s Length %s", start, length);
        }
    }

    public static SerializationMetadata extractMetadata(List<Field> fields) {
        if (fields.isEmpty()) return EmptyObjectMetadata;

        Offsets offsets = minMaxOffsets(fields);

        long totalSize = OBJECT_HEADER_SIZE + offsets.max - offsets.min + 1;
        return new SerializationMetadata(offsets.min, padToObjectAlignment(totalSize) - OBJECT_HEADER_SIZE);
    }

    public static SerializationMetadata extractMetadataForPartialCopy(List<Field> fields) {
        if (fields.isEmpty()) return EmptyObjectMetadata;

        Offsets offsets = minMaxOffsets(fields);

        Field lastField = fields.get(fields.size() - 1);

        return new SerializationMetadata(offsets.min, offsets.max + sizeOf(lastField) - OBJECT_HEADER_SIZE);
    }

    private static Offsets minMaxOffsets(List<Field> fields) {
        long minOffset = UNSAFE.objectFieldOffset(fields.get(0));
        long maxOffset = UNSAFE.objectFieldOffset(fields.get(fields.size() - 1));

        return new Offsets(minOffset, maxOffset);
    }

    private static long padToObjectAlignment(long length) {
        if ((length & OBJECT_ALIGNMENT_MASK) != 0) {
            long padding = OBJECT_ALIGNMENT - (length & OBJECT_ALIGNMENT_MASK);
            length += padding;
        }

        return length;
    }

    private static long sizeOf(Field field) {
        if (boolean.class.equals(field.getType())) return 1;
        else if (byte.class.equals(field.getType())) return 1;
        else if (short.class.equals(field.getType())) return 2;
        else if (char.class.equals(field.getType())) return 2;
        else if (int.class.equals(field.getType())) return 4;
        else if (float.class.equals(field.getType())) return 4;
        else return 8;
    }

    private static final class Offsets {
        public final long min;
        public final long max;

        private Offsets(long min, long max) {
            this.min = min;
            this.max = max;
        }
    }
}