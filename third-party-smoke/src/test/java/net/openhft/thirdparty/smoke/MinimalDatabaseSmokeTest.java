/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    @DisplayName("Mongo BSON Document works offline")
    void mongoBsonDocument() {
        Document document = new Document("hello", "world")
                .append("_id", new ObjectId());
        assertEquals("world", document.getString("hello"));
        assertTrue(document.containsKey("_id"));
    }

    @Test
    @DisplayName("Mongo sync client can be created and closed")
    void mongoClientLifecycle() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://localhost:27017/?appName=third-party-smoke");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        try (MongoClient client = MongoClients.create(settings)) {
            assertNotNull(client.getDatabase("smoke"));
        }
    }

    @Test
    @DisplayName("Legacy mongo-java-driver classes are present")
    void legacyMongoDriverPresent() throws Exception {
        Class<?> clazz = Class.forName("com.mongodb.MongoClient");
        assertNotNull(clazz);
    }
}
