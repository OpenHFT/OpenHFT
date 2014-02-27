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
     * @return the offset read/written which must be <= limit()
     */
    long position();

    /**
     * @param position to skip to
     */
    Bytes position(long position);

    /**
     * @return the current limit which must be <= capacity()
     */
    long limit();

    /**
     * @param limit the new limit which must be <= capacity()
     */
    Bytes limit(long limit);

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
     * Clears this buffer.  The position is set to zero, the limit is set to
     * the capacity, and the mark is discarded.
     * <p></p>
     * <p> Invoke this method before using a sequence of channel-read or
     * <i>put</i> operations to fill this buffer.  For example:
     * <p></p>
     * <blockquote><pre>
     * buf.clear();     // Prepare buffer for reading
     * in.read(buf);    // Read data</pre></blockquote>
     * <p></p>
     * <p> This method does not actually erase the data in the buffer, but it
     * is named as if it did because it will most often be used in situations
     * in which that might as well be the case. </p>
     *
     * @return This buffer
     */
    Bytes clear();

    /**
     * Flips this buffer.  The limit is set to the current position and then
     * the position is set to zero.  If the mark is defined then it is
     * discarded.
     * <p></p>
     * <p> After a sequence of channel-read or <i>put</i> operations, invoke
     * this method to prepare for a sequence of channel-write or relative
     * <i>get</i> operations.  For example:
     * <p></p>
     * <blockquote><pre>
     * buf.put(magic);    // Prepend header
     * in.read(buf);      // Read data into rest of buffer
     * buf.flip();        // Flip buffer
     * out.write(buf);    // Write header + data to channel</pre></blockquote>
     * <p></p>
     * <p> This method is often used in conjunction with the {@link
     * java.nio.ByteBuffer#compact compact} method when transferring data from
     * one place to another.  </p>
     *
     * @return This buffer
     */
    Bytes flip();

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
     * Access every page to ensure those pages are in memory.
     *
     * @return this.
     */
    Bytes load();
}
