/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.io.ByteBufferBytes;
import net.openhft.lang.io.Bytes;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Collections;

import static net.openhft.lang.io.serialization.direct.TestClasses.*;
import static org.junit.Assert.*;

public class ObjectMarshallerTest {

    @Test
    public void marshalAndUnmarshalPrimitives1() {
        Primitives1 p = new Primitives1();
        p.a = 55;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives1> marshaller = ObjectMarshallers.forClass(Primitives1.class);
        marshaller.write(b, p);

        p.a = 0;
        b.clear();
        marshaller.read(b, p);

        assertEquals(55, p.a);

        b.clear();

        Primitives1 newP = new Primitives1();
        marshaller.read(b, newP);

        assertEquals(55, newP.a);
    }

    @Test
    public void marshalAndUnmarshalPrimitives2() {
        Primitives2 p = new Primitives2();
        p.a = 55;
        p.b = -10;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives2> marshaller = ObjectMarshallers.forClass(Primitives2.class);
        marshaller.write(b, p);

        p.a = 0;
        p.b = 0;
        b.clear();
        marshaller.read(b, p);

        assertEquals(55, p.a);
        assertEquals(-10, p.b);

        b.clear();

        Primitives2 newP = new Primitives2();
        marshaller.read(b, newP);

        assertEquals(55, newP.a);
        assertEquals(-10, newP.b);
    }

    @Test
    public void marshalAndUnmarshalPrimitives3() {
        Primitives3 p = new Primitives3();
        p.a = 55;
        p.b = -10;
        p.c = 92;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives3> marshaller = ObjectMarshallers.forClass(Primitives3.class);
        marshaller.write(b, p);

        p.a = 0;
        p.b = 0;
        p.c = 0;
        b.clear();
        marshaller.read(b, p);

        assertEquals(55, p.a);
        assertEquals(-10, p.b);
        assertEquals(92, p.c);

        b.clear();

        Primitives3 newP = new Primitives3();
        marshaller.read(b, newP);

        assertEquals(55, newP.a);
        assertEquals(-10, newP.b);
        assertEquals(92, newP.c);
    }

    @Test
    public void marshalAndUnmarshalPrimitives4() {
        Primitives4 p = new Primitives4();
        p.a = true;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives4> marshaller = ObjectMarshallers.forClass(Primitives4.class);
        marshaller.write(b, p);

        p.a = false;
        b.clear();
        marshaller.read(b, p);

        assertTrue(p.a);

        b.clear();

        Primitives4 newP = new Primitives4();
        marshaller.read(b, newP);

        assertTrue(newP.a);
    }

    @Test
    public void marshalAndUnmarshalPrimitives5() {
        Primitives5 p = new Primitives5();
        p.a = true;
        p.b = Long.MIN_VALUE;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives5> marshaller = ObjectMarshallers.forClass(Primitives5.class);
        marshaller.write(b, p);

        p.a = false;
        p.b = 0;
        b.clear();
        marshaller.read(b, p);

        assertTrue(p.a);
        assertEquals(Long.MIN_VALUE, p.b);

        b.clear();

        Primitives5 newP = new Primitives5();
        marshaller.read(b, newP);

        assertTrue(newP.a);
        assertEquals(Long.MIN_VALUE, newP.b);
    }

    @Test
    public void marshalAndUnmarshalPrimitives6() {
        Primitives6 p = new Primitives6();
        p.a = true;
        p.b = Integer.MAX_VALUE;
        p.c = Short.MAX_VALUE;
        p.d = Long.MAX_VALUE;
        p.e = Double.MAX_VALUE;

        Bytes b = createByteStore();
        ObjectMarshaller<Primitives6> marshaller = ObjectMarshallers.forClass(Primitives6.class);
        marshaller.write(b, p);

        p.a = false;
        p.b = 0;
        p.c = 0;
        p.d = 0;
        p.e = 0;
        b.clear();
        marshaller.read(b, p);

        assertTrue(p.a);
        assertEquals(Integer.MAX_VALUE, p.b);
        assertEquals(Short.MAX_VALUE, p.c);
        assertEquals(Long.MAX_VALUE, p.d);
        assertEquals(Double.MAX_VALUE, p.e, 0);

        b.clear();

        Primitives6 newP = new Primitives6();
        marshaller.read(b, newP);

        assertTrue(newP.a);
        assertEquals(Integer.MAX_VALUE, p.b);
        assertEquals(Short.MAX_VALUE, p.c);
        assertEquals(Long.MAX_VALUE, p.d);
        assertEquals(Double.MAX_VALUE, p.e, 0);
    }

    @Test
    public void marshalAndUnmarshalMixedClass() {
        MixedFields m = new MixedFields();
        m.intField = Integer.MIN_VALUE + 1;
        m.byteField = Byte.MAX_VALUE - 1;
        m.shortField = Short.MIN_VALUE + 1;
        m.longField = Long.MAX_VALUE - 1;

        m.doubleArray = new double[]{-50.0, 50.0};
        m.stringList = Collections.singletonList("Foo");
        m.objectArray = new Object[]{new Primitives1()};
        m.transientShort = 12;
        m.transientObject = new Primitives2();

        Bytes b = createByteStore();
        ObjectMarshaller<MixedFields> marshaller = ObjectMarshallers.forClass(MixedFields.class);
        marshaller.write(b, m);

        m.intField = 0;
        m.byteField = 0;
        m.shortField = 0;
        m.longField = 0;
        m.doubleArray = null;
        m.stringList = null;
        m.objectArray = null;
        m.transientShort = 0;
        m.transientObject = null;

        b.clear();
        marshaller.read(b, m);

        assertEquals(Integer.MIN_VALUE + 1, m.intField);
        assertEquals(0, m.byteField); // because of jvm field rearrangement the two shorts are packed into 4 bytes and this eligible field is skipped
        assertEquals(Short.MIN_VALUE + 1, m.shortField);
        assertEquals(Long.MAX_VALUE - 1, m.longField);
        assertNull(m.doubleArray);
        assertNull(m.stringList);
        assertNull(m.objectArray);
        assertNull(m.transientObject);
        assertEquals(0, m.transientShort);

        b.clear();

        MixedFields newM = new MixedFields();
        marshaller.read(b, newM);

        assertEquals(Integer.MIN_VALUE + 1, newM.intField);
        assertEquals(0, newM.byteField);
        assertEquals(Short.MIN_VALUE + 1, newM.shortField);
        assertEquals(Long.MAX_VALUE - 1, newM.longField);
        assertNull(newM.doubleArray);
        assertNull(newM.stringList);
        assertNull(newM.objectArray);
        assertNull(newM.transientObject);
        assertEquals(0, newM.transientShort);
    }

    private Bytes createByteStore() {
        return ByteBufferBytes.wrap(ByteBuffer.allocate(64));
    }
}