/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Flags missing messages for assertions, throws, annotations, and logs.
 */
public final class MMMissingMessage extends AbstractMessageRule {
    /**
     * Create the rule instance.
     */
    public MMMissingMessage() {
        super(RuleId.MISSING_MESSAGE);
    }

    @Override
    protected void doEvaluate(MessageContext context, ViolationCollector collector,
                              RuleEvaluationState state) {
        if (!context.candidate().missingMessage()) {
            return;
        }
        if (record(context, collector, fixFor(context.candidate()))) {
            state.requestStopProcessing();
        }
    }

    private String fixFor(MessageCandidate candidate) {
        if (candidate.source() == MessageSource.COMMENT) {
            MissingMessageKind kind = candidate.missingMessageKind();
            if (kind != null) {
                switch (kind) {
                    case RETURN_NULL:
                        return "add a single-line comment on the line before explaining why "
                                + "returning null is required";
                    case SYSTEM_CALL:
                        return "add a single-line comment on the line before explaining why "
                                + "java.lang.System is required here";
                    case RUNTIME_CALL:
                        return "add a single-line comment on the line before explaining why "
                                + "java.lang.Runtime is required here";
                    default:
                        break;
                }
            }
            return "add a single-line comment on the line before explaining why this is required";
        }
        return "add a meaningful message, supply a Throwable, or add a single-line comment "
                + "on the line before when a message must be omitted";
    }
}
