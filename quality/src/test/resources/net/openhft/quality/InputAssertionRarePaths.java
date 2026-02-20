/*
 * Test input for rare assertion paths in AssertionMessageExtractor.
 * Exercises edge cases with minimal or unusual argument patterns.
 */
package net.openhft.quality;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests rare assertion paths for coverage.
 */
@SuppressWarnings("MM-all")
public class InputAssertionRarePaths {

    // --- assertAll with various argument counts ---

    static {
        // Static assertions are unusual but valid
        if (!"test".equals("test")) {
            throw new AssertionError("static check failed");
        }
    }

    {
        // Instance initializer assertion
        assertNotNull(this.getClass(), "class should not be null");
    }

    @Test
    void assertAllNoArguments() {
        // Empty assertAll - should not require a message
        assertAll();
    }

    // --- assertThrows with 2 arguments ---

    @Test
    void assertAllSingleExecutable() {
        // Single executable, no heading
        assertAll(() -> assertTrue(true));
    }

    @Test
    void assertAllWithHeading() {
        // With heading string
        assertAll("grouped assertions",
                () -> assertTrue(true),
                () -> assertNotNull("value"));
    }

    // --- assertTimeout with various argument counts ---

    @Test
    void assertThrowsTwoArgs() {
        // Exactly 2 arguments - no message
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException();
        });
    }

    @Test
    void assertThrowsThreeArgs() {
        // violation below, needs message placeholder
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("bad");
        }, "should throw when invalid");
    }

    @Test
    void assertTimeoutTwoArgs() {
        // 2 arguments - duration and executable
        assertTimeout(Duration.ofSeconds(1), () -> {
            // do something
        });
    }

    // --- assertDoesNotThrow variants ---

    @Test
    void assertTimeoutThreeArgsMessage() {
        // 3 arguments with message
        assertTimeout(Duration.ofSeconds(1), () -> {
            // do something
        }, "should complete within timeout");
    }

    @Test
    void assertTimeoutThreeArgsSupplier() {
        // 3 arguments with Supplier
        String result = assertTimeout(Duration.ofSeconds(1), () -> "computed");
        assertNotNull(result, "result should not be null");
    }

    @Test
    void assertDoesNotThrowSingleArg() {
        // Single argument - just the executable
        assertDoesNotThrow(() -> {
            String s = "test";
        });
    }

    // --- assertEquals with minimal arguments ---

    @Test
    void assertDoesNotThrowTwoArgsMessage() {
        // Two arguments - executable and message
        assertDoesNotThrow(() -> {
            String s = "test";
        }, "should not throw");
    }

    @Test
    void assertDoesNotThrowTwoArgsSupplier() {
        // Two arguments - ThrowingSupplier returns value
        String result = assertDoesNotThrow(() -> "value");
        assertNotNull(result, "result should not be null");
    }

    @Test
    void assertEqualsNoMessage() {
        // 2 arguments - no message
        assertEquals(1, 1);
    }

    // --- Nested assertion calls ---

    @Test
    void assertEqualsWithMessage() {
        // 3 arguments - with message
        assertEquals(1, 1, "values should be equal");
    }

    @Test
    void assertEqualsWithSupplier() {
        // 3 arguments - with Supplier message
        assertEquals(1, 1, () -> "computed message");
    }

    @Test
    void nestedAssertions() {
        assertAll("outer",
                () -> assertAll("inner",
                        () -> assertTrue(true),
                        () -> assertEquals(1, 1)),
                () -> assertNotNull("value"));
    }

    // --- fail() variants ---

    @Test
    void assertionInLambda() {
        Runnable r = () -> assertTrue(true, "lambda assertion");
        r.run();
    }

    @Test
    void supplierReturningAssertion() {
        Supplier<Boolean> s = () -> {
            assertTrue(true, "inside supplier");
            return true;
        };
        assertTrue(s.get(), "supplier should return true");
    }

    @Test
    void failNoArgs() {
        try {
            fail();
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    void failWithMessage() {
        try {
            fail("explicit failure message");
        } catch (AssertionError e) {
            // expected
        }
    }

    // --- Qualified assertion calls ---

    @Test
    void failWithSupplier() {
        try {
            fail(() -> "computed failure message");
        } catch (AssertionError e) {
            // expected
        }
    }

    // --- Assertions with complex expressions ---

    @Test
    void failWithCause() {
        try {
            fail("failure with cause", new RuntimeException("cause"));
        } catch (AssertionError e) {
            // expected
        }
    }

    @Test
    void qualifiedAssertions() {
        org.junit.jupiter.api.Assertions.assertTrue(true);
        org.junit.jupiter.api.Assertions.assertEquals(1, 1);
    }

    // --- Edge case: assertion in static initializer ---

    @Test
    void assertWithMethodCallInExpected() {
        assertEquals(String.valueOf(1), "1", "method call as expected value");
    }

    // --- Edge case: assertion in instance initializer ---

    @Test
    void assertWithTernaryMessage() {
        boolean flag = true;
        assertTrue(flag, flag ? "flag is true" : "flag is false");
    }

    // --- Edge case: chained method returning assertion-like ---

    @Test
    void chainedMethodCalls() {
        StringBuilder sb = new StringBuilder();
        assertNotNull(sb.append("a").append("b").toString(), "chained result");
    }
}
