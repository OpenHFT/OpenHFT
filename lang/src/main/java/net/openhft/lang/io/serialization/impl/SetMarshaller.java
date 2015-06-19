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
import net.openhft.lang.io.serialization.CompactBytesMarshaller;

import java.util.LinkedHashSet;
import java.util.Set;

public class SetMarshaller<E> extends CollectionMarshaller<E, Set<E>> implements CompactBytesMarshaller<Set<E>> {
    SetMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        super(eBytesMarshaller);
    }

    public static <E> BytesMarshaller<Set<E>> of(BytesMarshaller<E> eBytesMarshaller) {
        return new SetMarshaller<E>(eBytesMarshaller);
    }

    @Override
    public byte code() {
        return SET_CODE;
    }

    @Override
    Set<E> newCollection() {
        return new LinkedHashSet<E>();
    }

    @Override
    Set<E> readCollection(Bytes bytes, Set<E> es, int length) {
        es.clear();

        for (int i = 0; i < length; i++) {
            es.add(eBytesMarshaller.read(bytes));
        }

        return es;
    }
}
