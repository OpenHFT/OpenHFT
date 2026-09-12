/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flags messages containing runs of repeated whitespace.
 */
public final class MMWhitespaceRun extends AbstractMessageRule {
    private static final Pattern WHITESPACE_RUN_PATTERN = Pattern.compile("\\s{2,}");

    /**
     * Create the rule instance.
     */
    public MMWhitespaceRun() {
        super(RuleId.WHITESPACE_RUN);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (context.candidate().placeholderCount() != 0) {
            return;
        }
        String message = context.candidate().message();
        if (message == null) {
            return;
        }
        String match = extractMatch(message);
        if (match != null) {
            recordWarning(context, collector, state, message, match);
        }
    }

    private String extractMatch(String text) {
        Matcher matcher = MMWhitespaceRun.WHITESPACE_RUN_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
