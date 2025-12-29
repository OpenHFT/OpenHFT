/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that are shorter than the minimum word count.
 */
public final class MMTooShort extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMTooShort() {
        super(RuleId.TOO_SHORT);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageMetrics metrics = requireMetrics(context, state);
        if (metrics == null) {
            return;
        }
        int minWordCount = context.candidate().source().minWordCount();
        if (metrics.totalWordCount() < minWordCount) {
            if (record(context, collector, context.candidate().message(),
                    metrics.totalWordCount(), minWordCount)) {
                state.requestStopProcessing();
            }
        }
    }
}
