/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InputMapStringObject {

    void badMap() {
        Map<String, Object> config = new HashMap<>();
        assertNotNull(config, "config map should not be null after initialisation");
    }

    void goodMap() {
        Map<String, Object> params = new HashMap<>(); // MM-reason: generic config map
        assertNotNull(params, "params map should not be null after initialisation");
    }

    void stringStringMap() {
        Map<String, String> headers = new HashMap<>();
        assertNotNull(headers, "headers map should not be null after initialisation");
    }

    void wildcardMap() {
        Map<String, ?> data = new HashMap<>();
        assertNotNull(data, "wildcard data map should not be null after initialisation");
    }

    void nonStringKeyMap() {
        Map<Integer, Object> counts = new HashMap<>();
        assertNotNull(counts, "counts map should not be null after initialisation");
    }
}
