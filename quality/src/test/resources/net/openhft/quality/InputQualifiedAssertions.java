/*
 * Test input for fully-qualified assertion calls.
 * Exercises: AssertionMessageExtractor.resolveAssertionStyle() — qualified JUnit4 and JUnit5 paths,
 *            isMissingMessageForAssertAssume() JUNIT5 arm,
 *            assertAll with no heading (qualified).
 */
package net.openhft.quality;

public class InputQualifiedAssertions {

    public void qualifiedJunit4Assert() {
        org.junit.Assert.assertEquals("total should remain stable after recomputation", 1, 1);
    }

    public void qualifiedJunit4Assume() {
        org.junit.Assume.assumeTrue("platform should support native allocation for this test", true);
    }

    public void qualifiedJunit5Assert() {
        org.junit.jupiter.api.Assertions.assertEquals(1, 1,
                "computed digest should match expected hash value");
    }

    public void qualifiedJunit5Assume() {
        org.junit.jupiter.api.Assumptions.assumeTrue(true,
                "runtime should expose management beans for monitoring");
    }

    public void qualifiedAssertAll() {
        org.junit.jupiter.api.Assertions.assertAll(
                () -> org.junit.jupiter.api.Assertions.assertTrue(true,
                        "first condition should hold after initialisation"),
                () -> org.junit.jupiter.api.Assertions.assertNotNull("value",
                        "reference should not be null after construction")
        );
    }
}
