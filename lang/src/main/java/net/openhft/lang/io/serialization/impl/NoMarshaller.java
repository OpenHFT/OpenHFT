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

/**
 * Created with IntelliJ IDEA. User: peter.lawrey Date: 19/09/13 Time: 18:26 To change this template use File | Settings | File
 * Templates.
 */
public enum NoMarshaller implements BytesMarshaller<Void> {
    INSTANCE;

    @Override
    public void write(Bytes bytes, Void aVoid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void read(Bytes bytes) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Void read(Bytes bytes, @Nullable Void aVoid) {
        throw new UnsupportedOperationException();
    }
}
