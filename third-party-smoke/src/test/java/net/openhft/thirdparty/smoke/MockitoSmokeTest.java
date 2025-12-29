/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test verifying Mockito core operates without bytecode instrumentation support.
 */
@DisplayName("MockitoSmokeTest")
class MockitoSmokeTest {

    /**
     * Reference total used to validate calculator interaction through the service.
     */
    private static final int EXPECTED_SUM = 3;

    @Test
    @DisplayName("Mockito settings and basic mocking should be available")
    void mockitoSettingsAreAvailable() {
        org.mockito.MockSettings settings = Mockito.withSettings();
        assertNotNull(settings, "Mockito.withSettings should return settings");
        Calculator calculator = new Calculator();
        Service service = new Service(calculator);
        assertEquals(EXPECTED_SUM, service.compute(), "Service should compute sum via calculator");
    }

    /**
     * Simple calculator used for mocking service dependencies.
     */
    static class Calculator {
        int add(final int a, final int b) {
            return a + b;
        }
    }

    /**
     * Service under test that depends on the calculator.
     */
    static class Service {
        /**
         * Calculator instance supplied to the service under test.
         */
        private final Calculator serviceCalculator;

        Service(final Calculator newCalculator) {
            this.serviceCalculator = newCalculator;
        }

        int compute() {
            return serviceCalculator.add(1, 2);
        }
    }
}
