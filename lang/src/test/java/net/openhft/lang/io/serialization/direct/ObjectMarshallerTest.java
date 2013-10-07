package net.openhft.lang.io.serialization.direct;

import net.openhft.lang.io.*;
import org.junit.Test;

import java.nio.ByteBuffer;

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
        b.reset();
        marshaller.read(b, p);

        assertEquals(55, p.a);

        b.reset();

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
        b.reset();
        marshaller.read(b, p);

        assertEquals(55, p.a);
        assertEquals(-10, p.b);

        b.reset();

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
        b.reset();
        marshaller.read(b, p);

        assertEquals(55, p.a);
        assertEquals(-10, p.b);
        assertEquals(92, p.c);

        b.reset();

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
        b.reset();
        marshaller.read(b, p);

        assertTrue(p.a);

        b.reset();

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
        b.reset();
        marshaller.read(b, p);

        assertTrue(p.a);
        assertEquals(Long.MIN_VALUE, p.b);

        b.reset();

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
        b.reset();
        marshaller.read(b, p);

        assertTrue(p.a);
        assertEquals(Integer.MAX_VALUE, p.b);
        assertEquals(Short.MAX_VALUE, p.c);
        assertEquals(Long.MAX_VALUE, p.d);
        assertEquals(Double.MAX_VALUE, p.e, 0);

        b.reset();

        Primitives6 newP = new Primitives6();
        marshaller.read(b, newP);

        assertTrue(newP.a);
        assertEquals(Integer.MAX_VALUE, p.b);
        assertEquals(Short.MAX_VALUE, p.c);
        assertEquals(Long.MAX_VALUE, p.d);
        assertEquals(Double.MAX_VALUE, p.e, 0);
    }

    private ByteBufferBytes createByteStore() {
        return new ByteBufferBytes(ByteBuffer.allocate(64));
    }
}