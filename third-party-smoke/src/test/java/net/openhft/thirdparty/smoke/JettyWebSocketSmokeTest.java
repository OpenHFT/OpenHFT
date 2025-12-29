/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests verifying Jetty WebSocket client and server classes construct cleanly.
 */
@DisplayName("JettyWebSocketSmokeTest")
class JettyWebSocketSmokeTest {

    @Test
    @DisplayName("Jetty WebSocket API classes should be present")
    void websocketApiClassesArePresent() {
        Class<?> sessionClass = Session.class;
        assertNotNull(sessionClass, "Jetty Session class should be loadable");
    }

    @Test
    @DisplayName("Jetty WebSocket client and server should construct and stop")
    void websocketClientAndServerFactoryConstructAndStop() throws Exception {
        WebSocketClient client = new WebSocketClient();
        WebSocketServerFactory serverFactory = new WebSocketServerFactory();
        assertNotNull(client, "Jetty WebSocketClient should construct");
        assertNotNull(serverFactory, "Jetty web socket server factory should construct");

        client.start();
        serverFactory.start();

        client.stop();
        serverFactory.stop();
    }

    /**
     * Simple adapter to touch WebSocketAdapter type.
     */
    static class EchoSocket extends WebSocketAdapter {
    }
}
