/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.bootstrap.ServerBootstrap;
import io.undertow.Undertow;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal usage smoke tests for networking dependencies from third-party-bom.
 * <p>
 * These tests validate web server framework initialisation without starting
 * actual listeners.
 * See {@code SMOKE-TEST-005} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
class MinimalNetworkingSmokeTest {

    @Test
    @DisplayName("Undertow can build without starting")
    void undertowBuilds() {
        Undertow undertow = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(exchange ->
                        exchange.getResponseSender().send("ok"))
                .build();
        undertow.stop();
    }

    @Test
    @DisplayName("Jetty WebSocket client constructs")
    void jettyClientConstructs() {
        assertDoesNotThrow(() -> new WebSocketClient());
    }

    @Test
    @DisplayName("Netty bootstrap can be configured")
    void nettyBootstrapConfigurable() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        assertNotNull(bootstrap);
    }

    @Test
    @DisplayName("Grizzly HTTP server base class is available")
    void grizzlyServerAccessible() {
        FilterChainBuilder builder = FilterChainBuilder.stateless();
        assertNotNull(builder);
    }

    @Test
    @DisplayName("MockWebServer can be constructed and closed")
    void mockWebServerLifecycle() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            assertNotNull(server);
        }
    }
}
