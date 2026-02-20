/*
 * Test input for MeaningfulMessageCheck template edge cases.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;
import java.util.Locale;

public class InputTemplateEdgeCases {

    public void testTemplateEdgeCases() {
        Locale locale = Locale.UK;

        assertEquals(String.format(locale, "comparison"), 1, 1);
        assertEquals(String.format(Locale.getDefault(), "comparison"), 1, 1);
        assertEquals(MessageFormat.format("value {0}", 1), 1, 1);
    }
}
