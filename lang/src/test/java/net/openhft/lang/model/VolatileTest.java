package net.openhft.lang.model;

import net.openhft.compiler.CachedCompiler;
import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.MaxSize;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by daniel on 11/06/2014.
 */
public class VolatileTest {
    @Test
    public void testGenerateJavaCode() throws Exception {
        DataValueGenerator dvg = new DataValueGenerator();
        //dvg.setDumpCode(true);

        try{
            BadInterface1 jbi = dvg.heapInstance(BadInterface1.class);
            assertFalse("Should have thrown an IllegalArgumentException", true);
        }catch(AssertionError e){
            assertTrue("Throws an IllegalArgumentException", true);
        }

        try{
            BadInterface2 jbi = dvg.heapInstance(BadInterface2.class);
            assertFalse("Should have thrown an IllegalArgumentException", true);
        }catch(AssertionError e){
            assertTrue("Throws an IllegalArgumentException", true);
        }

        //Test the heap interface
        try{
            GoodInterface jbi = dvg.heapInstance(GoodInterface.class);

            jbi.setOrderedX(5);
            assertEquals(5, jbi.getVolatileX());
            jbi.setOrderedIntAt(0,0);
            jbi.setOrderedIntAt(1,1);
            jbi.setOrderedIntAt(2,2);
            jbi.setOrderedIntAt(3,3);

            assertEquals(0, jbi.getVolatileIntAt(0));
            assertEquals(1, jbi.getVolatileIntAt(1));
            assertEquals(2, jbi.getVolatileIntAt(2));
            assertEquals(3, jbi.getVolatileIntAt(3));
        }catch(AssertionError e){
            e.printStackTrace();
            assertFalse("Throws an IllegalArgumentException", true);
        }

        //Test the native interface
        try{
            String actual = new DataValueGenerator().generateNativeObject(GoodInterface.class);
            System.out.println(actual);
            CachedCompiler cc = new CachedCompiler(null, null);
            Class aClass = cc.loadFromJava(GoodInterface.class.getName() + "$$Native", actual);
            GoodInterface jbi = (GoodInterface) aClass.asSubclass(GoodInterface.class).newInstance();
            Bytes bytes = new ByteBufferBytes(ByteBuffer.allocate(64));
            ((Byteable) jbi).bytes(bytes, 0L);
            
            jbi.setOrderedX(5);
            assertEquals(5, jbi.getVolatileX());
            jbi.setOrderedIntAt(0,0);
            jbi.setOrderedIntAt(1,1);
            jbi.setOrderedIntAt(2,2);
            jbi.setOrderedIntAt(3,3);

            assertEquals(0, jbi.getVolatileIntAt(0));
            assertEquals(1, jbi.getVolatileIntAt(1));
            assertEquals(2, jbi.getVolatileIntAt(2));
            assertEquals(3, jbi.getVolatileIntAt(3));
        }catch(AssertionError e){
            e.printStackTrace();
            assertFalse("Throws an IllegalArgumentException", true);
        }

    }

    public interface BadInterface1{
        public int getX();
        public void setOrderedX(int x);
    }

    public interface BadInterface2{
        public int getVolatileX();
        public void setX(int x);
    }

    public interface GoodInterface{
        public int getVolatileX();
        public void setOrderedX(int x);

        void setOrderedIntAt(@MaxSize(4) int idx, int i);
        int getVolatileIntAt(int idx);
    }
}
