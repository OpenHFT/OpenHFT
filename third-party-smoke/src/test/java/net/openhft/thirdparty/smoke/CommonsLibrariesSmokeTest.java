/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for Commons Lang, CLI, and Logging.
 */
class CommonsLibrariesSmokeTest {

    @Test
    void commonsLangStringUtilsWorks() {
        String joined = StringUtils.join(new String[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", joined);
    }

    @Test
    void commonsCliParsesSimpleOption() throws Exception {
        Options options = new Options()
                .addOption("v", "verbose", false, "verbose output");
        CommandLine cmd = new DefaultParser()
                .parse(options, new String[]{"-v"});
        assertTrue(cmd.hasOption("v"));
    }

    @Test
    void commonsLoggingObtainsLogger() {
        Log log = LogFactory.getLog(CommonsLibrariesSmokeTest.class);
        log.info("commons-logging smoke");
        assertNotNull(log);
    }
}
