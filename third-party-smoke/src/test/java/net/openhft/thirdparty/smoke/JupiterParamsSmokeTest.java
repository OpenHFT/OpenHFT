/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Exercises junit-jupiter-params using a simple value source for basic coverage.
 */
class JupiterParamsSmokeTest {

    @ParameterizedTest
    @ValueSource(strings = {"a", "bb", "ccc"})
    void stringsAreNotEmpty(final String value) {
        assertFalse(value.isEmpty(), "value-source string should be non-empty");
    }
}
