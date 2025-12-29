/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that only restate the method name.
 */
public final class MMRedundantMethodName extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMRedundantMethodName() {
        super(RuleId.REDUNDANT_METHOD);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        String methodName = context.currentMethodName();
        if (message == null || methodName == null) {
            return;
        }
        MessageRuleSupport support = context.ruleSupport();
        String matchedVariant = support.findNameVariant(message, methodName);
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
        recordWarning(context, collector, state, message, methodName, diagnosis);
    }
}
