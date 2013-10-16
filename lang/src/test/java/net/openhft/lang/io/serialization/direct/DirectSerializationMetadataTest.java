package net.openhft.lang.io.serialization.direct;

import org.junit.Test;

import static net.openhft.lang.io.AbstractBytes.UNSIGNED_INT_MASK;
import static net.openhft.lang.io.NativeBytes.UNSAFE;
import static net.openhft.lang.io.serialization.direct.DirectSerializationMetadata.*;
import static net.openhft.lang.io.serialization.direct.TestClasses.*;
import static org.junit.Assert.assertEquals;

public class DirectSerializationMetadataTest {

    @Test
    public void primitives1Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives1());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives1.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(4, serializationMetadata.length);
    }

    @Test
    public void primitives2Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives2());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives2.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(12, serializationMetadata.length);
    }

    @Test
    public void primitives3Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives3());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives3.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(12, serializationMetadata.length);
    }

    @Test
    public void primitives4Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives4());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives4.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(4, serializationMetadata.length);
    }

    @Test
    public void primitives5Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives5());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives5.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(12, serializationMetadata.length);
    }

    @Test
    public void primitives6Metadata() {
        unsafeClassHeaderSize64BitCompressedOops(new Primitives6());

        SerializationMetadata serializationMetadata = extractMetadata(Introspect.fields(Primitives6.class));
        assertEquals(OBJECT_HEADER_SIZE, serializationMetadata.start);
        assertEquals(28, serializationMetadata.length);
    }

    private void unsafeClassHeaderSize64BitCompressedOops(Object instance) {
        if (System.getProperty("useUnsafeClassHeaders") != null) {
            long narrowKlassPointer = UNSAFE.getInt(instance, NATIVE_WORD_SIZE) & UNSIGNED_INT_MASK;
            long wideKlassPointer = narrowKlassPointer << 3;
            long sizeField = wideKlassPointer + 3 * UNSAFE.addressSize();

            System.out.println(String.format("Size of %s is %s",
                    instance.getClass().getName(),
                    UNSAFE.getAddress(sizeField) & UNSIGNED_INT_MASK));
        }
    }
}
