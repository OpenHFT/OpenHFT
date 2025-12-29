/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

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
}
