/*
 * Test input with good assert messages - no violations expected.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

import java.util.Objects;

public class InputGoodAssertMessages {
    public void test() {
        assertEquals("expected positive range value", 1, 1);
        assertEquals("array contents should be empty", 0, 0);
        assertEquals("connection should be open", true, true);
    }

    public void testRequireNonNull(Object value) {
        Objects.requireNonNull(value, "value");
    }
}
