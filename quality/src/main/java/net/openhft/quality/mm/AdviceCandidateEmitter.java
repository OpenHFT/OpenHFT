/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.FileContents;

/**
 * Builds advice candidates from rule evaluations.
 */
public final class AdviceCandidateEmitter implements AdviceEmitter {
    private final AdviceCollector collector;
    private final SuppressionTracker suppressionTracker;
    private final String fileName;

    public AdviceCandidateEmitter(AdviceCollector collector, SuppressionTracker suppressionTracker,
                                  FileContents fileContents) {
        this.collector = collector;
        this.suppressionTracker = suppressionTracker;
        this.fileName = fileContents == null ? "unknown" : fileContents.getFileName();
    }

    @Override
    public void record(MessageContext context, RuleId ruleId) {
        if (collector == null || context == null || ruleId == null) {
            return;
        }
        MessageCandidate candidate = context.candidate();
        if (candidate == null) {
            return;
        }
        AdviceSource adviceSource = resolveAdviceSource(candidate);
        AdviceId adviceId = AdviceId.forRule(ruleId, adviceSource);
        if (adviceId == AdviceId.UNKNOWN) {
            throw new IllegalStateException("Missing AdviceId mapping for " + ruleId + " and " + adviceSource);
        }
        if (suppressionTracker != null) {
            int lineNo = candidate.lineNo();
            if (suppressionTracker.isSuppressed(ruleId, lineNo)) {
                return;
            }
            if (suppressionTracker.isSuppressed(adviceId, lineNo)) {
                return;
            }
        }
        AdviceMetrics metrics = buildMetrics(candidate, context.metrics(), ruleId, context.ruleSupport());
        CandidateAdvice advice = new CandidateAdvice.Builder()
                .fileName(fileName)
                .lineNo(candidate.lineNo())
                .source(adviceSource)
                .adviceId(adviceId)
                .ruleId(ruleId)
                .messageLiteral(candidate.message())
                .messageExpr(candidate.messageExpr())
                .metrics(metrics)
                .build();
        collector.record(advice);
    }

    private AdviceSource resolveAdviceSource(MessageCandidate candidate) {
        AdviceSource adviceSource = candidate.adviceSource();
        if (adviceSource != null) {
            return adviceSource;
        }
        AdviceSource mapped = AdviceSource.fromMessageSource(candidate.source());
        if (mapped != null) {
            return mapped;
        }
        throw new IllegalStateException("Missing advice source for " + candidate.source());
    }

    private AdviceMetrics buildMetrics(MessageCandidate candidate, MessageMetrics metrics,
                                       RuleId ruleId, MessageRuleSupport ruleSupport) {
        int wordCount = metrics == null ? 0 : metrics.wordCount();
        int meaningfulWordCount = metrics == null ? 0 : metrics.meaningfulWordCount();
        int totalWordCount = metrics == null ? 0 : metrics.totalWordCount();
        int effectiveMeaningfulWordCount = metrics == null ? 0 : metrics.effectiveMeaningfulWordCount();
        Integer minWordCount = null;
        Integer maxWordCount = null;
        Integer minMeaningfulWordCount = null;
        Integer maxWordLength = null;
        MessageMetricsCalculator calculator = ruleSupport == null ? null : ruleSupport.metricsCalculator();
        if (ruleId != null) {
            switch (ruleId) {
                case TOO_SHORT:
                    minWordCount = candidate.source().minWordCount();
                    if (calculator != null) {
                        maxWordCount = calculator.maxWordCount();
                    }
                    break;
                case TOO_LONG:
                    if (calculator != null) {
                        maxWordCount = calculator.maxWordCount();
                    }
                    break;
                case TOO_FEW_MEANINGFUL:
                    minMeaningfulWordCount = candidate.source().minMeaningfulWordCount();
                    break;
                case LONG_WORD:
                    if (calculator != null) {
                        maxWordLength = calculator.maxWordLength();
                    }
                    break;
                default:
                    break;
            }
        }
        return new AdviceMetrics(wordCount, meaningfulWordCount,
                totalWordCount, effectiveMeaningfulWordCount,
                candidate.placeholderCount(), candidate.keyValueLabelCount(),
                minWordCount, maxWordCount, minMeaningfulWordCount, maxWordLength,
                candidate.comparisonOperator(), candidate.comparisonLeftOperand(),
                candidate.comparisonRightOperand(), candidate.stringSearchMethod(),
                candidate.stringSearchTarget(), candidate.stringSearchArg());
    }
}
