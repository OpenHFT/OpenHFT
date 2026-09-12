/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AdviceCandidateEmitter} to improve branch and mutation coverage.
 */
@DisplayName("Advice candidate emitter tests")
class AdviceCandidateEmitterTest {

    @Test
    @DisplayName("Constructor with null fileContents uses unknown as filename")
    void constructor_nullFileContentsUsesUnknown() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        assertNotNull(emitter, "Emitter should be created with null fileContents");
    }

    @Test
    @DisplayName("record with null collector returns early")
    void record_nullCollectorReturnsEarly() {
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(null, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("test message")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        assertDoesNotThrow(() -> emitter.record(context, RuleId.MISSING_MESSAGE),
                "record with null collector should return early");
    }

    @Test
    @DisplayName("record with null context returns early")
    void record_nullContextReturnsEarly() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        assertDoesNotThrow(() -> emitter.record(null, RuleId.MISSING_MESSAGE),
                "record with null context should return early");
    }

    @Test
    @DisplayName("record with null ruleId returns early")
    void record_nullRuleIdReturnsEarly() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("test message")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        assertDoesNotThrow(() -> emitter.record(context, null),
                "record with null ruleId should return early");
    }

    @Test
    @DisplayName("record with null candidate in context returns early")
    void record_nullCandidateInContextReturnsEarly() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageContext context = new MessageContext(null, null, "TestClass", "testMethod",
                false, null, null, null);
        assertDoesNotThrow(() -> emitter.record(context, RuleId.MISSING_MESSAGE),
                "record with null candidate should return early");
    }

    @Test
    @DisplayName("record with suppressed RuleId returns early")
    void record_suppressedRuleIdReturnsEarly() {
        AdviceCollector collector = new AdviceCollector();
        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMMissingMessage");
        tracker.pushScopeForTest(scope);

        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, tracker, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .missingMessage(true)
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, tracker, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        // Collector should be empty because the rule was suppressed
        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(0, candidates.size(), "No candidates should be recorded when suppressed");
    }

    @Test
    @DisplayName("record with suppressed AdviceId returns early")
    void record_suppressedAdviceIdReturnsEarly() {
        AdviceCollector collector = new AdviceCollector();
        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMAssertionMessageMissing");
        tracker.pushScopeForTest(scope);

        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, tracker, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .missingMessage(true)
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, tracker, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        // Collector should be empty because the advice was suppressed
        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(0, candidates.size(), "No candidates should be recorded when advice suppressed");
    }

    @Test
    @DisplayName("record with valid inputs records candidate")
    void record_validInputsRecordsCandidate() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("test message")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(1, candidates.size(), "One line should have candidates");
        assertEquals(1, candidates.get(10).size(), "One candidate should be recorded on line 10");
        assertEquals(AdviceId.MMAssertionMessageMissing, candidates.get(10).get(0).adviceId(),
                "AdviceId should match");
    }

    @Test
    @DisplayName("record uses explicit adviceSource from candidate")
    void record_usesExplicitAdviceSource() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ANNOTATION)
                .adviceSource(AdviceSource.ANNOTATION_DISPLAY_NAME)
                .lineNo(10)
                .message("test display name")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.CONTEXTLESS);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(1, candidates.get(10).size(), "One candidate should be recorded");
        assertEquals(AdviceSource.ANNOTATION_DISPLAY_NAME, candidates.get(10).get(0).source(),
                "Source should use explicit adviceSource");
    }

    @Test
    @DisplayName("record maps MessageSource to AdviceSource when no explicit source")
    void record_mapsMessageSourceToAdviceSource() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(10)
                .message("error message")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(AdviceSource.THROW, candidates.get(10).get(0).source(),
                "Source should be mapped from MessageSource.THROW");
    }

    @Test
    @DisplayName("record throws for ANNOTATION source without explicit adviceSource")
    void record_throwsForAnnotationWithoutExplicitSource() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ANNOTATION)
                .lineNo(10)
                .message("annotation message")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);

        assertThrows(IllegalStateException.class, () -> emitter.record(context, RuleId.MISSING_MESSAGE),
                "ANNOTATION source without explicit adviceSource should throw");
    }

    @Test
    @DisplayName("buildMetrics handles null MessageMetrics")
    void buildMetrics_handlesNullMessageMetrics() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("test")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertNotNull(metrics, "Metrics should not be null");
        assertEquals(0, metrics.wordCount(), "wordCount should be 0 when MessageMetrics is null");
    }

    @Test
    @DisplayName("buildMetrics populates from MessageMetrics")
    void buildMetrics_populatesFromMessageMetrics() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageMetrics msgMetrics = new MessageMetrics(50, 5, 3, 7, 4, Collections.emptyList());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("test message with content")
                .placeholderCount(2)
                .keyValueLabelCount(1)
                .build();
        MessageContext context = new MessageContext(candidate, msgMetrics, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(5, metrics.wordCount(), "wordCount from MessageMetrics");
        assertEquals(3, metrics.meaningfulWordCount(), "meaningfulWordCount from MessageMetrics");
        assertEquals(7, metrics.totalWordCount(), "totalWordCount from MessageMetrics");
        assertEquals(4, metrics.effectiveMeaningfulWordCount(), "effectiveMeaningfulWordCount from MessageMetrics");
        assertEquals(2, metrics.placeholderCount(), "placeholderCount from candidate");
        assertEquals(1, metrics.keyValueLabelCount(), "keyValueLabelCount from candidate");
    }

    @Test
    @DisplayName("buildMetrics sets minWordCount for TOO_SHORT rule")
    void buildMetrics_setsMinWordCountForTooShort() {
        AdviceCollector collector = new AdviceCollector();
        MessageMetricsCalculator calculator = new MessageMetricsCalculator();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(calculator);
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageMetrics msgMetrics = new MessageMetrics(10, 2, 1, 2, 1, Collections.emptyList());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("hi")
                .build();
        MessageContext context = new MessageContext(candidate, msgMetrics, "TestClass", "testMethod",
                false, ruleSupport, null, null);
        emitter.record(context, RuleId.TOO_SHORT);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(Integer.valueOf(4), metrics.minWordCount(),
                "minWordCount should be set from MessageSource.ASSERTION (4)");
        assertEquals(Integer.valueOf(42), metrics.maxWordCount(),
                "maxWordCount should be set from calculator");
    }

    @Test
    @DisplayName("buildMetrics sets maxWordCount for TOO_LONG rule")
    void buildMetrics_setsMaxWordCountForTooLong() {
        AdviceCollector collector = new AdviceCollector();
        MessageMetricsCalculator calculator = new MessageMetricsCalculator();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(calculator);
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageMetrics msgMetrics = new MessageMetrics(200, 50, 30, 50, 30, Collections.emptyList());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("very long message")
                .build();
        MessageContext context = new MessageContext(candidate, msgMetrics, "TestClass", "testMethod",
                false, ruleSupport, null, null);
        emitter.record(context, RuleId.TOO_LONG);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(Integer.valueOf(42), metrics.maxWordCount(),
                "maxWordCount should be set from calculator for TOO_LONG");
    }

    @Test
    @DisplayName("buildMetrics sets minMeaningfulWordCount for TOO_FEW_MEANINGFUL rule")
    void buildMetrics_setsMinMeaningfulWordCountForTooFewMeaningful() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageMetrics msgMetrics = new MessageMetrics(10, 3, 0, 3, 0, Collections.emptyList());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("the a is")
                .build();
        MessageContext context = new MessageContext(candidate, msgMetrics, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.TOO_FEW_MEANINGFUL);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(Integer.valueOf(2), metrics.minMeaningfulWordCount(),
                "minMeaningfulWordCount should be set from MessageSource.ASSERTION (2)");
    }

    @Test
    @DisplayName("buildMetrics sets maxWordLength for LONG_WORD rule")
    void buildMetrics_setsMaxWordLengthForLongWord() {
        AdviceCollector collector = new AdviceCollector();
        MessageMetricsCalculator calculator = new MessageMetricsCalculator();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(calculator);
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageMetrics msgMetrics = new MessageMetrics(100, 5, 3, 5, 3, Collections.emptyList());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("contains superlongidentifiername here")
                .build();
        MessageContext context = new MessageContext(candidate, msgMetrics, "TestClass", "testMethod",
                false, ruleSupport, null, null);
        emitter.record(context, RuleId.LONG_WORD);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(Integer.valueOf(42), metrics.maxWordLength(),
                "maxWordLength should be set from calculator for LONG_WORD");
    }

    @Test
    @DisplayName("buildMetrics includes comparison values from candidate")
    void buildMetrics_includesComparisonValues() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("comparison")
                .comparisonOperator(">=")
                .comparisonLeftOperand("actual")
                .comparisonRightOperand("expected")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals(">=", metrics.comparisonOperator(), "comparisonOperator from candidate");
        assertEquals("actual", metrics.comparisonLeftOperand(), "comparisonLeftOperand from candidate");
        assertEquals("expected", metrics.comparisonRightOperand(), "comparisonRightOperand from candidate");
    }

    @Test
    @DisplayName("buildMetrics includes string search values from candidate")
    void buildMetrics_includesStringSearchValues() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("search")
                .stringSearchMethod("contains")
                .stringSearchTarget("str")
                .stringSearchArg("needle")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        AdviceMetrics metrics = advice.metrics();
        assertEquals("contains", metrics.stringSearchMethod(), "stringSearchMethod from candidate");
        assertEquals("str", metrics.stringSearchTarget(), "stringSearchTarget from candidate");
        assertEquals("needle", metrics.stringSearchArg(), "stringSearchArg from candidate");
    }

    @Test
    @DisplayName("record preserves message literal and expression")
    void record_preservesMessageLiteralAndExpression() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("the literal message")
                .messageExpr("\"the literal message\"")
                .build();
        MessageContext context = new MessageContext(candidate, null, "TestClass", "testMethod",
                false, null, null, null);
        emitter.record(context, RuleId.MISSING_MESSAGE);

        CandidateAdvice advice = collector.candidatesForFile("unknown").get(10).get(0);
        assertEquals("the literal message", advice.messageLiteral(), "messageLiteral should be preserved");
        assertEquals("\"the literal message\"", advice.messageExpr(), "messageExpr should be preserved");
    }

    @Test
    @DisplayName("Multiple rules on different lines creates separate candidates")
    void multipleRulesOnDifferentLines_createsSeparateCandidates() {
        AdviceCollector collector = new AdviceCollector();
        AdviceCandidateEmitter emitter = new AdviceCandidateEmitter(collector, null, null);

        MessageCandidate candidate1 = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(10)
                .message("first")
                .build();
        MessageContext context1 = new MessageContext(candidate1, null, "TestClass", "test1",
                false, null, null, null);
        emitter.record(context1, RuleId.MISSING_MESSAGE);

        MessageCandidate candidate2 = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(20)
                .message("second")
                .build();
        MessageContext context2 = new MessageContext(candidate2, null, "TestClass", "test2",
                false, null, null, null);
        emitter.record(context2, RuleId.MISSING_MESSAGE);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertEquals(2, candidates.size(), "Two lines should have candidates");
        assertEquals(1, candidates.get(10).size(), "Line 10 should have one candidate");
        assertEquals(1, candidates.get(20).size(), "Line 20 should have one candidate");
    }
}
