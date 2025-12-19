/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test input for Phase 2 rules: AMQ13, AMQ14, AMQ15.
 */
public class InputPhase2Rules {

    // ========== AMQ13: Message duplicates input ==========

    void testDuplicatesInput() {
        String expected = "admin";
        String actual = "user";

        // These should trigger AMQ13
        assertEquals("admin", actual, "admin");  // message duplicates expected value
        assertEquals(expected, actual, "expected");  // message duplicates variable name

        // These are OK - message explains the invariant
        assertEquals("admin", actual, "user role should be admin after promotion");
        assertEquals(expected, actual, "computed hash should match stored value");
    }

    // ========== AMQ14: Low-signal assertAll headings ==========

    void testAssertAllHeadings() {
        Object order = new Object();
        Object user = new Object();

        // These should trigger AMQ14 - low-signal headings
        assertAll("assertAll",
            () -> assertTrue(true)
        );

        assertAll("assertions",
            () -> assertTrue(true)
        );

        assertAll("checks",
            () -> assertTrue(true)
        );

        assertAll("validation",
            () -> assertTrue(true)
        );

        assertAll("test",
            () -> assertTrue(true)
        );

        // These are OK - descriptive headings
        assertAll("order should be complete and valid",
            () -> assertTrue(true)
        );

        assertAll("user initialization checks",
            () -> assertTrue(true)
        );
    }

    // ========== AMQ15: Restates derived assertion ==========

    void testRestatesDerived() {
        // These should trigger AMQ15
        assertTrue(true, "empty");
        assertTrue(true, "is empty");
        assertTrue(true, "not empty");
        assertTrue(true, "blank");
        assertTrue(true, "present");
        assertTrue(true, "not present");
        assertTrue(true, "contains");
        assertTrue(true, "matches");
        assertTrue(true, "size");
        assertTrue(true, "zero");
        assertTrue(true, "positive");
        assertTrue(true, "negative");

        // These are OK - explain why the property matters
        assertTrue(true, "list should be empty after cleanup");
        assertTrue(true, "email should contain @ symbol");
        assertTrue(true, "pattern should match expected format");
    }
}
