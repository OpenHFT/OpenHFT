/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test verifying JMH annotations and benchmark discovery helpers are available.
 */
@SuppressWarnings({"java:S5786", "PMD.JUnit5TestShouldBePackagePrivate"})
// JMH/JUnit rules disagree; JMH requires public
@DisplayName("JmhSmokeTest")
public class JmhSmokeTest {

    /**
     * Benchmark seed used to verify state handling across iterations.
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
    @DisplayName("JMH OptionsBuilder should create benchmark options")
    void canBuildJmhOptions() {
        Options opts = new OptionsBuilder()
                .include(JmhSmokeTest.class.getSimpleName())
                .forks(0)
                .build();
        assertNotNull(opts, "JMH OptionsBuilder should create options");
    }

    @Test
    @DisplayName("JMH core benchmarks JAR should be on classpath")
    void coreBenchmarksJarIsOnClasspath() throws Exception {
        Class<?> clazz = Class.forName(
                "org.openjdk.jmh.benchmarks.BlackholeConsumeCPUBench");
        assertNotNull(clazz, "JMH core benchmarks should be present on classpath");
    }

    /**
     * Sample state shared across benchmark invocations.
     */
    @State(Scope.Benchmark)
    public static class SampleState {
        /**
         * Benchmark value cached in state for sample execution.
         */
        private static final int VALUE = SAMPLE_VALUE;

        int value() {
            return VALUE;
        }
    }
}
