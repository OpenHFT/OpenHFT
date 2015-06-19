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

import net.openhft.lang.io.serialization.ObjectSerializer;

import java.io.File;

public interface BytesStore {
    /**
     * Create a bytes whose content is the whole bytes store. Call of this
     * method is equivalent to {@code bytes(0, size())} call.
     *
     * @return the new bytes
     * @see #bytes(long, long)
     */
    Bytes bytes();

    /**
     * Slice a {@code Bytes} object with start address of
     * {@link #address() address}{@code + offset} and capacity of {@code length}.
     * 
     * <p>If this {@code BytesStore} is {@code Bytes} itself rather than natural
     * {@code BytesStore} object, this method will offset the new bytes from the
     * bytes' start, not from bytes' position like
     * {@link Bytes#slice(long, long)}.
     * 
     * <p>{@code offset} should be non-negative, {@code length} should be positive,
     * {@code offset + length} should be less or equal to {@link #size() size}.
     *
     * @param offset offset of the new bytes from the bytes store address
     * @param length capacity and limit of the new bytes
     * @return the sliced {@code Bytes}
     * @see #bytes()
     */
    Bytes bytes(long offset, long length);

    long address();

    long size();

    void free();

    ObjectSerializer objectSerializer();

    File file();
}
