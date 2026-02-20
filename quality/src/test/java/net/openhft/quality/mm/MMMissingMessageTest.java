/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MMMissingMessage}.
 */
@DisplayName("MM missing message tests scenario case")
class MMMissingMessageTest {

    private MMMissingMessage rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMMissingMessage();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Rule id is missing message scenario")
    void ruleIdIsMissingMessage() {
        assertEquals(RuleId.MISSING_MESSAGE, rule.ruleId(), "ruleId should be MISSING_MESSAGE");
    }

    @Test
    @DisplayName("Evaluate does nothing when message present scenario")
    void evaluateDoesNothingWhenMessagePresent() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message("valid message")
                .missingMessage(false)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertFalse(state.shouldStopProcessing(), "should not stop when message present");
    }

    @Test
    @DisplayName("Evaluate records and stops when message missing")
    void evaluateRecordsAndStopsWhenMessageMissing() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(), "should stop when message missing");
    }

    @Test
    @DisplayName("Evaluate skips non applicable source scenario")
    void evaluateSkipsNonApplicableSource() {
        // MISSING_MESSAGE applies to ASSERTION, THROW, ANNOTATION, LOG
        // Create a source that is NOT in this list
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.JAVADOC_CLASS)  // Not applicable to MISSING_MESSAGE
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertFalse(state.shouldStopProcessing(),
                "should not stop for non-applicable source");
    }

    @Test
    @DisplayName("Evaluate applies to throw source scenario")
    void evaluateAppliesToThrowSource() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(15)
                .source(MessageSource.THROW)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(), "should stop for THROW source");
    }

    @Test
    @DisplayName("Evaluate applies to annotation source scenario")
    void evaluateAppliesToAnnotationSource() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(20)
                .source(MessageSource.ANNOTATION)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(), "should stop for ANNOTATION source");
    }

    @Test
    @DisplayName("Evaluate applies to log source scenario")
    void evaluateAppliesToLogSource() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(25)
                .source(MessageSource.LOG)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertTrue(state.shouldStopProcessing(), "should stop for LOG source");
    }

    @Test
    @DisplayName("Evaluate does not stop for javadoc member")
    void evaluateDoesNotStopForJavadocMember() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(30)
                .source(MessageSource.JAVADOC_MEMBER)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertFalse(state.shouldStopProcessing(),
                "should not stop for JAVADOC_MEMBER source");
    }

    @Test
    @DisplayName("Evaluate does not stop for precondition scenario")
    void evaluateDoesNotStopForPrecondition() {
        // MISSING_MESSAGE does NOT apply to PRECONDITION according to RuleId
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(35)
                .source(MessageSource.PRECONDITION)
                .message(null)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        assertFalse(state.shouldStopProcessing(),
                "should not stop for PRECONDITION source");
    }

    @Test
    @DisplayName("Comment missing message uses return null fix guidance")
    void commentMissingMessageUsesReturnNullFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(40)
                .source(MessageSource.COMMENT)
                .missingMessage(true)
                .missingMessageKind(MissingMessageKind.RETURN_NULL)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(40);
        assertNotNull(violation, "Violation should be recorded for missing comment");
        assertEquals("add a single-line comment on the line before explaining why returning null is required",
                violation.args()[0], "Return-null fix guidance should match");
    }

    @Test
    @DisplayName("Comment missing message uses system call fix guidance")
    void commentMissingMessageUsesSystemCallFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(41)
                .source(MessageSource.COMMENT)
                .missingMessage(true)
                .missingMessageKind(MissingMessageKind.SYSTEM_CALL)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(41);
        assertNotNull(violation, "Violation should be recorded for missing comment");
        assertEquals("add a single-line comment on the line before explaining why java.lang.System is required here",
                violation.args()[0], "System call fix guidance should match");
    }

    @Test
    @DisplayName("Comment missing message uses runtime call fix guidance")
    void commentMissingMessageUsesRuntimeCallFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(42)
                .source(MessageSource.COMMENT)
                .missingMessage(true)
                .missingMessageKind(MissingMessageKind.RUNTIME_CALL)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(42);
        assertNotNull(violation, "Violation should be recorded for missing comment");
        assertEquals("add a single-line comment on the line before explaining why java.lang.Runtime is required here",
                violation.args()[0], "Runtime call fix guidance should match");
    }

    @Test
    @DisplayName("Non comment missing message uses default fix guidance")
    void nonCommentMissingMessageUsesDefaultFixGuidance() {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .lineNo(43)
                .source(MessageSource.ASSERTION)
                .missingMessage(true)
                .build();
        MessageContext context = createContext(candidate);

        rule.evaluate(context, collector, state);

        Violation violation = collector.pendingForTesting().get(43);
        assertNotNull(violation, "Violation should be recorded for missing message");
        assertEquals("add a meaningful message, supply a Throwable, or add a single-line comment on the line before when a message must be omitted",
                violation.args()[0], "Default fix guidance should match");
    }

    private MessageContext createContext(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
    }
}
