/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

/**
 * Sink for message candidates extracted from source code.
 */
public interface MessageCandidateSink {
    /**
     * Emit a fully described message candidate.
     *
     * @param candidate candidate to evaluate.
     */
    void emitCandidate(MessageCandidate candidate);

    /**
     * Emit a missing-message candidate for a source location.
     *
     * @param lineNo line number where the message is missing.
     * @param source source category of the missing message.
     */
    void emitMissingMessage(int lineNo, MessageSource source);

    /**
     * Emit a missing-message candidate with an explicit advice source.
     *
     * @param lineNo        line number where the message is missing.
     * @param source        source category of the missing message.
     * @param adviceSource  advice source override.
     * @param missingMessageKind missing message kind for fix guidance.
     */
    void emitMissingMessage(int lineNo, MessageSource source,
                            AdviceSource adviceSource,
                            MissingMessageKind missingMessageKind);

    /**
     * Emit a missing-message candidate with additional context.
     * Delegates to the 4-arg overload with a {@code null} advice source.
     *
     * @param lineNo             line number where the message is missing.
     * @param source             source category of the missing message.
     * @param missingMessageKind missing message kind for fix guidance.
     */
    default void emitMissingMessage(int lineNo, MessageSource source,
                                    MissingMessageKind missingMessageKind) {
        emitMissingMessage(lineNo, source, null, missingMessageKind);
    }

    /**
     * Emit a debug signal for unhandled extraction cases.
     *
     * @param ast    AST node related to the unhandled case.
     * @param reason description of the unhandled case.
     */
    default void emitUnhandled(DetailAST ast, String reason) {
    }
}
