/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Migration support smoke to ensure JUnit 4 rules work under Jupiter.
 */
@EnableRuleMigrationSupport
class JUnitMigrationSupportSmokeTest {

    /**
     * JUnit 4 temporary folder rule.
     */
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    void temporaryFolderRuleIsHonoured() throws Exception {
        File file = temp.newFile("smoke.txt");
        assertTrue(file.isFile(), "Temporary file should exist");
    }
}
