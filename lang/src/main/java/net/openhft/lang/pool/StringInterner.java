/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.pool;

import net.openhft.lang.Maths;
import net.openhft.lang.io.IOTools;
import net.openhft.lang.io.NativeBytes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter.lawrey
 */
public class StringInterner {
    @NotNull
    private final String[] interner;
    private final int mask;

    public StringInterner(int capacity) {
        int n = Maths.nextPower2(capacity, 128);
        interner = new String[n];
        mask = n - 1;
    }

    private static boolean isEqual(@Nullable CharSequence s, @NotNull CharSequence cs) {
        if (s == null) return false;
        if (s.length() != cs.length()) return false;
        for (int i = 0; i < cs.length(); i++)
            if (s.charAt(i) != cs.charAt(i))
                return false;
        return true;
    }

    private static boolean isEqual(@Nullable CharSequence s, @NotNull byte[] bytes, int off, int len) {
        if (s == null) return false;
        if (s.length() != len) return false;
        for (int i = 0; i < len; i++)
            if (s.charAt(i) != (bytes[off + i] & 0xFF))
                return false;
        return true;
    }

    @NotNull
    public String intern(@NotNull byte[] bytes, int off, int len) {
        long hash = NativeBytes.longHash(bytes, off, len);
        int h = Maths.hash(hash) & mask;
        String s = interner[h];
        if (isEqual(s, bytes, off, len))
            return s;
        String s2 = new String(bytes, off, len, IOTools.ISO_8859_1);
        return interner[h] = s2;
    }

    @NotNull
    public String intern(@NotNull CharSequence cs) {
        long hash = 0;
        for (int i = 0; i < cs.length(); i++)
            hash = 57 * hash + cs.charAt(i);
        int h = Maths.hash(hash) & mask;
        String s = interner[h];
        if (isEqual(s, cs))
            return s;
        String s2 = cs.toString();
        return interner[h] = s2;
    }
}
