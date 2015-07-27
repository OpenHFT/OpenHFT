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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.Bytes;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.openhft.lang.io.NativeBytes.UNSAFE;

/**
 * User: peter.lawrey Date: 22/09/13 Time: 16:51
 */
public class RawCopier<T> {
    final int start;
    final int end;
    private final Class<T> tClass;

    private RawCopier(Class<T> tClass) {
        this.tClass = tClass;
        List<Field> fields = new ArrayList<Field>();
        addAllFields(fields, tClass);
        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                long off1 = UNSAFE.objectFieldOffset(o1);
                long off2 = UNSAFE.objectFieldOffset(o2);
                return Double.compare(off1, off2);
            }
        });
        start = (int) UNSAFE.objectFieldOffset(fields.get(0));
        Field lastField = null;
        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers()) || !field.getType().isPrimitive())
                break;
            lastField = field;
        }
        end = (int) UNSAFE.objectFieldOffset(lastField) + sizeOf(lastField.getType());

        assert end > start : "end <= start, start: " + start + ", end: " + end;
    }

    public static <T> RawCopier<T> copies(Class<T> tClass) {
        return new RawCopier<T>(tClass);
    }

    private static int sizeOf(Class<?> type) {
        return UNSAFE.arrayIndexScale(Array.newInstance(type, 0).getClass());
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public void toBytes(Object obj, Bytes bytes) {
        bytes.writeObject(obj, start, end);
    }

    public void fromBytes(Bytes bytes, Object obj) {
        bytes.readObject(obj, start, end);
    }

    public void copy(T from, T to) {
        long i;
        for (i = start; i < end - 7; i += 8) {
            UNSAFE.putLong(to, i, UNSAFE.getLong(from, i));
        }
        for (; i < end; i++) {
            UNSAFE.putByte(to, i, UNSAFE.getByte(from, i));
        }
    }

    private static void addAllFields(List<Field> fields, Class tClass) {
        if (tClass != null && tClass != Object.class)
            addAllFields(fields, tClass.getSuperclass());
        if (tClass != null) {
            for (Field field : tClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()))
                    fields.add(field);
            }
        }
    }
}
