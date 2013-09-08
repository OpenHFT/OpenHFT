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
import net.openhft.lang.io.BytesMarshallable;
import net.openhft.lang.io.BytesMarshaller;
import net.openhft.lang.io.StopCharTester;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * @author peter.lawrey
 */
public class BytesMarshallableMarshaller<E extends BytesMarshallable> implements BytesMarshaller<E> {
    @NotNull
    private final Class<E> classMarshaled;
    private final Constructor<E> constructor;

    public BytesMarshallableMarshaller(@NotNull Class<E> classMarshaled) {
        this.classMarshaled = classMarshaled;
        try {
            constructor = classMarshaled.getConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @NotNull
    @Override
    public Class<E> classMarshaled() {
        return classMarshaled;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull E e) {
        e.writeMarshallable(bytes);
    }

    @Override
    public void append(@NotNull Bytes bytes, @NotNull E e) {
        e.writeMarshallable(bytes);
    }

    @Override
    public E read(@NotNull Bytes bytes) {
        E e;
        try {
            e = constructor.newInstance();
        } catch (Exception e2) {
            throw new IllegalStateException(e2);
        }
        e.readMarshallable(bytes);
        return e;
    }

    @Override
    public E parse(@NotNull Bytes bytes, StopCharTester tester) {
        return read(bytes);
    }
}
