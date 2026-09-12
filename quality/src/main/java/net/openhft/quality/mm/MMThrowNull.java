/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags throw statements that use a null literal.
 */
public final class MMThrowNull extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMThrowNull() {
        super(RuleId.THROW_NULL);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (!context.candidate().throwNull()) {
            return;
        }
        if (record(context, collector)) {
            state.requestStopProcessing();
        }
    }
}
