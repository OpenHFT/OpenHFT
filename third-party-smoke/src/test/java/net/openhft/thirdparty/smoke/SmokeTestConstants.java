/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

/**
 * Shared constants for smoke tests to centralise magic numbers and commonly used test values.
 * <p>
 * This utility class improves maintainability and ensures consistency across the test suite
 * by providing reusable constants for floating-point deltas, memory sizes, and counter names.
 */
public final class SmokeTestConstants {

    /**
     * Tolerance used for floating-point comparisons (e.g., Prometheus counter values).
     */
    public static final double DELTA = 0.0001d;

    /**
     * Common test value used in integer-based assertions.
     */
    public static final int TEST_VAL_100 = 100;

    /**
     * Common test value (42) used in various assertions.
     */
    public static final long VALUE_42 = 42L;

    /**
     * Memory size in bytes used for pointer allocation tests.
     */
    public static final int EIGHT_BYTES = 8;

    /**
     * Zero offset constant for memory operations.
     */
    public static final int OFFSET_ZERO = 0;

    /**
     * Prometheus counter name used for registry lookups in observability tests.
     */
    public static final String PROMETHEUS_COUNTER_NAME = "third_party_smoke_counter_total";

    private SmokeTestConstants() {
        // Utility class - prevent instantiation
    }
}
