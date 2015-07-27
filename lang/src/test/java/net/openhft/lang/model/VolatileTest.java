/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.model;

import net.openhft.compiler.CachedCompiler;
import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.model.constraints.MaxSize;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by daniel on 11/06/2014.
 */
public class VolatileTest {
    @Test
    public void testGenerateJavaCode() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        DataValueGenerator dvg = new DataValueGenerator();
       // dvg.setDumpCode(true);

   /*     try{
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
*/
        //Test the heap interface
        try{
            GoodInterface jbi = dvg.heapInstance(GoodInterface.class);

            jbi.setOrderedY(5);
            assertEquals(5, jbi.getVolatileY());
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
            Bytes bytes = ByteBufferBytes.wrap(ByteBuffer.allocate(64));
            ((Byteable) jbi).bytes(bytes, 0L);

            jbi.setOrderedY(5);
            assertEquals(5, jbi.getVolatileY());
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
        int getX();

        void setOrderedX(int x);
    }

    public interface BadInterface2{
        int getVolatileX();

        void setX(int x);
    }

    public interface GoodInterface{
        int getX();

        void setX(int x);

        int getVolatileY();

        void setOrderedY(int y);

        int getY();

        void setY(int y);

        void setOrderedIntAt(@MaxSize(4) int idx, int i);
        int getVolatileIntAt(int idx);
    }
}
