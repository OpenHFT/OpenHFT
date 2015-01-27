package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StringZMapMarshallerTest {
    static final Bytes b = DirectStore.allocate(1024).bytes();

    @org.junit.Ignore("TC")
    @Test
    public void testWriteRead() {
        testWriteRead(null);
        testWriteRead(Collections.<String, String>emptyMap());
        testWriteRead(mapOf("Hello", "World", "aye", "alpha", "bee", "beta", "zed", "zeta"));
    }

    private void testWriteRead(Map<String, String> map) {
        b.clear();
        StringZMapMarshaller.INSTANCE.write(b, map);
        b.writeInt(0x12345678);
        b.flip();
        Map<String, String> s2 = StringZMapMarshaller.INSTANCE.read(b);
        assertEquals(map, s2);
        assertEquals(0x12345678, b.readInt());
    }

    public static <K, V> Map<K, V> mapOf(K k, V v, Object... keysAndValues) {
        Map<K, V> ret = new LinkedHashMap<K, V>();
        ret.put(k, v);
        for (int i = 0; i < keysAndValues.length - 1; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            ret.put((K) key, (V) value);
        }
        return ret;
    }

}