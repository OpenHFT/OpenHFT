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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.Nullable;

import java.io.Serializable;

/**
 * External marshaller for classes. From design patterns point of view, this interface
 * is marshalling <i>strategy</i>.
 *
 * @author peter.lawrey
 * @see BytesMarshallable
 */
public interface BytesMarshaller<E> extends Serializable {

    /**
     * Write the object out to the {@code bytes}.
     *
     * @param bytes to write to
     * @param e     the object to write
     */
    void write(Bytes bytes, E e);

    /**
     * Reads and returns an object from {@code bytes}.
     *
     * @param bytes to read
     * @return the read object
     */
    @Nullable
    E read(Bytes bytes);

    /**
     * Reads and returns an object from {@code bytes}, reusing the given object, if possible.
     *
     * @param bytes to read
     * @param e     an object to reuse, if possible. {@code null} could be passed, in this case
     *              a new object should be allocated anyway.
     * @return the read object
     */
    @Nullable
    E read(Bytes bytes, @Nullable E e);
}
