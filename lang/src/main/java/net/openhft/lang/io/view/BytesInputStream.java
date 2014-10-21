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

package net.openhft.lang.io.view;

import net.openhft.lang.io.Bytes;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * {@code InputStream} view of {@link Bytes}. Reading bytes from this stream pushes {@linkplain
 * Bytes#position() position} of the underlying {@code Bytes}. When {@linkplain Bytes#limit() limit}
 * is reached, {@code BytesInputStream} behaves as there is no more input.
 *
 * <p>This {@code InputStream} implementation supports {@link #mark(int)} and {@link #reset()}
 * methods.
 *
 * <p>{@code BytesInputStream} objects are reusable, see {@link #bytes(Bytes)} method.
 *
 * @see Bytes#inputStream()
 * @see BytesOutputStream
 */
public class BytesInputStream extends InputStream {
    private Bytes bytes;
    private long mark = 0;

    /**
     * Constructs a {@code BytesInputStream} backed by the given {@code bytes}.
     *
     * @param bytes the {@code Bytes} backing the constructed {@code BytesInputStream}
     */
    public BytesInputStream(Bytes bytes) {
        this.bytes = bytes;
    }

    /**
     * Constructs a {@code BytesInputStream} without backing {@code Bytes}, {@link #bytes(Bytes)}
     * method must be called before first actual use of the constructed {@code BytesInputStream}
     * instance.
     */
    public BytesInputStream() {}

    /**
     * Reassigns the underlying {@code Bytes} of this input stream.
     *
     * @param bytes new {@code Bytes} backing this {@code BytesInputStream}
     * @return this {@code BytesInputStream} object back
     */
    public BytesInputStream bytes(Bytes bytes) {
        this.bytes = bytes;
        mark = 0;
        return this;
    }

    @Override
    public int available() {
        return bytes.available();
    }

    @Override
    public void close() {
        bytes.finish();
    }

    @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
    @Override
    public void mark(int readLimit) {
        mark = bytes.position();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        return bytes.read(b, off, len);
    }

    @SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
    @Override
    public void reset() {
        bytes.position(mark);
    }

    @Override
    public long skip(long n) {
        return bytes.skip(n);
    }

    @Override
    public int read() {
        return bytes.read();
    }
}
