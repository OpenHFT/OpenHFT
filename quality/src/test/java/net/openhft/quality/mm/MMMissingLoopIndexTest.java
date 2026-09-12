/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MM missing loop index tests scenario")
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
    @DisplayName("Evaluate skips when missing loop index false")
    void evaluateSkipsWhenMissingLoopIndexFalse() {
        MessageCandidate candidate = baseCandidate()
                .missingLoopIndex(false)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when loop names empty")
    void evaluateSkipsWhenLoopNamesEmpty() {
        MessageCandidate candidate = baseCandidate()
                .loopNames(Collections.emptyList())
                .missingLoopIndex(true)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when loop names present")
    void evaluateRecordsWhenLoopNamesPresent() {
        MessageCandidate candidate = baseCandidate()
                .missingLoopIndex(true)
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
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
        return new MessageContext(candidate, null, "TestClass", "testMethod", false, null, null, null);
    }
}
