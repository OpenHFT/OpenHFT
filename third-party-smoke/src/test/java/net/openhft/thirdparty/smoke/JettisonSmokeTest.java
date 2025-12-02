/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test for Jettison JSON handling.
 */
class JettisonSmokeTest {

    /**
     * Value used in assertions.
     */
    private static final int ANSWER = 42;

    @Test
    void jettisonJsonObjectRoundTrip() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("hello", "world");
        obj.put("answer", ANSWER);

        assertEquals("world", obj.getString("hello"));
        assertEquals(ANSWER, obj.getInt("answer"));
    }
}
