/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test for JMH annotations and benchmark discovery helpers.
 */
@SuppressWarnings("java:S5786") // JMH requires specific method signatures
public class JmhSmokeTest {

    /**
     * Test value used in the sample benchmark.
     */
    private static final int SAMPLE_VALUE = 42;

    /**
     * Sample benchmark method discovered by JMH.
     *
     * @param state state holder
     * @return value used for benchmark output
     */
    @Benchmark
    public int sampleBenchmark(final SampleState state) {
        return state.value();
    }

    @Test
    void canBuildJmhOptions() {
        Options opts = new OptionsBuilder()
                .include(JmhSmokeTest.class.getSimpleName())
                .forks(0)
                .build();
        assertNotNull(opts);
    }

    @Test
    void coreBenchmarksJarIsOnClasspath() throws Exception {
        Class<?> clazz = Class.forName(
                "org.openjdk.jmh.benchmarks.BlackholeConsumeCPUBench");
        assertNotNull(clazz);
    }

    /**
     * Sample state shared across benchmark invocations.
     */
    @State(Scope.Benchmark)
    public static class SampleState {
        /**
         * Test value used by the sample benchmark.
         */
        private static final int VALUE = SAMPLE_VALUE;

        int value() {
            return VALUE;
        }
    }
}
