/*
 * Test input for MMMissingLoopIndex: assertion messages in loops.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

public class InputMissingLoopIndex {

    public void testClassicForMissingIndex() {
        for (int i = 0; i < 3; i++) {
            assertEquals(1, 1, "value should match");
            assertEquals(1, 1, "i should be within bounds");
            assertEquals(1, 1, String.format("value %d should be within bounds", i));
        }
    }

    public void testClassicForMissingIndexWithFail() {
        for (int idx = 0; idx < 3; idx++) {
            fail("unexpected failure");
            fail("idx=" + idx + " should be valid");
        }
    }

    public void testEnhancedForMissingIndex() {
        for (String name : new String[]{"a", "b"}) {
            assertTrue(true, "should pass");
            assertTrue(true, "name should pass in loop");
            assertTrue(true, "name=" + name + " should be valid");
        }
    }

    public void testAssertThatMissingIndex() {
        for (int pos = 0; pos < 2; pos++) {
            MatcherAssert.assertThat("match failed", pos, CoreMatchers.is(1));
            MatcherAssert.assertThat("pos should still match", pos, CoreMatchers.is(1));
        }
    }

    public void testAssertAllHeadingMissingIndex() {
        for (int k = 0; k < 1; k++) {
            assertAll("group checks",
                    () -> assertTrue(true, "k=" + k + " should be valid"),
                    () -> assertEquals(1, 1, "k=" + k + " should be equal"));
            assertAll("k=" + k + " should pass checks",
                    () -> assertTrue(true, "k=" + k + " should be valid again"),
                    () -> assertEquals(1, 1, "k=" + k + " should remain equal"));
        }
    }
}
