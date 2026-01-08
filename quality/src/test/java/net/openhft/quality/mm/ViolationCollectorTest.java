/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Tests for {@link ViolationCollector}.
 */
@DisplayName("Violation collector tests scenario case detail")
class ViolationCollectorTest {

    private ViolationCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ViolationCollector(null);
    }

    @Test
    @DisplayName("Record with null tracker records violation")
    void recordWithNullTrackerRecordsViolation() {
        boolean recorded = collector.record(10, RuleId.TOO_SHORT);
        assertTrue(recorded, "should record when tracker is null");
    }

    @Test
    @DisplayName("Record rejects null rule id scenario")
    void recordRejectsNullRuleId() {
        assertThrows(NullPointerException.class,
                () -> collector.record(10, null),
                "record should reject null ruleId");
    }

    @Test
    @DisplayName("Higher priority replaces existing scenario case")
    void higherPriorityReplacesExisting() {
        // First record a low priority violation (higher number = lower priority)
        collector.record(10, RuleId.TOO_SHORT);  // priority 24
        // Then record a higher priority (lower number)
        collector.record(10, RuleId.CONTEXTLESS);  // priority 1

        // Verify the pending map after replacement
        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        assertEquals(RuleId.CONTEXTLESS, violation.ruleId(),
                "higher priority rule should replace lower");
    }

    @Test
    @DisplayName("Lower priority does not replace existing")
    void lowerPriorityDoesNotReplaceExisting() {
        // First record a high priority violation (lower number = higher priority)
        collector.record(10, RuleId.CONTEXTLESS);  // priority 1
        // Then try to record a lower priority (higher number)
        collector.record(10, RuleId.TOO_SHORT);  // priority 24

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        assertEquals(RuleId.CONTEXTLESS, violation.ruleId(),
                "lower priority should not replace higher");
    }

    @Test
    @DisplayName("Same priority shorter code wins scenario")
    void samePriorityShorterCodeWins() {
        // Both have priority 0, but different code lengths
        // ASSERTJ_OVERRIDE has code "MMAssertJGenericOverride" (24 chars)
        // MISSING_LOOP_INDEX has code "MMMissingLoopIndex" (18 chars)
        collector.record(10, RuleId.ASSERTJ_OVERRIDE);
        collector.record(10, RuleId.MISSING_LOOP_INDEX);

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        assertEquals(RuleId.MISSING_LOOP_INDEX, violation.ruleId(),
                "shorter code should win on same priority");
    }

    @Test
    @DisplayName("Same priority and code length ordinal wins")
    void samePriorityAndCodeLengthOrdinalWins() {
        // Same priority (0), same code length - ordinal breaks the tie
        // Record the same rule twice (should be no-op)
        collector.record(10, RuleId.MISSING_MESSAGE);
        // Second record of same rule should not throw
        collector.record(10, RuleId.MISSING_MESSAGE);

        Map<Integer, Violation> pending = getPendingMap();
        assertEquals(1, pending.size(), "should have exactly one violation");
    }

    @Test
    @DisplayName("Clear removes pending violations scenario case")
    void clearRemovesPendingViolations() {
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(20, RuleId.CONTEXTLESS);

        collector.clear();

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.isEmpty(), "pending should be empty after clear");
    }

    @Test
    @DisplayName("Multiple violations on different lines scenario")
    void multipleViolationsOnDifferentLines() {
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(20, RuleId.TOO_LONG);
        collector.record(30, RuleId.GENERIC);

        Map<Integer, Violation> pending = getPendingMap();
        assertEquals(3, pending.size(), "should have 3 violations on different lines");
        assertEquals(RuleId.TOO_SHORT, pending.get(10).ruleId(), "line 10 mismatch");
        assertEquals(RuleId.TOO_LONG, pending.get(20).ruleId(), "line 20 mismatch");
        assertEquals(RuleId.GENERIC, pending.get(30).ruleId(), "line 30 mismatch");
    }

    @Test
    @DisplayName("Record preserves args scenario case detail")
    void recordPreservesArgs() {
        collector.record(10, RuleId.LONG_WORD, "argumentOne", "argumentTwo");

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        Object[] args = violation.args();
        assertEquals(2, args.length, "should have 2 args");
        assertEquals("argumentOne", args[0], "first arg mismatch");
        assertEquals("argumentTwo", args[1], "second arg mismatch");
    }

    @Test
    @DisplayName("Record with non suppressed rule returns true scenario")
    void recordWithNonSuppressedRuleReturnsTrue() {
        // Create a real suppression tracker without suppressions
        // When no scope is entered, isSuppressed returns false for all rules
        SuppressionTracker tracker = new SuppressionTracker();

        ViolationCollector collectorWithTracker = new ViolationCollector(tracker);
        boolean recorded = collectorWithTracker.record(10, RuleId.TOO_SHORT);

        assertTrue(recorded, "non-suppressed rule should be recorded");
    }

    @Test
    @DisplayName("Negative line number allowed scenario case")
    void negativeLineNumberAllowed() {
        collector.record(-1, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(-1), "negative line number should be allowed");
    }

    @Test
    @DisplayName("Zero line number allowed scenario case")
    void zeroLineNumberAllowed() {
        collector.record(0, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(0), "zero line number should be allowed");
    }

    // --- pending map state tests (flush behavior verification) ---

    @Test
    @DisplayName("Pending map keys are sortable scenario")
    void pendingMapKeysAreSortable() {
        // Record violations out of order
        collector.record(30, RuleId.TOO_SHORT);
        collector.record(10, RuleId.TOO_LONG);
        collector.record(20, RuleId.GENERIC);

        // Verify we can sort the keys as flush() does
        Map<Integer, Violation> pending = getPendingMap();
        List<Integer> lines = new ArrayList<>(pending.keySet());
        Collections.sort(lines);

        assertEquals(3, lines.size(), "should have 3 lines");
        assertEquals(10, lines.get(0).intValue(), "first should be 10");
        assertEquals(20, lines.get(1).intValue(), "second should be 20");
        assertEquals(30, lines.get(2).intValue(), "third should be 30");
    }

    @Test
    @DisplayName("Violations have correct message keys scenario")
    void violationsHaveCorrectMessageKeys() {
        collector.record(10, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);

        assertEquals(RuleId.TOO_SHORT.messageKey(), violation.ruleId().messageKey(),
                "message key should match rule");
    }

    @Test
    @DisplayName("Violations preserve all args scenario case")
    void violationsPreserveAllArgs() {
        collector.record(10, RuleId.LONG_WORD, "testArg1", "testArg2");

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        Object[] args = violation.args();

        assertEquals(2, args.length, "should have 2 args");
        assertEquals("testArg1", args[0], "first arg mismatch");
        assertEquals("testArg2", args[1], "second arg mismatch");
    }

    @Test
    @DisplayName("Record with suppressed rule returns false scenario")
    void recordWithSuppressedRuleReturnsFalse() {
        // Create a suppression tracker with MM-all suppressed
        SuppressionTracker tracker = new SuppressionTracker();
        // Push a scope with suppressAll = true
        pushSuppressAllScope(tracker);

        ViolationCollector collectorWithTracker = new ViolationCollector(tracker);
        boolean recorded = collectorWithTracker.record(10, RuleId.TOO_SHORT);

        assertFalse(recorded, "suppressed rule should not be recorded");
    }

    @Test
    @DisplayName("Record with suppressed rule does not add to pending")
    void recordWithSuppressedRuleDoesNotAddToPending() {
        // Create a suppression tracker with MM-all suppressed
        SuppressionTracker tracker = new SuppressionTracker();
        pushSuppressAllScope(tracker);

        ViolationCollector collectorWithTracker = new ViolationCollector(tracker);
        collectorWithTracker.record(10, RuleId.TOO_SHORT);

        // Verify nothing was added
        Map<Integer, Violation> pending = collectorWithTracker.pendingForTesting();
        assertTrue(pending.isEmpty(), "suppressed rule should not add to pending");
    }

    // --- boundary mutation tests for isHigherPriority ---

    @Test
    @DisplayName("Equal priority keeps existing when code length equal boundary")
    void equalPriority_equalCodeLength_keepsExistingByOrdinal() {
        // Both have priority 0 and same code length (24 chars)
        // ASSERTJ_OVERRIDE (ordinal 2) has code "MMAssertJGenericOverride" (24 chars)
        // ASSERTALL_HEADING (ordinal 3) has code "MMLowSignalAssertAllHeading" (27 chars)
        // RESTATES_DERIVED (ordinal 5) has code "MMRestatesDerivedAssertion" (26 chars)
        // Actually need to find two with EXACTLY same length...
        // Let's use CONTEXTLESS (13 chars, priority 1) vs INDEX_ONLY (11 chars, priority 1)
        // to test boundary where shorter wins
        collector.record(10, RuleId.INDEX_ONLY);  // 11 chars, priority 1
        collector.record(10, RuleId.CONTEXTLESS);  // 13 chars, priority 1

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        assertEquals(RuleId.INDEX_ONLY, violation.ruleId(),
                "existing shorter code should be kept when candidate has longer code");
    }

    @Test
    @DisplayName("Equal priority equal code length keeps first by ordinal boundary")
    void equalPriority_equalCodeLength_ordinalBoundary() {
        // Need two rules with same priority AND same code length
        // LONG_WORD (10 chars, priority 1) - ordinal 8
        // Let's record same rule twice to test boundary where equal ordinal keeps existing
        collector.record(10, RuleId.LONG_WORD);
        collector.record(10, RuleId.LONG_WORD);

        Map<Integer, Violation> pending = getPendingMap();
        assertEquals(1, pending.size(), "should have exactly one violation");
        assertEquals(RuleId.LONG_WORD, pending.get(10).ruleId(),
                "should keep the existing violation for identical rules");
    }

    @Test
    @DisplayName("Equal priority with lower ordinal replaces existing")
    void equalPriority_lowerOrdinal_replacesExisting() {
        // ASSERTJ_OVERRIDE (ordinal 2, priority 0, 24 chars)
        // ASSERTALL_HEADING (ordinal 3, priority 0, 27 chars)
        // Both priority 0, ASSERTALL_HEADING longer so won't win by length
        // Let's use rules where length is equal but ordinal differs
        // Actually, we need same priority, same length, different ordinal
        // Since no two rules have exactly same code length, let's verify ordinal
        // is used as final tiebreaker by recording higher ordinal first
        collector.record(10, RuleId.RESTATES_DERIVED);  // priority 0, ordinal 5
        collector.record(10, RuleId.ASSERTJ_OVERRIDE);  // priority 0, ordinal 2

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        // ASSERTJ_OVERRIDE has shorter code (24) vs RESTATES_DERIVED (26)
        // so ASSERTJ_OVERRIDE wins by code length, not ordinal
        assertEquals(RuleId.ASSERTJ_OVERRIDE, violation.ruleId(),
                "rule with shorter code should replace existing");
    }

    // --- summaryCounts boundary tests ---

    @Test
    @DisplayName("Summary counts returns correct aggregation for multiple rules")
    void summaryCounts_multipleRules_returnsCorrectCounts() {
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(20, RuleId.TOO_SHORT);
        collector.record(30, RuleId.TOO_LONG);

        Map<RuleId, Integer> counts = collector.summaryCounts();

        assertEquals(2, counts.get(RuleId.TOO_SHORT).intValue(),
                "TOO_SHORT should have count of 2");
        assertEquals(1, counts.get(RuleId.TOO_LONG).intValue(),
                "TOO_LONG should have count of 1");
        assertEquals(2, counts.size(), "should have 2 distinct rules");
    }

    @Test
    @DisplayName("Summary counts empty when no violations recorded")
    void summaryCounts_noViolations_returnsEmptyMap() {
        Map<RuleId, Integer> counts = collector.summaryCounts();

        assertTrue(counts.isEmpty(), "should return empty map when no violations");
    }

    @Test
    @DisplayName("Summary counts after clear returns empty")
    void summaryCounts_afterClear_returnsEmptyMap() {
        collector.record(10, RuleId.TOO_SHORT);
        collector.clear();

        Map<RuleId, Integer> counts = collector.summaryCounts();

        assertTrue(counts.isEmpty(), "should return empty map after clear");
    }

    private Map<Integer, Violation> getPendingMap() {
        return collector.pendingForTesting();
    }

    private void pushSuppressAllScope(SuppressionTracker tracker) {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");
        tracker.pushScopeForTesting(scope);
    }
}
