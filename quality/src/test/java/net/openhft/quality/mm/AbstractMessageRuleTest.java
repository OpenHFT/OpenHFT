/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Abstract message rule tests scenario case")
class AbstractMessageRuleTest {

    @Test
    @DisplayName("Record warning returns true and marks warning fired")
    void recordWarningReturnsTrueAndMarksWarningFired() {
        TestRule rule = new TestRule();
        RuleEvaluationState state = new RuleEvaluationState();
        ViolationCollector collector = new ViolationCollector(null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(3)
                .source(MessageSource.ASSERTION)
                .message("test")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(rule.recorded, "Record should return true for default collector");
        assertTrue(state.warningFired(), "Warning should be marked as fired");
    }

    @Test
    @DisplayName("Record warning returns false when collector refuses record")
    void recordWarningReturnsFalseWhenCollectorRefusesRecord() {
        TestRule rule = new TestRule();
        RuleEvaluationState state = new RuleEvaluationState();
        ViolationCollector collector = new ViolationCollector(null) {
            @Override
            public boolean record(int lineNo, RuleId ruleId, Object... args) {
                return false;
            }
        };
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(4)
                .source(MessageSource.ASSERTION)
                .message("test")
                .build();
        MessageContext context = new MessageContext(candidate, null,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertFalse(rule.recorded, "Record should return false for rejecting collector");
        assertFalse(state.warningFired(), "Warning should not be marked when record fails");
    }

    private static final class TestRule extends AbstractMessageRule {
        private boolean recorded;

        private TestRule() {
            super(RuleId.MISSING_MESSAGE);
        }

        @Override
        protected void doEvaluate(MessageContext context, ViolationCollector collector,
                                  RuleEvaluationState state) {
            recorded = recordWarning(context, collector, state, "detail");
        }
    }
}
