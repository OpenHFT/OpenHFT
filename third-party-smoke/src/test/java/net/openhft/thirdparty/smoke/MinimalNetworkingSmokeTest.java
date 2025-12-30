/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.bootstrap.ServerBootstrap;
import io.undertow.Undertow;
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
@DisplayName("Smoke test verifies minimal networking stacks load")
class MinimalNetworkingSmokeTest {

    @Test
    @DisplayName("Undertow builder should create a server without starting")
    void undertowBuilds() {
        Undertow undertow = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(exchange ->
                        exchange.getResponseSender().send("ok"))
                .build();
        assertNotNull(undertow, "Undertow builder should create a server");
        undertow.stop();
    }

    @Test
    @DisplayName("Jetty WebSocket client should construct cleanly")
    // Suppress "Utility classes should not have public constructors")
    @SuppressWarnings("java:S1612")
    void jettyClientConstructs() {
        assertDoesNotThrow(() -> new WebSocketClient(),
                "WebSocket client should construct");
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
        assertNotNull(server, "Jetty server should construct");
        server.start();
        server.stop();
    }

    @Test
    @DisplayName("Netty ServerBootstrap should allow configuration without binding")
    void nettyBootstrapConfigurable() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        assertNotNull(bootstrap, "Netty ServerBootstrap should construct");
    }

    @Test
    @DisplayName("Grizzly HTTP server base class is available")
    void grizzlyServerAccessible() {
        FilterChainBuilder builder = FilterChainBuilder.stateless();
        assertNotNull(builder, "Grizzly filter chain builder should be constructible");
    }

    @Test
    @DisplayName("MockWebServer can be constructed and closed")
    void mockWebServerLifecycle() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            assertNotNull(server, "MockWebServer should construct cleanly");
        }
    }

    @Test
    @DisplayName("Undertow server starts and stops on port 0")
    void undertowStartsAndStops() {
        Undertow server = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(exchange -> {
                    exchange.getResponseHeaders()
                            .put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender()
                            .send("Hello Undertow");
                })
                .build();
        assertNotNull(server, "Undertow server should build");
        server.start();
        server.stop();
    }

    @Test
    @DisplayName("Undertow servlet deployment should build with a name")
    void undertowServletDeploymentBuilds() {
        ServletInfo servlet = Servlets.servlet("helloServlet",
                        HelloServlet.class)
                .addMapping("/hello");
        DeploymentInfo deployment = Servlets.deployment()
                .setClassLoader(HelloServlet.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("hello.war")
                .addServlet(servlet);
        assertNotNull(deployment.getDeploymentName(), "Undertow deployment should have a name");
    }

    @Test
    @DisplayName("Undertow WebSocket endpoint config should build")
    void undertowWebsocketEndpointConfigBuilds() {
        javax.websocket.server.ServerEndpointConfig config =
                javax.websocket.server.ServerEndpointConfig.Builder
                        .create(UndertowWebsocketEndpoint.class, "/ws")
                        .build();
        assertNotNull(config, "Undertow websocket config should be built");
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
