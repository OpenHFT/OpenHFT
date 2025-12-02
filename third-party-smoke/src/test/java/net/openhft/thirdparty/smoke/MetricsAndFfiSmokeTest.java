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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for Prometheus client and JNR FFI.
 */
class MetricsAndFfiSmokeTest {

    /**
     * Tolerance used in numeric comparisons.
     */
    private static final double DELTA = 0.0001d;

    /**
     * Test counter name.
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
    void prometheusSimpleClientRegistersAndReadsCounter() {
        CollectorRegistry registry = new CollectorRegistry();
        Counter counter = Counter.build()
                .name(COUNTER_NAME)
                .help("Smoke test counter")
                .register(registry);

        counter.inc();
        Double value = registry.getSampleValue(COUNTER_NAME);
        assertNotNull(value);
        assertEquals(1.0, value, DELTA);
    }

    @Test
    void prometheusHttpServerCanStartAndStop() throws Exception {
        HTTPServer server = new HTTPServer(0);
        assertNotNull(server);
        server.close();
    }

    @Test
    void prometheusHotspotDefaultExportsInitialize() {
        DefaultExports.initialize();
    }

    @Test
    void jnrConstantsErrnoProvidesIntegerValue() {
        int ebadf = Errno.EBADF.intValue();
        assertTrue(ebadf > 0);
    }

    @Test
    void jnrFfiCanAllocateAndUseMemory() {
        Runtime runtime = Runtime.getSystemRuntime();
        Pointer pointer = Memory.allocate(runtime, EIGHT_BYTES);
        pointer.putLong(OFFSET_ZERO, VALUE_42);
        assertEquals(VALUE_42, pointer.getLong(OFFSET_ZERO));
    }
}
