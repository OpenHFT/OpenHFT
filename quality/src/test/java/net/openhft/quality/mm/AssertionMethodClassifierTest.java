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
}
