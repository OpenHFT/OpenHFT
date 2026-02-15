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

/**
 * Tests for {@link MMTooFewMeaningfulWords}.
 */
@DisplayName("MM too few meaningful words tests scenario case")
class MMTooFewMeaningfulWordsTest {

    private MMTooFewMeaningfulWords rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMTooFewMeaningfulWords();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Comment with System call uses comment fix guidance")
    void commentWithSystemCallUsesCommentFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(17)
                .source(MessageSource.COMMENT)
                .message("system reason")
                .missingMessageKind(MissingMessageKind.SYSTEM_CALL)
                .build();
        MessageMetrics metrics = new MessageMetrics(12, 2, 1, 2, 1,
                Collections.singletonList("system"), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(17);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("use two+ meaningful words about why java.lang.System is required",
                args[5], "Fix guidance should match System comment guidance");
    }

    @Test
    @DisplayName("Comment fix guidance uses return null wording")
    void commentFixGuidanceUsesReturnNullWording() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(19)
                .source(MessageSource.COMMENT)
                .message("null reason")
                .missingMessageKind(MissingMessageKind.RETURN_NULL)
                .build();
        MessageMetrics metrics = new MessageMetrics(12, 2, 1, 2, 1,
                Collections.singletonList("null"), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(19);
        assertNotNull(violation, "Violation should be recorded for return null comment");
        assertEquals("use two+ meaningful words about why returning null is required",
                violation.args()[5], "Return-null comment guidance should match");
    }

    @Test
    @DisplayName("Log message uses log fix guidance")
    void logMessageUsesLogFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(23)
                .source(MessageSource.LOG)
                .message("retry")
                .build();
        MessageMetrics metrics = new MessageMetrics(5, 1, 1, 1, 1,
                Collections.singletonList("retry"), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(23);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("add unique words: action + subject + identifier or outcome",
                args[5], "Fix guidance should match log guidance");
    }

    @Test
    @DisplayName("Javadoc class uses class fix guidance")
    void javadocClassUsesClassFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(5)
                .source(MessageSource.JAVADOC_CLASS)
                .message("class exists")
                .build();
        MessageMetrics metrics = new MessageMetrics(12, 2, 1, 0, 1,
                Collections.singletonList("class"), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", null, false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(5);
        assertNotNull(violation, "Violation should be recorded for Javadoc class");
        Object[] args = violation.args();
        assertEquals("add unique words: responsibility + lifecycle or thread-safety intent",
                args[5], "Fix guidance should match Javadoc class guidance");
    }

    @Test
    @DisplayName("Javadoc member uses member fix guidance")
    void javadocMemberUsesMemberFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(7)
                .source(MessageSource.JAVADOC_MEMBER)
                .message("member exists")
                .build();
        MessageMetrics metrics = new MessageMetrics(12, 2, 1, 0, 1,
                Collections.singletonList("member"), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(7);
        assertNotNull(violation, "Violation should be recorded for Javadoc member");
        Object[] args = violation.args();
        assertEquals("add unique words: contract + units, edge cases, or side effects",
                args[5], "Fix guidance should match Javadoc member guidance");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "ASSERTION|add unique words: subject + expected behaviour, include key values",
            "PRECONDITION|add unique words: parameter + constraint + unit",
            "THROW|add unique words: operation + input/state + failure reason",
            "ANNOTATION|add unique words: scenario + expected outcome"
    })
    @DisplayName("Fix guidance uses source specific wording")
    void fixGuidanceUsesSourceSpecificWording(MessageSource source, String expected) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(9)
                .source(source)
                .message("short")
                .build();
        MessageMetrics metrics = new MessageMetrics(4, 1, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(9);
        assertNotNull(violation, "Violation should be recorded for short message");
        assertEquals(expected, violation.args()[5], "Fix guidance should match source");
    }

    @Test
    @DisplayName("Collect filler words reports filler list")
    void collectFillerWordsReportsFillerList() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(11)
                .source(MessageSource.ASSERTION)
                .message("the value is the same")
                .build();
        MessageMetrics metrics = new MessageMetrics(20, 5, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(11);
        assertNotNull(violation, "Violation should be recorded for filler words");
        assertEquals("the, value, is, the", violation.args()[2],
                "Filler words should be listed in order");
    }

    @Test
    @DisplayName("Comment fix guidance uses runtime call wording")
    void commentFixGuidanceUsesRuntimeCallWording() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(12)
                .source(MessageSource.COMMENT)
                .message("runtime")
                .missingMessageKind(MissingMessageKind.RUNTIME_CALL)
                .build();
        MessageMetrics metrics = new MessageMetrics(5, 1, 0, 0, 0,
                Collections.emptyList(), Collections.emptyList());
        MessageContext context = new MessageContext(candidate, metrics,
                "TestClass", "testMethod", false, null, null, null);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(12);
        assertNotNull(violation, "Violation should be recorded for runtime comment");
        assertEquals("use two+ meaningful words about why java.lang.Runtime is required",
                violation.args()[5], "Runtime comment guidance should match");
    }
}
