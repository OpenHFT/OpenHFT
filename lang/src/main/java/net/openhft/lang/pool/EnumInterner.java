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

package net.openhft.lang.pool;

import net.openhft.lang.Maths;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

/**
 * @author peter.lawrey
 */
public class EnumInterner<E extends Enum<E>> implements CharSequenceInterner<Enum<E>> {
    private static final ClassValue<EnumInterner> internerForClass = new ClassValue<EnumInterner>() {
        @Override
        protected EnumInterner computeValue(Class<?> type) {
            return new EnumInterner(type);
        }
    };
    private final Enum<E>[] interner;
    private final int mask;
    private final Class<E> enumType;

    private EnumInterner(Class<E> enumType) {
        this.enumType = enumType;
        int n = 128;
        interner = (Enum<E>[]) new Enum[n];
        mask = n - 1;
    }

    public static <T extends Enum<T>> T intern(Class<T> enumType, CharSequence cs) {
        return (T) internerForClass.get(enumType).intern(cs);
    }

    public static boolean isEqual(@Nullable CharSequence s, @NotNull CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (s.charAt(i) != cs.charAt(i))
                return false;
        return true;
    }

    @Override
    @NotNull
    public Enum<E> intern(@NotNull CharSequence cs) {
        long hash = 0;
        for (int i = 0; i < cs.length(); i++)
            hash = 57 * hash + cs.charAt(i);
        int h = (int) Maths.hash(hash) & mask;
        Enum<E> e = interner[h];
        if (e != null && isEqual(e.name(), cs))
            return e;
        String s2 = cs.toString();
        return interner[h] = Enum.valueOf(enumType, s2);
    }
}
