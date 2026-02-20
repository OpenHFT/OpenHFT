/*
 * Test input for redundant line number check.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

public class InputRedundantLineNumber {
    public void test() {
        assertEquals("error at line 10", 1, 1);
        assertEquals("check L42 failed", 2, 2);
    }
}
