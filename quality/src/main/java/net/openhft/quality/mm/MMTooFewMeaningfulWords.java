/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages with too few meaningful words.
 */
public final class MMTooFewMeaningfulWords extends AbstractMessageRule {
    private static final MessageMetricsCalculator METRICS_CALCULATOR = new MessageMetricsCalculator();

    /**
     * Create the rule instance.
     */
    public MMTooFewMeaningfulWords() {
        super(RuleId.TOO_FEW_MEANINGFUL);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (context.candidate().argumentNameMessage()) {
            return;
        }
        MessageMetrics metrics = requireMetrics(context, state);
        if (metrics == null) {
            return;
        }
        int minMeaningfulWordCount = context.candidate().source().minMeaningfulWordCount();
        if (metrics.effectiveMeaningfulWordCount() < minMeaningfulWordCount) {
            String uniqueWords = metrics.meaningfulWords().isEmpty()
                    ? "(none)"
                    : String.join(", ", metrics.meaningfulWords());
            String fillerWords = collectFillerWords(context.candidate().message());
            record(context, collector, context.candidate().message(),
                    uniqueWords, fillerWords, metrics.effectiveMeaningfulWordCount(),
                    minMeaningfulWordCount);
        }
    }

    private String collectFillerWords(String message) {
        if (message == null) {
            return "(none)";
        }
        String[] words = METRICS_CALCULATOR.splitWords(message);
        StringBuilder filler = new StringBuilder();
        boolean first = true;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!METRICS_CALCULATOR.isFillerWord(word)) {
                continue;
            }
            if (!first) {
                filler.append(", ");
            }
            filler.append(word);
            first = false;
        }
        return first ? "(none)" : filler.toString();
    }
}
