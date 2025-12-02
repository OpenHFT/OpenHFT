/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests for Jetty WebSocket client/server classes.
 */
class JettyWebSocketSmokeTest {

    @Test
    void websocketApiClassesArePresent() {
        Class<?> sessionClass = Session.class;
        assertNotNull(sessionClass);
    }

    @Test
    void websocketClientAndServerFactoryConstructAndStop() throws Exception {
        WebSocketClient client = new WebSocketClient();
        WebSocketServerFactory serverFactory = new WebSocketServerFactory();

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
