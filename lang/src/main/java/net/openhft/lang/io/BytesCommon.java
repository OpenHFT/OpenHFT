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
import org.jetbrains.annotations.NotNull;

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
    long position();

    /**
     * @param position to skip to
     */
    void position(long position);

    /**
     * @return space available
     */
    long capacity();

    /**
     * @return space remaining in bytes
     */
    long remaining();

    /**
     * Mark the end of the message if writing and check we are at the end of the message if reading.
     *
     * @throws IndexOutOfBoundsException if too much data was written.
     */
    void finish() throws IndexOutOfBoundsException;

    /**
     * @return has finish been called.
     */
    boolean isFinished();

    /**
     * Start again, unfinished, position() == 0
     */
    void reset();

    /**
     * @return Byte order for reading binary
     */
    @NotNull
    ByteOrder byteOrder();

    /**
     * @return these Bytes as an InputStream
     */
    @NotNull
    InputStream inputStream();

    /**
     * @return these Bytes as an OutputStream
     */
    @NotNull
    OutputStream outputStream();

    /**
     * @return the factory for marshallers.
     */
    @NotNull
    BytesMarshallerFactory bytesMarshallerFactory();

    /**
     * @throws IndexOutOfBoundsException if the bounds of the Bytes has been exceeded.
     */
    void checkEndOfBuffer() throws IndexOutOfBoundsException;

    /**
     * Copy from one Bytes to another, moves the position by length
     *
     * @param bytes    to copy
     * @param position to copy from
     * @param length   to copy
     */
    void write(BytesCommon bytes, long position, long length);
}
