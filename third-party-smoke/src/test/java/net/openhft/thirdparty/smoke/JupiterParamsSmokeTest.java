/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Exercises junit-jupiter-params using a simple value source for basic coverage.
 */
@DisplayName("JupiterParamsSmokeTest")
class JupiterParamsSmokeTest {

    @ParameterizedTest
    @ValueSource(strings = {"a", "bb", "ccc"})
    @DisplayName("Value source strings are non-empty")
    void stringsAreNotEmpty(final String value) {
        assertFalse(value.isEmpty(), "value-source string should be non-empty");
    }
}
