/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MessageCandidateSink} and {@link MessageCandidate}.
 */
@DisplayName("Message candidate sink default behaviour tests")
class MessageCandidateSinkTest {

    @Test
    @DisplayName("Emit unhandled default method should be no-op")
    void emitUnhandledDefaultNoOp() {
        MessageCandidateSink sink = new MessageCandidateSink() {
            @Override
            public void emitCandidate(MessageCandidate candidate) {
            }

            @Override
            public void emitMissingMessage(int lineNo, MessageSource source) {
            }
        };

        assertDoesNotThrow(() -> sink.emitUnhandled(null, "no-op"),
                "Default emitUnhandled should not throw");
    }

    // --- MessageCandidate accessor mutation killing tests ---

    @Test
    @DisplayName("MessageCandidate keyValueLabelCount returns set value")
    void messageCandidateKeyValueLabelCountReturnsSetValue() {
        // This test kills the "replaced int return with 0" mutation
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .keyValueLabelCount(5)
                .build();

        assertEquals(5, candidate.keyValueLabelCount(),
                "keyValueLabelCount should return the set value");
        assertNotEquals(0, candidate.keyValueLabelCount(),
                "keyValueLabelCount should not return 0 when set to non-zero");
    }

    @Test
    @DisplayName("MessageCandidate comparisonRightOperand returns set value")
    void messageCandidateComparisonRightOperandReturnsSetValue() {
        // This test kills the "replaced return value with null" mutation
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .comparisonRightOperand("expected")
                .build();

        assertEquals("expected", candidate.comparisonRightOperand(),
                "comparisonRightOperand should return the set value");
        assertNotNull(candidate.comparisonRightOperand(),
                "comparisonRightOperand should not return null when set");
    }

    @Test
    @DisplayName("MessageCandidate builder comparisonRightOperand returns this")
    void messageCandidateBuilderComparisonRightOperandReturnsThis() {
        // Verify builder chaining works correctly
        MessageCandidate.Builder builder = new MessageCandidate.Builder();
        MessageCandidate.Builder result = builder.comparisonRightOperand("test");
        assertSame(builder, result, "builder method should return this for chaining");
    }
}
