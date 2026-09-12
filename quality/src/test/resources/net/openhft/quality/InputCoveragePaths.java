/*
 * Test input for MeaningfulMessageCheck code coverage paths.
 * Exercises edge cases and branches not covered by other test files.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

public class InputCoveragePaths {

    private String instanceField = "field value";

    // Lambda with parameters (should not extract message from parameterized lambda)
    public void lambdaWithParameters() {
        java.util.List<String> items = java.util.Arrays.asList("a", "b");
        items.forEach(x -> assertTrue(x != null, "each item should be validated"));
    }

    // Nested lambda expressions
    public void nestedLambdas() {
        Supplier<Supplier<String>> nested = () -> () -> "outer cache entry and inner should nest";
        assertFalse(false, nested.get().get());
    }

    // DOT expression for field access with type lookup
    public void dotExpressionFieldAccess() {
        Container c = new Container();
        assertTrue(c.value.length() > 0, "container field should have content");
    }

    // String search on literal (no variable name to extract)
    public void stringSearchOnLiteral() {
        assertTrue("hello world".contains("wor"), "literal string should contain");
    }

    // String search with method call argument
    public void stringSearchWithMethodArg() {
        String text = "alpha";
        assertTrue(text.startsWith(getPrefix()), "text should start with prefix");
    }

    // Cheap supplier description with very long string literal (triggers truncation)
    public void cheapSupplierWithLongString() {
        assertTrue(true, () -> "this is a very long string that exceeds twelve characters for truncation test " + instanceField);
    }

    // Supplier that returns empty after normalization
    public void supplierWithWhitespaceOnly() {
        Supplier<String> s = () -> "   " + instanceField;
        assertFalse(false, s.get());
    }

    // TYPECAST with recursive string check
    public void typecastExpression() {
        Object msg = "cast message should validate";
        assertTrue(true, (String) (Object) msg);
    }

    // Comparison with method call on both sides
    public void comparisonWithMethodCalls() {
        assertTrue(count() > limit(), "count should exceed limit");
    }

    // JUnit 4 style assertion (message first)
    public void junit4StyleAssertion() {
        assertEquals("expected comparison", 1, 1);
    }

    // Unknown assertion style - both arguments are string literals
    public void unknownAssertionStyle() {
        assertEquals("first string", "second string");
    }

    // Boolean assertion with complex condition
    public void complexBooleanCondition() {
        assertTrue(isReady() && isValid(), "condition should be ready and valid");
    }

    // Assertion with null message expression
    public void assertionWithNullMessage() {
        assertTrue(true, (String) null);
    }

    // String concatenation in supplier body
    public void supplierWithConcatenation() {
        int idx = 5;
        assertTrue(true, () -> "index: " + idx + " is valid");
    }

    // Supplier with format call
    public void supplierWithFormat() {
        assertTrue(true, () -> String.format("value is %d", 42));
    }

    // Deeply nested DOT expression
    public void deeplyNestedDot() {
        Container c = new Container();
        assertTrue(c.inner.deepValue.length() > 0, "deeply nested field should exist");
    }

    // Supplier returning only identifier
    public void supplierReturningIdent() {
        assertTrue(true, () -> instanceField);
    }

    // Cheap supplier with string literal prefix + identifier (exercises hasNonConstantPart L203)
    public void cheapSupplierWithLiteralPrefix() {
        assertTrue(true, () -> "prefix: " + instanceField);
    }

    // Cheap supplier with very long string literal + identifier (exercises describeCheapExpressionPart L217-218)
    public void cheapSupplierWithVeryLongLiteral() {
        assertTrue(true, () -> "this string is definitely longer than twelve characters: " + instanceField);
    }

    private int count() {
        return 10;
    }

    private int limit() {
        return 5;
    }

    private String getPrefix() {
        return "al";
    }

    private boolean isReady() {
        return true;
    }

    private boolean isValid() {
        return true;
    }

    private static final class Container {
        String value = "test";
        Inner inner = new Inner();
    }

    private static final class Inner {
        String deepValue = "deep";
    }
}
