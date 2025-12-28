/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags trivial Supplier messages that defeat lazy evaluation.
 */
public final class MMTrivialSupplier extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMTrivialSupplier() {
        super(RuleId.TRIVIAL_SUPPLIER);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String description = context.candidate().trivialSupplierDescription();
        if (description == null) {
            return;
        }
        recordWarning(context, collector, state, description);
    }
}
