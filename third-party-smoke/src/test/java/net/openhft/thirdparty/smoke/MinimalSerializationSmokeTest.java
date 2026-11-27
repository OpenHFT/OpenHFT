/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinimalSerializationSmokeTest {
    @Test
    void yamlRoundTrips() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("name", "hello: world");
        input.put("count", 42);
        Yaml yaml = new Yaml();
        assertEquals(input, yaml.load(yaml.dump(input)));
    }

    @Test
    void bsonRoundTripsWithoutConnectingToADatabase() {
        Document input = new Document("_id", new ObjectId()).append("name", "hello").append("count", 42);
        assertEquals(input, Document.parse(input.toJson()));
    }

}
