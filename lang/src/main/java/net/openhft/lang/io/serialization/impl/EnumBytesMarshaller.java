/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peter.lawrey
 */
public class EnumBytesMarshaller<E extends Enum<E>> extends ImmutableMarshaller<E>
        implements BytesMarshaller<E> {
    @SuppressWarnings("unchecked")
    private final E[] interner = (E[]) new Enum[1024];
    private final BitSet internerDup = new BitSet(1024);
    private final Map<String, E> map = new LinkedHashMap<String, E>(64);
    private final E defaultValue;
    private final int mask;
    private static final StringBuilderPool sbp = new StringBuilderPool();

    public EnumBytesMarshaller(@NotNull Class<E> classMarshaled, E defaultValue) {
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

    @Override
    public void write(@NotNull Bytes bytes, @Nullable E e) {
        bytes.writeUTFΔ(e == null ? "" : e.name());
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
        StringBuilder sb = sbp.acquireStringBuilder();
        bytes.readUTFΔ(sb);
        return builderToEnum(sb);
    }

    private E builderToEnum(StringBuilder sb) {
        int num = hashFor(sb);
        int idx = num & mask;
        E e = interner[idx];
        if (e != null) return e;
        if (!internerDup.get(idx)) return defaultValue;
        e = map.get(sb.toString());
        return e == null ? defaultValue : e;
    }
}
