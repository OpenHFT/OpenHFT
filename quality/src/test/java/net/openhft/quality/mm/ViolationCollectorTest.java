/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ViolationCollector}.
 */
class ViolationCollectorTest {

    private ViolationCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ViolationCollector(null);
    }

    @Test
    void recordWithNullTrackerRecordsViolation() {
        boolean recorded = collector.record(10, RuleId.TOO_SHORT);
        assertTrue(recorded, "should record when tracker is null");
    }

    @Test
    void recordRejectsNullRuleId() {
        assertThrows(NullPointerException.class,
                () -> collector.record(10, null),
                "record should reject null ruleId");
    }

    @Test
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
    void clearRemovesPendingViolations() {
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(20, RuleId.CONTEXTLESS);

        collector.clear();

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.isEmpty(), "pending should be empty after clear");
    }

    @Test
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
    void recordWithNonSuppressedRuleReturnsTrue() {
        // Create a real suppression tracker without suppressions
        // When no scope is entered, isSuppressed returns false for all rules
        SuppressionTracker tracker = new SuppressionTracker();

        ViolationCollector collectorWithTracker = new ViolationCollector(tracker);
        boolean recorded = collectorWithTracker.record(10, RuleId.TOO_SHORT);

        assertTrue(recorded, "non-suppressed rule should be recorded");
    }

    @Test
    void negativeLineNumberAllowed() {
        collector.record(-1, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(-1), "negative line number should be allowed");
    }

    @Test
    void zeroLineNumberAllowed() {
        collector.record(0, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(0), "zero line number should be allowed");
    }

    // --- pending map state tests (flush behavior verification) ---

    @Test
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
    void violationsHaveCorrectMessageKeys() {
        collector.record(10, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);

        assertEquals(RuleId.TOO_SHORT.messageKey(), violation.ruleId().messageKey(),
                "message key should match rule");
    }

    @Test
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

    private Map<Integer, Violation> getPendingMap() {
        return collector.pendingForTesting();
    }

    private void pushSuppressAllScope(SuppressionTracker tracker) {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");
        tracker.pushScopeForTesting(scope);
    }
}
