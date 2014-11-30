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

package net.openhft.lang.io.view;

import net.openhft.lang.io.Bytes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code OutputStream} view of {@link Bytes}. Writing data to this stream pushes {@linkplain
 * Bytes#position() position} of the underlying {@code Bytes}. On attempt of writing bytes beyond
 * backing {@code Bytes}' {@linkplain Bytes#limit() limit} {@link IOException} is thrown.
 *
 * <p>{@code BytesOutputStream} are reusable, see {@link #bytes(Bytes)} method.
 *
 * @see Bytes#outputStream()
 * @see BytesInputStream
 */
public class BytesOutputStream extends OutputStream {
    private Bytes bytes;

    /**
     * Constructs a {@code BytesOutputStream} backed by the given {@code bytes}.
     *
     * @param bytes the {@code Bytes} backing the constructed {@code BytesOutputStream}
     */
    public BytesOutputStream(Bytes bytes) {
        this.bytes = bytes;
    }

    /**
     * Constructs a {@code BytesOutputStream} without backing {@code Bytes}, {@link #bytes(Bytes)}
     * method must be called before first actual use of the constructed {@code BytesOutputStream}
     * instance.
     */
    public BytesOutputStream() {}

    /**
     * Reassigns the underlying {@code Bytes} of this output stream.
     *
     * @param bytes new {@code Bytes} backing this {@code BytesOutputStream}
     * @return this {@code BytesOutputStream} object back
     */
    public BytesOutputStream bytes(Bytes bytes) {
        this.bytes = bytes;
        return this;
    }

    @Override
    public void close() {
        bytes.finish();
    }

    private void checkNotClosed() throws IOException {
        if (bytes.isFinished())
            throw new IOException("Underlying bytes is closed");
    }

    private void checkAvailable(int n) throws IOException {
        if (n > bytes.remaining())
            throw new IOException("Not enough available space for writing " + n + " bytes");
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        checkNotClosed();
        checkAvailable(len);
        bytes.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        checkNotClosed();
        checkAvailable(1);
        bytes.writeUnsignedByte(b);
    }
}
