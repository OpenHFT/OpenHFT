/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises MMMissingStringSearchValue with string searches lacking the searched literal value in messages.
 */
public class InputStringSearchValues {

    void testMissingStringSearchValues() {
        String text = "hello@example.com";
        String name = "Mr Smith";
        String path = "/tmp/file.txt";

        // These should trigger MMMissingStringSearchValue - string search with constant message
        assertTrue(text.contains("@"), "email should contain symbol");
        assertTrue(name.startsWith("Mr"), "name should have title prefix");
        assertTrue(path.endsWith(".txt"), "path should be text file");
        assertFalse(text.contains("spam"), "email should not contain spam");
        assertFalse(name.startsWith("Mrs"), "name should not have wrong title");
        assertFalse(path.endsWith(".tmp"), "path should not be temp file");

        // These are OK - message includes dynamic values
        assertTrue(text.contains("@"), text + " should contain at symbol");
        assertTrue(name.startsWith("Mr"), name + " should start with Mr prefix");

        // These are OK - message includes dynamic values
        assertTrue(text.contains("@"), "text should contain @ for " + text);

        // These should trigger MMTrivialSupplier - simple concatenation in supplier
        assertTrue(text.contains("@"), () -> text + " should contain @");

        // These are OK - not a string search method
        assertTrue(isValid(text), "text should be valid");
    }

    private boolean isValid(String s) {
        return s != null && !s.isEmpty();
    }
}
