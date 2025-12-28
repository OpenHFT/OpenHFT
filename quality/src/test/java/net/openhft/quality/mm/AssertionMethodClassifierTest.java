/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link AssertionMethodClassifier}.
 */
public class AssertionMethodClassifierTest {

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

    @ParameterizedTest
    @CsvSource({
            "requireNonNull, true",
            "assertTrue, false"
    })
    void isPreconditionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isPreconditionMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "requireNonNull, true",
            "requireNotNull, true",
            "require, false"
    })
    void isRequireNotNullMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isRequireNotNullMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "fail, true",
            "assertTrue, false"
    })
    void isFailMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isFailMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertThat, true",
            "assertTrue, false"
    })
    void isAssertThatMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertThatMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "assertFalse, true",
            "assertEquals, false"
    })
    void isBooleanAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isBooleanAssertionMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertNull, true",
            "assertNotNull, true",
            "assertTrue, false"
    })
    void isNullnessAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isNullnessAssertionMethod(methodName));
    }

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

    @ParameterizedTest
    @CsvSource({
            "assertThrows, true",
            "assertThrowsExactly, true",
            "assertTrue, false"
    })
    void isAssertThrowsMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertThrowsMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertTimeout, true",
            "assertTimeoutPreemptively, true",
            "assertTrue, false"
    })
    void isTimeoutAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isTimeoutAssertionMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertDoesNotThrow, true",
            "assertTrue, false"
    })
    void isDoesNotThrowMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isDoesNotThrowMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "withFailMessage, true",
            "overridingErrorMessage, true",
            "as, false"
    })
    void isAssertJOverrideMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isAssertJOverrideMethod(methodName));
    }

    @ParameterizedTest
    @CsvSource({
            "assertTrue, true",
            "fail, true",
            "someMethod, false"
    })
    void isLoopIndexAssertionMethod(String methodName, boolean expected) {
        assertEquals(expected, AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName));
    }

    @ParameterizedTest
    @NullSource
    void isLoopIndexAssertionMethod_null_returnsFalse(String methodName) {
        assertFalse(AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName));
    }
}
