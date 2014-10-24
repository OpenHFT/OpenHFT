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

import net.openhft.lang.Maths;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            return Maths.compare(offset(first), offset(second));
        }

        private static long offset(Field field) {
            return isStatic(field) ?
                    UNSAFE.staticFieldOffset(field) :
                    UNSAFE.objectFieldOffset(field);
        }
    }
}