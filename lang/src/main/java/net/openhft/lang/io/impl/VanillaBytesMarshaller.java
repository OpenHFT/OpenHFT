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

package net.openhft.lang.io.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.BytesMarshaller;
import net.openhft.lang.io.StopCharTester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class VanillaBytesMarshaller<E extends Enum<E>> implements BytesMarshaller<E> {
    @NotNull
    private final Class<E> classMarshaled;
    @SuppressWarnings("unchecked")
    private final E[] interner = (E[]) new Enum[1024];
    private final BitSet internerDup = new BitSet(1024);
    private final Map<String, E> map = new LinkedHashMap<String, E>();
    private final E defaultValue;
    private final int mask;
    private final StringBuilder reader = new StringBuilder();

    public VanillaBytesMarshaller(@NotNull Class<E> classMarshaled, E defaultValue) {
        this.classMarshaled = classMarshaled;
        this.defaultValue = defaultValue;

        mask = interner.length - 1;
        for (E e : classMarshaled.getEnumConstants()) {
            map.put(e.name(), e);
            int idx = hashFor(e.name());
            if (!internerDup.get(idx)) {
                if (interner[idx] != null) {
                    //noinspection UnqualifiedFieldAccess,AssignmentToNull
                    interner[idx] = null;
                    internerDup.set(idx);
                } else {
                    interner[idx] = e;
                }
            }
        }
    }

    @NotNull
    @Override
    public Class<E> classMarshaled() {
        return classMarshaled;
    }

    @Override
    public void write(@NotNull Bytes bytes, @Nullable E e) {
        bytes.writeUTFΔ(e == null ? "" : e.name());
    }

    @Override
    public void append(@NotNull Bytes bytes, @Nullable E e) {
        bytes.append(e == null ? "" : e.name());
    }

    private int hashFor(@NotNull CharSequence cs) {
        int h = 0;

        for (int i = 0, length = cs.length(); i < length; i++)
            h = 57 * h + cs.charAt(i);

        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        return h & mask;
    }

    @Override
    public E read(@NotNull Bytes bytes) {
        bytes.readUTFΔ(reader);
        return builderToEnum();
    }

    @Override
    public E parse(@NotNull Bytes bytes, @NotNull StopCharTester tester) {
        reader.setLength(0);
        bytes.parseUTF(reader, tester);
        return builderToEnum();
    }

    private E builderToEnum() {
        int num = hashFor(reader);
        int idx = num & mask;
        E e = interner[idx];
        if (e != null) return e;
        if (!internerDup.get(idx)) return defaultValue;
        e = map.get(reader.toString());
        return e == null ? defaultValue : e;
    }
}
