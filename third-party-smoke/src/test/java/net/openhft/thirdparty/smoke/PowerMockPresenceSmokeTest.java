/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Presence-only smoke test to ensure Powermock classes resolve for dependency loading on classpath.
 */
class PowerMockPresenceSmokeTest {

    /**
     * Confirms Powermock types are on the classpath.
     */
    @Test
    @DisplayName("PowerMock API, Runner and PrepareForTest classes should be present on classpath")
    void powermockClassesLoad() {
        assertNotNull(PowerMockito.class, "PowerMockito should be on classpath");
        assertNotNull(PowerMockRunner.class, "PowerMockRunner should be on classpath");
        assertNotNull(PrepareForTest.class, "PrepareForTest annotation should be on classpath");
    }
}
