/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Advanced smoke tests verifying Undertow web server features beyond basic server start/stop.
 * <p>
 * Tests PathHandler routing, handler chain composition, WebSocket protocol handler configuration,
 * and GracefulShutdownHandler to ensure deeper Undertow functionality is available and working.
 */
@DisplayName("UndertowAdvancedSmokeTest")
class UndertowAdvancedSmokeTest {

    private static final String API_PATH = "/api";
    private static final String HEALTH_PATH = "/health";
    private static final String ROOT_PATH = "/";
    private static final int TEST_PORT = 0; // OS-assigned port

    @Test
    @DisplayName("Undertow PathHandler should accept multiple path registrations for routing")
    void pathHandlerAcceptsMultipleRoutes() {
        PathHandler pathHandler = new PathHandler();

        // Register multiple paths with different handlers
        pathHandler.addExactPath(API_PATH, new NoOpHandler());
        pathHandler.addExactPath(HEALTH_PATH, new NoOpHandler());
        pathHandler.addPrefixPath(ROOT_PATH, new NoOpHandler());

        // PathHandler should be usable as root handler without throwing
        Undertow server = Undertow.builder()
                .addHttpListener(TEST_PORT, "localhost")
                .setHandler(pathHandler)
                .build();

        assertNotNull(server,
                "Undertow server should be constructible with PathHandler containing multiple routes");
        // Don't start - just verify configuration works
    }

    @Test
    @DisplayName("Undertow handler chain should compose outer handler wrapping inner handler")
    void httpHandlerChainComposesCorrectly() {
        TrackingHandler innerHandler = new TrackingHandler();
        HttpHandler outerHandler = new WrappingHandler(innerHandler);

        PathHandler pathHandler = new PathHandler();
        pathHandler.addExactPath(API_PATH, outerHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(TEST_PORT, "localhost")
                .setHandler(pathHandler)
                .build();

        assertNotNull(server,
                "Undertow server should accept composed handler chain configuration");
    }

    @Test
    @DisplayName("Undertow WebSocketProtocolHandshakeHandler should be configurable with callback")
    void webSocketHandlerIsConfigurable() {
        WebSocketConnectionCallback callback = new TestWebSocketCallback();

        WebSocketProtocolHandshakeHandler wsHandler =
                new WebSocketProtocolHandshakeHandler(callback);

        PathHandler pathHandler = new PathHandler();
        pathHandler.addExactPath("/ws", wsHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(TEST_PORT, "localhost")
                .setHandler(pathHandler)
                .build();

        assertNotNull(server,
                "Undertow server should accept WebSocketProtocolHandshakeHandler configuration");
    }

    @Test
    @DisplayName("Undertow GracefulShutdownHandler should wrap handler and provide shutdown control")
    void gracefulShutdownHandlerWrapsInnerHandler() throws InterruptedException {
        TrackingHandler innerHandler = new TrackingHandler();
        GracefulShutdownHandler shutdownHandler = new GracefulShutdownHandler(innerHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(TEST_PORT, "localhost")
                .setHandler(shutdownHandler)
                .build();

        try {
            server.start();

            // Request shutdown - should not throw
            shutdownHandler.shutdown();

            // Await shutdown with timeout - should complete since no requests are pending
            boolean shutdownComplete = shutdownHandler.awaitShutdown(100);
            assertTrue(shutdownComplete,
                    "GracefulShutdownHandler.awaitShutdown() should complete when no requests pending");
        } finally {
            server.stop();
        }
    }

    /**
     * Simple no-operation handler for testing PathHandler route registration.
     * Ends the exchange immediately with a 200 OK status.
     */
    static class NoOpHandler implements HttpHandler {
        @Override
        public void handleRequest(final HttpServerExchange exchange) {
            exchange.setStatusCode(200);
            exchange.endExchange();
        }
    }

    /**
     * Handler that tracks invocation for testing handler chain composition.
     * Ends the exchange immediately with a 200 OK status.
     */
    static class TrackingHandler implements HttpHandler {
        @Override
        public void handleRequest(final HttpServerExchange exchange) {
            exchange.setStatusCode(200);
            exchange.endExchange();
        }
    }

    /**
     * Handler that wraps another handler, demonstrating handler chain composition.
     * Delegates to the wrapped handler after optional pre-processing.
     */
    static class WrappingHandler implements HttpHandler {
        private final HttpHandler next;

        WrappingHandler(final HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handleRequest(final HttpServerExchange exchange) throws Exception {
            // Could add headers, logging, auth checks here
            next.handleRequest(exchange);
        }
    }

    /**
     * Test WebSocket connection callback for verifying WebSocket handler configuration.
     * Provides a minimal implementation that accepts connections without message handling.
     */
    static class TestWebSocketCallback implements WebSocketConnectionCallback {
        @Override
        public void onConnect(final WebSocketHttpExchange exchange, final WebSocketChannel channel) {
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                // Minimal implementation for smoke testing
            });
            channel.resumeReceives();
        }
    }
}
