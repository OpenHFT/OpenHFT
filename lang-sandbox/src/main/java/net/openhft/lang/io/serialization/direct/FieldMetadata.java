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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class FieldMetadata {
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
