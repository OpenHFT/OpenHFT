/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.List;

/**
 * Flags messages that refer to a loop but omit the loop index.
 */
public final class MMMissingLoopIndex extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMMissingLoopIndex() {
        super(RuleId.MISSING_LOOP_INDEX);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageCandidate candidate = context.candidate();
        if (!candidate.missingLoopIndex()) {
            return;
        }
        List<String> loopNames = candidate.loopNames();
        if (loopNames.isEmpty()) {
            return;
        }
        recordWarning(context, collector, state, String.join(", ", loopNames));
    }
}
