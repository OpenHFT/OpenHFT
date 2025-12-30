/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test input for MMDisplayName rule validation.
 */
@SuppressWarnings({"MMTooShort", "MMTooFewMeaningfulWords"})
class InputDisplayNameMessages {

    // violation below: test method without @DisplayName
    @Test
    void testWithoutDisplayName() { // violation
    }

    // no violation: test method with @DisplayName
    @Test
    @DisplayName("Verifies that addition works correctly")
    void testWithDisplayName() {
    }

    // violation below: parameterized test without @DisplayName
    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    void parameterizedWithoutDisplayName(String value) { // violation
    }

    // no violation: parameterized test with @DisplayName
    @ParameterizedTest
    @DisplayName("Validates string processing")
    @ValueSource(strings = {"a", "b"})
    void parameterizedWithDisplayName(String value) {
    }

    // no violation: regular method (not a test)
    void helperMethod() {
    }

    // no violation: private helper method
    private void privateHelper() {
    }
}
