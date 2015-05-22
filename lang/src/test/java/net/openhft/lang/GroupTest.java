package net.openhft.lang;

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.IByteBufferBytes;
import net.openhft.lang.model.Byteable;
import net.openhft.lang.model.DataValueClasses;
import net.openhft.lang.model.constraints.Group;
import net.openhft.lang.model.constraints.MaxSize;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * Rob Austin
 */
public class GroupTest {

    @Test
    public void test() {
        IByteBufferBytes byteBufferBytes = ByteBufferBytes.wrap(ByteBuffer.allocate(1024));

        {
            BaseInterface baseInterface = DataValueClasses.newDirectReference(BaseInterface.class);

            ((Byteable) baseInterface).bytes(byteBufferBytes, 0);

            baseInterface.setInt(1);
            baseInterface.setStr("Hello World");

            Assert.assertEquals(1, baseInterface.getInt());
            Assert.assertEquals("Hello World", baseInterface.getStr());
        }
        {
            ExtInterface extInterface = DataValueClasses.newDirectReference(ExtInterface.class);
            byteBufferBytes.clear();
            ((Byteable) extInterface).bytes(byteBufferBytes, 0);
            extInterface.setInt2(43);

            Assert.assertEquals(1, extInterface.getInt());
            Assert.assertEquals(43, extInterface.getInt2());
            Assert.assertEquals("Hello World", extInterface.getStr());
            extInterface.setInt(2);

            Assert.assertEquals(2, extInterface.getInt());
        }
    }

    @Test
    public void test2() {
        IByteBufferBytes byteBufferBytes = ByteBufferBytes.wrap(ByteBuffer.allocate(1024));

        {
            ExtInterface extInterface = DataValueClasses.newDirectReference(ExtInterface.class);
            byteBufferBytes.clear();
            ((Byteable) extInterface).bytes(byteBufferBytes, 0);

            extInterface.setInt(1);
            extInterface.setInt2(2);
            extInterface.setStr("Hello World");

            Assert.assertEquals(1, extInterface.getInt());
            Assert.assertEquals(1, extInterface.getInt());
            Assert.assertEquals("Hello World", extInterface.getStr());
        }

        {
            BaseInterface baseInterface = DataValueClasses.newDirectReference(BaseInterface.class);
            byteBufferBytes.clear();
            ((Byteable) baseInterface).bytes(byteBufferBytes, 0);

            Assert.assertEquals(1, baseInterface.getInt());
            Assert.assertEquals("Hello World", baseInterface.getStr());
        }
    }

    public interface BaseInterface {

        String getStr();

        void setStr(@MaxSize(15) String str);

        int getInt();

        void setInt(int i);
    }

    public interface ExtInterface extends BaseInterface {

        int getInt2();

        @Group(1)
        void setInt2(int i);
    }
}
