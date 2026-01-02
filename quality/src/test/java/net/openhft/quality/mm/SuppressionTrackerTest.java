/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SuppressionTracker}.
 */
@SuppressWarnings("MMDisplayName")
class SuppressionTrackerTest {

    private SuppressionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new SuppressionTracker();
    }

    @Test
    void constructorCreatesEmptyTracker() {
        assertNotNull(tracker, "tracker should not be null");
    }

    @Test
    void isSuppressed_emptyScope_returnsFalse() {
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return false when no scope entered");
    }

    @Test
    void isSuppressed_nullRuleId_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> tracker.isSuppressed(null),
                "should throw NPE for null ruleId");
    }

    @Test
    void leaveScope_emptyStack_doesNotThrow() {
        // Should not throw even with empty stack
        tracker.leaveScope();
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should still return false after leaveScope on empty stack");
    }

    // --- stripQuotes tests ---

    @Test
    void stripQuotes_normalString() {
        assertEquals("test", tracker.stripQuotes("\"test\""),
                "should strip surrounding quotes");
    }

    @Test
    void stripQuotes_noQuotes() {
        assertEquals("test", tracker.stripQuotes("test"),
                "should return unchanged if no quotes");
    }

    @Test
    void stripQuotes_emptyQuotes() {
        assertEquals("", tracker.stripQuotes("\"\""),
                "should return empty string for empty quotes");
    }

    @Test
    void stripQuotes_singleChar() {
        assertEquals("x", tracker.stripQuotes("x"),
                "should return unchanged for single char");
    }

    // --- SuppressionScope inner class tests ---

    @Test
    void suppressionScope_addToken_mmAll_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");

        assertTrue(scope.suppressAll,
                "MM-all should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_meaningfulMessage_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MeaningfulMessage");

        assertTrue(scope.suppressAll,
                "MeaningfulMessage should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_meaningfulMessageCheck_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MeaningfulMessageCheck");

        assertTrue(scope.suppressAll,
                "MeaningfulMessageCheck should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_knownCode_addsToSet() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MMTooShort");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "known code should be added to suppressedCodes");
    }

    @Test
    void suppressionScope_addToken_unknownCode_notAdded() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("UnknownCode");

        Set<String> codes = scope.suppressedCodes;
        assertFalse(codes.contains("UnknownCode"),
                "unknown code should not be added");
    }

    @Test
    void suppressionScope_addToken_emptyString_notAdded() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.isEmpty(),
                "empty string should not be added");
    }

    @Test
    void suppressionScope_addToken_checkstylePrefix_stripped() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("checkstyle:MMTooShort");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "checkstyle: prefix should be stripped");
    }

    @Test
    void suppressionScope_addToken_whitespace_trimmed() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("  MMTooShort  ");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "whitespace should be trimmed");
    }

    @Test
    void suppressionScope_copyConstructor_inheritsCodes() {
        SuppressionTracker.SuppressionScope parentScope = new SuppressionTracker.SuppressionScope();
        parentScope.addToken("MMTooShort");

        SuppressionTracker.SuppressionScope childScope = new SuppressionTracker.SuppressionScope(parentScope);

        Set<String> codes = childScope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "child scope should inherit parent codes");
    }

    @Test
    void suppressionScope_copyConstructor_inheritsSuppressAll() {
        SuppressionTracker.SuppressionScope parentScope = new SuppressionTracker.SuppressionScope();
        parentScope.addToken("MM-all");

        SuppressionTracker.SuppressionScope childScope = new SuppressionTracker.SuppressionScope(parentScope);

        assertTrue(childScope.suppressAll,
                "child scope should inherit suppressAll flag");
    }

    @Test
    void cleanToken_stripsCheckstylePrefix() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("checkstyle:MMTooShort"),
                "should strip checkstyle: prefix");
    }

    @Test
    void cleanToken_trimsWhitespace() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("  MMTooShort  "),
                "should trim whitespace");
    }

    @Test
    void cleanToken_preservesNonPrefixed() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("MMTooShort"),
                "should preserve non-prefixed token");
    }

    // --- Test isSuppressed with manually manipulated scope stack ---

    @Test
    void isSuppressed_withSuppressAll_returnsTrue() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");
        tracker.pushScopeForTesting(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true when suppressAll is set");
        assertTrue(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return true for any rule when suppressAll is set");
    }

    @Test
    void isSuppressed_withSpecificCode_returnsTrueForMatch() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTesting(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true for suppressed rule");
        assertFalse(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return false for non-suppressed rule");
    }
}
