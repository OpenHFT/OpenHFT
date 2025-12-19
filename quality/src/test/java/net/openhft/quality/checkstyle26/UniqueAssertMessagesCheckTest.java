/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.Test;

public class UniqueAssertMessagesCheckTest extends AbstractModuleTestSupport {

    @Override
    protected String getPackageLocation() {
        return "net/openhft/quality/checkstyle26";
    }

    @Test
    public void testDuplicateMessages() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                "18: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "expected value should match", 12),
        };

        verify(checkConfig, getPath("InputDuplicateAssertMessages.java"), expected);
    }

    @Test
    public void testRedundantClassName() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                "10: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "InputRedundantClassName failed here", "InputRedundantClassName",
                        "only filler words remain: failed, here"),
        };

        verify(checkConfig, getPath("InputRedundantClassName.java"), expected);
    }

    @Test
    public void testRedundantMethodName() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                "10: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "testSomething assertion failed", "testSomething",
                        "only filler words remain: assertion, failed"),
        };

        verify(checkConfig, getPath("InputRedundantMethodName.java"), expected);
    }

    @Test
    public void testRedundantLineNumber() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                "10: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "error at line 10", "line 10"),
                "11: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "check L42 failed", "L42"),
        };

        verify(checkConfig, getPath("InputRedundantLineNumber.java"), expected);
    }

    @Test
    public void testGoodMessages() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {};

        verify(checkConfig, getPath("InputGoodAssertMessages.java"), expected);
    }

    /**
     * Comprehensive test that verifies ALL warning types are detected.
     * This test uses InputAllWarningTypes.java which contains examples
     * of every possible warning the check can produce.
     */
    @Test
    public void testAllWarningTypes() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                // MSG_DUPLICATE - Duplicate assert messages
                "19: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "expected non-null value", 17),
                "24: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "expected non-null value", 17),
                "31: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "condition must hold", 29),
                "39: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "alpha test should pass", 36),
                "40: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "beta test should pass", 37),

                // MSG_REDUNDANT_CLASS - Class name in message
                "49: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "InputAllWarningTypes test failed", "InputAllWarningTypes",
                        "only filler words remain: test, failed"),
                "54: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "inputallwarningtypes error occurred", "InputAllWarningTypes",
                        "only filler words remain: error, occurred"),
                "59: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "Error in InputAllWarningTypes.method()", "InputAllWarningTypes",
                        "only filler words remain: Error, in, method"),

                // MSG_REDUNDANT_METHOD - Method name in message
                "68: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "testRedundantMethodName assertion failed", "testRedundantMethodName",
                        "only filler words remain: assertion, failed"),
                "73: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "anotherMethodWithItsNameInMessage should not fail", "anotherMethodWithItsNameInMessage",
                        "only filler words remain: should, not, fail"),
                "78: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "VALIDATESOMETHING check", "validateSomething",
                        "only filler words remain: check"),

                // MSG_REDUNDANT_LINE - Line number patterns in message
                "87: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "error at line 42", "line 42"),
                "89: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "failed at line #100", "line #100"),
                "91: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "check L55 failed", "L55"),
                "93: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "MyClass.java:123 error", ".java:123"),
                "95: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "see line 999 for details", "line 999"),

                // Combined warnings - multiple issues on same line
                // Line 104: class name + line number
                "104: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "InputAllWarningTypes failed at line 50", "InputAllWarningTypes",
                        "only filler words remain: failed, at, line, 50"),
                "104: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "InputAllWarningTypes failed at line 50", "line 50"),

                // Line 109: method name + line number
                "109: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "testCombinedMethodAndLine error at L99", "testCombinedMethodAndLine",
                        "only 1 meaningful word(s): L99 (filler: error, at)"),
                "109: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "testCombinedMethodAndLine error at L99", "L99"),

                // Line 114: class name + method name + line number (3 warnings)
                "114: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_CLASS,
                        "InputAllWarningTypes.testTripleCombined at line 42", "InputAllWarningTypes",
                        "only 1 meaningful word(s): testTripleCombined (filler: at, line, 42)"),
                "114: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_METHOD,
                        "InputAllWarningTypes.testTripleCombined at line 42", "testTripleCombined",
                        "only 1 meaningful word(s): InputAllWarningTypes (filler: at, line, 42)"),
                "114: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_REDUNDANT_LINE,
                        "InputAllWarningTypes.testTripleCombined at line 42", "line 42"),
        };

        verify(checkConfig, getPath("InputAllWarningTypes.java"), expected);
    }

    /**
     * Test detection of trivial Supplier&lt;String&gt; lambdas.
     * These are lambdas like {@code () -> "constant"} that defeat the purpose
     * of lazy evaluation.
     */
    @Test
    public void testTrivialSupplier() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                // Simple trivial supplier
                "16: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TRIVIAL_SUPPLIER,
                        "expected match"),

                // Concatenation of literals is still trivial
                "21: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATE,
                        "expected match", 16),
                "21: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TRIVIAL_SUPPLIER,
                        "expected match"),

                // Multiple concatenations
                "26: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TRIVIAL_SUPPLIER,
                        "abc"),

                // assertTrue with trivial supplier
                "31: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TRIVIAL_SUPPLIER,
                        "condition should be true"),

                // assertFalse with trivial supplier
                "36: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TRIVIAL_SUPPLIER,
                        "condition should be false"),
        };

        verify(checkConfig, getPath("InputTrivialSupplier.java"), expected);
    }

    /**
     * Test detection of low-signal messages: generic, assertion-restating,
     * index-only, and contextless messages.
     */
    @Test
    public void testLowSignalMessages() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                // MSG_GENERIC - Generic single-word messages (lines 17-26)
                "17: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "actual"),
                "18: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "expected"),
                "19: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "value"),
                "20: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "result"),
                "21: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "data"),
                "22: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "object"),
                "23: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "condition"),
                "24: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "message"),
                "25: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "msg"),
                "26: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "ok"),

                // MSG_RESTATES_ASSERTION - Assertion type repetition (lines 35-51, skipping 39)
                "35: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "assertEquals"),
                "36: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "assertTrue"),
                "37: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "assertFalse"),
                "38: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "assertNull"),
                "40: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "assertNotNull"),
                "41: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should be true"),
                "42: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should be false"),
                "43: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should be null"),
                "44: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should not be null"),
                "45: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "must match"),
                "46: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should match"),
                "47: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "equals"),
                "48: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "not null"),
                "49: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "is null"),
                "50: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "is true"),
                "51: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "is false"),

                // MSG_INDEX_ONLY - Index-only messages (lines 59-68)
                "59: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "0"),
                "60: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "1"),
                "61: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "[0]"),
                "62: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "[5]"),
                "63: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "index 0"),
                "64: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "index 42"),
                "65: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "element 0"),
                "66: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "item 3"),
                "67: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "#0"),
                "68: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_INDEX_ONLY, "i=5"),

                // MSG_CONTEXTLESS - Contextless comparison messages (lines 77-82)
                "77: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "comparison"),
                "78: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "validation"),
                "79: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "equality"),
                "80: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "mismatch"),
                "81: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "failed"),
                "82: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "failure"),

                // Combined: GENERIC + CONTEXTLESS (lines 90-92)
                "90: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "test"),
                "90: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "test"),
                "91: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "check"),
                "91: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "check"),
                "92: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "error"),
                "92: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "error"),

                // Combined: RESTATES_ASSERTION + CONTEXTLESS (lines 100-105)
                "100: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "should be equal"),
                "100: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should be equal"),
                "101: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "must be equal"),
                "101: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "must be equal"),
                "102: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "should equal"),
                "102: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "should equal"),
                "103: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "must equal"),
                "103: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "must equal"),
                "104: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "values should match"),
                "104: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "values should match"),
                "105: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_CONTEXTLESS, "value should match"),
                "105: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_ASSERTION, "value should match"),
        };

        verify(checkConfig, getPath("InputLowSignalMessages.java"), expected);
    }

    /**
     * Test detection of word count and word length issues.
     * Messages should have 3-20 words, and no word longer than 16 characters.
     */
    @Test
    public void testWordMetrics() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                // MSG_TOO_SHORT - Too few words
                "16: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TOO_SHORT,
                        "short", 1, 3),
                "18: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TOO_SHORT,
                        "too short", 2, 3),

                // MSG_TOO_LONG - Too many words (23 words in the message)
                "27: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_TOO_LONG,
                        "this is a very long message with way too many words that exceeds the maximum allowed word count limit of twenty words total",
                        23, 20),

                // MSG_LONG_WORD - Words exceeding 16 characters
                "36: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_LONG_WORD,
                        "error in SomeVeryLongClassName here", "SomeVeryLongClassName", 16),
                "38: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_LONG_WORD,
                        "problem with AnotherExtremelyLongWord detected", "AnotherExtremelyLongWord", 16),
        };

        verify(checkConfig, getPath("InputWordMetrics.java"), expected);
    }

    /**
     * Test Phase 2 rules: AMQ13 (duplicates input), AMQ14 (assertAll heading), AMQ15 (restates derived).
     */
    @Test
    public void testPhase2Rules() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(UniqueAssertMessagesCheck.class);

        final String[] expected = {
                // AMQ13: Message duplicates input
                "20: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATES_INPUT,
                        "admin", "admin"),
                // Line 21: "expected" triggers both AMQ06 (generic) and AMQ13 (duplicates variable name)
                "21: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_GENERIC, "expected"),
                "21: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_DUPLICATES_INPUT,
                        "expected", "expected"),

                // AMQ14: Low-signal assertAll headings
                "35: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_ASSERTALL_HEADING,
                        "assertAll"),
                "39: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_ASSERTALL_HEADING,
                        "assertions"),
                "43: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_ASSERTALL_HEADING,
                        "checks"),
                "47: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_ASSERTALL_HEADING,
                        "validation"),
                "51: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_ASSERTALL_HEADING,
                        "test"),

                // AMQ15: Restates derived assertion
                "69: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "empty", "empty"),
                "70: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "is empty", "is empty"),
                "71: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "not empty", "not empty"),
                "72: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "blank", "blank"),
                "73: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "present", "present"),
                "74: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "not present", "not present"),
                "75: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "contains", "contains"),
                "76: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "matches", "matches"),
                "77: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "size", "size"),
                "78: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "zero", "zero"),
                "79: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "positive", "positive"),
                "80: " + getCheckMessage(UniqueAssertMessagesCheck.MSG_RESTATES_DERIVED,
                        "negative", "negative"),
        };

        verify(checkConfig, getPath("InputPhase2Rules.java"), expected);
    }
}
