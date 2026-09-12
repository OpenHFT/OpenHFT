/*
 * Test input for trivial Supplier<String> detection.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

public class InputTrivialSupplier {

    // ============================================
    // MSG_TRIVIAL_SUPPLIER - Trivial lambdas
    // ============================================

    public void testTrivialSupplierSimple() {
        // Simple trivial supplier - should warn (line 16)
        assertEquals(1, 1, () -> "expected match");
    }

    public void testTrivialSupplierConcatenation() {
        // Concatenation of string literals is still trivial - should warn (line 21)
        assertEquals(1, 1, () -> "expected " + "match");
    }

    public void testTrivialSupplierMultipleConcatenation() {
        // Multiple concatenations still trivial - should warn (line 26)
        assertEquals(1, 1, () -> "a" + "b" + "c");
    }

    public void testTrivialSupplierInAssertTrue() {
        // Also applies to assertTrue - should warn (line 31)
        assertTrue(true, () -> "condition should be true");
    }

    public void testTrivialSupplierInAssertFalse() {
        // Also applies to assertFalse - should warn (line 36)
        assertFalse(false, () -> "condition should be false");
    }

    // ============================================
    // Simple concatenations - should warn
    // ============================================

    private String computeExpensiveValue() {
        return "computed";
    }

    public void testValidSupplierWithMethodCall() {
        // Simple concatenation with method call - should warn
        assertEquals(1, 1, () -> "expected " + computeExpensiveValue());
    }

    public void testValidSupplierWithVariable() {
        // Simple concatenation with variable - should warn
        int x = 5;
        assertEquals(1, 1, () -> "expected value " + x);
    }

    public void testValidSupplierWithToString() {
        // Simple concatenation with toString() - should warn
        Object obj = new Object();
        assertEquals(1, 1, () -> "object: " + obj);
    }

    public void testPlainStringIsOk() {
        // Plain string (not in lambda) is OK - no trivial supplier warning
        // (but may trigger other warnings if duplicate)
        assertEquals(1, 1, "plain string message here");
    }

    // ============================================
    // Edge cases
    // ============================================

    public void testLambdaWithParameter() {
        // Lambda with parameter is not a Supplier<String> - should NOT warn
        // This is a different functional interface
        java.util.function.Function<Integer, String> f = (n) -> "value: " + n;
    }
}
