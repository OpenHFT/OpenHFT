/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(),
                "Rule should request stop processing when too short");
        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertEquals(1, pending.size(), "One violation should be recorded");
        Violation violation = pending.get(12);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        assertEquals(12, violation.lineNo(), "Violation should preserve the candidate line");
        Object[] args = violation.args();
        assertEquals(5, args.length, "Violation should include message, counts, and fix guidance");
        assertEquals("state responsibility + lifecycle, thread-safety, or performance intent",
                args[4], "Fix guidance should match class Javadoc guidance");
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
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(21);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("explain why returning null is required",
                args[4], "Fix guidance should match return-null comment guidance");
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
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(28);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("include action + subject + identifier or outcome",
                args[4], "Fix guidance should match log guidance");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "ASSERTION|add subject + expected behaviour, include key values if relevant",
            "PRECONDITION|name parameter + constraint + unit where relevant",
            "THROW|state operation + input/state + failure reason",
            "ANNOTATION|describe scenario + expected outcome",
            "JAVADOC_MEMBER|state contract + units, edge cases, or side effects"
    })
    @DisplayName("Short message fix guidance uses source specific wording")
    void shortMessageFixGuidanceUsesSourceSpecificWording(MessageSource source, String expected) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(30)
                .source(source)
                .message("short")
                .build();
        MessageMetrics metrics = new MessageMetrics(4, 1, 1, 1, 1,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(30);
        assertNotNull(violation, "Violation should be recorded for short message");
        assertEquals(expected, violation.args()[4], "Fix guidance should match source");
    }

    @Test
    @DisplayName("Short comment uses system and runtime fix guidance")
    void shortCommentUsesSystemAndRuntimeFixGuidance() {
        MessageMetrics metrics = new MessageMetrics(4, 1, 1, 1, 1,
                Collections.emptyList(), Collections.emptyList());

        MessageCandidate systemCandidate = new MessageCandidate.Builder()
                .lineNo(40)
                .source(MessageSource.COMMENT)
                .message("system")
                .missingMessageKind(MissingMessageKind.SYSTEM_CALL)
                .build();
        rule.evaluate(new MessageContext(systemCandidate, metrics,
                "TestClass", "testMethod", false, null, null, null), collector, state);

        MessageCandidate runtimeCandidate = new MessageCandidate.Builder()
                .lineNo(41)
                .source(MessageSource.COMMENT)
                .message("runtime")
                .missingMessageKind(MissingMessageKind.RUNTIME_CALL)
                .build();
        rule.evaluate(new MessageContext(runtimeCandidate, metrics,
                "TestClass", "testMethod", false, null, null, null), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        assertEquals("explain why java.lang.System is required here",
                pending.get(40).args()[4], "System comment guidance should match");
        assertEquals("explain why java.lang.Runtime is required here",
                pending.get(41).args()[4], "Runtime comment guidance should match");
    }
}
