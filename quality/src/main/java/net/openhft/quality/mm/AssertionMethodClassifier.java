/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Classifies method names to determine their assertion type.
 * All methods are static since classification is purely string-based.
 */
public final class AssertionMethodClassifier {

    private AssertionMethodClassifier() {
    }

    /**
     * Check if the method name is an assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an assertion method.
     */
    public static boolean isAssertionMethod(String methodName) {
        if (methodName.equals("assertClassesLoad")
                || methodName.equals("assertPackagePresent")
                || methodName.equals("assertResourcePresent")) {
            return false;
        }
        return methodName.startsWith("assert")
                || methodName.startsWith("require")
                || methodName.equals("fail")
                || methodName.equals("as")
                || methodName.equals("describedAs")
                || methodName.equals("withFailMessage")
                || methodName.equals("overridingErrorMessage");
    }

    /**
     * Check if the method name is a precondition method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a precondition method.
     */
    public static boolean isPreconditionMethod(String methodName) {
        return methodName.startsWith("require");
    }

    /**
     * Check if the method is a requireNotNull/requireNonNull method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a requireNotNull method.
     */
    public static boolean isRequireNotNullMethod(String methodName) {
        return methodName.equals("requireNotNull")
                || methodName.equals("requireNonNull");
    }

    /**
     * Check if the method name is a fail method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a fail method.
     */
    public static boolean isFailMethod(String methodName) {
        return methodName.equals("fail");
    }

    /**
     * Check if the method name is an assertThat method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an assertThat method.
     */
    public static boolean isAssertThatMethod(String methodName) {
        return methodName.equals("assertThat");
    }

    /**
     * Check if the method name is a boolean assertion method (assertTrue/assertFalse).
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a boolean assertion method.
     */
    public static boolean isBooleanAssertionMethod(String methodName) {
        return methodName.equals("assertTrue")
                || methodName.equals("assertFalse");
    }

    /**
     * Check if the method name is a nullness assertion method (assertNull/assertNotNull).
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a nullness assertion method.
     */
    public static boolean isNullnessAssertionMethod(String methodName) {
        return methodName.equals("assertNull")
                || methodName.equals("assertNotNull");
    }

    /**
     * Check if the method name is an equality assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an equality assertion method.
     */
    public static boolean isEqualityAssertionMethod(String methodName) {
        return methodName.equals("assertEquals")
                || methodName.equals("assertNotEquals")
                || methodName.equals("assertSame")
                || methodName.equals("assertNotSame")
                || methodName.equals("assertArrayEquals")
                || methodName.equals("assertIterableEquals")
                || methodName.equals("assertLinesMatch");
    }

    /**
     * Check if the method name is an AssertJ message method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an AssertJ message method.
     */
    public static boolean isAssertJMessageMethod(String methodName) {
        return methodName.equals("as")
                || methodName.equals("describedAs")
                || methodName.equals("withFailMessage")
                || methodName.equals("overridingErrorMessage");
    }

    /**
     * Check if the method name is an assertThrows method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an assertThrows method.
     */
    public static boolean isAssertThrowsMethod(String methodName) {
        return methodName.equals("assertThrows")
                || methodName.equals("assertThrowsExactly");
    }

    /**
     * Check if the method name is a timeout assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a timeout assertion method.
     */
    public static boolean isTimeoutAssertionMethod(String methodName) {
        return methodName.equals("assertTimeout")
                || methodName.equals("assertTimeoutPreemptively");
    }

    /**
     * Check if the method name is a doesNotThrow method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is a doesNotThrow method.
     */
    public static boolean isDoesNotThrowMethod(String methodName) {
        return methodName.equals("assertDoesNotThrow");
    }

    /**
     * Check if the method name is an AssertJ override method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is an AssertJ override method.
     */
    public static boolean isAssertJOverrideMethod(String methodName) {
        return methodName.equals("withFailMessage")
                || methodName.equals("overridingErrorMessage");
    }

    /**
     * Check if the method name is a loop index assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is suitable for loop index checking.
     */
    public static boolean isLoopIndexAssertionMethod(String methodName) {
        return methodName != null
                && (methodName.startsWith("assert") || methodName.equals("fail"));
    }
}
