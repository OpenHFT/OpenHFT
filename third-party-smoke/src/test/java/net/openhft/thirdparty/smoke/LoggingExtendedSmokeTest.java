/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Extended logging smoke to cover bridges and JUL integration behaviour.
 */
@DisplayName("Smoke test verifies extended logging bindings load")
class LoggingExtendedSmokeTest {

    @Test
    @DisplayName("Log4j2 API and Core should work")
    void log4jApiCoreWork() {
        Logger logger = LogManager.getLogger(LoggingExtendedSmokeTest.class);
        logger.info("Log4j2 logger should log message");
        assertNotNull(logger, "Log4j2 API should return a logger");
    }

    @Test
    @DisplayName("SLF4J to Log4j2 binding should not throw")
    void slf4jApiToLog4jBindingDoesNotThrow() {
        org.slf4j.Logger logger =
                LoggerFactory.getLogger("slf4jSmoke");
        logger.info("SLF4J logger should log message");
        assertNotNull(logger, "SLF4J binding should produce a logger");
    }

    @Test
    @DisplayName("JUL bridge to Log4j2 should log messages without throwing")
    void julBridgeDoesNotThrow() {
        java.util.logging.Logger julLogger =
                java.util.logging.Logger.getLogger("julSmoke");
        julLogger.setLevel(Level.INFO);
        julLogger.info("JUL logger should log message");
        assertNotNull(julLogger, "JUL bridge should provide a logger");
    }
}
