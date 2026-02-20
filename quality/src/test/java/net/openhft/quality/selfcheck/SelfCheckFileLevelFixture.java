/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Provides self-check fixture coverage with stable wording so baseline counts remain predictable.
 */
public class SelfCheckFileLevelFixture {

    public void triggerFileLevelWarnings() {
        assertTrue(true, "cache entry update cycle one");
        assertTrue(true, "cache entry update cycle two");
        assertTrue(true, "cache entry update cycle three");
        assertTrue(true, "cache entry update cycle four");
        assertTrue(true, "cache entry update cycle five");
        assertTrue(true, "cache entry update cycle six");
        assertTrue(true, "cache entry update cycle seven");
        assertTrue(true, "cache entry update cycle eight");
        assertTrue(true, "cache entry update cycle nine");
        assertTrue(true, "cache entry update cycle ten");
        assertTrue(true, "cache entry update cycle eleven");
        assertTrue(true, "cache entry update cycle twelve");
    }
}
