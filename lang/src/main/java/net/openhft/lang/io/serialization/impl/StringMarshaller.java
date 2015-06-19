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
import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;
import net.openhft.lang.pool.StringInterner;

/**
 * @author peter.lawrey
 */
public class StringMarshaller extends ImmutableMarshaller<String>
        implements CompactBytesMarshaller<String> {
    private final int size;
    private static final StringBuilderPool sbp = new StringBuilderPool();
    private transient StringInterner interner;

    public StringMarshaller(int size) {
        this.size = size;
    }

    @Override
    public void write(@NotNull Bytes bytes, String s) {
        bytes.writeUTFΔ(s);
    }

    @Nullable
    @Override
    public String read(@NotNull Bytes bytes) {
        StringBuilder sb = sbp.acquireStringBuilder();
        if (bytes.readUTFΔ(sb))
            return builderToString(sb);
        return null;
    }

    private String builderToString(StringBuilder reader) {
        if (interner == null) {
            if (size == 0)
                return reader.toString();
            interner = new StringInterner(size);
        }
        return interner.intern(reader);
    }

    public byte code() {
        return STRING_CODE;
    }
}
