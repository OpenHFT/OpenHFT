/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MMMissingLoopIndexTest {

    private MMMissingLoopIndex rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMMissingLoopIndex();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    void evaluateSkipsWhenMissingLoopIndexFalse() {
        MessageCandidate candidate = baseCandidate()
                .missingLoopIndex(false)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateSkipsWhenLoopNamesEmpty() {
        MessageCandidate candidate = baseCandidate()
                .loopNames(Collections.<String>emptyList())
                .missingLoopIndex(true)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTesting().isEmpty());
    }

    @Test
    void evaluateRecordsWhenLoopNamesPresent() {
        MessageCandidate candidate = baseCandidate()
                .missingLoopIndex(true)
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(12);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_LOOP_INDEX, violation.ruleId());
    }

    private MessageCandidate.Builder baseCandidate() {
        return new MessageCandidate.Builder()
                .lineNo(12)
                .source(MessageSource.ASSERTION)
                .message("loop should include index")
                .constantMessage(true)
                .loopNames(Collections.singletonList("i"))
                .missingLoopIndex(true);
    }

    private MessageContext context(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod", false, null, null);
    }
}
