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

package net.openhft.lang.io.serialization;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.io.serialization.impl.VanillaBytesMarshallerFactory;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * User: peter.lawrey Date: 20/09/13 Time: 09:28
 */
public class VanillaBytesMarshallerTest {

    enum BuySell {
        BUY, SELL
    }

    @Test
    public void testObjects() {
        DirectBytes bytes = new DirectStore(1024).bytes();
        Object[] objects = {1, 1L, 1.0, "Hello"};
        for (Object o : objects) {
            long pos = bytes.position();
            bytes.writeObject(o);
            System.out.printf("%s used %,d bytes%n", o.getClass(), bytes.position() - pos);
        }
        bytes.clear();
        for (Object o : objects) {
            Object o2 = bytes.readObject();
            assertEquals(o, o2);
        }
    }

    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr + capacity);

        nativeBytes.writeObject(BuySell.BUY);
        nativeBytes.writeObject(BuySell.SELL);
        nativeBytes.finish();
        nativeBytes.clear();
        assertEquals(BuySell.BUY, nativeBytes.readObject());
        assertEquals(BuySell.SELL, nativeBytes.readObject());
    }

    @Test
    public void testExceptionWithoutCause() {
        final int capacity = 2 * 1024;
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        final long addr = ((DirectBuffer) byteBuffer).address();

        final NativeBytes nativeBytes = new NativeBytes(
                new VanillaBytesMarshallerFactory(), addr, addr + capacity, new AtomicInteger(1));

        Throwable expected = new IOException("io-exception");

        nativeBytes.writeObject(expected);
        nativeBytes.finish();
        nativeBytes.clear();

        Throwable actual = nativeBytes.readObject(Throwable.class);
        assertNotNull(actual);
        assertNull(actual.getCause());

        assertEquals(expected.getMessage(), actual.getMessage());
        assertArrayEquals(expected.getStackTrace(), actual.getStackTrace());
    }

    @Test
    public void testExceptionWithCause() {
        final int capacity = 2 * 1024;
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        final long addr = ((DirectBuffer) byteBuffer).address();

        final NativeBytes nativeBytes = new NativeBytes(
                new VanillaBytesMarshallerFactory(), addr, addr + capacity, new AtomicInteger(1));

        Throwable expected = new IOException(
                "io-exception", new EOFException("eof-exception"));

        nativeBytes.writeObject(expected);
        nativeBytes.finish();
        nativeBytes.clear();

        Throwable actual = nativeBytes.readObject(Throwable.class);
        assertNotNull(actual);
        assertNotNull(actual.getCause());

        assertEquals(expected.getMessage(), actual.getMessage());
        assertEquals(expected.getCause().getMessage(), actual.getCause().getMessage());
        assertArrayEquals(expected.getStackTrace(), actual.getStackTrace());
        assertArrayEquals(expected.getCause().getStackTrace(), actual.getCause().getStackTrace());
    }
}
