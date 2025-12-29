/*
 * Test input for MeaningfulMessageCheck covering duplicate, redundant class/method/line,
 * whitespace run, and too-few-meaningful warnings.
 */
package net.openhft.quality;

import static org.junit.Assert.*;

public class InputAllWarningTypes {

    // ============================================
    // MSG_DUPLICATE - Duplicate assert messages
    // ============================================

    public void testDuplicateMessages() {
        // First occurrence - OK (JUnit 4: message, expected, actual)
        assertEquals("expected checksum digest value", 1, 1);
        // Duplicate - should warn (line 19)
        assertEquals("expected checksum digest value", 2, 2);
    }

    public void testDuplicateInDifferentMethods() {
        // This is still a duplicate (same file) - should warn (line 24)
        assertEquals("expected checksum digest value", 3, 3);
    }

    public void testDuplicateWithJavaAssert() {
        // First occurrence - OK
        assert true : "condition must always hold";
        // Duplicate - should warn (line 31)
        assert false : "condition must always hold";
    }

    public void testMultipleDuplicates() {
        // First occurrences - OK
        assertEquals("alpha test should pass", 1, 1);
        assertEquals("beta test should pass", 2, 2);
        // Duplicates - should warn (lines 39, 40)
        assertEquals("alpha test should pass", 3, 3);
        assertEquals("beta test should pass", 4, 4);
    }

    // ============================================
    // MSG_REDUNDANT_CLASS - Class name in message
    // ============================================

    public void testRedundantClassNameExact() {
        // Contains exact class name - should warn (line 48)
        assertEquals("InputAllWarningTypes test failed", 1, 1);
    }

    public void testRedundantClassNameCaseInsensitive() {
        // Contains class name (different case) - should warn (line 53)
        assertEquals("inputallwarningtypes error occurred", 1, 1);
    }

    public void testRedundantClassNamePartial() {
        // Contains class name as substring - should warn (line 58)
        assertEquals("Error in InputAllWarningTypes.method()", 1, 1);
    }

    // ============================================
    // MSG_REDUNDANT_METHOD - Method name in message
    // ============================================

    public void testRedundantMethodName() {
        // Contains method name - should warn (line 66)
        assertEquals("testRedundantMethodName assertion failed", 1, 1);
    }

    public void anotherMethodWithItsNameInMessage() {
        // Contains method name - should warn (line 71)
        assertEquals("anotherMethodWithItsNameInMessage should not fail", 1, 1);
    }

    public void validateSomething() {
        // Contains method name (case insensitive) - should warn (line 76)
        assertEquals("VALIDATESOMETHING check", 1, 1);
    }

    // ============================================
    // MSG_REDUNDANT_LINE - Line number in message
    // ============================================

    public void testRedundantLineNumberPatterns() {
        // Pattern: "line N" - should warn (line 84)
        assertEquals("error at line 42", 1, 1);
        // Pattern: "line #N" - should warn (line 86)
        assertEquals("failed at line #100", 1, 1);
        // Pattern: "LN" (capital L followed by number) - should warn (line 88)
        assertEquals("check L55 failed", 1, 1);
        // Pattern: ":N" (colon followed by number, like stack trace) - should warn (line 90)
        assertEquals("MyClass.java:123 error", 1, 1);
        // Pattern: "line N" (with word "line") - should warn (line 92)
        assertEquals("see line 999 for details", 1, 1);
    }

    // ============================================
    // Combined warnings - multiple issues in one message
    // ============================================

    public void testCombinedClassAndLine() {
        // Contains both class name AND line number - should warn twice (line 100)
        assertEquals("InputAllWarningTypes failed at line 50", 1, 1);
    }

    public void testCombinedMethodAndLine() {
        // Contains both method name AND line number - should warn twice (line 105)
        assertEquals("testCombinedMethodAndLine error at L99", 1, 1);
    }

    public void testTripleCombined() {
        // Contains class, method AND line - should warn three times (line 110)
        assertEquals("InputAllWarningTypes.testTripleCombined at line 42", 1, 1);
    }

    // ============================================
    // Valid messages - should NOT produce warnings
    // ============================================

    public void testValidMessages() {
        // These should all be OK - no warnings
        assertEquals("expected positive range value", 1, 1);
        assertEquals("array contents should be empty", 0, 0);
        assertEquals("connection should be established", true, true);
        assertEquals("user count mismatch noted", 5, 5);
        assertEquals("price should be within range", 100.0, 100.0, 0.01);
        assertTrue("config file should exist", true);
        assertFalse("error flag should be cleared", false);
        assertNotNull("result cache entry should not be null", new Object());
        assertNull("optional value should be absent", null);
    }

    // Short names should not trigger false positives
    public void ab() {
        // Method name "ab" is too short (< 3 chars) - should NOT warn
        assertEquals("ab test message ok", 1, 1);
    }

    // ============================================
    // MSG_WHITESPACE_RUN - Repeated whitespace
    // ============================================

    public void testWhitespaceRun() {
        // Contains double space - should warn (line 146)
        assertEquals("order  should persist", 1, 1);
    }

    // ============================================
    // MSG_TOO_FEW_MEANINGFUL - Not enough meaningful words
    // ============================================

    public void testTooFewMeaningfulWords() {
        // 4 words, but only 1 meaningful word - should warn (line 155)
        assertEquals("expected value should match", 1, 1);
    }
}
