/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseAndSchemaSmokeTest {
    @Test
    void hsqldbInMemoryRoundTrip() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:smoke;shutdown=true", "SA", "")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE sample(id INT PRIMARY KEY, name VARCHAR(50))");
                assertEquals(1, statement.executeUpdate("INSERT INTO sample VALUES (1, 'hello')"));
                try (ResultSet rows = statement.executeQuery("SELECT id, name FROM sample")) {
                    assertTrue(rows.next(), "the inserted row must be returned");
                    assertEquals(1, rows.getInt(1));
                    assertEquals("hello", rows.getString(2));
                    assertFalse(rows.next(), "the result must contain exactly one row");
                }
            }
        }
    }
}
