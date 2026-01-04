/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MMMissingStringSearchValueTest {

    private MMMissingStringSearchValue rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMMissingStringSearchValue();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    void evaluateSkipsWhenMessageNotConstant() {
        MessageCandidate candidate = baseCandidate()
                .constantMessage(false)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateSkipsWhenSearchMethodMissing() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchMethod(null)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateSkipsWhenMessageNull() {
        MessageCandidate candidate = baseCandidate()
                .message(null)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateSkipsWhenMessageContainsSearchLiteral() {
        MessageCandidate candidate = baseCandidate()
                .message("value should contain abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateRecordsWhenLiteralIsSingleCharacter() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"x\"")
                .message("value should contain x")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    void evaluateRecordsWhenLiteralNotQuoted() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("abc")
                .message("value should contain abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    private MessageCandidate.Builder baseCandidate() {
        return new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .constantMessage(true)
                .stringSearchMethod("contains")
                .stringSearchTarget("value")
                .stringSearchArg("\"abc\"")
                .message("value should include abc");
    }

    private MessageContext context(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod", false, null, null);
    }
}
