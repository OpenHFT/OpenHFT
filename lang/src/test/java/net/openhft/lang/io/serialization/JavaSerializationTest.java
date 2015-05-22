package net.openhft.lang.io.serialization;

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Rob Austin.
 */
public class JavaSerializationTest {

    @Test
    @Ignore
    public void NullPointerException() {
        final Bytes bytes = ByteBufferBytes.wrap(ByteBuffer.allocate((int) 1024).order
                (ByteOrder.nativeOrder()));

        NullPointerException expected = new NullPointerException("test");
        bytes.writeObject(expected);

        bytes.position(0);
        NullPointerException actual = (NullPointerException)bytes.readObject();

        Assert.assertEquals(expected, actual);
    }
}
