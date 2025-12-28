/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises rules for duplicate inputs, low-signal headings, and derived assertions.
 */
public class InputDuplicateInputHeadingDerivedRules {

    // ========== MMDuplicatesInput: Message duplicates input ==========

    void testDuplicatesInput() {
        String expected = "admin";
        String actual = "user";

        // These should trigger MMDuplicatesInput
        assertEquals("admin", actual, "admin");  // message duplicates expected value
        assertEquals(expected, actual, "expected");  // message duplicates variable name

        // These are OK - message explains the invariant
        assertEquals("admin", actual, "user role should be admin after promotion");
        assertEquals(expected, actual, "computed hash should match stored value");
    }

    // ========== MMLowSignalAssertAllHeading: Low-signal assertAll headings ==========

    void testAssertAllHeadings() {
        Object order = new Object();
        Object user = new Object();

        // These should trigger MMLowSignalAssertAllHeading - low-signal headings
        assertAll("assertAll",
            () -> assertTrue(true, "order should be checked in group 1")
        );

        assertAll("assertions",
            () -> assertTrue(true, "order should be checked in group 2")
        );

        assertAll("checks",
            () -> assertTrue(true, "order should be checked in group 3")
        );

        assertAll("validation",
            () -> assertTrue(true, "order should be checked in group 4")
        );

        assertAll("test",
            () -> assertTrue(true, "order should be checked in group 5")
        );

        // These are OK - descriptive headings
        assertAll("order should be complete and valid",
            () -> assertTrue(true, "order should be checked in group 6")
        );

        assertAll("user initialization checks pass",
            () -> assertTrue(true, "user should be initialised in group 7")
        );
    }

    // ========== MMRestatesDerivedAssertion: Restates derived assertion ==========

    void testRestatesDerived() {
        // These should trigger MMRestatesDerivedAssertion
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
