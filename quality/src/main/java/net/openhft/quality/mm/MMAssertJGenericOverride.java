/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags AssertJ override messages that are generic or low signal.
 */
public final class MMAssertJGenericOverride extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMAssertJGenericOverride() {
        super(RuleId.ASSERTJ_OVERRIDE);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (!context.candidate().assertJOverride()) {
            return;
        }
        String message = context.candidate().message();
        if (message == null || message.isEmpty()) {
            return;
        }
        MessageRuleSupport support = context.ruleSupport();
        if (support.isGenericMessage(message)
                || support.isRestatesAssertion(message)
                || support.isContextless(message)) {
            record(context, collector, message);
            return;
        }
        MessageMetrics metrics = context.metrics();
        if (metrics != null && metrics.wordCount() < MessageSource.ASSERTION.minWordCount()) {
            record(context, collector, message);
        }
    }
}
