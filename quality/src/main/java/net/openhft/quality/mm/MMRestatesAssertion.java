/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that restate the assertion itself.
 */
public final class MMRestatesAssertion extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMRestatesAssertion() {
        super(RuleId.RESTATES_ASSERTION);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        if (context.ruleSupport().isRestatesAssertion(message)) {
            recordWarning(context, collector, state, message);
        }
    }
}
