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
