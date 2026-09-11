/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class JUnitPlatformLauncherSmokeTest {
    @Test
    void executesExactlyOneSuccessfulTest() {
        requireOneSuccess(execute(PassingSample.class));
    }

    @Test
    void rejectsAnExecutedFailure() {
        TestExecutionSummary summary = execute(FailingSample.class);
        assertEquals(1, summary.getTestsFailedCount());
        assertThrows(AssertionError.class, () -> requireOneSuccess(summary));
    }

    @Test
    void rejectsAnAbortedTest() {
        TestExecutionSummary summary = execute(AbortedSample.class);
        assertEquals(1, summary.getTestsAbortedCount());
        assertThrows(AssertionError.class, () -> requireOneSuccess(summary));
    }

    private static TestExecutionSummary execute(Class<?> sample) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(sample)).build(), listener);
        return listener.getSummary();
    }

    private static void requireOneSuccess(TestExecutionSummary summary) {
        assertEquals(1, summary.getTestsFoundCount(), "one intended test must be discovered");
        assertEquals(1, summary.getTestsStartedCount(), "the discovered test must start");
        assertEquals(0, summary.getTotalFailureCount(), "test and container failures must fail the probe");
        assertEquals(0, summary.getTestsAbortedCount(), "an aborted test is not a successful probe");
        assertEquals(0, summary.getTestsSkippedCount(), "a skipped test is not a successful probe");
        assertEquals(1, summary.getTestsSucceededCount(), "the intended test must pass");
    }

    static class PassingSample {
        @Test
        void sample() {
            assertEquals(4, 2 + 2);
        }
    }

    static class FailingSample {
        @Test
        void sample() {
            fail("intentional launcher negative control");
        }
    }

    static class AbortedSample {
        @Test
        void sample() {
            assumeTrue(false, "intentional launcher abort control");
        }
    }
}
