/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Presence-only smoke to ensure Powermock classes are resolvable.
 */
class PowerMockPresenceSmokeTest {

    /**
     * Confirms Powermock types are on the classpath.
     */
    @Test
    void powermockClassesLoad() {
        assertNotNull(PowerMockito.class);
        assertNotNull(PowerMockRunner.class);
        assertNotNull(PrepareForTest.class);
    }
}
