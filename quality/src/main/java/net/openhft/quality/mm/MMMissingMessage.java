/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags missing messages for assertions, throws, annotations, and logs.
 */
public final class MMMissingMessage extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMMissingMessage() {
        super(RuleId.MISSING_MESSAGE);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (!context.candidate().missingMessage()) {
            return;
        }
        if (record(context, collector)) {
            state.requestStopProcessing();
        }
    }
}
