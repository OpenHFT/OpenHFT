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
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.io.serialization.BytesMarshaller;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter.lawrey
 */
public class BytesMarshallableMarshaller<E extends BytesMarshallable> implements BytesMarshaller<E> {
    @NotNull
    private final Class<E> classMarshaled;

    public BytesMarshallableMarshaller(@NotNull Class<E> classMarshaled) {
        this.classMarshaled = classMarshaled;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull E e) {
        e.writeMarshallable(bytes);
    }

    @Override
    public E read(@NotNull Bytes bytes) {
        E e;
        try {
            e = (E) NativeBytes.UNSAFE.allocateInstance(classMarshaled);
        } catch (Exception e2) {
            throw new IllegalStateException(e2);
        }
        e.readMarshallable(bytes);
        return e;
    }
}
