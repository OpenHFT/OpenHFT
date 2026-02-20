/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises MMMissingComparisonValues with assertions that omit comparison values and context in messages.
 */
public class InputComparisonValues {

    void testMissingComparisonValues() {
        int a = 5;
        int b = 3;
        int count = 10;
        int minCount = 5;

        // These should trigger MMMissingComparisonValues - comparison with constant message
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

        // These are OK - message includes dynamic values
        assertTrue(a > b, "comparison should hold for a=" + a + " b=" + b);

        // These should trigger MMTrivialSupplier - simple concatenation in supplier
        assertTrue(a > b, () -> a + " should be > " + b);

        // These are OK - not a comparison
        assertTrue(isValid(), "result should be valid");
        assertFalse(isEmpty(), "collection should not be empty");
    }

    private boolean isValid() {
        return true;
    }

    private boolean isEmpty() {
        return false;
    }
}
