package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class StringZMarshallerTest {
    static final Bytes b = DirectStore.allocate(1024).bytes();

    @Test
    public void testWriteRead() {
        testWriteRead("");
        testWriteRead(null);
        testWriteRead("Hello World");
        testWriteRead(new String(new char[1024]));
        byte[] bytes = new byte[1024];
        new Random().nextBytes(bytes);
        testWriteRead(new String(bytes, StandardCharsets.US_ASCII));
    }

    private void testWriteRead(String s) {
        b.clear();
        StringZMarshaller.INSTANCE.write(b, s);
        b.writeInt(0x12345678);
        b.flip();
        String s2 = StringZMarshaller.INSTANCE.read(b);
        assertEquals(0x12345678, b.readInt());
        assertEquals(s, s2);
    }

}