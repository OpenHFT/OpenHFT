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

package net.openhft.lang.model;

import net.openhft.compiler.CachedCompiler;
import net.openhft.lang.io.ByteBufferBytes;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: plawrey
 * Date: 06/10/13
 * Time: 20:13
 */
public class DataValueGeneratorTest {
    @Test
    public void testGenerateJavaCode() throws Exception {
        String actual = new DataValueGenerator().generateJavaCode(JavaBeanInterface.class);
//        System.out.println(actual);
        CachedCompiler cc = new CachedCompiler(null, null);
        Class aClass = cc.loadFromJava(JavaBeanInterface.class.getName() + '_', actual);
        JavaBeanInterface jbi = (JavaBeanInterface) aClass.asSubclass(JavaBeanInterface.class).newInstance();
        jbi.setByte((byte) 1);
        jbi.setChar('2');
        jbi.setShort((short) 3);
        jbi.setInt(4);
        jbi.setFloat(5);
        jbi.setLong(6);
        jbi.setDouble(7);
        jbi.setFlag(true);
        assertEquals(1, jbi.getByte());
        assertEquals('2', jbi.getChar());
        assertEquals(3, jbi.getShort());
        assertEquals(4, jbi.getInt());
        assertEquals(5.0, jbi.getFloat(), 0);
        assertEquals(6, jbi.getLong());
        assertEquals(7.0, jbi.getDouble(), 0.0);
        assertTrue(jbi.getFlag());
    }

    @Test
    public void testGenerateJavaCode2() throws Exception {
        String actual = new DataValueGenerator().generateJavaCode(MinimalInterface.class);
        CachedCompiler cc = new CachedCompiler(null, null);
        Class aClass = cc.loadFromJava(MinimalInterface.class.getName() + '_', actual);
        MinimalInterface mi = (MinimalInterface) aClass.asSubclass(MinimalInterface.class).newInstance();
        mi.byte$((byte) 1);
        mi.char$('2');
        mi.short$((short) 3);
        mi.int$(4);
        mi.float$(5);
        mi.long$(6);
        mi.double$(7);
        mi.flag(true);

        assertEquals(1, mi.byte$());
        assertEquals('2', mi.char$());
        assertEquals(3, mi.short$());
        assertEquals(4, mi.int$());
        assertEquals(5.0, mi.float$(), 0);
        assertEquals(6, mi.long$());
        assertEquals(7.0, mi.double$(), 0.0);
        assertTrue(mi.flag());

        ByteBufferBytes bbb = new ByteBufferBytes(ByteBuffer.allocate(64));
        mi.writeExternal(bbb);
        System.out.println("size: " + bbb.position());

        MinimalInterface mi2 = (MinimalInterface) aClass.newInstance();
        bbb.position(0);
        mi2.readExternal(bbb);


        assertEquals(1, mi2.byte$());
        assertEquals('2', mi2.char$());
        assertEquals(3, mi2.short$());
        assertEquals(4, mi2.int$());
        assertEquals(5.0, mi2.float$(), 0);
        assertEquals(6, mi2.long$());
        assertEquals(7.0, mi2.double$(), 0.0);
        assertTrue(mi2.flag());
    }
}
