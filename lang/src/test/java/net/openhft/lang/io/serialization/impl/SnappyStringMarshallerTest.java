package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SnappyStringMarshallerTest {

    @org.junit.Ignore("TC")
    @Test
    public void testWriteRead() {
        Bytes b = DirectStore.allocate(64 * 1024).bytes();
        testWriteRead(b, "");
        testWriteRead(b, null);
        testWriteRead(b, "Hello World");
        testWriteRead(b, new String(new char[1000000]));
        byte[] bytes = new byte[64000];
        Random random = new Random();
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) ('A' + random.nextInt(26));
        testWriteRead(b, new String(bytes, Charset.forName("ISO-8859-1")));
    }

    private void testWriteRead(Bytes b, String s) {
        b.clear();
        SnappyStringMarshaller.INSTANCE.write(b, s);
        b.writeInt(0x12345678);
        b.flip();
        String s2 = SnappyStringMarshaller.INSTANCE.read(b);
        assertEquals(0x12345678, b.readInt());
        assertEquals(s, s2);
    }

}