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

import java.util.ArrayList;
import java.util.List;

public class ListMarshaller<E> extends CollectionMarshaller<E, List<E>> implements CompactBytesMarshaller<List<E>> {

    ListMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        super(eBytesMarshaller);
    }

    public static <E> BytesMarshaller<List<E>> of(BytesMarshaller<E> eBytesMarshaller) {
        return new ListMarshaller<E>(eBytesMarshaller);
    }

    @Override
    public byte code() {
        return LIST_CODE;
    }

    @Override
    List<E> newCollection() {
        return new ArrayList<E>();
    }

    @Override
    List<E> readCollection(Bytes bytes, List<E> es, int length) {
        List<E> ret = es;
        ret.clear();

        for (int i = 0; i < length; i++) {
            ret.add(eBytesMarshaller.read(bytes));
        }

        return ret;
    }
}
