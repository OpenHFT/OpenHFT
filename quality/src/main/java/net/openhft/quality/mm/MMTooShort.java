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
        if (context.candidate().argumentNameMessage()) {
            return;
        }
        MessageMetrics metrics = requireMetrics(context, state);
        if (metrics == null) {
            return;
        }
        MessageSource source = context.candidate().source();
        int minWordCount = source.minWordCount();
        if (metrics.totalWordCount() < minWordCount) {
            if (record(context, collector, context.candidate().message(),
                    metrics.totalWordCount(), minWordCount, fixFor(source))) {
                state.requestStopProcessing();
            }
        }
    }

    private static String fixFor(MessageSource source) {
        switch (source) {
            case ASSERTION:
                return "add subject + expected behaviour, include key values if relevant";
            case PRECONDITION:
                return "name parameter + constraint + unit where relevant";
            case THROW:
                return "state operation + input/state + failure reason";
            case ANNOTATION:
                return "describe scenario + expected outcome";
            case LOG:
                return "include action + subject + identifier or outcome";
            case JAVADOC_CLASS:
                return "state responsibility + lifecycle, thread-safety, or performance intent";
            case JAVADOC_MEMBER:
                return "state contract + units, edge cases, or side effects";
            default:
                return "add subject + expected behaviour";
        }
    }
}
