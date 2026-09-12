/*
 * Test input for array-valued @SuppressWarnings.
 * Exercises: SuppressionTracker.collectStringValues() ANNOTATION_ARRAY_INIT branch,
 *            SuppressionTracker.findAnnotationValue() ASSIGN pair resolution.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputSuppressWarningsArray {

    @SuppressWarnings({"MMTooShort", "MMContextless"})
    public void suppressedArrayLiteral() {
        assertEquals(1, 1, "ok");
        assertTrue(true, "check");
    }

    @SuppressWarnings(value = {"checkstyle:MeaningfulMessage"})
    public void suppressedNamedValueArray() {
        assertEquals(1, 1, "value");
    }

    @SuppressWarnings(value = "MM-all")
    public void suppressedNamedValueSingle() {
        assertEquals(1, 1, "value");
    }

    public void unsuppressedMethod() {
        assertEquals(1, 1, "expected");
    }
}
