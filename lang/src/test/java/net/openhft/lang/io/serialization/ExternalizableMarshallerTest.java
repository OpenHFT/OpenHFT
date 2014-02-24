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

import net.openhft.lang.io.NativeBytes;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey
 * Date: 20/09/13
 * Time: 09:28
 */
public class ExternalizableMarshallerTest {
    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr + capacity);

        Externalizable bm = new MockExternalizable(12345678);
        nativeBytes.writeObject(bm);
        nativeBytes.finish();
        nativeBytes.clear();
        Externalizable bm2 = nativeBytes.readObject(MockExternalizable.class);
        assertEquals(bm, bm2);
    }

    static class MockExternalizable implements Externalizable {
        long number;

        MockExternalizable(long number) {
            this.number = number;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(number);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            number = in.readLong();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MockExternalizable that = (MockExternalizable) o;

            if (number != that.number) return false;

            return true;
        }
    }
}
