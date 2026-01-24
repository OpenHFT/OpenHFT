/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MMDuplicate}.
 */
@DisplayName("MM duplicate tests scenario case")
class MMDuplicateTest {

    private Map<String, Integer> messageOccurrences;
    private MMDuplicate rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        messageOccurrences = new HashMap<>();
        rule = new MMDuplicate(messageOccurrences);
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Skips candidates with argument name message flag")
    void skipsCandidatesWithArgumentNameMessageFlag() {
        messageOccurrences.put("value is null", 5);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("value is null")
                .normalisedMessage("value is null")
                .argumentNameMessage(true)
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "rule should skip candidates with argumentNameMessage flag");
        assertFalse(state.warningFired(),
                "warning should not fire for skipped candidate");
    }

    @Test
    @DisplayName("Processes candidates without argument name message flag")
    void processesCandidatesWithoutArgumentNameMessageFlag() {
        messageOccurrences.put("value is null", 5);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("value is null")
                .normalisedMessage("value is null")
                .argumentNameMessage(false)
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertEquals(1, collector.pendingForTesting().size(),
                "rule should detect duplicate for non-argument-name message");
        assertTrue(state.warningFired(),
                "warning should fire for duplicate message");
    }

    @Test
    @DisplayName("Log source does not mark warning fired")
    void logSourceDoesNotMarkWarningFired() {
        messageOccurrences.put("retrying operation", 5);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.LOG)
                .message("Retrying operation")
                .normalisedMessage("retrying operation")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertEquals(1, collector.pendingForTesting().size(),
                "rule should detect duplicate for log message");
        assertFalse(state.warningFired(),
                "warning should not be marked as fired for LOG source");
    }

    @Test
    @DisplayName("Non-log source marks warning fired on duplicate")
    void nonLogSourceMarksWarningFiredOnDuplicate() {
        messageOccurrences.put("value should not be null", 5);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.THROW)
                .message("Value should not be null")
                .normalisedMessage("value should not be null")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertEquals(1, collector.pendingForTesting().size(),
                "rule should detect duplicate for throw message");
        assertTrue(state.warningFired(),
                "warning should be marked as fired for THROW source");
    }

    @Test
    @DisplayName("First occurrence records message in map")
    void firstOccurrenceRecordsMessageInMap() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(15)
                .source(MessageSource.ASSERTION)
                .message("Order should be valid")
                .normalisedMessage("order should be valid")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "no violation for first occurrence");
        assertEquals(Integer.valueOf(15), messageOccurrences.get("order should be valid"),
                "first occurrence should record line number in map");
    }

    @Test
    @DisplayName("Empty message skips processing")
    void emptyMessageSkipsProcessing() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("")
                .normalisedMessage("")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "empty message should not trigger violation");
        assertTrue(messageOccurrences.isEmpty(),
                "empty message should not be recorded");
    }

    @Test
    @DisplayName("Null message skips processing")
    void nullMessageSkipsProcessing() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message(null)
                .normalisedMessage("")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(collector.pendingForTesting().isEmpty(),
                "null message should not trigger violation");
    }
}
