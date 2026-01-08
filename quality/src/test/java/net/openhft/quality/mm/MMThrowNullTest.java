/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MMThrowNull}.
 */
@DisplayName("MM throw null rule tests scenario case")
class MMThrowNullTest {

    private MMThrowNull rule;
    private ViolationCollector collector;

    @BeforeEach
    void setUp() {
        rule = new MMThrowNull();
        collector = new ViolationCollector(null);
    }

    @Test
    @DisplayName("Evaluate with throwNull true records violation")
    void evaluate_throwNullTrue_recordsViolation() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(10)
                .throwNull(true)
                .build();
        MessageContext context = createContext(candidate);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertEquals(1, pending.size(), "should record one violation");
        Violation violation = pending.get(10);
        assertNotNull(violation, "violation should be on line 10");
        assertEquals(RuleId.THROW_NULL, violation.ruleId(), "should use THROW_NULL rule");
    }

    @Test
    @DisplayName("Evaluate with throwNull true stops processing")
    void evaluate_throwNullTrue_stopsProcessing() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(10)
                .throwNull(true)
                .build();
        MessageContext context = createContext(candidate);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(),
                "should request stop processing after throwNull violation");
    }

    @Test
    @DisplayName("Evaluate with throwNull false does not record violation")
    void evaluate_throwNullFalse_noViolation() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(10)
                .throwNull(false)
                .message("Error occurred")
                .build();
        MessageContext context = createContext(candidate);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertTrue(pending.isEmpty(), "should not record violation when throwNull is false");
        assertFalse(state.shouldStopProcessing(),
                "should not stop processing when no violation");
    }

    @Test
    @DisplayName("Evaluate with non-throw source skips evaluation")
    void evaluate_nonThrowSource_skipsEvaluation() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .throwNull(true)
                .build();
        MessageContext context = createContext(candidate);
        RuleEvaluationState state = new RuleEvaluationState();

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertTrue(pending.isEmpty(),
                "should skip evaluation for non-THROW source");
    }

    private MessageContext createContext(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null);
    }
}
