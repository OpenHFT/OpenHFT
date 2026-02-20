/*
 * Test input for MeaningfulMessageCheck rule-code suppressions.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputRuleCodeSuppressions {

    @SuppressWarnings("MMTooShort")
    public void suppressedTooShort() {
        assertTrue(false, "alpha beta gamma");
    }

    public void unsuppressedTooShort() {
        assertTrue(false, "delta epsilon zeta");
    }

    @SuppressWarnings("checkstyle:MMContextless")
    public void suppressedContextless() {
        assertTrue(false, "operation result should equal expected value");
    }

    public void unsuppressedContextless() {
        assertTrue(false, "operation result should equal expected value");
    }

    @SuppressWarnings("MMDuplicate")
    public void suppressedDuplicateMessage() {
        assertTrue(false, "duplicate message should be suppressed by rule code");
        assertTrue(false, "duplicate message should be suppressed by rule code");
    }
}
