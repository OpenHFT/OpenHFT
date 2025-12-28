/*
 * Test input for throw message template variables.
 */
package net.openhft.quality;

public class InputThrowTemplateVariables {

    public void testFormatTemplateVariable(String template, long value) {
        throw new IllegalArgumentException(String.format(template, value));
    }

    public void testFormattedTemplateVariable(String template, long value) {
        throw new IllegalArgumentException(template.formatted(value));
    }
}
