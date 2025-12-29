/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test verifying javax.inject annotations are available at runtime for reflection.
 */
@DisplayName("JavaxInjectSmokeTest")
class JavaxInjectSmokeTest {

    @Test
    @DisplayName("javax.inject annotations should be present at runtime")
    void injectAnnotationsPresentAtRuntime() throws Exception {
        Field serviceField = Client.class.getDeclaredField("service");
        Field providerField = Client.class.getDeclaredField("serviceProvider");

        assertTrue(serviceField.isAnnotationPresent(Inject.class), "service field should carry @Inject");
        assertTrue(providerField.isAnnotationPresent(Inject.class), "provider field should carry @Inject");
        assertEquals(Provider.class, providerField.getType(), "provider field should be a Provider");
    }

    /**
     * Sample service type used for injection tests.
     */
    static class Service {
    }

    /**
     * Sample client type with injected fields.
     */
    static class Client {
        /**
         * Injected service instance used by the client under test.
         */
        @Inject
        private Service service;

        /**
         * Provider used to demonstrate lazy injection support.
         */
        @Inject
        private Provider<Service> serviceProvider;
    }
}
