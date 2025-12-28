/*
 * Test input for @SuppressWarnings handling.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputSuppressWarnings {

    @SuppressWarnings("checkstyle:MeaningfulMessage")
    public void suppressedMethod() {
        assertEquals(1, 1, "actual");
    }

    public void unsuppressedMethod() {
        assertEquals(1, 1, "expected");
    }

    @SuppressWarnings("checkstyle:MeaningfulMessage")
    public static class SuppressedInner {
        public void innerSuppressedMethod() {
            assertEquals(1, 1, "value");
        }
    }

    @SuppressWarnings("MM-all")
    public static class SuppressedAllInner {
        public void innerSuppressedAllMethod() {
            assertEquals(1, 1, "value");
        }
    }

    @SuppressWarnings("MM-all")
    public void suppressedAllWarnings() {
        assertEquals(1, 1, "value");
    }
}
