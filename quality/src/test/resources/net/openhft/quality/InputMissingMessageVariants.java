/*
 * Test input for various assertion message extraction paths.
 * Covers isMissingAssertionMessage, selectByStyle, selectMessageExpression branches.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InputMissingMessageVariants {

    // --- JUnit 5 style assertions with first-arg message ---

    public void junitFirstArgMessage() {
        // Message as first arg (JUnit 4 style)
        org.junit.Assert.assertTrue("Value should be positive for valid input", true);
        org.junit.Assert.assertFalse("Value should not be negative for valid input", false);
        org.junit.Assert.assertEquals("Counts should match after increment", 1, 1);
        org.junit.Assert.assertNull("Reference should be null after clear", null);
    }

    // --- JUnit 5 style assertions with last-arg message ---

    public void junitLastArgMessage() {
        // Message as last arg (JUnit 5 style)
        assertTrue(true, "Value should be true after initialization");
        assertFalse(false, "Value should be false after reset operation");
        assertEquals(1, 1, "Counts should match after update operation");
        assertNull(null, "Reference should be null after cleanup");
    }

    // --- JUnit 5 supplier message (lambda) ---

    public void junitSupplierMessage() {
        int value = 42;
        assertTrue(value > 0, () -> "Value " + value + " should be positive");
        assertEquals(42, value, () -> String.format("Expected 42 but was %d", value));
    }

    // --- AssertJ fluent assertions ---

    public void assertjFluent() {
        assertThat(true).as("Boolean flag should be true after enable").isTrue();
        assertThat("hello").as("Greeting should start with expected prefix").startsWith("he");
        assertThat(42).as("Count should equal expected value").isEqualTo(42);
    }

    // --- AssertJ with describedAs ---

    public void assertjDescribedAs() {
        assertThat(true).describedAs("Flag should be set after operation").isTrue();
        assertThat("world").describedAs("String should contain expected substring").contains("or");
    }

    // --- AssertJ withFailMessage override ---

    public void assertjWithFailMessage() {
        assertThat(true).withFailMessage("Custom failure: flag not set").isTrue();
        assertThat(42).withFailMessage("Custom failure: value mismatch").isEqualTo(42);
    }

    // --- Mixed first/last argument patterns ---

    public void mixedArgumentPatterns() {
        // First string is expected value, not message
        assertEquals("expected", "expected", "Strings should match exactly");

        // Float comparison with delta and message
        assertEquals(1.0f, 1.0f, 0.01f, "Float values should match within tolerance");

        // Array comparison
        assertArrayEquals(new int[]{1, 2}, new int[]{1, 2}, "Arrays should be equal");
    }

    // --- assertAll with heading ---

    public void assertAllWithHeading() {
        assertAll("User validation checks should pass",
                () -> assertTrue(true, "User should be active after login"),
                () -> assertFalse(false, "User should not be locked after login")
        );
    }

    // --- Assertions in loops (need loop index) ---

    public void assertionsInLoop() {
        int[] values = {1, 2, 3};
        for (int i = 0; i < values.length; i++) {
            assertTrue(values[i] > 0, "Value at index " + i + " should be positive");
        }

        for (int value : values) {
            assertTrue(value > 0, "Each value in array should be positive");
        }
    }

    // --- Precondition assertions ---

    public void preconditionAssertions() {
        Object obj = new Object();
        java.util.Objects.requireNonNull(obj, "Object parameter must not be null");

        String name = "test";
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name parameter must not be empty");
        }
    }

    // --- JUnit 5 no-message assertions (missing messages) ---

    public void junitNoMessage() {
        assertTrue(true);
        assertFalse(false);
        assertEquals(1, 1);
        assertNotEquals(1, 2);
        assertNull(null);
        assertNotNull(new Object());
        assertSame("a", "a");
        assertNotSame("a", "b");
    }

    // --- AssertJ fluent chain without as/describedAs ---

    public void assertjFluentNoMessage() {
        assertThat(true).isTrue();
        assertThat("hello").startsWith("he");
        assertThat(42).isEqualTo(42);
        assertThat(java.util.List.of(1, 2)).hasSize(2);
        assertThat("text").isNotEmpty();
    }

    // --- Static import variants ---

    public void staticImportVariants() {
        // Fully qualified vs static import assertions
        org.junit.jupiter.api.Assertions.assertTrue(true, "Full qualified assertion should pass");
        assertTrue(true, "Static imported assertion should pass");
    }

    // --- Good messages that should not trigger violations ---

    public void goodMessages() {
        assertTrue(true, "Cache entry should be valid after initialization");
        assertFalse(false, "Queue should not be empty after adding element");
        assertEquals(1, 1, "Counter value should match expected after increment");
        assertNotNull(new Object(), "Factory should return non-null instance");
    }
}
