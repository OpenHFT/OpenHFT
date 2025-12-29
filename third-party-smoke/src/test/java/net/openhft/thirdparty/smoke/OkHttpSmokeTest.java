/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test verifying OkHttp client can call MockWebServer and read response bodies.
 */
@DisplayName("OkHttpSmokeTest")
class OkHttpSmokeTest {

    @Test
    @DisplayName("OkHttp client should call MockWebServer and read response body correctly")
    void okHttpCanCallMockWebServer() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("hello"));
            server.start();

            String baseUrl = server.url("/hello").toString();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(baseUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                assertEquals("hello", response.body().string(), "OkHttp should receive mocked response body");
            }
        }
    }
}
