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

package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.pool.StringInterner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter.lawrey
 */
public class StringMarshaller implements BytesMarshaller<String> {
    private final int size;
    private final StringBuilder reader = new StringBuilder();
    private StringInterner interner;

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
        if (bytes.readUTFΔ(reader))
            return builderToString();
        return null;
    }


    private String builderToString() {
        if (interner == null)
            interner = new StringInterner(size);
        return interner.intern(reader);
    }
}
