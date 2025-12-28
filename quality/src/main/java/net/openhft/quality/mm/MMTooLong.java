/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that exceed the maximum word count.
 */
public final class MMTooLong extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMTooLong() {
        super(RuleId.TOO_LONG);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageMetrics metrics = requireMetrics(context, state);
        if (metrics == null) {
            return;
        }
        int maxWordCount = context.ruleSupport().metricsCalculator().maxWordCount();
        if (metrics.totalWordCount() > maxWordCount) {
            record(context, collector, context.candidate().message(),
                    metrics.totalWordCount(), maxWordCount);
        }
    }
}
