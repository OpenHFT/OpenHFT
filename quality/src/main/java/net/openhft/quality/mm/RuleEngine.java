/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Evaluates extracted message candidates against the rule set.
 */
public final class RuleEngine {
    private final MessageRuleSupport ruleSupport;
    private final AbstractMessageRule missingMessageRule;
    private final List<AbstractMessageRule> rules;

    /**
     * Create a rule engine for the provided rule support and message map.
     *
     * @param ruleSupport rule helper utilities.
     * @param messageOccurrences map of normalised messages to first line numbers.
     */
    public RuleEngine(MessageRuleSupport ruleSupport, Map<String, Integer> messageOccurrences) {
        this.ruleSupport = ruleSupport;
        this.missingMessageRule = new MMMissingMessage();
        this.rules = buildRules(messageOccurrences);
    }

    /**
     * Evaluate a candidate against all configured rules.
     *
     * @param candidate message candidate to evaluate.
     * @param metrics precomputed message metrics, or {@code null} to compute on demand.
     * @param currentClassName current class name.
     * @param currentMethodName current method name.
     * @param verbose {@code true} when verbose reporting is enabled.
     * @param suppressionTracker suppression tracker for the file.
     * @param collector collector for violations.
     */
    public void evaluate(MessageCandidate candidate, MessageMetrics metrics,
                         String currentClassName,
                         String currentMethodName, boolean verbose,
                         SuppressionTracker suppressionTracker,
                         ViolationCollector collector) {
        MessageMetrics resolvedMetrics = metrics;
        if (resolvedMetrics == null && candidate.message() != null) {
            resolvedMetrics = ruleSupport.metricsCalculator().calculate(candidate.message(),
                    candidate.placeholderCount(), candidate.keyValueLabelCount());
        }
        MessageContext context = new MessageContext(candidate, resolvedMetrics, currentClassName,
                currentMethodName, verbose, ruleSupport, suppressionTracker);
        RuleEvaluationState state = new RuleEvaluationState();

        if (candidate.missingMessage()) {
            missingMessageRule.evaluate(context, collector, state);
            return;
        }

        for (AbstractMessageRule rule : rules) {
            rule.evaluate(context, collector, state);
            if (state.shouldStopProcessing()) {
                return;
            }
        }
    }

    private List<AbstractMessageRule> buildRules(Map<String, Integer> messageOccurrences) {
        List<AbstractMessageRule> ordered = new ArrayList<>();
        ordered.add(new MMLowSignalAssertAllHeading());
        ordered.add(new MMAssertJGenericOverride());
        ordered.add(new MMTrivialSupplier());
        ordered.add(new MMDuplicatesInput());
        ordered.add(new MMMissingLoopIndex());
        ordered.add(new MMMissingComparisonValues());
        ordered.add(new MMMissingStringSearchValue());
        ordered.add(new MMDuplicate(messageOccurrences));
        ordered.add(new MMRedundantClassName());
        ordered.add(new MMRedundantMethodName());
        ordered.add(new MMRedundantLineNumber());
        ordered.add(new MMGenericMessage());
        ordered.add(new MMRestatesAssertion());
        ordered.add(new MMIndexOnly());
        ordered.add(new MMContextless());
        ordered.add(new MMRestatesDerivedAssertion());
        ordered.add(new MMMissingSubject());
        ordered.add(new MMWhitespaceRun());
        ordered.add(new MMLongWord());
        ordered.add(new MMTooShort());
        ordered.add(new MMTooLong());
        ordered.add(new MMTooFewMeaningfulWords());
        return ordered;
    }
}
