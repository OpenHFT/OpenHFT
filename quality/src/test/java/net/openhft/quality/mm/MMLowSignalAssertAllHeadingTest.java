/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MMLowSignalAssertAllHeading to improve mutation coverage.
 * Targets the surviving mutations for record() conditional and requestStopProcessing().
 */
class MMLowSignalAssertAllHeadingTest {

    private MessageCandidate createCandidate(String message, boolean assertAllHeading, int lineNo) {
        return new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(lineNo)
                .message(message)
                .normalisedMessage(message)
                .assertAllHeading(assertAllHeading)
                .build();
    }

    private MessageContext createContext(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null);
    }

    @Test
    @DisplayName("requestStopProcessing should be called when low-signal heading is recorded")
    void requestStopProcessing_calledWhenRecorded() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        MessageCandidate candidate = createCandidate("assertAll", true, 10);
        MessageContext context = createContext(candidate);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(),
                "requestStopProcessing should be called when violation is recorded");
        assertFalse(collector.pendingForTesting().isEmpty(),
                "violation should be recorded");
    }

    @Test
    @DisplayName("requestStopProcessing should NOT be called when recording is suppressed")
    void requestStopProcessing_notCalledWhenSuppressed() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        MessageCandidate candidate = createCandidate("checks", true, 20);
        MessageContext context = createContext(candidate);

        // Create a SuppressionTracker that suppresses all rules
        SuppressionTracker tracker = mock(SuppressionTracker.class);
        when(tracker.isSuppressed(any(RuleId.class))).thenReturn(true);

        ViolationCollector collector = new ViolationCollector(tracker);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertFalse(state.shouldStopProcessing(),
                "requestStopProcessing should NOT be called when violation is suppressed");
        assertTrue(collector.pendingForTesting().isEmpty(),
                "no violation should be recorded when suppressed");
    }

    @Test
    @DisplayName("doEvaluate should skip when candidate is not assertAllHeading")
    void doEvaluate_skipsWhenNotAssertAllHeading() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        MessageCandidate candidate = createCandidate("assertAll", false, 30);
        MessageContext context = createContext(candidate);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "no violation should be recorded for non-assertAllHeading");
        assertFalse(state.shouldStopProcessing(),
                "stop processing should not be requested");
    }

    @Test
    @DisplayName("doEvaluate should skip when message is null")
    void doEvaluate_skipsWhenMessageNull() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        MessageCandidate candidate = createCandidate(null, true, 40);
        MessageContext context = createContext(candidate);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "no violation should be recorded for null message");
    }

    @Test
    @DisplayName("doEvaluate should skip when message does not match low-signal pattern")
    void doEvaluate_skipsWhenNoMatch() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        MessageCandidate candidate = createCandidate("meaningful heading with context", true, 50);
        MessageContext context = createContext(candidate);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "no violation should be recorded for meaningful message");
    }

    @Test
    @DisplayName("doEvaluate should match various low-signal patterns")
    void doEvaluate_matchesVariousPatterns() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        String[] lowSignalPatterns = {"assertAll", "assertions", "checks", "validation", "test"};

        for (int i = 0; i < lowSignalPatterns.length; i++) {
            String pattern = lowSignalPatterns[i];
            MessageCandidate candidate = createCandidate(pattern, true, 100 + i);
            MessageContext context = createContext(candidate);

            ViolationCollector collector = new ViolationCollector(null);
            RuleEvaluationState state = new RuleEvaluationState();

            rule.evaluate(context, collector, state);

            assertFalse(collector.pendingForTesting().isEmpty(),
                    "violation should be recorded for low-signal pattern: " + pattern);
            assertTrue(state.shouldStopProcessing(),
                    "stop processing should be requested for: " + pattern);
        }
    }

    @Test
    @DisplayName("rule should return correct ruleId")
    void ruleId_returnsCorrectValue() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();
        assertEquals(RuleId.ASSERTALL_HEADING, rule.ruleId(),
                "Rule should return ASSERTALL_HEADING as its ruleId");
    }

    @Test
    @DisplayName("rule should not apply to non-ASSERTION sources")
    void evaluate_skipsNonAssertionSources() {
        MMLowSignalAssertAllHeading rule = new MMLowSignalAssertAllHeading();

        for (MessageSource source : MessageSource.values()) {
            if (source == MessageSource.ASSERTION) {
                continue;
            }

            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(source)
                    .lineNo(200)
                    .message("assertAll")
                    .normalisedMessage("assertAll")
                    .assertAllHeading(true)
                    .build();
            MessageContext context = createContext(candidate);

            ViolationCollector collector = new ViolationCollector(null);
            RuleEvaluationState state = new RuleEvaluationState();

            rule.evaluate(context, collector, state);

            assertTrue(collector.pendingForTesting().isEmpty(),
                    "no violation should be recorded for source: " + source);
        }
    }
}
