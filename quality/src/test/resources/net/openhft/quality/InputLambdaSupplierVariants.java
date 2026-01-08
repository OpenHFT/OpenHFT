/*
 * Test input for lambda message supplier extraction paths.
 * Covers extractConstantSupplierMessage, extractTrivialLambdaMessageDirect branches.
 */
package net.openhft.quality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class InputLambdaSupplierVariants {

    private static final Logger LOG = LoggerFactory.getLogger(InputLambdaSupplierVariants.class);

    // --- Simple constant string lambda ---

    public void simpleConstantLambda() {
        assertTrue(true, () -> "Simple constant message for assertion");
    }

    // --- String.format in lambda ---

    public void formatCallLambda() {
        int count = 42;
        assertTrue(count > 0, () -> String.format("Count %d should be positive", count));
    }

    // --- String concatenation in lambda ---

    public void concatenationLambda() {
        String name = "test";
        assertTrue(name != null, () -> "Name parameter: " + name + " should not be null");
    }

    // --- Block-bodied lambda (SLIST) ---

    public void blockBodyLambda() {
        int value = 10;
        assertTrue(value > 0, () -> {
            String prefix = "Validation failed: ";
            return prefix + "value " + value + " is not positive";
        });
    }

    // --- Method reference (not a lambda body) ---

    public void methodReferenceLambda() {
        assertTrue(true, this::createValidationMessage);
    }

    private String createValidationMessage() {
        return "Validation should pass for valid input";
    }

    // --- Nested method call in lambda ---

    public void nestedMethodCallLambda() {
        assertTrue(true, () -> buildMessage("nested", 123));
    }

    private String buildMessage(String key, int value) {
        return String.format("Key %s has value %d", key, value);
    }

    // --- Lambda with parameters (Supplier has none, but test edge case) ---

    public void loggerWithSupplierMessage() {
        int count = 5;
        // SLF4J 2.x style with Supplier
        LOG.debug("Processing {} items", count);
        LOG.info("Cache size: {}", count);
    }

    // --- Empty or trivial lambdas ---

    public void trivialLambdas() {
        assertTrue(true, () -> "");
        assertTrue(true, () -> " ");
    }

    // --- Lambda returning constant field ---

    private static final String CONSTANT_MSG = "Constant field message";

    public void constantFieldLambda() {
        assertTrue(true, () -> CONSTANT_MSG);
    }

    // --- Lambda with ternary operator ---

    public void ternaryLambda() {
        boolean flag = true;
        assertTrue(flag, () -> flag ? "Flag is enabled as expected" : "Flag is disabled unexpectedly");
    }

    // --- Multiple lambdas in one method ---

    public void multipleLambdas() {
        assertAll("Multiple validation checks",
                () -> assertTrue(true, () -> "First check should pass"),
                () -> assertFalse(false, () -> "Second check should pass")
        );
    }
}
