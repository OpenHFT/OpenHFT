package net.openhft.lang.io;

import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter
 * Date: 20/09/13
 * Time: 09:28
 */
public class VanillaBytesMarshallerTest {
    @Test
    public void testMarshallable() {
        int capacity = 2 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(capacity);
        long addr = ((DirectBuffer) byteBuffer).address();
        NativeBytes nativeBytes = new NativeBytes(addr, addr, addr + capacity);

        nativeBytes.writeObject(BuySell.BUY);
        nativeBytes.writeObject(BuySell.SELL);
        nativeBytes.finish();
        nativeBytes.reset();
        assertEquals(BuySell.BUY, nativeBytes.readObject());
        assertEquals(BuySell.SELL, nativeBytes.readObject());
    }

    enum BuySell {
        BUY, SELL
    }
}
