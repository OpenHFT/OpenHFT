/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MM duplicates input tests")
class MMDuplicatesInputTest {

    private MMDuplicatesInput rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMDuplicatesInput();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Evaluate skips when message is null")
    void evaluateSkipsWhenMessageIsNull() {
        MessageCandidate candidate = baseCandidate()
                .message(null)
                .inputValues(Collections.singletonList("admin"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when input values are empty")
    void evaluateSkipsWhenInputValuesAreEmpty() {
        MessageCandidate candidate = baseCandidate()
                .message("admin")
                .inputValues(Collections.<String>emptyList())
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when non-empty input matches after empty input")
    void evaluateRecordsWhenNonEmptyInputMatchesAfterEmptyInput() {
        MessageCandidate candidate = baseCandidate()
                .message("expected admin")
                .inputValues(Arrays.asList("", "admin"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.DUPLICATES_INPUT, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate does not record when no input value matches")
    void evaluateDoesNotRecordWhenNoInputValueMatches() {
        MessageCandidate candidate = baseCandidate()
                .message("value did not match")
                .inputValues(Arrays.asList("admin", "root"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    @DisplayName("Evaluate throws NPE when input list contains null value before any match")
    void evaluateThrowsOnNullInputValue() {
        MessageCandidate candidate = baseCandidate()
                .message("something else")
                .inputValues(Arrays.asList(null, "admin"))
                .build();

        assertThrows(NullPointerException.class,
                () -> rule.evaluate(context(candidate), collector, state));
    }

    @Test
    @DisplayName("Evaluate does not match when input values are whitespace-only")
    void evaluateDoesNotMatchWhenInputValuesAreWhitespaceOnly() {
        MessageCandidate candidate = baseCandidate()
                .message("admin")
                .inputValues(Arrays.asList("   ", "\t"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records exact match case-insensitive")
    void evaluateRecordsExactMatchCaseInsensitive() {
        MessageCandidate candidate = baseCandidate()
                .message("Admin")
                .inputValues(Collections.singletonList("admin"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.DUPLICATES_INPUT, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when message matches input plus value suffix")
    void evaluateRecordsWhenMessageMatchesInputPlusValueSuffix() {
        MessageCandidate candidate = baseCandidate()
                .message("admin value")
                .inputValues(Collections.singletonList("admin"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.DUPLICATES_INPUT, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when message matches expected prefix plus input")
    void evaluateRecordsWhenMessageMatchesExpectedPrefixPlusInput() {
        MessageCandidate candidate = baseCandidate()
                .message("expected admin")
                .inputValues(Collections.singletonList("admin"))
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.DUPLICATES_INPUT, violation.ruleId());
    }

    private MessageCandidate.Builder baseCandidate() {
        return new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION);
    }

    private MessageContext context(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod", false, null, null, null);
    }
}
