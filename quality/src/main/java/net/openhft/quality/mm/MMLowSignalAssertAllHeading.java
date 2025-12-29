/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Pattern;

/**
 * Flags low-signal headings passed to assertAll.
 */
public final class MMLowSignalAssertAllHeading extends AbstractMessageRule {
    private static final Pattern LOW_SIGNAL_HEADING_PATTERN = Pattern.compile(
            "(?i)^("
                    + "assert(all|ions?)?|"
                    + "grouped? assertions?|"
                    + "checks?|"
                    + "validat(e|ions?)?|"
                    + "tests?"
                    + ")$"
    );

    /**
     * Create the rule instance.
     */
    public MMLowSignalAssertAllHeading() {
        super(RuleId.ASSERTALL_HEADING);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (!context.candidate().assertAllHeading()) {
            return;
        }
        String message = context.candidate().message();
        if (message == null) {
            return;
        }
        if (LOW_SIGNAL_HEADING_PATTERN.matcher(message.trim()).matches()) {
            if (record(context, collector, message)) {
                state.requestStopProcessing();
            }
        }
    }
}
