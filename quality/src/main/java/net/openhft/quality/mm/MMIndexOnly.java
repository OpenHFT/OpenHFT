/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Pattern;

/**
 * Flags messages that only contain an index value.
 */
public final class MMIndexOnly extends AbstractMessageRule {
    private static final Pattern INDEX_ONLY_PATTERN = Pattern.compile(
            "^(\\d+|\\[\\d+\\]|index\\s*\\d+|element\\s*\\d+|item\\s*\\d+|#\\d+|i=\\d+)$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Create the rule instance.
     */
    public MMIndexOnly() {
        super(RuleId.INDEX_ONLY);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        if (message != null && INDEX_ONLY_PATTERN.matcher(message).matches()) {
            recordWarning(context, collector, state, message);
        }
    }
}
