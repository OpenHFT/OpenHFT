/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import jnr.constants.platform.Errno;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying Prometheus client metrics and JNR FFI integration.
 */
@DisplayName("Smoke test verifies metrics and FFI libraries load")
class MetricsAndFfiSmokeTest {

    /**
     * Tolerance value used for numeric comparison assertions.
     */
    private static final double DELTA = 0.0001d;

    /**
     * Prometheus counter name used for registry lookups.
     */
    private static final String COUNTER_NAME =
            "third_party_smoke_counter_total";

    /**
     * Pointer offset used in JNR memory checks.
     */
    private static final int OFFSET_ZERO = 0;

    /**
     * Memory size in bytes for pointer allocation.
     */
    private static final int EIGHT_BYTES = 8;

    /**
     * Sample long value written to memory.
     */
    private static final long VALUE_42 = 42L;

    @Test
    @DisplayName("Prometheus SimpleClient should register and read counter")
    void prometheusSimpleClientRegistersAndReadsCounter() {
        CollectorRegistry registry = new CollectorRegistry();
        Counter counter = Counter.build()
                .name(COUNTER_NAME)
                .help("Smoke test counter")
                .register(registry);

        counter.inc();
        Double value = registry.getSampleValue(COUNTER_NAME);
        assertNotNull(value, "Prometheus counter should be registered");
        assertEquals(1.0, value, DELTA, "Prometheus counter should increment");
    }

    @Test
    @DisplayName("Prometheus HTTPServer should start and stop")
    void prometheusHttpServerCanStartAndStop() throws Exception {
        HTTPServer server = new HTTPServer(0);
        assertNotNull(server, "Prometheus HTTPServer should start on an ephemeral port");
        server.close();
    }

    @Test
    @DisplayName("Prometheus hotspot DefaultExports should initialize JVM metrics collection")
    void prometheusHotspotDefaultExportsInitialize() {
        DefaultExports.initialize();
        assertTrue(true, "Prometheus DefaultExports.initialize should complete");
    }

    @Test
    @DisplayName("JNR Constants Errno should provide integer value")
    void jnrConstantsErrnoProvidesIntegerValue() {
        int ebadf = Errno.EBADF.intValue();
        assertTrue(ebadf > 0, "JNR Errno.EBADF should be > 0 but was " + ebadf);
    }

    @Test
    @DisplayName("JNR FFI should allocate and use memory")
    void jnrFfiCanAllocateAndUseMemory() {
        Runtime runtime = Runtime.getSystemRuntime();
        Pointer pointer = Memory.allocate(runtime, EIGHT_BYTES);
        pointer.putLong(OFFSET_ZERO, VALUE_42);
        assertEquals(VALUE_42, pointer.getLong(OFFSET_ZERO), "JNR pointer should read back written long");
    }
}
