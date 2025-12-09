/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Extended logging smoke to cover bridges and JUL integration.
 */
class LoggingExtendedSmokeTest {

    @Test
    void log4jApiCoreWork() {
        Logger logger = LogManager.getLogger(LoggingExtendedSmokeTest.class);
        logger.info("Log4j2 hello");
        assertNotNull(logger, "Log4j2 API should return a logger");
    }

    @Test
    void slf4jApiToLog4jBindingDoesNotThrow() {
        org.slf4j.Logger logger =
                LoggerFactory.getLogger("slf4jSmoke");
        logger.info("SLF4J hello");
        assertNotNull(logger, "SLF4J binding should produce a logger");
    }

    @Test
    void julBridgeDoesNotThrow() {
        java.util.logging.Logger julLogger =
                java.util.logging.Logger.getLogger("julSmoke");
        julLogger.setLevel(Level.INFO);
        julLogger.info("JUL via Log4j2");
        assertNotNull(julLogger, "JUL bridge should provide a logger");
    }
}
