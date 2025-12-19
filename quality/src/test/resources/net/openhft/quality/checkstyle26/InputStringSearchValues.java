/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test input for AMQ19: Missing string search value in message.
 */
public class InputStringSearchValues {

    void testMissingStringSearchValues() {
        String text = "hello@example.com";
        String name = "Mr Smith";
        String path = "/tmp/file.txt";

        // These should trigger AMQ19 - string search with constant message
        assertTrue(text.contains("@"), "email should contain symbol");
        assertTrue(name.startsWith("Mr"), "name should have title prefix");
        assertTrue(path.endsWith(".txt"), "path should be text file");
        assertFalse(text.contains("spam"), "email should not contain spam");
        assertFalse(name.startsWith("Mrs"), "should not have wrong title");
        assertFalse(path.endsWith(".tmp"), "should not be temp file");

        // These are OK - message includes dynamic values
        assertTrue(text.contains("@"), text + " should contain at symbol");
        assertTrue(name.startsWith("Mr"), name + " should start with Mr prefix");

        // These are OK - no message provided
        assertTrue(text.contains("@"));

        // These are OK - lambda supplier (assumes dynamic content)
        assertTrue(text.contains("@"), () -> text + " should contain @");

        // These are OK - not a string search method
        assertTrue(isValid(text), "should be valid");
    }

    private boolean isValid(String s) {
        return s != null && !s.isEmpty();
    }
}
