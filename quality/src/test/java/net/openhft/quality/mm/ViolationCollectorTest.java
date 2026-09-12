/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Violation collector tests scenario case")
class ViolationCollectorTest {

    @Test
    @DisplayName("Flush skips null entries without logging")
    void flushSkipsNullEntriesWithoutLogging() throws Exception {
        ViolationCollector collector = new ViolationCollector(null);
        collector.putPendingForTest(5, null);
        collector.putPendingForTest(6, new Violation(6, RuleId.MISSING_MESSAGE, new Object[]{}));

        TestCheck check = new TestCheck();
        check.configure(new com.puppycrawl.tools.checkstyle.DefaultConfiguration("TestCheck"));
        collector.flush(check);

        assertEquals(1, check.getViolations().size(),
                "flush should log only non-null violations");
    }

    @Test
    @DisplayName("Flush emits all violations from unsorted input")
    void flushEmitsAllViolationsFromUnsortedInput() throws Exception {
        ViolationCollector collector = new ViolationCollector(null);
        collector.putPendingForTest(30, new Violation(30, RuleId.MISSING_MESSAGE, new Object[]{}));
        collector.putPendingForTest(10, new Violation(10, RuleId.TOO_SHORT, new Object[]{}));
        collector.putPendingForTest(20, new Violation(20, RuleId.GENERIC, new Object[]{}));

        TestCheck check = new TestCheck();
        check.configure(new com.puppycrawl.tools.checkstyle.DefaultConfiguration("TestCheck"));
        collector.flush(check);

        assertEquals(3, check.getViolations().size(), "flush should log all violations");
        // Verify violations are in sorted order in the result set
        List<com.puppycrawl.tools.checkstyle.api.Violation> violations = 
                new ArrayList<>(check.getViolations());
        assertEquals(10, violations.get(0).getLineNo(), "first should be line 10");
        assertEquals(20, violations.get(1).getLineNo(), "second should be line 20");
        assertEquals(30, violations.get(2).getLineNo(), "third should be line 30");
    }

    @Test
    @DisplayName("Priority comparison uses order when lengths match")
    void priorityComparisonUsesOrderWhenLengthsMatch() {
        assertTrue(ViolationCollector.isHigherPriority(1, "AA", 1, 1, "BB", 2),
                "lower order should win when priority and code length match");
        assertFalse(ViolationCollector.isHigherPriority(1, "AA", 3, 1, "BB", 2),
                "higher order should not win when priority and code length match");
    }

    @Test
    @DisplayName("Priority comparison handles equal priority case")
    void priorityComparisonHandlesEqualPriorityCase() {
        assertFalse(ViolationCollector.isHigherPriority(5, "ABC", 1, 5, "DEF", 1),
                "equal priority with equal length and order should not be higher priority");
        assertTrue(ViolationCollector.isHigherPriority(4, "ABC", 1, 5, "DEF", 1),
                "lower priority value should be higher priority");
        assertFalse(ViolationCollector.isHigherPriority(6, "ABC", 1, 5, "DEF", 1),
                "higher priority value should not be higher priority");
    }

    @Test
    @DisplayName("Priority comparison handles equal code length case")
    void priorityComparisonHandlesEqualCodeLengthCase() {
        assertFalse(ViolationCollector.isHigherPriority(3, "ABC", 5, 3, "DEF", 5),
                "equal priority with equal length and equal order should not be higher priority");
        assertTrue(ViolationCollector.isHigherPriority(3, "AB", 1, 3, "DEF", 1),
                "shorter code should be higher priority when priority equal");
        assertFalse(ViolationCollector.isHigherPriority(3, "ABCD", 1, 3, "DEF", 1),
                "longer code should not be higher priority when priority equal");
    }

    @Test
    @DisplayName("Priority comparison handles equal order case")
    void priorityComparisonHandlesEqualOrderCase() {
        assertFalse(ViolationCollector.isHigherPriority(2, "AB", 7, 2, "CD", 7),
                "equal priority with equal length and equal order should not be higher priority");
        assertTrue(ViolationCollector.isHigherPriority(2, "AB", 6, 2, "CD", 7),
                "lower order should be higher priority when priority and length equal");
        assertFalse(ViolationCollector.isHigherPriority(2, "AB", 8, 2, "CD", 7),
                "higher order should not be higher priority when priority and length equal");
    }

    @Test
    @DisplayName("Higher priority returns false for strictly lower priority candidates")
    void higherPriorityReturnsFalseForLowerPriorityCandidates() {
        assertFalse(ViolationCollector.isHigherPriority(10, "XXXX", 99, 1, "A", 1),
                "higher priority value, longer code, and higher order is clearly not higher priority");
    }

    @Test
    @DisplayName("Record replaces existing violation when candidate has higher priority")
    void recordReplacesExistingViolationWhenCandidateHasHigherPriority() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.TOO_LONG);
        collector.record(10, RuleId.MISSING_MESSAGE);

        assertEquals(1, collector.pendingForTest().size(),
                "only one violation should remain for the line");
        assertEquals(RuleId.MISSING_MESSAGE, collector.pendingForTest().get(10).ruleId(),
                "higher priority violation should replace existing");
    }

    @Test
    @DisplayName("Record keeps existing violation when candidate has lower priority")
    void recordKeepsExistingViolationWhenCandidateHasLowerPriority() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.MISSING_MESSAGE);
        collector.record(10, RuleId.TOO_LONG);

        assertEquals(1, collector.pendingForTest().size(),
                "only one violation should remain for the line");
        assertEquals(RuleId.MISSING_MESSAGE, collector.pendingForTest().get(10).ruleId(),
                "existing higher priority violation should be kept");
    }

    @Test
    @DisplayName("Summary counts returns empty map for empty collector")
    void summaryCountsReturnsEmptyMapForEmptyCollector() {
        ViolationCollector collector = new ViolationCollector(null);
        assertTrue(collector.summaryCounts().isEmpty(),
                "empty collector should return empty summary");
    }

    @Test
    @DisplayName("Summary counts aggregates by rule id")
    void summaryCountsAggregatesByRuleId() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.MISSING_MESSAGE);
        collector.record(20, RuleId.MISSING_MESSAGE);
        collector.record(30, RuleId.TOO_SHORT);

        java.util.Map<RuleId, Integer> summary = collector.summaryCounts();
        assertEquals(Integer.valueOf(2), summary.get(RuleId.MISSING_MESSAGE),
                "should count two MISSING_MESSAGE violations");
        assertEquals(Integer.valueOf(1), summary.get(RuleId.TOO_SHORT),
                "should count one TOO_SHORT violation");
    }

    @Test
    @DisplayName("Has violations returns false when empty")
    void hasViolationsReturnsFalseWhenEmpty() {
        ViolationCollector collector = new ViolationCollector(null);
        assertFalse(collector.hasViolations(), "empty collector has no violations");
    }

    @Test
    @DisplayName("Has violations returns true when not empty")
    void hasViolationsReturnsTrueWhenNotEmpty() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.MISSING_MESSAGE);
        assertTrue(collector.hasViolations(), "non-empty collector has violations");
    }

    @Test
    @DisplayName("Clear removes all pending violations")
    void clearRemovesAllPendingViolations() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.MISSING_MESSAGE);
        collector.record(20, RuleId.TOO_SHORT);

        collector.clear();

        assertFalse(collector.hasViolations(), "clear should remove all violations");
    }

    @Test
    @DisplayName("Flush skips empty collector")
    void flushSkipsEmptyCollector() throws Exception {
        ViolationCollector collector = new ViolationCollector(null);
        TestCheck check = new TestCheck();
        check.configure(new com.puppycrawl.tools.checkstyle.DefaultConfiguration("TestCheck"));
        
        collector.flush(check);

        assertTrue(check.getViolations().isEmpty(),
                "empty collector should not log any violations");
    }

    // Additional tests to kill surviving mutations

    @Test
    @DisplayName("Priority comparison returns false when code lengths are equal")
    void priorityComparisonReturnsFalseWhenCodeLengthsAreEqual() {
        // When priorities are equal and lengths are equal, we proceed to order comparison
        // Test boundary: candidateLength < existingLength when both are equal should return false
        assertFalse(ViolationCollector.isHigherPriority(1, "ABC", 5, 1, "DEF", 5),
                "equal priority and equal length should proceed to order, equal order returns false");
    }

    @Test
    @DisplayName("Priority comparison returns false when orders are equal")
    void priorityComparisonReturnsFalseWhenOrdersAreEqual() {
        // When priorities, lengths, and orders are all equal, should return false
        assertFalse(ViolationCollector.isHigherPriority(2, "XY", 10, 2, "AB", 10),
                "identical priority, length, and order should return false");
    }

    @Test
    @DisplayName("Priority comparison boundary at code length equality")
    void priorityComparisonBoundaryAtCodeLengthEquality() {
        // Test the boundary: when lengths differ by 1
        assertTrue(ViolationCollector.isHigherPriority(1, "AB", 1, 1, "ABC", 1),
                "shorter code (2 chars) beats longer code (3 chars)");
        assertFalse(ViolationCollector.isHigherPriority(1, "ABC", 1, 1, "AB", 1),
                "longer code (3 chars) does not beat shorter code (2 chars)");
    }

    @Test
    @DisplayName("Priority comparison boundary at order equality")
    void priorityComparisonBoundaryAtOrderEquality() {
        // Test the boundary: when orders differ by 1
        assertTrue(ViolationCollector.isHigherPriority(1, "AB", 5, 1, "CD", 6),
                "lower order (5) beats higher order (6)");
        assertFalse(ViolationCollector.isHigherPriority(1, "AB", 6, 1, "CD", 5),
                "higher order (6) does not beat lower order (5)");
    }

    @Test
    @DisplayName("Record returns false when violation is suppressed by tracker")
    void recordReturnsFalseWhenSuppressedByTracker() {
        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTest(scope);

        ViolationCollector collector = new ViolationCollector(tracker);
        boolean result = collector.record(10, RuleId.TOO_SHORT);

        assertFalse(result, "record should return false when violation is suppressed");
        assertFalse(collector.hasViolations(),
                "suppressed violation should not be stored");
    }

    @Test
    @DisplayName("Record returns true and stores violation when not suppressed by tracker")
    void recordReturnsTrueWhenNotSuppressedByTracker() {
        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTest(scope);

        ViolationCollector collector = new ViolationCollector(tracker);
        boolean result = collector.record(10, RuleId.MISSING_MESSAGE);

        assertTrue(result, "record should return true when not suppressed");
        assertTrue(collector.hasViolations(),
                "non-suppressed violation should be stored");
    }

    @Test
    @DisplayName("Record with duplicate line and equal priority keeps first violation")
    void recordWithDuplicateLineAndEqualPriorityKeepsFirst() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(10, RuleId.TOO_SHORT);

        assertEquals(1, collector.pendingForTest().size(),
                "should keep only one violation per line");
    }

    @Test
    @DisplayName("Record replaces violation with negative priority candidate")
    void recordReplacesViolationWithNegativePriorityCandidate() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.MISSING_MESSAGE, "first");
        collector.record(10, RuleId.THROW_NULL, "second");

        java.util.Map<Integer, Violation> pending = collector.pendingForTest();
        assertEquals(1, pending.size(), "should have one violation for line 10");
    }

    @Test
    @DisplayName("Record keeps higher priority violation when lower priority candidate arrives")
    void recordKeepsHigherPriorityViolationWhenLowerPriorityCandidateArrives() {
        ViolationCollector collector = new ViolationCollector(null);
        collector.record(10, RuleId.THROW_NULL, "higher priority first");
        collector.record(10, RuleId.TOO_SHORT, "lower priority second");

        java.util.Map<Integer, Violation> pending = collector.pendingForTest();
        Violation violation = pending.get(10);
        assertEquals(RuleId.THROW_NULL, violation.ruleId(),
                "higher priority THROW_NULL should be kept over TOO_SHORT");
    }

    @Test
    @DisplayName("Same priority violations use code length as tiebreaker")
    void samePriorityViolationsUseCodeLengthAsTiebreaker() {
        assertTrue(ViolationCollector.isHigherPriority(1, "AB", 1, 1, "ABC", 1),
                "shorter code should be higher priority when priority is equal");
        assertFalse(ViolationCollector.isHigherPriority(1, "ABC", 1, 1, "AB", 1),
                "longer code should not be higher priority when priority is equal");
    }

    @Test
    @DisplayName("Same priority and code length violations use order as tiebreaker")
    void samePriorityAndCodeLengthViolationsUseOrderAsTiebreaker() {
        assertTrue(ViolationCollector.isHigherPriority(1, "AB", 1, 1, "AB", 2),
                "lower order should be higher priority");
        assertFalse(ViolationCollector.isHigherPriority(1, "AB", 2, 1, "AB", 1),
                "higher order should not be higher priority");
        assertFalse(ViolationCollector.isHigherPriority(1, "AB", 1, 1, "AB", 1),
                "equal order should not be higher priority");
    }

    private static final class TestCheck extends AbstractCheck {

        @Override
        public int[] getDefaultTokens() {
            return new int[0];
        }

        @Override
        public int[] getAcceptableTokens() {
            return new int[0];
        }

        @Override
        public int[] getRequiredTokens() {
            return new int[0];
        }
    }
}
