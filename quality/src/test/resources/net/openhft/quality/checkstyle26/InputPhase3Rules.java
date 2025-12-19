/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Test input for Phase 3 rules: AMQ16, AMQ17.
 */
public class InputPhase3Rules {

    // ========== AMQ16: AssertJ generic override ==========

    void testAssertJOverride() {
        Object result = new Object();
        Object expected = new Object();

        // These should trigger AMQ16 - generic/problematic messages with withFailMessage
        assertThat(result).withFailMessage("value").isEqualTo(expected);
        assertThat(result).withFailMessage("check").isEqualTo(expected);
        assertThat(result).overridingErrorMessage("test").isEqualTo(expected);
        assertThat(result).withFailMessage("ok").isEqualTo(expected);

        // These are OK - as() is preferred for context
        assertThat(result).as("computed hash should match stored value").isEqualTo(expected);

        // These are OK - withFailMessage with good message
        assertThat(result).withFailMessage("API response status should be 200 for valid request").isEqualTo(expected);
    }

    // ========== AMQ17: Cheap supplier ==========

    void testCheapSupplier() {
        String name = "test";
        int value = 42;

        // These should trigger AMQ17 - cheap concatenation in supplier
        assertEquals(1, 2, () -> "value: " + name);
        assertEquals(1, 2, () -> "failed for " + value);
        assertEquals(1, 2, () -> "prefix " + name + " suffix");

        // These are OK - trivial supplier (constant only) triggers AMQ05 instead
        assertEquals(1, 2, () -> "constant message");

        // These are OK - plain String concatenation (no supplier)
        assertEquals(1, 2, "value: " + name);

        // These are OK - expensive operations justify supplier
        // (method calls are not detected as cheap)
        assertEquals(1, 2, () -> "computed: " + computeExpensiveValue());
    }

    private String computeExpensiveValue() {
        return "expensive";
    }
}
