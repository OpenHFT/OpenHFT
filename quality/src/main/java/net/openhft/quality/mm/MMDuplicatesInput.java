/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Flags messages that duplicate input values.
 */
public final class MMDuplicatesInput extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMDuplicatesInput() {
        super(RuleId.DUPLICATES_INPUT);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        List<String> inputValues = context.candidate().inputValues();
        if (message == null || inputValues.isEmpty()) {
            return;
        }
        String messageLower = message.toLowerCase().trim();
        for (String input : inputValues) {
            requireNonNull(input);
            if (input.isEmpty()) {
                continue;
            }
            String inputLower = input.toLowerCase().trim();
            if (messageLower.equals(inputLower)
                    || messageLower.equals(inputLower + " value")
                    || messageLower.equals(inputLower + " result")
                    || messageLower.equals("expected " + inputLower)
                    || messageLower.equals("actual " + inputLower)) {
                if (recordWarning(context, collector, state, message, input)) {
                    return;
                }
            }
        }
    }
}
