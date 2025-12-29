/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags string-search assertions missing search values in messages.
 */
public final class MMMissingStringSearchValue extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMMissingStringSearchValue() {
        super(RuleId.MISSING_STRING_VALUE);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageCandidate candidate = context.candidate();
        if (!candidate.constantMessage()) {
            return;
        }
        if (candidate.stringSearchMethod() == null) {
            return;
        }
        record(context, collector, candidate.stringSearchMethod(),
                candidate.stringSearchTarget(), candidate.stringSearchArg());
    }
}
