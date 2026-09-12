/*
 * Test input for text block message extraction.
 * Exercises: MessageExtractionContext.findArgumentListRangeByScan() text-block detection,
 *            MessageTemplateExtractor text-block template extraction.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputTextBlockMessages {

    public void textBlockMessage() {
        assertTrue(true, """
                cache should remain warm after init because \
                cold starts are expensive and noisy""");
    }

    public void textBlockWithPlaceholder() {
        assertEquals(1, 1, """
                computed result should match expected \
                baseline value for consistency""");
    }
}
