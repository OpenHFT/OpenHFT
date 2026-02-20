/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdviceId} branch coverage.
 */
@DisplayName("AdviceId tests")
class AdviceIdTest {

    @Test
    @DisplayName("forName with null returns UNKNOWN")
    void forName_null_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forName(null));
    }

    @Test
    @DisplayName("forName with empty string returns UNKNOWN")
    void forName_empty_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forName(""));
    }

    @Test
    @DisplayName("forName with blank string returns UNKNOWN")
    void forName_blank_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forName("   "));
    }

    @Test
    @DisplayName("forName with unknown name returns UNKNOWN")
    void forName_unknownName_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forName("NonExistentAdvice"));
    }

    @Test
    @DisplayName("forName with valid name returns correct AdviceId")
    void forName_validName_returnsCorrectId() {
        assertEquals(AdviceId.MMAssertionMessageMissing,
                AdviceId.forName("MMAssertionMessageMissing"));
        assertEquals(AdviceId.MMThrowNull,
                AdviceId.forName("MMThrowNull"));
        assertEquals(AdviceId.UNKNOWN,
                AdviceId.forName("UNKNOWN"));
    }

    @Test
    @DisplayName("forRule with null ruleId and source returns UNKNOWN")
    void forRule_nullRuleIdAndSource_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forRule(null, null));
    }

    @Test
    @DisplayName("forRule with null ruleId returns UNKNOWN")
    void forRule_nullRuleId_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forRule(null, AdviceSource.ASSERTION));
    }

    @Test
    @DisplayName("forRule with null source returns UNKNOWN")
    void forRule_nullSource_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forRule(RuleId.MISSING_MESSAGE, null));
    }

    @Test
    @DisplayName("forRule with valid rule and source returns correct AdviceId")
    void forRule_validRuleAndSource_returnsCorrectId() {
        assertEquals(AdviceId.MMAssertionMessageMissing,
                AdviceId.forRule(RuleId.MISSING_MESSAGE, AdviceSource.ASSERTION));
        assertEquals(AdviceId.MMThrowMessageMissing,
                AdviceId.forRule(RuleId.MISSING_MESSAGE, AdviceSource.THROW));
    }

    @Test
    @DisplayName("forRule with unmatched source returns UNKNOWN")
    void forRule_unmatchedSource_returnsUnknown() {
        // THROW_NULL only has THROW source
        assertEquals(AdviceId.UNKNOWN,
                AdviceId.forRule(RuleId.THROW_NULL, AdviceSource.LOG));
    }

    @Test
    @DisplayName("forRule single-arg with null returns UNKNOWN")
    void forRule_singleArg_null_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN, AdviceId.forRule(null));
    }

    @Test
    @DisplayName("forRule single-arg with file-level rule returns correct AdviceId")
    void forRule_singleArg_fileLevelRule_returnsCorrectId() {
        assertEquals(AdviceId.MMOverusedWord,
                AdviceId.forRule(RuleId.OVERUSED_WORD));
        assertEquals(AdviceId.MMLacksPurpose,
                AdviceId.forRule(RuleId.LACKS_PURPOSE));
        assertEquals(AdviceId.MMLowEntropy,
                AdviceId.forRule(RuleId.LOW_ENTROPY));
    }

    @Test
    @DisplayName("forRule single-arg with non-file-level rule returns UNKNOWN")
    void forRule_singleArg_nonFileLevelRule_returnsUnknown() {
        assertEquals(AdviceId.UNKNOWN,
                AdviceId.forRule(RuleId.MISSING_MESSAGE));
    }

    @Test
    @DisplayName("isFileLevel returns true for file-level advice ids")
    void isFileLevel_trueForFileLevelIds() {
        assertTrue(AdviceId.MMOverusedWord.isFileLevel());
        assertTrue(AdviceId.MMLacksPurpose.isFileLevel());
        assertTrue(AdviceId.MMLowEntropy.isFileLevel());
    }

    @Test
    @DisplayName("isFileLevel returns false for non-file-level advice ids")
    void isFileLevel_falseForNonFileLevelIds() {
        assertFalse(AdviceId.MMAssertionMessageMissing.isFileLevel());
        assertFalse(AdviceId.MMThrowNull.isFileLevel());
        assertFalse(AdviceId.MMUnhandled.isFileLevel());
        assertFalse(AdviceId.UNKNOWN.isFileLevel());
    }

    @ParameterizedTest
    @EnumSource(AdviceId.class)
    @DisplayName("forName round-trip for all AdviceId values")
    void forName_roundTrip_allValues(AdviceId adviceId) {
        assertEquals(adviceId, AdviceId.forName(adviceId.name()));
    }

    @Test
    @DisplayName("ruleId and adviceSource accessors return expected values")
    void ruleIdAndAdviceSource_accessors() {
        assertEquals(RuleId.MISSING_MESSAGE, AdviceId.MMAssertionMessageMissing.ruleId());
        assertEquals(AdviceSource.ASSERTION, AdviceId.MMAssertionMessageMissing.adviceSource());
        assertNull(AdviceId.UNKNOWN.ruleId());
        assertNull(AdviceId.UNKNOWN.adviceSource());
        assertNull(AdviceId.MMOverusedWord.adviceSource());
    }

    @Test
    @DisplayName("forRule with UNHANDLED rule returns MMUnhandled")
    void forRule_unhandled_returnsMmUnhandled() {
        assertEquals(AdviceId.MMUnhandled, AdviceId.forRule(RuleId.UNHANDLED));
    }
}
