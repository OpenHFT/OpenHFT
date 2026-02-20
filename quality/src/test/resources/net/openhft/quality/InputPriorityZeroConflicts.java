/*
 * Test input for priority-0 rule conflicts (missing loop index vs restated derived assertions).
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputPriorityZeroConflicts {

    public void testMissingLoopIndexVsDerived() {
        for (int i = 0; i < 1; i++) {
            assertTrue("".isEmpty(), "empty");
            assertTrue("value".isEmpty(), "blank");
        }
    }

    public void testMissingLoopIndexVsDerivedForEach() {
        for (String name : new String[]{"a", "b"}) {
            assertTrue(name.isEmpty(), "present");
        }
    }
}
