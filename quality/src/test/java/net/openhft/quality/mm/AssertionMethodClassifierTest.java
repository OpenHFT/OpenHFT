/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link AssertionMethodClassifier}.
 */
@DisplayName("Assertion method classifier tests scenario case")
public class AssertionMethodClassifierTest {

    @DisplayName("Is assertion method scenario case detail path")
    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "assertClassesLoad, false",
            "assertPackagePresent, false",
            "assertResourcePresent, false",
            "requireNonNull, true",
            "fail, true",
            "as, true",
            "describedAs, true",
            "someRandomMethod, false"
    })
    void isAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertionMethod(methodName));
    }

    @DisplayName("Is assertion method null returns false scenario case")
    @ParameterizedTest
    @NullSource
    void isAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssertionMethod(methodName));
    }

    @DisplayName("Is precondition method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "requireNonNull, true",
            "assertTrue, false"
    })
    void isPreconditionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isPreconditionMethod(methodName));
    }

    @DisplayName("Is require not null method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "requireNonNull, true",
            "requireNotNull, true",
            "require, false"
    })
    void isRequireNotNullMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isRequireNotNullMethod(methodName));
    }

    @DisplayName("Is fail method scenario case detail path")
    @ParameterizedTest
    @CsvSource({
            "fail, true",
            "assertTrue, false"
    })
    void isFailMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isFailMethod(methodName));
    }

    @DisplayName("Is assert that method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertThat, true",
            "assertTrue, false"
    })
    void isAssertThatMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertThatMethod(methodName));
    }

    @DisplayName("Is boolean assertion method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "assertFalse, true",
            "assertEquals, false"
    })
    void isBooleanAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isBooleanAssertionMethod(methodName));
    }

    @DisplayName("Is nullness assertion method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertNull, true",
            "assertNotNull, true",
            "assertTrue, false"
    })
    void isNullnessAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isNullnessAssertionMethod(methodName));
    }

    @DisplayName("Is equality assertion method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertEquals, true",
            "assertNotEquals, true",
            "assertSame, true",
            "assertArrayEquals, true",
            "assertTrue, false"
    })
    void isEqualityAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isEqualityAssertionMethod(methodName));
    }

    @DisplayName("Is assert J message method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "as, true",
            "describedAs, true",
            "withFailMessage, true",
            "assertTrue, false"
    })
    void isAssertJMessageMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertJMessageMethod(methodName));
    }

    @DisplayName("Is assert throws method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertThrows, true",
            "assertThrowsExactly, true",
            "assertTrue, false"
    })
    void isAssertThrowsMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertThrowsMethod(methodName));
    }

    @DisplayName("Is timeout assertion method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertTimeout, true",
            "assertTimeoutPreemptively, true",
            "assertTrue, false"
    })
    void isTimeoutAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isTimeoutAssertionMethod(methodName));
    }

    @DisplayName("Is does not throw method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "assertDoesNotThrow, true",
            "assertTrue, false"
    })
    void isDoesNotThrowMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isDoesNotThrowMethod(methodName));
    }

    @DisplayName("Is recognised assertion method scenario case")
    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "assertAll, true",
            "assertThat, true",
            "assertDoesNotThrow, true",
            "assertOtherFieldsUnchanged, false",
            "assertReceivedOrders, false"
    })
    void isRecognisedAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isRecognisedAssertionMethod(methodName));
    }

    @DisplayName("Is assert J override method scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "withFailMessage, true",
            "overridingErrorMessage, true",
            "as, false"
    })
    void isAssertJOverrideMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertJOverrideMethod(methodName));
    }

    @DisplayName("Is loop index assertion method scenario case")
    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "fail, true",
            "someMethod, false"
    })
    void isLoopIndexAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName));
    }

    @DisplayName("Is loop index assertion method null returns false scenario case")
    @ParameterizedTest
    @NullSource
    void isLoopIndexAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName));
    }

    @DisplayName("Is assumption method scenario case")
    @ParameterizedTest
    @CsvSource({
            "assumeTrue, true",
            "assumeFalse, true",
            "assertTrue, false",
            "assumption, false"
    })
    void isAssumptionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssumptionMethod(methodName));
    }

    // -- Null guard tests for all public methods --

    @DisplayName("isPreconditionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isPreconditionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isPreconditionMethod(methodName));
    }

    @DisplayName("isRequireNotNullMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isRequireNotNullMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isRequireNotNullMethod(methodName));
    }

    @DisplayName("isFailMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isFailMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isFailMethod(methodName));
    }

    @DisplayName("isAssertThatMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isAssertThatMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssertThatMethod(methodName));
    }

    @DisplayName("isBooleanAssertionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isBooleanAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isBooleanAssertionMethod(methodName));
    }

    @DisplayName("isAssumptionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isAssumptionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssumptionMethod(methodName));
    }

    @DisplayName("isNullnessAssertionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isNullnessAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isNullnessAssertionMethod(methodName));
    }

    @DisplayName("isEqualityAssertionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isEqualityAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isEqualityAssertionMethod(methodName));
    }

    @DisplayName("isAssertJMessageMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isAssertJMessageMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssertJMessageMethod(methodName));
    }

    @DisplayName("isAssertThrowsMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isAssertThrowsMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssertThrowsMethod(methodName));
    }

    @DisplayName("isTimeoutAssertionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isTimeoutAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isTimeoutAssertionMethod(methodName));
    }

    @DisplayName("isDoesNotThrowMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isDoesNotThrowMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isDoesNotThrowMethod(methodName));
    }

    @DisplayName("isAssertJOverrideMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isAssertJOverrideMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isAssertJOverrideMethod(methodName));
    }

    @DisplayName("isRecognisedAssertionMethod null returns false")
    @ParameterizedTest
    @NullSource
    void isRecognisedAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isRecognisedAssertionMethod(methodName));
    }

    // -- Additional branch coverage for equality methods --

    @DisplayName("isEqualityAssertionMethod covers assertNotSame and tail branches")
    @ParameterizedTest
    @CsvSource({
            "assertNotSame, true",
            "assertIterableEquals, true",
            "assertLinesMatch, true"
    })
    void isEqualityAssertionMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isEqualityAssertionMethod(methodName));
    }

    @DisplayName("isRecognisedAssertionMethod covers fail and assertThrows branches")
    @ParameterizedTest
    @CsvSource({
            "fail, true",
            "assertThrows, true",
            "assertTimeout, true",
            "assertNull, true",
            "assertEquals, true",
            "assertAll, true",
            "assertFalse, true",
            "assertDoesNotThrow, true"
    })
    void isRecognisedAssertionMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isRecognisedAssertionMethod(methodName));
    }

    @DisplayName("isAssertionMethod covers withFailMessage and overridingErrorMessage")
    @ParameterizedTest
    @CsvSource({
            "withFailMessage, true",
            "overridingErrorMessage, true",
            "assumeTrue, true"
    })
    void isAssertionMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertionMethod(methodName));
    }

    @DisplayName("isBooleanAssertionMethod covers assumeTrue and assumeFalse")
    @ParameterizedTest
    @CsvSource({
            "assumeTrue, true",
            "assumeFalse, true"
    })
    void isBooleanAssertionMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isBooleanAssertionMethod(methodName));
    }

    @DisplayName("isAssertJMessageMethod covers overridingErrorMessage")
    @ParameterizedTest
    @CsvSource({
            "overridingErrorMessage, true"
    })
    void isAssertJMessageMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertJMessageMethod(methodName));
    }

    @DisplayName("isLoopIndexAssertionMethod covers assume prefix")
    @ParameterizedTest
    @CsvSource({
            "assumeTrue, true"
    })
    void isLoopIndexAssertionMethod_additionalBranches(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName));
    }
}
