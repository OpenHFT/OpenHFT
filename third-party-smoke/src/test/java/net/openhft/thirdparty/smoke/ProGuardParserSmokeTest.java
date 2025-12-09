/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import proguard.Configuration;
import proguard.ConfigurationParser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test for ProGuard configuration parsing.
 */
class ProGuardParserSmokeTest {

    /**
     * Ensures minimal ProGuard arguments parse without execution.
     *
     * @throws Exception parsing failure
     */
    @Test
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
