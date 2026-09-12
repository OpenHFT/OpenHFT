/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags generic messages with low information content.
 */
public final class MMGenericMessage extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMGenericMessage() {
        super(RuleId.GENERIC);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (context.candidate().argumentNameMessage()) {
            return;
        }
        String message = context.candidate().message();
        if (context.ruleSupport().isGenericMessage(message)) {
            recordWarning(context, collector, state, message);
        }
    }
}
