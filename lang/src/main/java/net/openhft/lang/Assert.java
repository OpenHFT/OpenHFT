/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang;

/**
 * Like org.junit.Assert but without creating garbage for passing tests.
 */
public enum Assert {
    ;

    /**
     * Asserts that a condition is true. If it isn't it throws an
     * {@link AssertionError} with the given message.
     *
     * @param message   the identifying message for the {@link AssertionError} (<code>null</code>
     *                  okay)
     * @param condition condition to be checked
     */
    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * Asserts that a condition is true. If it isn't it throws an
     * {@link AssertionError} without a message.
     *
     * @param condition condition to be checked
     */
    public static void assertTrue(boolean condition) {
        assertTrue(null, condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws an
     * {@link AssertionError} with the given message.
     *
     * @param message   the identifying message for the {@link AssertionError} (<code>null</code>
     *                  okay)
     * @param condition condition to be checked
     */
    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws an
     * {@link AssertionError} without a message.
     *
     * @param condition condition to be checked
     */
    public static void assertFalse(boolean condition) {
        assertFalse(null, condition);
    }

    /**
     * Fails a test with the given message.
     *
     * @param message the identifying message for the {@link AssertionError} (<code>null</code>
     *                okay)
     * @see AssertionError
     */
    public static void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }

    /**
     * Fails a test with no message.
     */
    public static void fail() {
        fail(null);
    }

    /**
     * Asserts that two objects are equal. If they are not, an
     * {@link AssertionError} without a message is thrown. If
     * <code>expected</code> and <code>actual</code> are <code>null</code>,
     * they are considered equal.
     *
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     */
    public static void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two objects are equal. If they are not, an
     * {@link AssertionError} is thrown with the given message. If
     * <code>expected</code> and <code>actual</code> are <code>null</code>,
     * they are considered equal.
     *
     * @param message  the identifying message for the {@link AssertionError} (<code>null</code>
     *                 okay)
     * @param expected expected value
     * @param actual   actual value
     */
    public static void assertEquals(String message, Object expected,
                                    Object actual) {

        if (expected == null ? actual != null : !expected.equals(actual)) {
            if (expected instanceof String && actual instanceof String) {
                String cleanMessage = message == null ? "" : message;
                throw new ComparisonFailure(cleanMessage, (String) expected,
                        (String) actual);
            } else {
                failNotEquals(message, expected, actual);
            }
        }
    }

    /**
     * Asserts that two doubles are equal to within a positive delta.
     * If they are not, an {@link AssertionError} is thrown with the given
     * message. If the expected value is infinity then the delta value is
     * ignored. NaNs are considered equal:
     * <code>assertEquals(Double.NaN, Double.NaN, *)</code> passes
     *
     * @param message  the identifying message for the {@link AssertionError} (<code>null</code>
     *                 okay)
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     * @param delta    the maximum delta between <code>expected</code> and
     *                 <code>actual</code> for which both numbers are still
     *                 considered equal.
     */
    public static void assertEquals(String message, double expected,
                                    double actual, double delta) {
        if (Double.compare(expected, actual) != 0 && (Math.abs(expected - actual) > delta)) {
            failNotEquals(message, expected, actual);
        }
    }

    /**
     * Asserts that two floats are equal to within a positive delta.
     * If they are not, an {@link AssertionError} is thrown with the given
     * message. If the expected value is infinity then the delta value is
     * ignored. NaNs are considered equal:
     * <code>assertEquals(Float.NaN, Float.NaN, *)</code> passes
     *
     * @param message  the identifying message for the {@link AssertionError} (<code>null</code>
     *                 okay)
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     * @param delta    the maximum delta between <code>expected</code> and
     *                 <code>actual</code> for which both numbers are still
     *                 considered equal.
     */
    public static void assertEquals(String message, float expected,
                                    float actual, float delta) {
        if (Float.compare(expected, actual) == 0) {
            return;
        }
        if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, expected, actual);
        }
    }

    private static boolean doubleIsDifferent(double d1, double d2, double delta) {
        return Double.compare(d1, d2) != 0 && (Math.abs(d1 - d2) > delta);

    }

    /**
     * Asserts that two longs are equal. If they are not, an
     * {@link AssertionError} is thrown.
     *
     * @param expected expected long value.
     * @param actual   actual long value
     */
    public static void assertEquals(long expected, long actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * Asserts that two longs are equal. If they are not, an
     * {@link AssertionError} is thrown with the given message.
     *
     * @param message  the identifying message for the {@link AssertionError} (<code>null</code>
     *                 okay)
     * @param expected long expected value.
     * @param actual   long actual value
     */
    public static void assertEquals(String message, long expected, long actual) {
        if (expected != actual)
            failNotEquals(message, expected, actual);
    }

    /**
     * Asserts that two doubles are equal to within a positive delta.
     * If they are not, an {@link AssertionError} is thrown. If the expected
     * value is infinity then the delta value is ignored.NaNs are considered
     * equal: <code>assertEquals(Double.NaN, Double.NaN, *)</code> passes
     *
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     * @param delta    the maximum delta between <code>expected</code> and
     *                 <code>actual</code> for which both numbers are still
     *                 considered equal.
     */
    public static void assertEquals(double expected, double actual, double delta) {
        assertEquals(null, expected, actual, delta);
    }

    /**
     * Asserts that two floats are equal to within a positive delta.
     * If they are not, an {@link AssertionError} is thrown. If the expected
     * value is infinity then the delta value is ignored. NaNs are considered
     * equal: <code>assertEquals(Float.NaN, Float.NaN, *)</code> passes
     *
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     * @param delta    the maximum delta between <code>expected</code> and
     *                 <code>actual</code> for which both numbers are still
     *                 considered equal.
     */

    public static void assertEquals(float expected, float actual, float delta) {
        assertEquals(null, expected, actual, delta);
    }

    /**
     * Asserts that an object isn't null. If it is an {@link AssertionError} is
     * thrown with the given message.
     *
     * @param message the identifying message for the {@link AssertionError} (<code>null</code>
     *                okay)
     * @param object  Object to check or <code>null</code>
     */
    public static void assertNotNull(String message, Object object) {
        assertTrue(message, object != null);
    }

    /**
     * Asserts that an object isn't null. If it is an {@link AssertionError} is
     * thrown.
     *
     * @param object Object to check or <code>null</code>
     */
    public static void assertNotNull(Object object) {
        assertNotNull(null, object);
    }

    /**
     * Asserts that an object is null. If it is not, an {@link AssertionError}
     * is thrown with the given message.
     *
     * @param message the identifying message for the {@link AssertionError} (<code>null</code>
     *                okay)
     * @param object  Object to check or <code>null</code>
     */
    public static void assertNull(String message, Object object) {
        if (object == null) {
            return;
        }
        failNotNull(message, object);
    }

    /**
     * Asserts that an object is null. If it isn't an {@link AssertionError} is
     * thrown.
     *
     * @param object Object to check or <code>null</code>
     */
    public static void assertNull(Object object) {
        assertNull(null, object);
    }

    private static void failNotNull(String message, Object actual) {
        String formatted = "";
        if (message != null) {
            formatted = message + ' ';
        }
        fail(formatted + "expected null, but was:<" + actual + '>');
    }

    /**
     * Asserts that two objects refer to the same object. If they are not, an
     * {@link AssertionError} is thrown with the given message.
     *
     * @param message  the identifying message for the {@link AssertionError} (<code>null</code>
     *                 okay)
     * @param expected the expected object
     * @param actual   the object to compare to <code>expected</code>
     */
    public static void assertSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        failNotSame(message, expected, actual);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not the
     * same, an {@link AssertionError} without a message is thrown.
     *
     * @param expected the expected object
     * @param actual   the object to compare to <code>expected</code>
     */
    public static void assertSame(Object expected, Object actual) {
        assertSame(null, expected, actual);
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object, an {@link AssertionError} is thrown with the
     * given message.
     *
     * @param message    the identifying message for the {@link AssertionError} (<code>null</code>
     *                   okay)
     * @param unexpected the object you don't expect
     * @param actual     the object to compare to <code>unexpected</code>
     */
    public static void assertNotSame(String message, Object unexpected,
                                     Object actual) {
        if (unexpected == actual) {
            failSame(message);
        }
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object, an {@link AssertionError} without a message is
     * thrown.
     *
     * @param unexpected the object you don't expect
     * @param actual     the object to compare to <code>unexpected</code>
     */
    public static void assertNotSame(Object unexpected, Object actual) {
        assertNotSame(null, unexpected, actual);
    }

    private static void failSame(String message) {
        String formatted = "";
        if (message != null) {
            formatted = message + ' ';
        }
        fail(formatted + "expected not same");
    }

    private static void failNotSame(String message, Object expected,
                                    Object actual) {
        String formatted = "";
        if (message != null) {
            formatted = message + ' ';
        }
        fail(formatted + "expected same:<" + expected + "> was not:<" + actual
                + '>');
    }

    private static void failNotEquals(String message, Object expected,
                                      Object actual) {
        fail(format(message, expected, actual));
    }

    static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && !message.isEmpty()) {
            formatted = message + ' ';
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (expectedString.equals(actualString)) {
            return formatted + "expected: "
                    + formatClassAndValue(expected, expectedString)
                    + " but was: " + formatClassAndValue(actual, actualString);
        } else {
            return formatted + "expected:<" + expectedString + "> but was:<"
                    + actualString + '>';
        }
    }

    private static String formatClassAndValue(Object value, String valueString) {
        String className = value == null ? "null" : value.getClass().getName();
        return className + '<' + valueString + '>';
    }

    /**
     * Thrown when an {@link Assert#assertEquals(Object, Object) assertEquals(String, String)} fails. Create and throw
     * a <code>ComparisonFailure</code> manually if you want to show users the difference between two complex
     * strings.
     * <p/>
     * Inspired by a patch from Alex Chaffee (alex@purpletech.com)
     *
     * @since 4.0
     */
    public static class ComparisonFailure extends AssertionError {
        /**
         * The maximum length for fExpected and fActual. If it is exceeded, the strings should be shortened.
         *
         * @see ComparisonCompactor
         */
        private static final int MAX_CONTEXT_LENGTH = 20;
        private final String fExpected;
        private final String fActual;

        /**
         * Constructs a comparison failure.
         *
         * @param message  the identifying message or null
         * @param expected the expected string value
         * @param actual   the actual string value
         */
        public ComparisonFailure(String message, String expected, String actual) {
            super(message);
            fExpected = expected;
            fActual = actual;
        }

        /**
         * Returns "..." in place of common prefix and "..." in
         * place of common suffix between expected and actual.
         *
         * @see Throwable#getMessage()
         */
        @Override
        public String getMessage() {
            return new ComparisonCompactor(MAX_CONTEXT_LENGTH, fExpected, fActual).compact(super.getMessage());
        }

        /**
         * Returns the actual string value
         *
         * @return the actual string value
         */
        public String getActual() {
            return fActual;
        }

        /**
         * Returns the expected string value
         *
         * @return the expected string value
         */
        public String getExpected() {
            return fExpected;
        }

        private static class ComparisonCompactor {
            private static final String ELLIPSIS = "...";
            private static final String DELTA_END = "]";
            private static final String DELTA_START = "[";
            /**
             * The maximum length for <code>expected</code> and <code>actual</code>. When <code>contextLength</code>
             * is exceeded, the Strings are shortened
             */
            private final int fContextLength;
            private final String fExpected;
            private final String fActual;
            private int fPrefix;
            private int fSuffix;

            /**
             * @param contextLength the maximum length for <code>expected</code> and <code>actual</code>. When contextLength
             *                      is exceeded, the Strings are shortened
             * @param expected      the expected string value
             * @param actual        the actual string value
             */
            public ComparisonCompactor(int contextLength, String expected, String actual) {
                fContextLength = contextLength;
                fExpected = expected;
                fActual = actual;
            }

            private String compact(String message) {
                if (fExpected == null || fActual == null || areStringsEqual()) {
                    return format(message, fExpected, fActual);
                }

                findCommonPrefix();
                findCommonSuffix();
                String expected = compactString(fExpected);
                String actual = compactString(fActual);
                return format(message, expected, actual);
            }

            private String compactString(String source) {
                String result = DELTA_START + source.substring(fPrefix, source.length() - fSuffix + 1) + DELTA_END;
                if (fPrefix > 0) {
                    result = computeCommonPrefix() + result;
                }
                if (fSuffix > 0) {
                    result += computeCommonSuffix();
                }
                return result;
            }

            private void findCommonPrefix() {
                fPrefix = 0;
                int end = Math.min(fExpected.length(), fActual.length());
                for (; fPrefix < end; fPrefix++) {
                    if (fExpected.charAt(fPrefix) != fActual.charAt(fPrefix)) {
                        break;
                    }
                }
            }

            private void findCommonSuffix() {
                int expectedSuffix = fExpected.length() - 1;
                int actualSuffix = fActual.length() - 1;
                for (; actualSuffix >= fPrefix && expectedSuffix >= fPrefix; actualSuffix--, expectedSuffix--) {
                    if (fExpected.charAt(expectedSuffix) != fActual.charAt(actualSuffix)) {
                        break;
                    }
                }
                fSuffix = fExpected.length() - expectedSuffix;
            }

            private String computeCommonPrefix() {
                return (fPrefix > fContextLength ? ELLIPSIS : "") + fExpected.substring(Math.max(0, fPrefix - fContextLength), fPrefix);
            }

            private String computeCommonSuffix() {
                int end = Math.min(fExpected.length() - fSuffix + 1 + fContextLength, fExpected.length());
                return fExpected.substring(fExpected.length() - fSuffix + 1, end) + (fExpected.length() - fSuffix + 1 < fExpected.length() - fContextLength ? ELLIPSIS : "");
            }

            private boolean areStringsEqual() {
                return fExpected.equals(fActual);
            }
        }
    }
}
