/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Base class for message rules that evaluate a candidate and report violations.
 */
public abstract class AbstractMessageRule {
    private final RuleId ruleId;

    /**
     * Create a rule with the supplied identifier.
     *
     * @param ruleId identifier used for applicability checks and reporting.
     */
    protected AbstractMessageRule(RuleId ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * Return the rule identifier for this rule.
     *
     * @return the rule identifier for this rule.
     */
    public final RuleId ruleId() {
        return ruleId;
    }

    /**
     * Evaluate the rule for a candidate when the rule applies to its source.
     *
     * @param context   message context holding the candidate.
     * @param collector collector for violations.
     * @param state     evaluation state for this candidate.
     */
    public final void evaluate(MessageContext context, ViolationCollector collector,
                               RuleEvaluationState state) {
        if (!ruleId.appliesTo(context.candidate().source())) {
            return;
        }
        doEvaluate(context, collector, state);
    }

    /**
     * Perform the rule-specific evaluation once applicability is confirmed.
     *
     * @param context   message context holding the candidate.
     * @param collector collector for violations.
     * @param state     evaluation state for this candidate.
     */
    protected abstract void doEvaluate(MessageContext context, ViolationCollector collector,
                                       RuleEvaluationState state);

    /**
     * Record a violation for the candidate.
     *
     * @param context   message context holding the candidate.
     * @param collector collector for violations.
     * @param args      rule-specific arguments for the violation.
     * @return {@code true} if the violation was recorded.
     */
    protected boolean record(MessageContext context, ViolationCollector collector, Object... args) {
        return collector.record(context.candidate().lineNo(), ruleId, args);
    }

    /**
     * Record a violation and mark that a warning has fired for this candidate.
     *
     * @param context   message context holding the candidate.
     * @param collector collector for violations.
     * @param state     evaluation state for this candidate.
     * @param args      rule-specific arguments for the violation.
     * @return {@code true} if the violation was recorded.
     */
    protected boolean recordWarning(MessageContext context, ViolationCollector collector,
                                    RuleEvaluationState state, Object... args) {
        boolean recorded = record(context, collector, args);
        if (recorded) {
            state.markWarningFired();
        }
        return recorded;
    }

    /**
     * Return metrics if available and no warning has fired, otherwise {@code null}.
     * Use for rules that depend on metrics and should skip if a prior warning fired.
     *
     * @param context message context.
     * @param state   evaluation state.
     * @return metrics if available and no warning fired, otherwise {@code null}.
     */
    protected MessageMetrics requireMetrics(MessageContext context, RuleEvaluationState state) {
        if (state.warningFired()) {
            return null;
        }
        return context.metrics();
    }
}
