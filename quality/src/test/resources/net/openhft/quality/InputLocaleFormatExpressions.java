/*
 * Test input for locale detection in String.format.
 */
package net.openhft.quality;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

public class InputLocaleFormatExpressions {

    private final Locale fieldLocale = Locale.CANADA;

    public void testLocaleFormats() {
        Locale locale = Locale.UK;

        assertEquals(String.format(locale, "alpha beta gamma"), 1, 1);
        assertEquals(String.format(Locale.US, "delta epsilon zeta"), 1, 1);
        assertEquals(String.format(locale.US, "eta theta iota"), 1, 1);
        assertEquals(String.format(new Locale("en", "GB"), "kappa lambda mu"), 1, 1);
        assertEquals(String.format(Locale.getDefault(), "nu xi omicron"), 1, 1);
        assertEquals(String.format(locale.stripExtensions(), "pi rho sigma"), 1, 1);
        assertEquals(String.format(fieldLocale, "tau upsilon phi"), 1, 1);
    }
}
