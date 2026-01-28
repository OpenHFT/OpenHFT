/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Flags duplicate messages within the same file.
 */
public final class MMDuplicate extends AbstractMessageRule {
    private final Map<String, Integer> messageOccurrences;

    /**
     * Create the rule instance with a shared occurrence map.
     *
     * @param messageOccurrences map of normalised messages to first line number.
     */
    public MMDuplicate(Map<String, Integer> messageOccurrences) {
        super(RuleId.DUPLICATE);
        this.messageOccurrences = messageOccurrences;
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        MessageCandidate candidate = context.candidate();
        if (candidate.argumentNameMessage()) {
            return;
        }
        String message = candidate.message();
        if (message == null || message.isEmpty()) {
            return;
        }
        SuppressionTracker suppressionTracker = context.suppressionTracker();
        if (suppressionTracker != null && suppressionTracker.isSuppressed(ruleId(), candidate.lineNo())) {
            return;
        }
        String normalised = requireNonNull(candidate.normalisedMessage());
        if (normalised.isEmpty()) {
            normalised = MessageNormaliser.normalise(message);
        }
        if (normalised.isEmpty()) {
            return;
        }
        Integer firstOccurrence = messageOccurrences.get(normalised);
        if (firstOccurrence != null) {
            boolean recorded = record(context, collector, message, firstOccurrence);
            if (recorded && candidate.source() != MessageSource.LOG) {
                state.markWarningFired();
            }
            return;
        }
        messageOccurrences.put(normalised, candidate.lineNo());
    }
}
