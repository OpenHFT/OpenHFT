package net.openhft.lang.io;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 09:28
 */
public class ByteMarshallableMarshallerTest {
    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr, addr + capacity);

        BytesMarshallable bm = new MockBytesMarshallable(12345678);
        nativeBytes.writeObject(bm);
        nativeBytes.finish();
        nativeBytes.reset();
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
