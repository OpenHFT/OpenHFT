/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.List;

/**
 * Flags messages containing overly long words.
 */
public final class MMLongWord extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMLongWord() {
        super(RuleId.LONG_WORD);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageMetrics metrics = context.metrics();
        if (metrics == null) {
            return;
        }
        List<String> longWords = metrics.longWords();
        if (longWords.isEmpty()) {
            return;
        }
        String className = context.currentClassName();
        String methodName = context.currentMethodName();
        int maxWordLength = context.ruleSupport().metricsCalculator().maxWordLength();
        for (String word : longWords) {
            if (!matchesIgnoreCase(word, className) && !word.equalsIgnoreCase(methodName)) {
                record(context, collector, context.candidate().message(), word, maxWordLength);
                break;
            }

        }
    }

    private boolean matchesIgnoreCase(String word, String name) {
        return word.equalsIgnoreCase(name);
    }
}
