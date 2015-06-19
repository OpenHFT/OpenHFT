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

package net.openhft.lang.io;

public interface BytesHasher {
    /**
     * Provide a 64-bit hash for the bytes in Bytes between the bytes.position() and bytes.limit();
     *
     * @param bytes to hash
     * @return 64-bit hash
     */
    public long hash(Bytes bytes);

    /**
     * Provide a 64-bit hash for the bytes between offset and limit
     *
     * @param bytes  to hash
     * @param offset the start inclusive
     * @param limit  the end exclusive
     * @return 64-bit hash.
     */
    public long hash(Bytes bytes, long offset, long limit);
}
