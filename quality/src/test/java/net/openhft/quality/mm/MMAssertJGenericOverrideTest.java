/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AssertJ override message rule tests")
class MMAssertJGenericOverrideTest {

    @Test
    @DisplayName("Rule ignores non AssertJ override candidates")
    void ignoresNonAssertJOverrideCandidates() {
        MessageCandidate candidate = buildCandidate("expected", false);
        MessageContext context = buildContext(candidate, null);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        new MMAssertJGenericOverride().evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "Should not record when assertJOverride is false");
    }

    @Test
    @DisplayName("Rule ignores null and empty messages")
    void ignoresNullAndEmptyMessages() {
        MessageCandidate nullMessage = buildCandidate(null, true);
        MessageCandidate emptyMessage = buildCandidate("", true);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();
        MMAssertJGenericOverride rule = new MMAssertJGenericOverride();

        rule.evaluate(buildContext(nullMessage, null), collector, state);
        rule.evaluate(buildContext(emptyMessage, null), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "Should not record for null or empty messages");
    }

    @Test
    @DisplayName("Rule records generic messages")
    void recordsGenericMessages() {
        MessageCandidate candidate = buildCandidate("expected", true);
        MessageContext context = buildContext(candidate, null);

        Map<Integer, Violation> pending = evaluate(context);

        assertEquals(RuleId.ASSERTJ_OVERRIDE, pending.values().iterator().next().ruleId(),
                "Should record AssertJ override for generic messages");
    }

    @Test
    @DisplayName("Rule records restated assertion messages")
    void recordsRestatedAssertionMessages() {
        MessageCandidate candidate = buildCandidate("assertEquals", true);
        MessageContext context = buildContext(candidate, null);

        Map<Integer, Violation> pending = evaluate(context);

        assertEquals(RuleId.ASSERTJ_OVERRIDE, pending.values().iterator().next().ruleId(),
                "Should record AssertJ override for restated assertions");
    }

    @Test
    @DisplayName("Rule records contextless messages")
    void recordsContextlessMessages() {
        MessageCandidate candidate = buildCandidate("comparison", true);
        MessageContext context = buildContext(candidate, null);

        Map<Integer, Violation> pending = evaluate(context);

        assertEquals(RuleId.ASSERTJ_OVERRIDE, pending.values().iterator().next().ruleId(),
                "Should record AssertJ override for contextless messages");
    }

    @Test
    @DisplayName("Rule records short messages when metrics available")
    void recordsShortMessagesWhenMetricsAvailable() {
        String message = "alpha beta";
        MessageMetrics metrics = new MessageMetricsCalculator().calculate(message, 0, 0);
        MessageCandidate candidate = buildCandidate(message, true);
        MessageContext context = buildContext(candidate, metrics);

        Map<Integer, Violation> pending = evaluate(context);

        assertEquals(RuleId.ASSERTJ_OVERRIDE, pending.values().iterator().next().ruleId(),
                "Should record when message is too short");
    }

    @Test
    @DisplayName("Rule ignores short messages without metrics")
    void ignoresShortMessagesWithoutMetrics() {
        MessageCandidate candidate = buildCandidate("alpha beta", true);
        MessageContext context = buildContext(candidate, null);

        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();

        new MMAssertJGenericOverride().evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "Should not record when metrics are unavailable");
    }

    private Map<Integer, Violation> evaluate(MessageContext context) {
        ViolationCollector collector = new ViolationCollector(null);
        RuleEvaluationState state = new RuleEvaluationState();
        new MMAssertJGenericOverride().evaluate(context, collector, state);
        return collector.pendingForTesting();
    }

    private MessageCandidate buildCandidate(String message, boolean assertJOverride) {
        return new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message(message)
                .normalisedMessage(message)
                .assertJOverride(assertJOverride)
                .build();
    }

    private MessageContext buildContext(MessageCandidate candidate, MessageMetrics metrics) {
        MessageRuleSupport support = new MessageRuleSupport(new MessageMetricsCalculator());
        return new MessageContext(candidate, metrics, "TestClass", "testMethod",
                false, support, null);
    }
}
