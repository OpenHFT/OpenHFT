/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MMMissingMessage}.
 */
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
    void ruleIdIsMissingMessage() {
        assertEquals(RuleId.MISSING_MESSAGE, rule.ruleId(), "ruleId should be MISSING_MESSAGE");
    }

    @Test
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
