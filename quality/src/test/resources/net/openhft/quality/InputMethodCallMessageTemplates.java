/*
 * Test input for MeaningfulMessageCheck method-call message templates.
 */
package net.openhft.quality;

import static org.junit.Assert.*;

import java.text.MessageFormat;
import java.util.Locale;

public class InputMethodCallMessageTemplates {

    public void testFormatMessages() {
        assertEquals(String.format("expected"), 1, 1);
        assertEquals(String.format(Locale.US, "comparison"), 1, 1);
        assertEquals(String.format("value %s", 1), 1, 1);
        assertEquals("ok %s".formatted(1), 1, 1);
        assertEquals(MessageFormat.format("expected", 1), 1, 1);
    }
}
