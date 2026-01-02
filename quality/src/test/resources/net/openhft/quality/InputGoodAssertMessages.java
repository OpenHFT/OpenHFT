/*
 * Test input with good assert messages - no violations expected.
 */
package net.openhft.quality;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

public class InputGoodAssertMessages {
    public void test() {
        assertEquals("expected positive range value", 1, 1);
        assertEquals("array contents should be empty", 0, 0);
        assertEquals("connection should be open", true, true);
    }

    public void testRequireNonNull(Object value) {
        requireNonNull(value);
    }
}
