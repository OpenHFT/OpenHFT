/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that only restate the class name.
 */
public final class MMRedundantClassName extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMRedundantClassName() {
        super(RuleId.REDUNDANT_CLASS);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        String className = context.currentClassName();
        if (message == null || className == null) {
            return;
        }
        MessageRuleSupport support = context.ruleSupport();
        String matchedVariant = support.findNameVariant(message, className);
        if (matchedVariant == null) {
            return;
        }
        MessageRuleSupport.SubstanceAnalysis analysis = support.analyseSubstance(message, matchedVariant);
        if (analysis.hasSubstance()) {
            return;
        }
        String diagnosis = context.verbose()
                ? analysis.diagnosis() + " " + analysis.verboseDetails()
                : analysis.diagnosis();
        recordWarning(context, collector, state, message, className, diagnosis);
    }
}
