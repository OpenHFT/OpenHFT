/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for HSQLDB and Jackson jsonSchema.
 */
class DatabaseAndSchemaSmokeTest {

    @Test
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
    void jacksonJsonSchemaGeneratorProducesSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(Person.class);
        assertNotNull(schema, "JsonSchemaGenerator should produce a schema for Person");
    }

    /**
     * Simple POJO for schema generation.
     */
    static class Person {
        /**
         * Person name.
         */
        private String name;

        /**
         * Default constructor.
         */
        Person() {
            this.name = "";
        }

        /**
         * Creates a person with a name.
         *
         * @param newName person name
         */
        Person(final String newName) {
            this.name = newName;
        }

        /**
         * Returns the name.
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
