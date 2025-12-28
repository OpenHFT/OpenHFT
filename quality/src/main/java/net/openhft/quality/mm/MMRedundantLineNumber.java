/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flags messages that redundantly include line numbers.
 */
public final class MMRedundantLineNumber extends AbstractMessageRule {
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile(
            "(?i)(?:"
                    + "\\bline\\s+#?\\d+|"
                    + "\\bL\\d+\\b|"
                    + "\\.java:\\d+|"
                    + "\\.kt:\\d+|"
                    + "\\.scala:\\d+"
                    + ")"
    );

    /**
     * Create the rule instance.
     */
    public MMRedundantLineNumber() {
        super(RuleId.REDUNDANT_LINE);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        String message = context.candidate().message();
        if (message == null) {
            return;
        }
        String match = extractMatch(message);
        if (match == null) {
            return;
        }
        recordWarning(context, collector, state, message, match);
    }

    private String extractMatch(String text) {
        Matcher matcher = MMRedundantLineNumber.LINE_NUMBER_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
