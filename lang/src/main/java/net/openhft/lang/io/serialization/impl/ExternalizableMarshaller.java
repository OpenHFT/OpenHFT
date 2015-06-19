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
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.io.Externalizable;
import java.io.IOException;

/**
 * @author peter.lawrey
 */
public class ExternalizableMarshaller<E extends Externalizable> implements BytesMarshaller<E> {
    private static final long serialVersionUID = 0L;

    @NotNull
    private final Class<E> classMarshaled;

    public ExternalizableMarshaller(@NotNull Class<E> classMarshaled) {
        this.classMarshaled = classMarshaled;
    }

    public final Class<E> marshaledClass() {
        return classMarshaled;
    }

    @Override
    public void write(Bytes bytes, @NotNull E e) {
        try {
            e.writeExternal(bytes);
        } catch (IOException e2) {
            throw new IllegalStateException(e2);
        }
    }

    @Override
    public E read(Bytes bytes) {
        return read(bytes, null);
    }

    @Nullable
    @Override
    public E read(Bytes bytes, @Nullable E e) {
        try {
            if (e == null)
                e = getInstance();
            e.readExternal(bytes);
            return e;
        } catch (Exception e2) {
            throw new IllegalStateException(e2);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected E getInstance() throws Exception {
        return (E) NativeBytes.UNSAFE.allocateInstance(classMarshaled);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() &&
                ((ExternalizableMarshaller) obj).classMarshaled == classMarshaled;
    }

    @Override
    public int hashCode() {
        return classMarshaled.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{marshaledClass=" + classMarshaled + "}";
    }
}
