/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying SLF4J MDC (Mapped Diagnostic Context) API is present and callable.
 * <p>
 * Tests that MDC operations can be invoked without exceptions. Note that MDC functionality
 * depends on the SLF4J binding - with slf4j-simple the MDC operations are no-ops, but the
 * API should still be callable for code that uses MDC when a real binding is present.
 */
class Slf4jMdcSmokeTest {

    private static final String REQUEST_ID_KEY = "requestId";
    private static final String SAMPLE_REQUEST_ID = "req-12345";

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("SLF4J MDC class should be loadable and put/get methods should be callable")
    void mdcPutAndGetAreCallable() {
        // MDC.put() and MDC.get() should not throw, even if MDC is a NOP adapter
        assertDoesNotThrow(() -> MDC.put(REQUEST_ID_KEY, SAMPLE_REQUEST_ID),
                "MDC.put() should be callable without throwing an exception");
        assertDoesNotThrow(() -> MDC.get(REQUEST_ID_KEY),
                "MDC.get() should be callable without throwing an exception");
    }

    @Test
    @DisplayName("SLF4J MDC remove method should be callable without exception")
    void mdcRemoveIsCallable() {
        MDC.put(REQUEST_ID_KEY, SAMPLE_REQUEST_ID);

        assertDoesNotThrow(() -> MDC.remove(REQUEST_ID_KEY),
                "MDC.remove() should be callable without throwing an exception");
    }

    @Test
    @DisplayName("SLF4J MDC getCopyOfContextMap should return map or null without exception")
    void mdcGetCopyOfContextMapIsCallable() {
        MDC.put(REQUEST_ID_KEY, SAMPLE_REQUEST_ID);

        // getCopyOfContextMap() may return null with NOP adapter, but should not throw
        assertDoesNotThrow(MDC::getCopyOfContextMap,
                "MDC.getCopyOfContextMap() should be callable without throwing an exception");
    }

    @Test
    @DisplayName("SLF4J MDC clear method should be callable without exception")
    void mdcClearIsCallable() {
        MDC.put(REQUEST_ID_KEY, SAMPLE_REQUEST_ID);

        assertDoesNotThrow(MDC::clear,
                "MDC.clear() should be callable without throwing an exception");
    }

    @Test
    @DisplayName("SLF4J MDC setContextMap should be callable for context restoration")
    void mdcSetContextMapIsCallable() {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        // setContextMap accepts null with some adapters, but may throw with others
        // We test with an actual map if available, otherwise skip
        assertDoesNotThrow(() -> {
            if (contextMap != null) {
                MDC.setContextMap(contextMap);
            }
        }, "MDC.setContextMap() should be callable with a valid context map");
    }
}
