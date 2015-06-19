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

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.NativeBytes;
import net.openhft.lang.model.constraints.NotNull;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 09:28
 */
public class ByteMarshallableMarshallerTest {
    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr + capacity);

        BytesMarshallable bm = new MockBytesMarshallable(12345678);
        nativeBytes.writeObject(bm);
        nativeBytes.finish();
        nativeBytes.clear();
        BytesMarshallable bm2 = nativeBytes.readObject(MockBytesMarshallable.class);
        assertEquals(bm, bm2);
    }

    static class MockBytesMarshallable implements BytesMarshallable {
        long number;

        MockBytesMarshallable(long number) {
            this.number = number;
        }

        @Override
        public void readMarshallable(@NotNull Bytes in) throws IllegalStateException {
            number = in.readLong();
        }

        @Override
        public void writeMarshallable(@NotNull Bytes out) {
            out.writeLong(number);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockBytesMarshallable that = (MockBytesMarshallable) o;

            if (number != that.number) return false;

            return true;
        }
    }
}
