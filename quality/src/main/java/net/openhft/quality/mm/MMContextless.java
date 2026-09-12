/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        if (context.ruleSupport() == null) return;
        String message = context.candidate().message();
        if (context.ruleSupport().isContextless(message)) {
            recordWarning(context, collector, state, message);
        }
    }
}
