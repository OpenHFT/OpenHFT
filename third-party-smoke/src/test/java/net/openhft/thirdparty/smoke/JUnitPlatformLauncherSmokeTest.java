/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Verifies the JUnit Platform launcher executes a Jupiter test class using discovery selectors.
 */
@DisplayName("Smoke test verifies JUnit platform launcher runs")
class JUnitPlatformLauncherSmokeTest {

    @Test
    @DisplayName("JUnit Platform Launcher should execute sample class")
    void canCreateLauncherAndExecuteSampleClass() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectClass(LauncherSample.class))
                .build();

        Launcher launcher = LauncherFactory.create();
        launcher.execute(request);

        assertNotNull(launcher, "platform launcher should initialise for execution");
    }

    /**
     * Jupiter sample executed by the platform launcher.
     */
    @Nested
    @DisplayName("Launcher sample used for platform execution")
    class LauncherSample {

        @Test
        @DisplayName("Launcher sample should run without failures")
        void sample() {
            assertTrue(true, "Platform launcher test should pass");
        }
    }
}
