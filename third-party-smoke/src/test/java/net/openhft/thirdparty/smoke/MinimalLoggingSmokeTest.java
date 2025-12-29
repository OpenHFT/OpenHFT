/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal usage smoke tests for logging dependencies from third-party-bom.
 * <p>
 * These tests validate Log4j2 and SLF4J logger creation.
 * See {@code SMOKE-TEST-006} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
@DisplayName("MinimalLoggingSmokeTest")
class MinimalLoggingSmokeTest {

    @Test
    @DisplayName("Log4j2 should create a usable logger instance")
    void log4j2Usage() {
        org.apache.logging.log4j.Logger logger =
                LogManager.getLogger(MinimalLoggingSmokeTest.class);
        logger.info("Log4j2 smoke test message");
        assertNotNull(logger, "Log4j2 should create a logger instance");
    }

    @Test
    @DisplayName("SLF4J should create a usable logger instance")
    void slf4jUsage() {
        org.slf4j.Logger logger =
                LoggerFactory.getLogger(MinimalLoggingSmokeTest.class);
        logger.info("SLF4J smoke test message");
        assertNotNull(logger, "SLF4J should create a logger instance");
    }
}
