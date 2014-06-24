/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io;

import net.openhft.lang.io.serialization.BytesMarshallerFactory;
import net.openhft.lang.io.serialization.ObjectSerializer;

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
     * <p/>
     * <p>If this {@code BytesStore} is {@code Bytes} itself rather than natural
     * {@code BytesStore} object, this method will offset the new bytes from the
     * bytes' start, not from bytes' position like
     * {@link Bytes#slice(long, long)}.
     * <p/>
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
}
