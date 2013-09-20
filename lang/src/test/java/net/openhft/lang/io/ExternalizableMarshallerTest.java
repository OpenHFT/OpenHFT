package net.openhft.lang.io;

import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 09:28
 */
public class ExternalizableMarshallerTest {
    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr, addr + capacity);

        Externalizable bm = new MockExternalizable(12345678);
        nativeBytes.writeObject(bm);
        nativeBytes.finish();
        nativeBytes.reset();
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
