/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        String message = candidate.message();
        if (message == null) {
            return;
        }
        String searchLiteral = unquoteLiteral(candidate.stringSearchArg());
        if (searchLiteral != null && !searchLiteral.isEmpty()
                && shouldSkipForLiteral(message, searchLiteral)) {
            return;
        }
        record(context, collector, candidate.stringSearchMethod(),
                candidate.stringSearchTarget(), candidate.stringSearchArg());
    }

    private static boolean shouldSkipForLiteral(String message, String literal) {
        if (literal.length() == 1 && Character.isLetterOrDigit(literal.charAt(0))) {
            return false;
        }
        return message.contains(literal);
    }

    private static String unquoteLiteral(String text) {
        if (text == null) {
            return null;
        }
        int length = text.length();
        if (length >= 2 && text.charAt(0) == '"' && text.charAt(length - 1) == '"') {
            return text.substring(1, length - 1);
        }
        return null;
    }
}
