/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.gson.Gson;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal usage smoke tests for serialisation dependencies from
 * third-party-bom.
 * <p>
 * These tests validate JSON, YAML, and XML serialisation libraries can
 * round-trip simple objects.
 * See {@code SMOKE-TEST-004} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
class MinimalSerializationSmokeTest {

    @Test
    @DisplayName("JSONAssert should compare identical JSON payloads")
    void jsonAssertWorks() throws Exception {
        JSONAssert.assertEquals("JSONAssert reports no diff for identical payloads",
                "{\"key\":1}", "{\"key\":1}", false);
    }

    @Test
    @DisplayName("SnakeYAML can parse a simple document")
    void snakeYamlParses() {
        Yaml yaml = new Yaml();
        Map<?, ?> parsed = yaml.load("name: demo");
        assertEquals("demo", parsed.get("name"), "SnakeYAML should parse key/value");
    }

    @Test
    @DisplayName("Gson should serialise and deserialise a simple payload")
    void gsonRoundTrips() {
        Gson gson = new Gson();
        SimplePojo pojo = new SimplePojo("gson");
        String json = gson.toJson(pojo);
        SimplePojo read = gson.fromJson(json, SimplePojo.class);
        assertEquals(pojo.value, read.value, "Gson should round-trip SimplePojo");
    }

    @Test
    @DisplayName("Jackson jsonSchema can generate a schema")
    void jacksonSchemaGenerates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(SimplePojo.class);
        assertNotNull(schema, "Jackson jsonSchema should generate schema for SimplePojo");
    }

    @Test
    @DisplayName("Jettison can build a JSON object")
    void jettisonBuildsJson() throws Exception {
        JSONObject object = new JSONObject();
        object.put("hello", "world");
        assertEquals("world", object.getString("hello"), "Jettison should read inserted value");
    }

    /**
     * Simple POJO for serialisation round-trip tests.
     */
    private static final class SimplePojo {
        /**
         * Stored value used for round-tripping.
         */
        private final String value;

        /**
         * Creates a new instance using the supplied text value.
         *
         * @param newValue textual value
         */
        private SimplePojo(final String newValue) {
            this.value = newValue;
        }

        /**
         * Default constructor required for deserialisation by Jackson.
         */
        @SuppressWarnings("unused")
        SimplePojo() {
            this.value = "";
        }
    }
}
