/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test for javax.inject annotations.
 */
class JavaxInjectSmokeTest {

    /**
     * Sample service type.
     */
    static class Service {
    }

    /**
     * Sample client type with injected fields.
     */
    static class Client {
        /**
         * Injected service instance.
         */
        @Inject
        private Service service;

        /**
         * Provider for lazy injection.
         */
        @Inject
        private Provider<Service> serviceProvider;
    }

    @Test
    void injectAnnotationsPresentAtRuntime() throws Exception {
        Field serviceField = Client.class.getDeclaredField("service");
        Field providerField = Client.class.getDeclaredField("serviceProvider");

        assertTrue(serviceField.isAnnotationPresent(Inject.class));
        assertTrue(providerField.isAnnotationPresent(Inject.class));
        assertEquals(Provider.class, providerField.getType());
    }
}
