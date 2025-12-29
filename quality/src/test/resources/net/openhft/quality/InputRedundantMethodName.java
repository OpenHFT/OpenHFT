/*
 * Test input for redundant method name check.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

public class InputRedundantMethodName {
    public void testSomething() {
        assertEquals("testSomething assertion failed", 1, 1);
    }
}
