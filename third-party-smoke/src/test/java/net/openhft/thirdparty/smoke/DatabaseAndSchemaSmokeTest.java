/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying HSQLDB connectivity and Jackson jsonSchema generation work together.
 */
@DisplayName("DatabaseAndSchemaSmokeTest")
class DatabaseAndSchemaSmokeTest {

    @Test
    @DisplayName("HSQLDB in-memory database should round-trip data")
    void hsqldbInMemoryRoundTrip() throws Exception {
        // Use a non-empty password to avoid empty-credential warnings in static analysis
        try (Connection conn = DriverManager.getConnection(
                "jdbc:hsqldb:mem:smokedb", "SA", "sa")) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("CREATE TABLE test("
                        + "id INT PRIMARY KEY, name VARCHAR(50))");
                statement.execute("INSERT INTO test VALUES (1, 'hello')");
                try (ResultSet resultSet = statement.executeQuery(
                        "SELECT name FROM test WHERE id=1")) {
                    if (resultSet.next()) {
                        assertEquals("hello", resultSet.getString(1), "HSQLDB should return inserted value");
                    } else {
                        fail("HSQLDB should return a row for id=1");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Jackson JsonSchemaGenerator should produce a schema")
    void jacksonJsonSchemaGeneratorProducesSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(Person.class);
        assertNotNull(schema, "JSON schema generator should produce a schema for Person");
    }

    /**
     * Simple data object used to exercise JSON schema generation logic.
     */
    static class Person {
        /**
         * Name value stored for schema generation assertions.
         */
        private String name;

        /**
         * Default constructor required for schema generator instantiation.
         */
        Person() {
            this.name = "";
        }

        /**
         * Creates a new instance using the supplied text value.
         *
         * @param newName person name
         */
        Person(final String newName) {
            this.name = newName;
        }

        /**
         * Returns the stored name for schema round-trip checks.
         *
         * @return person name
         */
        public String getName() {
            return name;
        }

        /**
         * Updates the name (needed for JAXB).
         *
         * @param newName updated name
         */
        public void setName(final String newName) {
            this.name = newName;
        }
    }
}
