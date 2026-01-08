/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags comparison messages that omit the compared values.
 */
public final class MMMissingComparisonValues extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMMissingComparisonValues() {
        super(RuleId.MISSING_COMPARISON_VALUES);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageCandidate candidate = context.candidate();
        if (!candidate.constantMessage()) {
            return;
        }
        if (candidate.comparisonOperator() == null) {
            return;
        }
        record(context, collector, candidate.comparisonOperator(),
                candidate.comparisonLeftOperand(), candidate.comparisonRightOperand());
    }
}
