/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MM missing string search value tests")
class MMMissingStringSearchValueTest {

    private MMMissingStringSearchValue rule;
    private ViolationCollector collector;
    private RuleEvaluationState state;

    @BeforeEach
    void setUp() {
        rule = new MMMissingStringSearchValue();
        collector = new ViolationCollector(null);
        state = new RuleEvaluationState();
    }

    @Test
    @DisplayName("Evaluate skips when message not constant")
    void evaluateSkipsWhenMessageNotConstant() {
        MessageCandidate candidate = baseCandidate()
                .constantMessage(false)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when search method missing")
    void evaluateSkipsWhenSearchMethodMissing() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchMethod(null)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when message null scenario")
    void evaluateSkipsWhenMessageNull() {
        MessageCandidate candidate = baseCandidate()
                .message(null)
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when message contains search literal")
    void evaluateSkipsWhenMessageContainsSearchLiteral() {
        MessageCandidate candidate = baseCandidate()
                .message("value should contain abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when literal is single character")
    void evaluateRecordsWhenLiteralIsSingleCharacter() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"x\"")
                .message("value should contain x")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when literal not quoted")
    void evaluateRecordsWhenLiteralNotQuoted() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("abc")
                .message("value should contain abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when alnum literal appears only inside larger word")
    void evaluateRecordsWhenLiteralAppearsInsideLargerWordOnly() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"id\"")
                .message("identifier should be present")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate skips when alnum literal appears as standalone token")
    void evaluateSkipsWhenLiteralAppearsAsStandaloneToken() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"id\"")
                .message("missing value for id")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when alnum literal has only adjacent matches")
    void evaluateRecordsWhenLiteralHasOnlyAdjacentMatches() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"id\"")
                .message("idid")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when quoted literal is empty")
    void evaluateRecordsWhenQuotedLiteralIsEmpty() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"\"")
                .message("value should include anything")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when search argument is null")
    void evaluateRecordsWhenSearchArgumentIsNull() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg(null)
                .message("value should include abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate records when quoted literal is malformed")
    void evaluateRecordsWhenQuotedLiteralIsMalformed() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"")
                .message("value should include quote")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate skips when non-alnum literal with punctuation is present in message")
    void evaluateSkipsWhenNonAlnumLiteralWithPunctuationIsPresent() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\".txt\"")
                .message("path should end with .txt")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when non-alnum literal with punctuation is absent from message")
    void evaluateRecordsWhenNonAlnumLiteralWithPunctuationIsAbsent() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\".txt\"")
                .message("path should have correct extension")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate skips when punctuation-only literal like @ is present in message")
    void evaluateSkipsWhenPunctuationOnlyLiteralIsPresent() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"@\"")
                .message("email should contain @")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when underscore literal appears as standalone token")
    void evaluateSkipsWhenUnderscoreLiteralAppearsAsStandaloneToken() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"foo_bar\"")
                .message("value should contain foo_bar here")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate records when underscore literal is embedded in larger token")
    void evaluateRecordsWhenUnderscoreLiteralIsEmbeddedInLargerToken() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"foo_bar\"")
                .message("prefix_foo_bar_suffix detected")
                .build();

        rule.evaluate(context(candidate), collector, state);

        Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertNotNull(violation);
        assertEquals(RuleId.MISSING_STRING_VALUE, violation.ruleId());
    }

    @Test
    @DisplayName("Evaluate skips when alnum literal appears at start of message")
    void evaluateSkipsWhenAlnumLiteralAppearsAtStartOfMessage() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"abc\"")
                .message("abc should be present")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    @Test
    @DisplayName("Evaluate skips when alnum literal appears at end of message")
    void evaluateSkipsWhenAlnumLiteralAppearsAtEndOfMessage() {
        MessageCandidate candidate = baseCandidate()
                .stringSearchArg("\"abc\"")
                .message("value should be abc")
                .build();

        rule.evaluate(context(candidate), collector, state);

        assertTrue(collector.pendingForTest().isEmpty());
    }

    private MessageCandidate.Builder baseCandidate() {
        return new MessageCandidate.Builder()
                .lineNo(10)
                .source(MessageSource.ASSERTION)
                .constantMessage(true)
                .stringSearchMethod("contains")
                .stringSearchTarget("value")
                .stringSearchArg("\"abc\"")
                .message("value should include abc");
    }

    private MessageContext context(MessageCandidate candidate) {
        return new MessageContext(candidate, null, "TestClass", "testMethod", false, null, null, null);
    }
}
