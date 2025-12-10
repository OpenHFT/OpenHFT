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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Smoke tests for HSQLDB and Jackson jsonSchema.
 */
class DatabaseAndSchemaSmokeTest {

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

    @Test
    void hsqldbInMemoryRoundTrip() throws Exception {
        // Ensure the HSQLDB driver is registered on Java 8
        // where auto-registration can be skipped
        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:hsqldb:mem:smokedb", "SA", "")) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE test("
                        + "id INT PRIMARY KEY, name VARCHAR(50))");
                st.execute("INSERT INTO test VALUES (1, 'hello')");
                try (ResultSet rs = st.executeQuery(
                        "SELECT name FROM test WHERE id=1")) {
                    if (!rs.next()) {
                        fail("Expected a row for id=1");
                    }
                    assertEquals("hello", rs.getString(1));
                }
            }
        }
    }

    @Test
    void jacksonJsonSchemaGeneratorProducesSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(Person.class);
        assertNotNull(schema);
    }
}
