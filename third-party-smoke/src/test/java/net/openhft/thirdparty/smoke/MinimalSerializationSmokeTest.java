/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.google.gson.Gson;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinimalSerializationSmokeTest {
    @Test
    void gsonRoundTrips() {
        Gson gson = new Gson();
        Payload input = new Payload();
        input.value = "quoted \"text\"";
        input.count = 42;
        Payload output = gson.fromJson(gson.toJson(input), Payload.class);
        assertEquals(input.value, output.value);
        assertEquals(input.count, output.count);
    }

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

    private static class Payload {
        String value;
        int count;
    }
}
