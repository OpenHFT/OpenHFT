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

import net.openhft.lang.io.serialization.ObjectFactory;

/**
 * Placeholder object factory which always throws {@code UnsupportedOperationException}.
 */
public enum NoObjectFactory implements ObjectFactory {
    INSTANCE;

    /**
     * Always throws {@code UnsupportedOperationException}.
     *
     * @return nothing
     * @throws UnsupportedOperationException always
     */
    @Override
    public Object create() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
