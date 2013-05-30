/*
 * Copyright ${YEAR} Peter Lawrey
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

import java.io.Externalizable;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * @author peter.lawrey
 */
public class ExternalizableMarshaller<E extends Externalizable> implements BytesMarshaller<E> {
    private final Class<E> classMarshaled;
    private final Constructor<E> constructor;

    public ExternalizableMarshaller(Class<E> classMarshaled) {
        this.classMarshaled = classMarshaled;
        try {
            constructor = classMarshaled.getConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Class<E> classMarshaled() {
        return classMarshaled;
    }

    @Override
    public void write(Bytes bytes, E e) {
        try {
            e.writeExternal(bytes);
        } catch (IOException e2) {
            throw new IllegalStateException(e2);
        }
    }

    @Override
    public void append(Bytes bytes, E e) {
        write(bytes, e);
    }

    @Override
    public E read(Bytes bytes) {
        try {
            E e = constructor.newInstance();
            e.readExternal(bytes);
            return e;
        } catch (Exception e2) {
            throw new IllegalStateException(e2);
        }
    }

    @Override
    public E parse(Bytes bytes, StopCharTester tester) {
        return read(bytes);
    }
}
