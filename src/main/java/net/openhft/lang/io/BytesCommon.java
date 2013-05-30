/*
 * Copyright ${YEAR} Peter Lawrey
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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * @author peter.lawrey
 */
public interface BytesCommon {
    /**
     * @return the offset read/written so far
     */
    int position();

    /**
     * @param position to skip to
     */
    void position(int position);

    /**
     * @return space available
     */
    int capacity();

    /**
     * @return space remaining in bytes
     */
    int remaining();

    /**
     * Mark the end of the message if writing and check we are at the end of the message if reading.
     */
    void finish();

    boolean isFinished();

    /**
     * @return Byte order for reading binary
     */
    ByteOrder byteOrder();

    /**
     * @return these Bytes as an InputStream
     */
    InputStream inputStream();

    /**
     * @return these Bytes as an OutputStream
     */
    OutputStream outputStream();

    /**
     * @return the factory for marshallers.
     */
    BytesMarshallerFactory bytesMarshallerFactory();

    /**
     * @throws IndexOutOfBoundsException if the bounds of the Bytes has been exceeded.
     */
    void checkEndOfBuffer() throws IndexOutOfBoundsException;
}
