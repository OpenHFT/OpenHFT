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

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import net.openhft.lang.io.NativeBytes;
import org.junit.Test;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * User: peter.lawrey Date: 20/09/13 Time: 09:28
 */
public class VanillaBytesMarshallerTest {
    @Test
    public void testObjects() {
        DirectBytes bytes = new DirectStore(1024).createSlice();
        Object[] objects = {1, 1L, 1.0, "Hello"};
        for (Object o : objects) {
            long pos = bytes.position();
            bytes.writeObject(o);
            System.out.printf("%s used %,d bytes%n", o.getClass(), bytes.position() - pos);
        }
        bytes.reset();
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
