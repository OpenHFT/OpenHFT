/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Pattern;

/**
 * Flags messages that start with a verb and omit a subject.
 */
public final class MMMissingSubject extends AbstractMessageRule {
    private static final Pattern MISSING_SUBJECT_PATTERN = Pattern.compile(
            "(?i)^(should|must|will|can|cannot|can't|won't|shouldn't|mustn't) "
    );

    /**
     * Create the rule instance.
     */
    public MMMissingSubject() {
        super(RuleId.MISSING_SUBJECT);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (state.warningFired()) {
            return;
        }
        String message = context.candidate().message();
        if (message == null) {
            return;
        }
        if (MISSING_SUBJECT_PATTERN.matcher(message).find()) {
            recordWarning(context, collector, state, message);
        }
    }
}
