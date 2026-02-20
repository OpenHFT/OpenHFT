/*
 * Test input with good assert messages - no violations expected.
 */
package net.openhft.quality;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputGoodAssertMessages {
    public void test() {
        assertEquals(1, 1, "value should be in positive range because rounding must be stable");
        assertEquals(0, 0, "array contents should be empty");
        assertEquals(true, true, "connection should be open");
    }

    public void testRequireNonNull(Object value) {
        requireNonNull(value);
    }

    public Object fallbackValue() {
        // Fallback cache miss returns null to preserve fixture semantics during warmup verification.
        return null;
    }
}
