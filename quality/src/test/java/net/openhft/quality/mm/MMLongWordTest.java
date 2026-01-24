/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MMLongWord}.
 */
@DisplayName("MM long word tests scenario case")
class MMLongWordTest {

    private MMLongWord rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMLongWord();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Skips firing when long word matches class name case insensitive")
    void skipsFiringWhenLongWordMatchesClassName() {
        String longWord = "ConfigurationManager";
        List<String> longWords = Collections.singletonList(longWord);
        MessageMetrics metrics = new MessageMetrics(50, 5, 5, 5, 5,
                Collections.emptyList(), longWords);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("ConfigurationManager should not be null")
                .normalisedMessage("configurationmanager should not be null")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "ConfigurationManager", "testMethod", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "long word matching class name should not trigger violation");
    }

    @Test
    @DisplayName("Skips firing when long word matches class name different case")
    void skipsFiringWhenLongWordMatchesClassNameDifferentCase() {
        String longWord = "CONFIGURATIONMANAGER";
        List<String> longWords = Collections.singletonList(longWord);
        MessageMetrics metrics = new MessageMetrics(50, 5, 5, 5, 5,
                Collections.emptyList(), longWords);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("CONFIGURATIONMANAGER should not be null")
                .normalisedMessage("configurationmanager should not be null")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "ConfigurationManager", "testMethod", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "long word matching class name case-insensitive should not trigger violation");
    }

    @Test
    @DisplayName("Skips firing when long word matches method name")
    void skipsFiringWhenLongWordMatchesMethodName() {
        String longWord = "processOrderNotification";
        List<String> longWords = Collections.singletonList(longWord);
        MessageMetrics metrics = new MessageMetrics(50, 5, 5, 5, 5,
                Collections.emptyList(), longWords);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(15)
                .source(MessageSource.ASSERTION)
                .message("processOrderNotification should succeed")
                .normalisedMessage("processordernotification should succeed")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "OrderService", "processOrderNotification", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "long word matching method name should not trigger violation");
    }

    @Test
    @DisplayName("Fires violation when long word does not match class or method name")
    void firesViolationWhenLongWordDoesNotMatchClassOrMethod() {
        String longWord = "UnrelatedLongIdentifier";
        List<String> longWords = Collections.singletonList(longWord);
        MessageMetrics metrics = new MessageMetrics(50, 5, 5, 5, 5,
                Collections.emptyList(), longWords);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(20)
                .source(MessageSource.ASSERTION)
                .message("UnrelatedLongIdentifier should be valid")
                .normalisedMessage("unrelatedlongidentifier should be valid")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "OrderService", "processOrder", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertEquals(1, collector.pendingForTesting().size(),
                "long word not matching class or method should trigger violation");
        assertEquals(RuleId.LONG_WORD, collector.pendingForTesting().get(20).ruleId(),
                "violation should be for long word rule");
    }

    @Test
    @DisplayName("Skips evaluation when metrics are null")
    void skipsEvaluationWhenMetricsAreNull() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("Some message with UnrelatedLongIdentifier")
                .normalisedMessage("some message")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "null metrics should skip evaluation");
    }

    @Test
    @DisplayName("Skips evaluation when long words list is empty")
    void skipsEvaluationWhenLongWordsListIsEmpty() {
        MessageMetrics metrics = new MessageMetrics(50, 5, 5, 5, 5,
                Collections.emptyList(), Collections.emptyList());

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("Short message")
                .normalisedMessage("short message")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "empty long words list should skip evaluation");
    }

    @Test
    @DisplayName("Only fires for first non-matching long word")
    void onlyFiresForFirstNonMatchingLongWord() {
        List<String> longWords = java.util.Arrays.asList(
                "FirstLongIdentifier", "SecondLongIdentifier");
        MessageMetrics metrics = new MessageMetrics(100, 10, 10, 10, 10,
                Collections.emptyList(), longWords);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(25)
                .source(MessageSource.ASSERTION)
                .message("FirstLongIdentifier and SecondLongIdentifier should be valid")
                .normalisedMessage("firstlongidentifier and secondlongidentifier should be valid")
                .build();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, ruleSupport, null, null);

        rule.evaluate(context, collector, state);

        assertEquals(1, collector.pendingForTesting().size(),
                "only one violation should be recorded for multiple long words");
    }
}
