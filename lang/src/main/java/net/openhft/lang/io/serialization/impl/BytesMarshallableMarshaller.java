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
import net.openhft.lang.io.serialization.BytesMarshallable;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

/**
 * @author peter.lawrey
 */
public class BytesMarshallableMarshaller<E extends BytesMarshallable>
        implements BytesMarshaller<E> {
    private static final long serialVersionUID = 0L;
    @NotNull
    private final Class<E> classMarshaled;

    public BytesMarshallableMarshaller(@NotNull Class<E> classMarshaled) {
        this.classMarshaled = classMarshaled;
    }

    public final Class<E> marshaledClass() {
        return classMarshaled;
    }

    @Override
    public void write(@NotNull Bytes bytes, @NotNull E e) {
        e.writeMarshallable(bytes);
    }

    @Override
    public E read(@NotNull Bytes bytes) {
        return read(bytes, null);
    }

    @Nullable
    @Override
    public E read(Bytes bytes, @Nullable E e) {
        if (e == null) {
            try {
                e = getInstance();
            } catch (Exception e2) {
                throw new IllegalStateException(e2);
            }
        }
        e.readMarshallable(bytes);
        return e;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected E getInstance() throws Exception {
        return (E) NativeBytes.UNSAFE.allocateInstance(classMarshaled);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() &&
                ((BytesMarshallableMarshaller) obj).classMarshaled == classMarshaled;
    }

    @Override
    public int hashCode() {
        return classMarshaled.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{classMarshaled=" + classMarshaled + "}";
    }
}
