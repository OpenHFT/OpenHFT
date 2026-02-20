/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying OkHttp client builder patterns and configuration options.
 * <p>
 * Tests that the OkHttp client can be configured with timeouts, interceptors,
 * and connection pooling without requiring actual network calls.
 */
@DisplayName("Smoke test verifies OkHttp client configuration works")
class OkHttpClientSmokeTest {

    /**
     * Timeout value in seconds for connect, read, and write operations.
     */
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * Maximum idle connections for connection pool configuration.
     */
    private static final int MAX_IDLE_CONNECTIONS = 5;

    /**
     * Keep-alive duration in minutes for connection pool.
     */
    private static final int KEEP_ALIVE_MINUTES = 5;

    @Test
    @DisplayName("OkHttpClient builder should configure connect, read and write timeouts")
    void okHttpClientBuilderConfiguresTimeouts() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        assertEquals(TIMEOUT_SECONDS * 1000, client.connectTimeoutMillis(),
                "OkHttpClient should retain configured connect timeout in milliseconds");
        assertEquals(TIMEOUT_SECONDS * 1000, client.readTimeoutMillis(),
                "OkHttpClient should retain configured read timeout in milliseconds");
        assertEquals(TIMEOUT_SECONDS * 1000, client.writeTimeoutMillis(),
                "OkHttpClient should retain configured write timeout in milliseconds");
    }

    @Test
    @DisplayName("OkHttpClient builder should accept custom interceptors for request processing")
    void okHttpClientSupportsInterceptors() {
        LoggingInterceptor interceptor = new LoggingInterceptor();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        assertTrue(client.interceptors().contains(interceptor),
                "OkHttpClient interceptors() should contain " + interceptor);
        assertEquals(1, client.interceptors().size(),
                "OkHttpClient should have exactly one custom interceptor configured");
    }

    @Test
    @DisplayName("OkHttpClient builder should configure connection pool parameters")
    void okHttpClientConnectionPoolConfiguration() {
        ConnectionPool pool = new ConnectionPool(
                MAX_IDLE_CONNECTIONS,
                KEEP_ALIVE_MINUTES,
                TimeUnit.MINUTES
        );

        OkHttpClient client = new OkHttpClient.Builder()
                .connectionPool(pool)
                .build();

        assertSame(pool, client.connectionPool(),
                "OkHttpClient should use the configured connection pool instance");
        assertEquals(0, pool.connectionCount(),
                "New connection pool should have zero active connections initially");
    }

    @Test
    @DisplayName("OkHttp Request builder should construct requests with headers and query params")
    void okHttpRequestBuilderConstructsRequests() {
        Request request = new Request.Builder()
                .url("https://example.com/api?param=value")
                .header("Authorization", "Bearer token")
                .header("Content-Type", "application/json")
                .get()
                .build();

        assertEquals("https://example.com/api?param=value", request.url().toString(),
                "Request should retain configured URL with query parameters");
        assertEquals("Bearer token", request.header("Authorization"),
                "Request should retain configured Authorization header value");
        assertEquals("GET", request.method(),
                "Request should use GET method as specified by builder");
    }

    /**
     * Simple logging interceptor for smoke test validation of interceptor support.
     */
    static class LoggingInterceptor implements Interceptor {

        @Override
        public @NotNull Response intercept(final Chain chain) throws IOException {
            // In a real implementation, this would log request details
            return chain.proceed(chain.request());
        }
    }
}
