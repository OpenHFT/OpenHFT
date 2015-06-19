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
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.model.constraints.Nullable;

import java.util.Collection;

abstract class CollectionMarshaller<E, C extends Collection<E>> {

    public static final int NULL_LENGTH = -1;
    final BytesMarshaller<E> eBytesMarshaller;

    protected CollectionMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        this.eBytesMarshaller = eBytesMarshaller;
    }

    public void write(Bytes bytes, C c) {
        if (c == null) {
            bytes.writeStopBit(NULL_LENGTH);
            return;
        }
        bytes.writeStopBit(c.size());
        for (E e : c) {
            eBytesMarshaller.write(bytes, e);
        }
    }

    public C read(Bytes bytes) {
        return read(bytes, null);
    }

    abstract C newCollection();

    public C read(Bytes bytes, @Nullable C c) {
        long length = bytes.readStopBit();

        if (length == 0 && c != null) {
            c.clear();
            return c;
        }

        if (length < NULL_LENGTH || length > Integer.MAX_VALUE)
            throw new IllegalStateException("Invalid length: " + length);

        if (length == NULL_LENGTH)
            return null;

        if (c == null)
            c = newCollection();

        return readCollection(bytes, c, (int) length);
    }

    abstract C readCollection(Bytes bytes, C c, int length);
}
