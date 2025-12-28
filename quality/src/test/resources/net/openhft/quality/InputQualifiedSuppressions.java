/*
 * Test input for MeaningfulMessageCheck qualified and array suppressions.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputQualifiedSuppressions {

    @java.lang.SuppressWarnings({"MMTooShort", "checkstyle:MMContextless"})
    public void suppressedQualifiedArray() {
        assertTrue(false, "alpha beta gamma");
        assertTrue(false, "indices should be valid");
    }

    @java.lang.SuppressWarnings(value = {"MMTooShort"})
    public void suppressedQualifiedValue() {
        assertTrue(false, "delta epsilon zeta");
    }

    public void unsuppressed() {
        assertTrue(false, "eta theta iota");
        assertTrue(false, "result should match expected");
    }
}
