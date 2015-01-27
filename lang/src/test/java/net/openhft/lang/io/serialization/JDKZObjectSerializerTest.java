package net.openhft.lang.io.serialization;

import net.openhft.lang.io.DirectBytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.io.ObjectOutputStream;
import java.util.zip.DeflaterOutputStream;

import static org.junit.Assert.assertEquals;

public class JDKZObjectSerializerTest {

    @org.junit.Ignore("TC")
    @Test
    public void testReadSerializable() throws Exception {
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(bytes.outputStream());
            oos.writeObject("hello");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.release();
        }
        {
            DirectBytes bytes = DirectStore.allocate(1024).bytes();
            bytes.writeInt(0);
            ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(bytes.outputStream()));
            oos.writeObject("hello world");
            oos.close();
            bytes.writeUnsignedInt(0, bytes.position() - 4);

            bytes.flip();
            assertEquals("hello world", JDKZObjectSerializer.INSTANCE.readSerializable(bytes, null, null));
            bytes.close();
        }
    }
}