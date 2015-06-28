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
    private static final long serialVersionUID = 0L;

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
    public E create()   {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{eClass=" + eClass + "}";
    }
}
