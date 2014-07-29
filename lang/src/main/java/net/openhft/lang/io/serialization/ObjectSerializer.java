/*
 * Copyright 2014 Higher Frequency Trading
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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.NotNull;

import java.io.IOException;
import java.io.Serializable;

public interface ObjectSerializer extends Serializable {
    /**
     * write an object
     *
     * @param bytes         to write to
     * @param object        object to write
     * @param expectedClass which will be provided on read, can be null
     */
    void writeSerializable(@NotNull Bytes bytes, Object object, Class expectedClass) throws IOException;

    /**
     * Read an object
     *
     * @param bytes         to read
     * @param expectedClass proved when writing, can be null
     * @param object        to populate, can be null
     * @return object read.
     */
    <T> T readSerializable(@NotNull Bytes bytes, Class<T> expectedClass, T object) throws IOException, ClassNotFoundException;
}
