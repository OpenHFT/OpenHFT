/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Verifies the JUnit Platform launcher can execute a Jupiter class.
 */
class JUnitPlatformLauncherSmokeTest {

    @Test
    void canCreateLauncherAndExecuteSampleClass() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectClass(LauncherSample.class))
                .build();

        Launcher launcher = LauncherFactory.create();
        launcher.execute(request);

        assertNotNull(launcher, "Launcher should be created");
    }

    /**
     * Jupiter sample executed by the platform launcher.
     */
    static class LauncherSample {

        @Test
        void sample() {
            Assertions.assertTrue(true, "Launcher sample should pass");
        }
    }
}
