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

import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA. User: peter Date: 09/12/13 Time: 17:05 To change this template use File | Settings | File
 * Templates.
 */
public class CompactEnumBytesMarshaller<E> extends GenericEnumMarshaller<E> implements CompactBytesMarshaller<E> {
    private final byte code;

    public CompactEnumBytesMarshaller(@NotNull Class<E> classMarshaled, int capacity, byte code) {
        super(classMarshaled, capacity);
        this.code = code;
    }

    @Override
    public byte code() {
        return code;
    }
}
