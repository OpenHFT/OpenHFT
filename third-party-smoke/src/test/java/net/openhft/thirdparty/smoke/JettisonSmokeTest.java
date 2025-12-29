/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test verifying Jettison can parse and emit simple JSON objects.
 */
@DisplayName("JettisonSmokeTest")
class JettisonSmokeTest {

    /**
     * Answer value used in JSON assertions for round-trip.
     */
    private static final int ANSWER = 42;

    @Test
    @DisplayName("Jettison JSONObject should round-trip values")
    void jettisonJsonObjectRoundTrip() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("hello", "world");
        obj.put("answer", ANSWER);

        assertEquals("world", obj.getString("hello"), "Jettison should read back string field");
        assertEquals(ANSWER, obj.getInt("answer"), "Jettison should read back numeric field");
    }
}
