/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MMTooShort}.
 */
@DisplayName("MM too short tests scenario case")
class MMTooShortTest {

    private MMTooShort rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMTooShort();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Short class Javadoc triggers class fix guidance")
    void shortClassJavadocTriggersClassFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(12)
                .source(MessageSource.JAVADOC_CLASS)
                .message("Too short.")
                .normalisedMessage(MessageNormaliser.normalise("Too short."))
                .build();
        MessageMetrics metrics = new MessageMetrics(10, 2, 2, 2, 2,
                Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(),
                "Rule should request stop processing when too short");
        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertEquals(1, pending.size(), "One violation should be recorded");
        Violation violation = pending.get(12);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        assertEquals(12, violation.lineNo(), "Violation should preserve the candidate line");
        Object[] args = violation.args();
        assertEquals(4, args.length, "Violation should include message, counts, and fix guidance");
        assertEquals("state responsibility + lifecycle, thread-safety, or performance intent",
                args[3], "Fix guidance should match class Javadoc guidance");
    }

    @Test
    @DisplayName("Short comment for return null uses comment fix guidance")
    void shortCommentForReturnNullUsesCommentFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(21)
                .source(MessageSource.COMMENT)
                .message("null reason")
                .missingMessageKind(MissingMessageKind.RETURN_NULL)
                .build();
        MessageMetrics metrics = new MessageMetrics(12, 2, 1, 2, 1,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(21);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("explain why returning null is required",
                args[3], "Fix guidance should match return-null comment guidance");
    }

    @Test
    @DisplayName("Short log message uses log fix guidance")
    void shortLogMessageUsesLogFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(28)
                .source(MessageSource.LOG)
                .message("retry")
                .build();
        MessageMetrics metrics = new MessageMetrics(5, 1, 1, 1, 1,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(28);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("include action + subject + identifier or outcome",
                args[3], "Fix guidance should match log guidance");
    }
}
