/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test input for AMQ18: Missing comparison values in message.
 */
public class InputComparisonValues {

    void testMissingComparisonValues() {
        int a = 5;
        int b = 3;
        int count = 10;
        int minCount = 5;

        // These should trigger AMQ18 - comparison with constant message
        assertTrue(a > b, "a should be greater than b");
        assertTrue(a >= b, "a should be at least b");
        assertTrue(a < b, "a should be less than b");
        assertTrue(a <= b, "a should be at most b");
        assertTrue(a == b, "a should equal b");
        assertTrue(a != b, "a should not equal b");
        assertFalse(a > b, "a should not be greater than b");

        // These are OK - message includes dynamic values
        assertTrue(a > b, a + " should be greater than " + b);
        assertTrue(count >= minCount, "count " + count + " should be >= " + minCount);

        // These are OK - no message provided
        assertTrue(a > b);

        // These are OK - lambda supplier (assumes dynamic content)
        assertTrue(a > b, () -> a + " should be > " + b);

        // These are OK - not a comparison
        assertTrue(isValid(), "should be valid");
        assertFalse(isEmpty(), "should not be empty");
    }

    private boolean isValid() {
        return true;
    }

    private boolean isEmpty() {
        return false;
    }
}
