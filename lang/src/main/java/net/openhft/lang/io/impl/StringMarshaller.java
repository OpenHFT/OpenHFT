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
import net.openhft.lang.pool.StringInterner;

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
    public Class<String> classMarshaled() {
        return String.class;
    }

    @Override
    public void write(Bytes bytes, String s) {
        bytes.writeUTFΔ(s);
    }

    @Override
    public void append(Bytes bytes, String s) {
        bytes.append(s);
    }

    @Override
    public String read(Bytes bytes) {
        if (bytes.readUTFΔ(reader))
            return builderToString();
        return null;
    }

    @Override
    public String parse(Bytes bytes, StopCharTester tester) {
        reader.setLength(0);
        bytes.parseUTF(reader, tester);
        return builderToString();
    }

    private String builderToString() {
        if (interner == null)
            interner = new StringInterner(size);
        return interner.intern(reader);
    }
}
