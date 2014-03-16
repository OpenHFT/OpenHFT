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
