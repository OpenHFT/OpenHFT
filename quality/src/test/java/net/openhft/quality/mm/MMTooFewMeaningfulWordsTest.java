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
                "TestClass", "testMethod", false, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(17);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("use two+ meaningful words about why java.lang.System is required",
                args[5], "Fix guidance should match System comment guidance");
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
                "TestClass", "testMethod", false, null, null);

        rule.evaluate(context, collector, state);

        Map<Integer, Violation> pending = collector.pendingForTesting();
        Violation violation = pending.get(23);
        assertNotNull(violation, "Violation should be recorded for the candidate line");
        Object[] args = violation.args();
        assertEquals("add unique words: action + subject + identifier or outcome",
                args[5], "Fix guidance should match log guidance");
    }
}
