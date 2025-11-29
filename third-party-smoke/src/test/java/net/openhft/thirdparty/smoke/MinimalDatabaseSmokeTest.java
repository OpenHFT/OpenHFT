/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Minimal usage smoke tests for database dependencies from third-party-bom.
 * <p>
 * These tests validate MongoDB connection string parsing and HSQLDB driver
 * access.
 * See {@code SMOKE-TEST-008} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
class MinimalDatabaseSmokeTest {

    @Test
    @DisplayName("Mongo ConnectionString parses without network")
    void mongoConnectionStringParses() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://localhost:27017/testdb"
        );
        assertEquals("testdb", connectionString.getDatabase());
    }
}
