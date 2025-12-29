/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Exercises rules for AssertJ overrides and trivial supplier messages in tests.
 */
public class InputAssertJAndSupplierRules {

    // ========== MMAssertJGenericOverride: AssertJ generic override ==========

    void testAssertJOverride() {
        Object result = new Object();
        Object expected = new Object();

        // These should trigger MMAssertJGenericOverride - generic/problematic messages with withFailMessage
        assertThat(result).withFailMessage("value").isEqualTo(expected);
        assertThat(result).withFailMessage("check").isEqualTo(expected);
        assertThat(result).overridingErrorMessage("test").isEqualTo(expected);
        assertThat(result).withFailMessage("ok").isEqualTo(expected);

        // These are OK - as() is preferred for context
        assertThat(result).as("computed hash should match stored value").isEqualTo(expected);

        // These are OK - withFailMessage with good message
        assertThat(result).withFailMessage("API response status should be 200 for valid request").isEqualTo(expected);
    }

    // ========== MMTrivialSupplier: Supplier messages ==========

    void testCheapSupplier() {
        String name = "test";
        int value = 42;
        String suffix = "suffix";

        // These should trigger MMTrivialSupplier - simple concatenation in supplier
        assertEquals(1, 2, () -> "value: " + name);
        assertEquals(1, 2, () -> name); // MMTrivialSupplier: cheap supplier (no literals)
        assertEquals(1, 2, () -> name + suffix); // MMTrivialSupplier: cheap supplier (+ without literals)
        assertEquals(1, 2, () -> "prefix " + name + " suffix");

        // These should trigger MMTrivialSupplier - constant supplier
        assertEquals(1, 2, () -> "constant message");

        // These are OK - plain String concatenation (no supplier)
        assertEquals(1, 2, "value: " + name);

        // These should trigger MMTrivialSupplier - simple concatenation with method call
        assertEquals(1, 2, () -> "computed: " + computeExpensiveValue());
    }

    private String computeExpensiveValue() {
        return "expensive";
    }
}
