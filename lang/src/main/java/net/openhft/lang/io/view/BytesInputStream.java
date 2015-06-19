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
