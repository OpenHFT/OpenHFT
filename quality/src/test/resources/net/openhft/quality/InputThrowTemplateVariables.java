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

    // Line-before comments should suppress missing message violations
    public void testLineBeforeCommentSuppresses(String msg) {
        // Caller supplies the message for this validation error
        throw new IllegalArgumentException(msg);
    }

    public void testLineBeforeCommentWithFormat(String template, long value) {
        // Format string supplied by caller describes the context
        throw new IllegalArgumentException(String.format(template, value));
    }

    // Inline comment should also suppress
    public void testInlineCommentSuppresses(String msg) {
        throw new IllegalArgumentException(/* caller-provided message */ msg);
    }
}
