package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SnappyStringMarshallerTest {
    static final Bytes b = DirectStore.allocate(1024).bytes();

    @Test
    public void testWriteRead() {
        testWriteRead("");
        testWriteRead(null);
        testWriteRead("Hello World");
        testWriteRead(new String(new char[1000]));
        byte[] bytes = new byte[960];
        Random random = new Random();
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) ('A' + random.nextInt(26));
        testWriteRead(new String(bytes, StandardCharsets.ISO_8859_1));
    }

    private void testWriteRead(String s) {
        b.clear();
        SnappyStringMarshaller.INSTANCE.write(b, s);
        b.writeInt(0x12345678);
        b.flip();
        String s2 = SnappyStringMarshaller.INSTANCE.read(b);
        assertEquals(0x12345678, b.readInt());
        assertEquals(s, s2);
    }

}