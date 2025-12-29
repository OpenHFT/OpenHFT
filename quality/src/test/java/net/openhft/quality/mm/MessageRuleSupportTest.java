/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MessageRuleSupport}.
 */
public class MessageRuleSupportTest {

    private MessageRuleSupport ruleSupport;

    @BeforeEach
    void setUp() {
        ruleSupport = new MessageRuleSupport(new MessageMetricsCalculator());
    }

    @Test
    void metricsCalculatorNotNull() {
        assertNotNull(ruleSupport.metricsCalculator());
    }

    // --- isGenericMessage tests ---

    @ParameterizedTest
    @ValueSource(strings = {
            "actual", "expected", "value", "result", "data", "object",
            "condition", "test", "check", "message", "msg",
            "err", "error", "fail", "ok", "true", "false",
            "ACTUAL", "Expected", "VALUE"
    })
    void isGenericMessageReturnsTrue(String message) {
        assertTrue("Expected generic: " + message, ruleSupport.isGenericMessage(message));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user account balance", "connection timeout", "invalid input",
            "file not found", "actual value differs"
    })
    void isGenericMessageReturnsFalse(String message) {
        assertFalse("Expected not generic: " + message, ruleSupport.isGenericMessage(message));
    }

    @Test
    void isGenericMessageWithNull() {
        assertFalse(ruleSupport.isGenericMessage(null));
    }

    // --- isRestatesAssertion tests ---

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
        assertTrue("Expected restates: " + message, ruleSupport.isRestatesAssertion(message));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user should have valid email", "balance must be positive",
            "connection should be established", "file must exist"
    })
    void isRestatesAssertionReturnsFalse(String message) {
        assertFalse("Expected not restates: " + message, ruleSupport.isRestatesAssertion(message));
    }

    @Test
    void isRestatesAssertionWithNull() {
        assertFalse(ruleSupport.isRestatesAssertion(null));
    }

    // --- isContextless tests ---

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
        assertTrue("Expected contextless: " + message, ruleSupport.isContextless(message));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user balance should equal expected amount",
            "connection timeout exceeded threshold",
            "invalid email format detected"
    })
    void isContextlessReturnsFalse(String message) {
        assertFalse("Expected not contextless: " + message, ruleSupport.isContextless(message));
    }

    @Test
    void isContextlessWithNull() {
        assertFalse(ruleSupport.isContextless(null));
    }

    // --- findNameVariant tests ---

    @Test
    void findNameVariantExactMatch() {
        assertEquals("userName", ruleSupport.findNameVariant("Check userName is valid", "userName"));
    }

    @Test
    void findNameVariantSwappedCase() {
        assertEquals("UserName", ruleSupport.findNameVariant("Check UserName is valid", "userName"));
    }

    @Test
    void findNameVariantLowerCase() {
        assertEquals("username", ruleSupport.findNameVariant("Check username is valid", "userName"));
    }

    @Test
    void findNameVariantUpperCase() {
        assertEquals("USERNAME", ruleSupport.findNameVariant("Check USERNAME is valid", "userName"));
    }

    @Test
    void findNameVariantNotFound() {
        assertNull(ruleSupport.findNameVariant("Check value is valid", "userName"));
    }

    @Test
    void findNameVariantTooShort() {
        assertNull(ruleSupport.findNameVariant("Check name is valid", "name"));
    }

    @Test
    void findNameVariantExactlyMinLength() {
        assertEquals("names", ruleSupport.findNameVariant("Check names is valid", "names"));
    }

    @Test
    void findNameVariantBoundaryAtStart() {
        assertEquals("userName", ruleSupport.findNameVariant("userName is valid", "userName"));
    }

    @Test
    void findNameVariantBoundaryAtEnd() {
        assertEquals("userName", ruleSupport.findNameVariant("Check userName", "userName"));
    }

    @Test
    void findNameVariantNotBoundedLeft() {
        assertNull(ruleSupport.findNameVariant("CheckuserName is valid", "userName"));
    }

    @Test
    void findNameVariantNotBoundedRight() {
        assertNull(ruleSupport.findNameVariant("Check userNameValue is valid", "userName"));
    }

    @Test
    void findNameVariantWithUnderscore() {
        assertNull(ruleSupport.findNameVariant("Check user_name is valid", "userName"));
    }

    @Test
    void findNameVariantUpperCaseOnlyWhenDifferent() {
        // When name is already uppercase, we should still find it
        assertEquals("VALUE", ruleSupport.findNameVariant("Check VALUE here", "value"));
    }

    @Test
    void findNameVariantLowerCaseMatchesDifferentFromSwapped() {
        // lowercase differs from swapped case for names starting with lowercase
        assertEquals("email", ruleSupport.findNameVariant("Check email address", "Email"));
    }

    // --- analyseSubstance tests ---

    @Test
    void analyseSubstanceWithEnoughWords() {
        // "balance" and "positive" are meaningful (not filler)
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName balance positive required", "userName");
        assertTrue(result.hasSubstance());
        assertNotNull(result.diagnosis());
        assertNotNull(result.verboseDetails());
    }

    @Test
    void analyseSubstanceWithOnlyFiller() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName the a is", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("filler"));
    }

    @Test
    void analyseSubstanceWithNothing() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("nothing remains"));
    }

    @Test
    void analyseSubstanceWithTooFewWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid", "userName");
        assertFalse(result.hasSubstance());
        assertTrue(result.diagnosis().contains("too short"));
    }

    @Test
    void analyseSubstanceWithMixedWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName the valid", "userName");
        assertFalse(result.hasSubstance());
        // Only 1 meaningful word, plus filler
        assertTrue(result.diagnosis().contains("meaningful word"));
    }

    @Test
    void analyseSubstanceWithNumericWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName 123 456", "userName");
        assertFalse(result.hasSubstance());
        // Numbers are counted as filler
    }

    @Test
    void analyseSubstanceWithSingleCharWords() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName a b c", "userName");
        assertFalse(result.hasSubstance());
        // Single char words are filler
    }

    @Test
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
    void analyseSubstanceRemovesCaseInsensitive() {
        // Verify the name is removed case-insensitively
        // "balance" and "positive" are meaningful
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("USERNAME balance positive required", "userName");
        assertTrue(result.hasSubstance());
    }

    // --- Edge cases ---

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
    void analyseSubstanceEmptyMessageAfterRemoval() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("value", "value");
        assertFalse(result.hasSubstance());
    }

    @Test
    void analyseSubstanceWithOnlyMeaningfulNoFiller() {
        MessageRuleSupport.SubstanceAnalysis result =
                ruleSupport.analyseSubstance("userName valid", "userName");
        assertFalse(result.hasSubstance());
        // Only 1 word, needs 2
        assertTrue(result.diagnosis().contains("too short"));
    }

    @Test
    void findNameVariantWithEmptyText() {
        assertNull(ruleSupport.findNameVariant("", "userName"));
    }

    @Test
    void findNameVariantAlreadyLowerCase() {
        // When the name is already lowercase and text has it lowercase
        assertEquals("username", ruleSupport.findNameVariant("Check username", "username"));
    }

    @Test
    void findNameVariantAlreadyUpperCase() {
        // When the name is already uppercase
        assertEquals("USERNAME", ruleSupport.findNameVariant("Check USERNAME", "USERNAME"));
    }
}
