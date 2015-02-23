/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.bytes;

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
    default E read(Bytes bytes) {
        return read(bytes, null);
    }

    /**
     * Reads and returns an object from {@code bytes}, reusing the given object, if possible.
     *
     * @param bytes to read
     * @param e     an object to reuse, if possible. {@code null} could be passed, in this case
     *              a new object should be allocated anyway.
     * @return the read object
     */
    E read(Bytes bytes, E e);
}
