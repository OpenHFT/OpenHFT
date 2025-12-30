/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages with too few meaningful words.
 */
public final class MMTooFewMeaningfulWords extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMTooFewMeaningfulWords() {
        super(RuleId.TOO_FEW_MEANINGFUL);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageMetrics metrics = requireMetrics(context, state);
        if (metrics == null) {
            return;
        }
        int minMeaningfulWordCount = context.candidate().source().minMeaningfulWordCount();
        if (metrics.effectiveMeaningfulWordCount() < minMeaningfulWordCount) {
            String uniqueWords = metrics.meaningfulWords().isEmpty()
                    ? "(none)"
                    : String.join(", ", metrics.meaningfulWords());
            record(context, collector, context.candidate().message(),
                    uniqueWords, metrics.effectiveMeaningfulWordCount(),
                    minMeaningfulWordCount);
        }
    }
}
