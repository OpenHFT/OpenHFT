/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.bootstrap.ServerBootstrap;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.util.Headers;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
    @DisplayName("Embedded Jetty server starts and stops on port 0")
    void jettyServerStartsAndStops() throws Exception {
        Server server = new Server(0);
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new HelloServlet()), "/hello");
        server.start();
        server.stop();
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

    @Test
    @DisplayName("Undertow server starts and stops on port 0")
    void undertowStartsAndStops() {
        Undertow server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(
                            final HttpServerExchange exchange) {
                        exchange.getResponseHeaders()
                                .put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender()
                                .send("Hello Undertow");
                    }
                })
                .build();
        server.start();
        server.stop();
    }

    @Test
    @DisplayName("Undertow servlet deployment builds")
    void undertowServletDeploymentBuilds() {
        ServletInfo servlet = Servlets.servlet("helloServlet",
                        HelloServlet.class)
                .addMapping("/hello");
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(HelloServlet.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("hello.war")
                .addServlet(servlet);
        assertNotNull(deployment.getDeploymentName());
    }

    @Test
    @DisplayName("Undertow WebSocket endpoint config builds")
    void undertowWebsocketEndpointConfigBuilds() {
        javax.websocket.server.ServerEndpointConfig config =
                javax.websocket.server.ServerEndpointConfig.Builder
                        .create(UndertowWebsocketEndpoint.class, "/ws")
                        .build();
        assertNotNull(config);
    }

    /**
     * Simple servlet for embedded Jetty smoke test.
     */
    public static final class HelloServlet
            extends javax.servlet.http.HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(final javax.servlet.http.HttpServletRequest req,
                             final javax.servlet.http.HttpServletResponse resp)
                throws java.io.IOException {
            resp.getWriter().write("Hello Jetty");
        }
    }

    /**
     * Dummy endpoint for Undertow WebSocket smoke test.
     */
    public static final class UndertowWebsocketEndpoint
            extends javax.websocket.Endpoint {
        @Override
        public void onOpen(final javax.websocket.Session session,
                           final javax.websocket.EndpointConfig config) {
            // no-op
        }
    }
}
