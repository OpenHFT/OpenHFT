package net.openhft.lang.data;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by peter on 1/10/15.
 */
public interface WireKey {
    String name();

    default int code() {
        return name().hashCode();
    }

    default Type type() {
        return defaultValue().getClass();
    }

    default boolean skipIfDefault() {
        return defaultValue() != null;
    }

    default boolean hasDefault() {
        return defaultValue() != null;
    }

    default EncodeMode encodeMode() {
        return EncodeMode.LITERAL;
    }

    default Object defaultValue() {
        return null;
    }

    static void checkKeys(WireKey[] keys) {
        Map<Integer, WireKey> codes = new HashMap<>();
        for (WireKey key : keys) {
            WireKey pkey = codes.put(key.code(), key);
            if (pkey != null)
                throw new AssertionError(pkey + " and " + key + " have the same code " + key.code());
        }
    }
}
