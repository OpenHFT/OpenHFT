/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MessageRuleSupport}.
 */
@DisplayName("Message rule support tests scenario case")
public class MessageRuleSupportTest {

    private MessageRuleSupport ruleSupport;

    @BeforeEach
    void setUp() {
        ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
    }

    @Test
    @DisplayName("Metrics calculator not null scenario case")
    void metricsCalculatorNotNull() {
        assertNotNull(ruleSupport.metricsCalculator());
    }

    // --- isGenericMessage tests ---

    @DisplayName("Is generic message returns true scenario case")
    @ParameterizedTest
    @ValueSource(strings = {
            "actual", "expected", "value", "result", "data", "object",
            "condition", "test", "check", "message", "msg",
            "err", "error", "fail", "ok", "true", "false",
            "ACTUAL", "Expected", "VALUE"
    })
    void isGenericMessageReturnsTrue(String message) {
        assertTrue(ruleSupport.isGenericMessage(message), "Expected generic: " + message);
    }

    @DisplayName("Is generic message returns false scenario case")
    @ParameterizedTest
    @ValueSource(strings = {
            "user account balance", "connection timeout", "invalid input",
            "file not found", "actual value differs"
    })
    void isGenericMessageReturnsFalse(String message) {
        assertFalse(ruleSupport.isGenericMessage(message), "Expected not generic: " + message);
    }

    @Test
    @DisplayName("Is generic message with null scenario case")
    void isGenericMessageWithNull() {
        assertFalse(ruleSupport.isGenericMessage(null));
    }

    // --- isRestatesAssertion tests ---

    @DisplayName("Is restates assertion returns true scenario case detail")
    @ParameterizedTest
    @ValueSource(strings = {
            "assertEquals", "assertTrue", "assertFalse", "assertNull",
            "assertNotNull", "assertSame", "assertNotEquals",
            "should be equal", "should be true", "should be false",
            "should not be null", "must be equal", "must not be null",
            "should match", "must match", "expected to match",
            "values should match", "value must match",
            "equals", "not null", "is null", "is true", "is false"
    })
    void isRestatesAssertionReturnsTrue(String message) {
        assertTrue(ruleSupport.isRestatesAssertion(message), "Expected restates: " + message);
    }

    @DisplayName("Is restates assertion returns false scenario case detail")
    @ParameterizedTest
    @ValueSource(strings = {
            "user should have valid email", "balance must be positive",
            "connection should be established", "file must exist"
    })
    void isRestatesAssertionReturnsFalse(String message) {
        assertFalse(ruleSupport.isRestatesAssertion(message), "Expected not restates: " + message);
    }

    @Test
    @DisplayName("Is restates assertion with null scenario case detail")
    void isRestatesAssertionWithNull() {
        assertFalse(ruleSupport.isRestatesAssertion(null));
    }

    // --- isContextless tests ---

    @DisplayName("Is contextless returns true scenario case detail")
    @ParameterizedTest
    @ValueSource(strings = {
            "comparison", "check", "validation", "equality", "verify", "test",
            "values match", "value should match", "values must equal",
            "should equal", "must be equal", "not equal", "mismatch",
            "failed", "failure", "error",
            "operation result should equal expected value",
            "result should match expected", "result should equal expected",
            "counter meets minimum", "counter meets maximum",
            "meets minimum", "meets maximum", "meets threshold",
            "within range", "within valid range", "within expected range",
            "indices should be valid", "config error rethrown"
    })
    void isContextlessReturnsTrue(String message) {
        assertTrue(ruleSupport.isContextless(message), "Expected contextless: " + message);
    }

    @DisplayName("Is contextless returns false scenario case detail")
    @ParameterizedTest
    @ValueSource(strings = {
            "user balance should equal expected amount",
            "connection timeout exceeded threshold",
            "invalid email format detected"
    })
    void isContextlessReturnsFalse(String message) {
        assertFalse(ruleSupport.isContextless(message), "Expected not contextless: " + message);
    }

    @Test
    @DisplayName("Is contextless with null scenario case detail")
    void isContextlessWithNull() {
        assertFalse(ruleSupport.isContextless(null));
    }

    // --- findNameVariant tests ---

    @Test
    @DisplayName("Find name variant exact match scenario")
    void findNameVariantExactMatch() {
        assertEquals("userName", ruleSupport.findNameVariant("Check userName is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant swapped case scenario")
    void findNameVariantSwappedCase() {
        assertEquals("UserName", ruleSupport.findNameVariant("Check UserName is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant lower case scenario")
    void findNameVariantLowerCase() {
        assertEquals("username", ruleSupport.findNameVariant("Check username is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant upper case scenario")
    void findNameVariantUpperCase() {
        assertEquals("USERNAME", ruleSupport.findNameVariant("Check USERNAME is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant not found scenario")
    void findNameVariantNotFound() {
        assertNull(ruleSupport.findNameVariant("Check value is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant too short scenario")
    void findNameVariantTooShort() {
        assertNull(ruleSupport.findNameVariant("Check name is valid", "name"));
    }

    @Test
    @DisplayName("Find name variant exactly min length")
    void findNameVariantExactlyMinLength() {
        assertEquals("names", ruleSupport.findNameVariant("Check names is valid", "names"));
    }

    @Test
    @DisplayName("Find name variant boundary at start")
    void findNameVariantBoundaryAtStart() {
        assertEquals("userName", ruleSupport.findNameVariant("userName is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant boundary at end")
    void findNameVariantBoundaryAtEnd() {
        assertEquals("userName", ruleSupport.findNameVariant("Check userName", "userName"));
    }

    @Test
    @DisplayName("Find name variant not bounded left")
    void findNameVariantNotBoundedLeft() {
        assertNull(ruleSupport.findNameVariant("CheckuserName is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant not bounded right")
    void findNameVariantNotBoundedRight() {
        assertNull(ruleSupport.findNameVariant("Check userNameValue is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant with underscore scenario")
    void findNameVariantWithUnderscore() {
        assertNull(ruleSupport.findNameVariant("Check user_name is valid", "userName"));
    }

    @Test
    @DisplayName("Find name variant upper case only when different")
    void findNameVariantUpperCaseOnlyWhenDifferent() {
        // When name is already uppercase, we should still find it
        assertEquals("VALUE", ruleSupport.findNameVariant("Check VALUE here", "value"));
    }

    @Test
    @DisplayName("Find name variant lower case matches different from swapped")
    void findNameVariantLowerCaseMatchesDifferentFromSwapped() {
        // lowercase differs from swapped case for names starting with lowercase
        assertEquals("email", ruleSupport.findNameVariant("Check email address", "Email"));
    }

    // --- analyseSubstance tests ---

    @Test
    @DisplayName("Analyse substance with enough words scenario")
    void analyseSubstanceWithEnoughWords() {
        // "balance" and "positive" are meaningful (not filler)
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName balance positive required", "userName");
        assertTrue(result.hasSubstance());
        assertNotNull(result.diagnosis());
        assertNotNull(result.verboseDetails());
    }

    @Test
    @DisplayName("Analyse substance with only filler scenario")
    void analyseSubstanceWithOnlyFiller() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName the a is", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("filler"));
    }

    @Test
    @DisplayName("Analyse substance with nothing scenario case")
    void analyseSubstanceWithNothing() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("nothing remains"));
    }

    @Test
    @DisplayName("Analyse substance with too few words")
    void analyseSubstanceWithTooFewWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("too short"));
    }

    @Test
    @DisplayName("Analyse substance with mixed words scenario")
    void analyseSubstanceWithMixedWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName the valid", "userName");
        assertFalse(result.hasSubstance());
        // Only 1 meaningful word, plus filler
        assertTrue(result.diagnosis().contains("meaningful word"));
    }

    @Test
    @DisplayName("Analyse substance uses unique meaningful words")
    void analyseSubstanceUsesUniqueMeaningfulWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid valid", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("unique"));
    }

    @Test
    @DisplayName("Analyse substance with numeric words scenario")
    void analyseSubstanceWithNumericWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName 123 456", "userName");
        assertFalse(result.hasSubstance());
        // Numbers are counted as filler
    }

    @Test
    @DisplayName("Analyse substance with single char words")
    void analyseSubstanceWithSingleCharWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName a b c", "userName");
        assertFalse(result.hasSubstance());
        // Single char words are filler
    }

    @Test
    @DisplayName("Analyse substance verbose details scenario case")
    void analyseSubstanceVerboseDetails() {
        // "validate" is meaningful, "should" is filler
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName should validate", "userName");
        String details = result.verboseDetails();
        assertTrue(details.contains("meaningful="));
        assertTrue(details.contains("filler="));
        assertTrue(details.contains("removed="));
    }

    @Test
    @DisplayName("Analyse substance removes case insensitive scenario")
    void analyseSubstanceRemovesCaseInsensitive() {
        // Verify the name is removed case-insensitively
        // "balance" and "positive" are meaningful
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("USERNAME balance positive required", "userName");
        assertTrue(result.hasSubstance());
    }

    // --- Edge cases ---

    @DisplayName("Find name variant min length boundary")
    @ParameterizedTest
    @CsvSource({
            "'', false",
            "'   ', false",
            "'a', false",
            "'ab', false",
            "'abc', false",
            "'abcd', false",
            "'abcde', true"
    })
    void findNameVariantMinLengthBoundary(String name, boolean shouldFind) {
        String text = "Check " + name + " is valid";
        String result = ruleSupport.findNameVariant(text, name);
        if (shouldFind) {
            assertEquals(name, result);
        } else {
            assertNull(result);
        }
    }

    @Test
    @DisplayName("Analyse substance empty message after removal")
    void analyseSubstanceEmptyMessageAfterRemoval() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("value", "value");
        assertFalse(result.hasSubstance());
    }

    // --- Boundary mutation killing tests ---

    @Test
    @DisplayName("Analyse substance boundary word length exactly two chars meaningful")
    void analyseSubstanceBoundaryWordLengthTwoChars() {
        // Word length boundary: < 2 means 1-char words are filler, 2-char words are meaningful
        // "to" is 2 chars - should be counted as meaningful if not filler
        // "ab" and "cd" are 2-char meaningful words
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName ab cd", "userName");
        // 2 meaningful 2-char words = hasSubstance
        assertTrue(result.hasSubstance(), "two 2-char meaningful words should be substance");
    }

    @Test
    @DisplayName("Analyse substance boundary single char as filler")
    void analyseSubstanceBoundarySingleCharAsFiller() {
        // Verify single char words are treated as filler
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName x y z", "userName");
        assertFalse(result.hasSubstance(), "single char words should be filler");
    }

    @Test
    @DisplayName("Analyse substance boundary numeric word as filler")
    void analyseSubstanceBoundaryNumericWordAsFiller() {
        // Verify numeric words are treated as filler even if > 1 char
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName 99 100", "userName");
        assertFalse(result.hasSubstance(), "numeric words should be filler");
    }

    @Test
    @DisplayName("Analyse substance meaningful empty and filler empty diagnosis")
    void analyseSubstanceMeaningfulEmptyFillerEmptyDiagnosis() {
        // Both meaningful and filler empty - tests the boundary condition
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName", "userName");
        assertTrue(result.diagnosis().contains("nothing remains"),
                "diagnosis should indicate nothing remains");
    }

    @Test
    @DisplayName("Analyse substance meaningful not empty filler not empty diagnosis")
    void analyseSubstanceMeaningfulNotEmptyFillerNotEmptyDiagnosis() {
        // Both meaningful and filler not empty
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid the", "userName");
        assertTrue(result.diagnosis().contains("meaningful") && result.diagnosis().contains("filler"),
                "diagnosis should mention both meaningful and filler");
    }

    @Test
    @DisplayName("Analyse substance with only meaningful no filler")
    void analyseSubstanceWithOnlyMeaningfulNoFiller() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid", "userName");
        assertFalse(result.hasSubstance());
        // Only 1 word, needs 2
        assertTrue(result.diagnosis().contains("too short"));
    }

    @Test
    @DisplayName("Find name variant with empty text")
    void findNameVariantWithEmptyText() {
        assertNull(ruleSupport.findNameVariant("", "userName"));
    }

    @Test
    @DisplayName("Find name variant already lower case")
    void findNameVariantAlreadyLowerCase() {
        // When the name is already lowercase and text has it lowercase
        assertEquals("username", ruleSupport.findNameVariant("Check username", "username"));
    }

    @Test
    @DisplayName("Find name variant already upper case")
    void findNameVariantAlreadyUpperCase() {
        // When the name is already uppercase
        assertEquals("USERNAME", ruleSupport.findNameVariant("Check USERNAME", "USERNAME"));
    }

    @Test
    @DisplayName("Find name variant swapped case found verifies non empty return")
    void findNameVariantSwappedCaseFoundVerifiesReturn() {
        // This test kills the "replaced return with empty string" mutation on swapFirstLetterCase
        // We pass "userName" but text contains "UserName" (swapped case)
        // swapFirstLetterCase("userName") returns "UserName" which should be found
        String result = ruleSupport.findNameVariant("Check UserName is valid", "userName");
        assertNotNull(result, "should find swapped case variant");
        assertEquals("UserName", result, "should return the swapped case variant");
        assertFalse(result.isEmpty(), "result should not be empty");
    }

    @Test
    @DisplayName("Find name variant swapped case uppercase to lower verifies return")
    void findNameVariantSwappedCaseUpperToLower() {
        // Starting with uppercase, swap to lowercase
        // swapFirstLetterCase("UserName") returns "userName"
        String result = ruleSupport.findNameVariant("Check userName is valid", "UserName");
        assertNotNull(result, "should find swapped case variant");
        assertEquals("userName", result, "should return lowercase first letter variant");
        assertFalse(result.isEmpty(), "result should not be empty");
    }
}
