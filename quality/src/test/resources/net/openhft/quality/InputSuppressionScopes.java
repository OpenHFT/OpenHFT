/*
 * Test input for MeaningfulMessageCheck suppression scopes.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputSuppressionScopes {

    @SuppressWarnings("MMTooShort")
    @SuppressWarnings("checkstyle:MMContextless")
    public void suppressedClassScope() {
        assertTrue(false, "alpha beta gamma");
        assertTrue(false, "operation result should equal expected value");
    }

    @SuppressWarnings(value = "MMTooShort")
    public void suppressedTooShortOnly() {
        assertTrue(false, "delta epsilon zeta");
        assertTrue(false, "result should match expected");
    }

    @SuppressWarnings("checkstyle:MMContextless")
    @SuppressWarnings("MMTooShort")
    public void suppressedArray() {
        assertTrue(false, "theta iota kappa");
        assertTrue(false, "values should be equal");
    }

    @SuppressWarnings("checkstyle:MeaningfulMessageCheck")
    public void suppressedAllByClassName() {
        assertTrue(false, "lambda mu nu");
        assertTrue(false, "result should equal expected");
    }

    public void unsuppressed() {
        assertTrue(false, "omicron pi rho");
        assertTrue(false, "indices should be valid");
    }
}
