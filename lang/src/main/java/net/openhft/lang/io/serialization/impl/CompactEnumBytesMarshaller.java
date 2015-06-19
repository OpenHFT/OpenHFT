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

import net.openhft.lang.io.serialization.CompactBytesMarshaller;
import net.openhft.lang.model.constraints.NotNull;

/**
 * Created with IntelliJ IDEA. User: peter.lawrey Date: 09/12/13 Time: 17:05
 * Templates.
 */
public class CompactEnumBytesMarshaller<E> extends GenericEnumMarshaller<E> implements CompactBytesMarshaller<E> {
    private final byte code;

    public CompactEnumBytesMarshaller(@NotNull Class<E> classMarshaled, int capacity, byte code) {
        super(classMarshaled, capacity);
        this.code = code;
    }

    @Override
    public byte code() {
        return code;
    }
}
