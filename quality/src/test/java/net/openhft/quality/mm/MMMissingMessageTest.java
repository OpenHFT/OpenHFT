/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
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

    private MessageContext createContext(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null);
    }
}
