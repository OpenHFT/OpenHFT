/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags messages that lack meaningful context.
 */
public final class MMContextless extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMContextless() {
        super(RuleId.CONTEXTLESS);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        if (context.ruleSupport().isContextless(message)) {
            recordWarning(context, collector, state, message);
        }
    }
}
