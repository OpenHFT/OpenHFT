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
import net.openhft.lang.io.Bytes;
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
        DataValueGenerator dvg = new DataValueGenerator();
//        dvg.setDumpCode(true);
        JavaBeanInterface jbi = dvg.heapInstance(JavaBeanInterface.class);
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
        DataValueGenerator dvg = new DataValueGenerator();
        MinimalInterface mi = dvg.heapInstance(MinimalInterface.class);

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
        mi.writeMarshallable(bbb);
        System.out.println("size: " + bbb.position());

        MinimalInterface mi2 = dvg.heapInstance(MinimalInterface.class);
        bbb.position(0);
        mi2.readMarshallable(bbb);


        assertEquals(1, mi2.byte$());
        assertEquals('2', mi2.char$());
        assertEquals(3, mi2.short$());
        assertEquals(4, mi2.int$());
        assertEquals(5.0, mi2.float$(), 0);
        assertEquals(6, mi2.long$());
        assertEquals(7.0, mi2.double$(), 0.0);
        assertTrue(mi2.flag());
    }

    @Test
    public void testGenerateNative() throws Exception {
        String actual = new DataValueGenerator().generateNativeObject(JavaBeanInterface.class);
//        System.out.println(actual);
        CachedCompiler cc = new CachedCompiler(null, null);
        Class aClass = cc.loadFromJava(JavaBeanInterface.class.getName() + "£native", actual);
        JavaBeanInterface jbi = (JavaBeanInterface) aClass.asSubclass(JavaBeanInterface.class).newInstance();
        Bytes bytes = new ByteBufferBytes(ByteBuffer.allocate(64));
        ((Byteable) jbi).bytes(bytes);
        jbi.setByte((byte) 1);
        jbi.setChar('2');
        jbi.setShort((short) 3);
        jbi.setInt(4);
        jbi.setFloat(5);
        jbi.setLong(6);
        jbi.setDouble(7);
        jbi.setFlag(true);
        assertEquals("", jbi.getString());
        jbi.setString("G'day");
        assertEquals(1, jbi.getByte());
        assertEquals('2', jbi.getChar());
        assertEquals(3, jbi.getShort());
        assertEquals(4, jbi.getInt());
        assertEquals(5.0, jbi.getFloat(), 0);
        assertEquals(6, jbi.getLong());
        assertEquals(7.0, jbi.getDouble(), 0.0);
        assertTrue(jbi.getFlag());
        assertEquals("G'day", jbi.getString());
        assertEquals(38, ((Byteable) jbi).maxSize());
    }


    @Test
    public void testStringFields() {
        DataValueGenerator dvg = new DataValueGenerator();
        StringInterface si = dvg.heapInstance(StringInterface.class);
        si.setString("Hello world");
        assertEquals("Hello world", si.getString());

        StringInterface si2 = dvg.nativeInstance(StringInterface.class);
        Bytes bytes = new ByteBufferBytes(ByteBuffer.allocate(192));
        ((Byteable) si2).bytes(bytes);
        si2.setString("Hello world £€");
        si2.setText("Hello world £€");
        assertEquals("Hello world £€", si2.getString());
        assertEquals("Hello world £€", si2.getText());
    }

    @Test
    public void testNested() {
        DataValueGenerator dvg = new DataValueGenerator();
        NestedB nestedB1 = dvg.heapInstance(NestedB.class);
        nestedB1.ask(100);
        nestedB1.bid(100);
        NestedB nestedB2 = dvg.heapInstance(NestedB.class);
        nestedB2.ask(91);
        nestedB2.bid(92);
        NestedA nestedA = dvg.nativeInstance(NestedA.class);
        Bytes bytes = new ByteBufferBytes(ByteBuffer.allocate(192));
        ((Byteable) nestedA).bytes(bytes);
        nestedA.key("key");
        nestedA.one(nestedB1);
        nestedA.two(nestedB2);
        assertEquals("key", nestedA.key());
        assertEquals(nestedB1.ask(), nestedA.one().ask(), 0.0);
        assertEquals(nestedB1.bid(), nestedA.one().bid(), 0.0);
        assertEquals(nestedB2.ask(), nestedA.two().ask(), 0.0);
        assertEquals(nestedB2.bid(), nestedA.two().bid(), 0.0);
        assertEquals(nestedB1, nestedA.one());
        assertEquals(nestedB2, nestedA.two());
        assertEquals(nestedB1.hashCode(), nestedA.one().hashCode());
        assertEquals(nestedB2.hashCode(), nestedA.two().hashCode());
    }
}
