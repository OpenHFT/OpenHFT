/*
 * Test input for JavadocMessageExtractor edge cases.
 * Tests paragraph extraction, inline tags, and HTML handling.
 */
package net.openhft.quality;

/**
 * Class javadoc first paragraph uses {@link String} and {@code code example}
 * to test inline tag placeholder counting.
 */
@SuppressWarnings("MM-all")
public class InputJavadocEdgeCases {

    /**
     * A field javadoc entry.
     */
    private String fieldWithJavadoc;

    /**
     * First paragraph ends here.
     * <p>
     * This is the second paragraph that should not be extracted.
     */
    public void methodWithParagraphTag() {
    }

    /**
     * Multi-line first paragraph that spans
     * multiple lines with continuation and
     * some extra content here.
     */
    public void multiLineFirstParagraph() {
    }

    /**
     * Short.
     *
     * @param x ignored
     * @return nothing
     */
    public void methodStopsAtAtTag(int x) {
    }

    /**
     * Uses empty inline tag {@code } here.
     */
    public void emptyInlineTag() {
    }

    /**
     * Contains <b>bold</b> and <i>italic</i> HTML.
     */
    public void htmlTagsInContent() {
    }

    /**
     * Uses {@link #methodWithParagraphTag()} and {@link String#valueOf(int)} links.
     */
    public void multipleLinkTags() {
    }

    /**
     * <p>Starts with p tag.
     */
    public void startsWithParagraphTag() {
    }

    /**
     * First paragraph here.<p>Second paragraph inline.
     */
    public void inlineParagraphTag() {
    }

    /**
     *
     */
    public void emptyJavadoc() {
    }

    /**
     * Single word.
     */
    public void singleWord() {
    }

    /**
     * {@code only inline tags here}
     */
    public void onlyInlineTags() {
    }

    /**
     * Enum constant javadoc.
     */
    public enum InnerEnum {
        /**
         * First constant.
         */
        ONE,
        /**
         * Second constant.
         */
        TWO
    }

    /**
     * Nested class javadoc.
     */
    public static class NestedClass {
        /**
         * Nested method.
         */
        public void nestedMethod() {
        }
    }
}
