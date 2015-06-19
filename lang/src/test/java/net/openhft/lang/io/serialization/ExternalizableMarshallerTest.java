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
