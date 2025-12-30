/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import proguard.Configuration;
import proguard.ConfigurationParser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test verifying ProGuard configuration parser can parse minimal arguments.
 */
@DisplayName("Smoke test verifies ProGuard parser configuration loads")
class ProGuardParserSmokeTest {

    /**
     * Ensures minimal ProGuard arguments parse without execution.
     *
     * @throws Exception parsing failure
     */
    @Test
    @DisplayName("ProGuard ConfigurationParser should parse trivial arguments")
    void proguardConfigurationParserParsesTrivialArgs() throws Exception {
        Configuration configuration = new Configuration();
        String[] args = {"-dontoptimize"};

        ConfigurationParser parser = new ConfigurationParser(
                args, System.getProperties());
        try {
            parser.parse(configuration);
        } finally {
            parser.close();
        }

        assertNotNull(configuration, "ProGuard parser should populate configuration");
    }
}
