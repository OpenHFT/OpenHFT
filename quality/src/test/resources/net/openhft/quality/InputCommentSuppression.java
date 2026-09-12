/*
 * Test input for comment-based suppression ranges.
 * Exercises: SuppressionTracker.recordCommentSuppressions() — OFF/ON range,
 *            MeaningfulMessageProcessor.isSuppressedInFile() returning true for comment range.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputCommentSuppression {

    // MeaningfulMessage: OFF
    public void insideCommentSuppressedRange() {
        assertEquals(1, 1, "ok");
        assertTrue(true, "check");
    }
    // MeaningfulMessage: ON

    public void outsideCommentSuppressedRange() {
        assertEquals(1, 1, "expected");
    }
}
