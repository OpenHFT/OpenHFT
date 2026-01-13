/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that are shorter than the minimum word count.
 */
public final class MMTooShort extends AbstractMessageRule {
    private static final int DEFAULT_MAX_WORD_COUNT = new MessageMetricsCalculator().maxWordCount();

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
        MessageCandidate candidate = context.candidate();
        MessageSource source = candidate.source();
        int minWordCount = source.minWordCount();
        int maxWordCount = DEFAULT_MAX_WORD_COUNT;
        MessageRuleSupport ruleSupport = context.ruleSupport();
        if (ruleSupport != null && ruleSupport.metricsCalculator() != null) {
            maxWordCount = ruleSupport.metricsCalculator().maxWordCount();
        }
        if (metrics.totalWordCount() < minWordCount) {
            if (record(context, collector, candidate.message(),
                    metrics.totalWordCount(), minWordCount, maxWordCount, fixFor(candidate))) {
                state.requestStopProcessing();
            }
        }
    }

    private static String fixFor(MessageCandidate candidate) {
        MessageSource source = candidate.source();
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
            case COMMENT:
                MissingMessageKind kind = candidate.missingMessageKind();
                if (kind != null) {
                    switch (kind) {
                        case RETURN_NULL:
                            return "explain why returning null is required";
                        case SYSTEM_CALL:
                            return "explain why java.lang.System is required here";
                        case RUNTIME_CALL:
                            return "explain why java.lang.Runtime is required here";
                        default:
                            break;
                    }
                }
                return "explain why the non-idiomatic statement is required";
            case JAVADOC_CLASS:
                return "state responsibility + lifecycle, thread-safety, or performance intent";
            case JAVADOC_MEMBER:
                return "state contract + units, edge cases, or side effects";
            default:
                return "add subject + expected behaviour";
        }
    }
}
