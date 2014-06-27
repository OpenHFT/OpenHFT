/*
 * Copyright 2014 Higher Frequency Trading
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

import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.ObjectFactory;

import java.lang.reflect.Modifier;

/**
 * Object factory which creates an object by means of {@code Unsafe.allocateInstance()} call,
 * i. e. without calling constructor.
 *
 * @param <E> type of created objects
 */
public final class AllocateInstanceObjectFactory<E> implements ObjectFactory<E> {
    private final Class<E> eClass;

    public AllocateInstanceObjectFactory(Class<E> eClass) {
        if (eClass.isInterface() || Modifier.isAbstract(eClass.getModifiers()) ||
                eClass.isEnum()) {
            throw new IllegalArgumentException(eClass + " should be a non-abstract non-enum class");
        }
        this.eClass = eClass;
    }

    public Class<E> allocatedClass() {
        return eClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E create() throws Exception {
        return (E) NativeBytes.UNSAFE.allocateInstance(eClass);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() &&
                ((AllocateInstanceObjectFactory) obj).eClass == eClass;
    }

    @Override
    public int hashCode() {
        return eClass.hashCode();
    }
}
