/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flags messages that restate derived assertion outcomes.
 */
public final class MMRestatesDerivedAssertion extends AbstractMessageRule {
    private static final Pattern RESTATES_DERIVED_PATTERN = Pattern.compile(
            "(?i)^("
                    + "(is )?empty|"
                    + "not empty|"
                    + "(is )?blank|"
                    + "not blank|"
                    + "(is )?present|"
                    + "not present|"
                    + "contains|"
                    + "does not contain|"
                    + "matches|"
                    + "does not match|"
                    + "(has |have )?size|"
                    + "(is )?zero|"
                    + "(is )?positive|"
                    + "(is )?negative"
                    + ")$"
    );

    /**
     * Create the rule instance.
     */
    public MMRestatesDerivedAssertion() {
        super(RuleId.RESTATES_DERIVED);
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
        Matcher matcher = MMRestatesDerivedAssertion.RESTATES_DERIVED_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
