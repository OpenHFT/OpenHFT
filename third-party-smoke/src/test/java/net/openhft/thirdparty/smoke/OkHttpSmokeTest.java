/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OkHttpSmokeTest {
    @Test
    void callsAnOwnedLoopbackServer() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build();
        try (MockWebServer server = new MockWebServer()) {
            server.start(InetAddress.getLoopbackAddress(), 0);
            server.enqueue(new MockResponse().setResponseCode(201).setBody("hello"));
            try (Response response = client.newCall(new Request.Builder()
                    .url(server.url("/hello")).build()).execute()) {
                assertEquals(201, response.code());
                assertNotNull(response.body());
                assertEquals("hello", response.body().string());
            }
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertNotNull(request, "the owned server must receive the request");
            assertEquals("GET", request.getMethod());
            assertEquals("/hello", request.getPath());
        } finally {
            client.connectionPool().evictAll();
            client.dispatcher().executorService().shutdownNow();
        }
    }
}
