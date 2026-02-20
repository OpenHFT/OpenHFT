/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
                    minMeaningfulWordCount, fixFor(context.candidate()));
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

    private String fixFor(MessageCandidate candidate) {
        MessageSource source = candidate.source();
        switch (source) {
            case ASSERTION:
                return "add unique words: subject + expected behaviour, include key values";
            case PRECONDITION:
                return "add unique words: parameter + constraint + unit";
            case THROW:
                return "add unique words: operation + input/state + failure reason";
            case ANNOTATION:
                return "add unique words: scenario + expected outcome";
            case LOG:
                return "add unique words: action + subject + identifier or outcome";
            case COMMENT:
                MissingMessageKind kind = candidate.missingMessageKind();
                if (kind != null) {
                    switch (kind) {
                        case RETURN_NULL:
                            return "use two+ meaningful words about why returning null is required";
                        case SYSTEM_CALL:
                            return "use two+ meaningful words about why java.lang.System is required";
                        case RUNTIME_CALL:
                            return "use two+ meaningful words about why java.lang.Runtime is required";
                        default:
                            break;
                    }
                }
                return "use two+ meaningful words explaining why this is required";
            case JAVADOC_CLASS:
                return "add unique words: responsibility + lifecycle or thread-safety intent";
            case JAVADOC_MEMBER:
                return "add unique words: contract + units, edge cases, or side effects";
            default:
                return "add unique meaningful words beyond filler";
        }
    }
}
