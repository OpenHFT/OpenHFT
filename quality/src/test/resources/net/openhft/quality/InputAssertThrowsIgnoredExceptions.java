/*
 * Test input for ignored exception classes in assertThrows and throw checks.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class InputAssertThrowsIgnoredExceptions {

    public void testIgnoredExceptions() {
        assertThrows(IllegalArgumentException.class,
                () -> { throw new IllegalArgumentException("bad"); },
                "IllegalArgumentException");
        assertThrowsExactly(UnsupportedOperationException.class,
                () -> { throw new UnsupportedOperationException("no"); },
                "UnsupportedOperationException");
    }

    public void testNotIgnoredException() {
        assertThrows(IllegalStateException.class,
                () -> { throw new IllegalStateException("bad"); },
                "IllegalStateException");
    }

    public void testIgnoredThrow() {
        throw new UnsupportedOperationException("operation not supported for this test");
    }
}
