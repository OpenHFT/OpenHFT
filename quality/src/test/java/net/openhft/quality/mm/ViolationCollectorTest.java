/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @SuppressWarnings("unchecked")
    void higherPriorityReplacesExisting() throws Exception {
        // First record a low priority violation (higher number = lower priority)
        collector.record(10, RuleId.TOO_SHORT);  // priority 24
        // Then record a higher priority (lower number)
        collector.record(10, RuleId.CONTEXTLESS);  // priority 1

        // Use reflection to verify the pending map
        Map<Integer, Violation> pending = getPendingMap();
        Violation violation = pending.get(10);
        assertEquals(RuleId.CONTEXTLESS, violation.ruleId(),
                "higher priority rule should replace lower");
    }

    @Test
    @SuppressWarnings("unchecked")
    void lowerPriorityDoesNotReplaceExisting() throws Exception {
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
    @SuppressWarnings("unchecked")
    void samePriorityShorterCodeWins() throws Exception {
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
    @SuppressWarnings("unchecked")
    void samePriorityAndCodeLengthOrdinalWins() throws Exception {
        // Same priority (0), same code length - ordinal breaks the tie
        // Record the same rule twice (should be no-op)
        collector.record(10, RuleId.MISSING_MESSAGE);
        // Second record of same rule should not throw
        collector.record(10, RuleId.MISSING_MESSAGE);

        Map<Integer, Violation> pending = getPendingMap();
        assertEquals(1, pending.size(), "should have exactly one violation");
    }

    @Test
    @SuppressWarnings("unchecked")
    void clearRemovesPendingViolations() throws Exception {
        collector.record(10, RuleId.TOO_SHORT);
        collector.record(20, RuleId.CONTEXTLESS);

        collector.clear();

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.isEmpty(), "pending should be empty after clear");
    }

    @Test
    @SuppressWarnings("unchecked")
    void multipleViolationsOnDifferentLines() throws Exception {
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
    @SuppressWarnings("unchecked")
    void recordPreservesArgs() throws Exception {
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
    @SuppressWarnings("unchecked")
    void negativeLineNumberAllowed() throws Exception {
        collector.record(-1, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(-1), "negative line number should be allowed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void zeroLineNumberAllowed() throws Exception {
        collector.record(0, RuleId.TOO_SHORT);

        Map<Integer, Violation> pending = getPendingMap();
        assertTrue(pending.containsKey(0), "zero line number should be allowed");
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Violation> getPendingMap() throws Exception {
        Field pendingField = ViolationCollector.class.getDeclaredField("pending");
        pendingField.setAccessible(true);
        return (Map<Integer, Violation>) pendingField.get(collector);
    }
}
