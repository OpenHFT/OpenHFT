/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MessageTemplateExtractor}.
 */
@SuppressWarnings("MMDisplayName")
class MessageTemplateExtractorTest {

    private MessageTemplateExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new MessageTemplateExtractor(null);
    }

    @Test
    void constructorWithNullLocaleDetector() {
        MessageTemplateExtractor ext = new MessageTemplateExtractor(null);
        assertNotNull(ext, "should create extractor with null locale detector");
    }

    @Test
    void constructorWithLocaleDetector() {
        MessageTemplateExtractor ext = new MessageTemplateExtractor(expr -> true);
        assertNotNull(ext, "should create extractor with locale detector");
    }

    // --- countKeyValueLabels tests ---

    @Test
    void countKeyValueLabels_null_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels(null),
                "null should return 0");
    }

    @Test
    void countKeyValueLabels_empty_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels(""),
                "empty string should return 0");
    }

    @Test
    void countKeyValueLabels_noLabels_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels("some text without labels"),
                "text without labels should return 0");
    }

    @Test
    void countKeyValueLabels_oneLabel() {
        assertEquals(1, extractor.countKeyValueLabels("key="),
                "one label should return 1");
    }

    @Test
    void countKeyValueLabels_oneLabel_withColon() {
        assertEquals(1, extractor.countKeyValueLabels("key:"),
                "one label with colon should return 1");
    }

    @Test
    void countKeyValueLabels_multipleLabels() {
        assertEquals(2, extractor.countKeyValueLabels("index= size="),
                "two labels should return 2");
    }

    // --- countAnnotationPlaceholders tests ---

    @Test
    void countAnnotationPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders(null),
                "null should return 0");
    }

    @Test
    void countAnnotationPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders(""),
                "empty string should return 0");
    }

    @Test
    void countAnnotationPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders("some text"),
                "text without placeholders should return 0");
    }

    @Test
    void countAnnotationPlaceholders_indexPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{index}]"),
                "one {index} should return 1");
    }

    @Test
    void countAnnotationPlaceholders_displayNamePlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{displayName}]"),
                "one {displayName} should return 1");
    }

    @Test
    void countAnnotationPlaceholders_argumentsPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{arguments}]"),
                "one {arguments} should return 1");
    }

    @Test
    void countAnnotationPlaceholders_numberedPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{0}]"),
                "one {0} should return 1");
    }

    @Test
    void countAnnotationPlaceholders_multiplePlaceholders() {
        assertEquals(2, extractor.countAnnotationPlaceholders("Test [{index}] [{displayName}]"),
                "two placeholders should return 2");
    }

    // --- countLogPlaceholders tests ---

    @Test
    void countLogPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders(null),
                "null should return 0");
    }

    @Test
    void countLogPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders(""),
                "empty string should return 0");
    }

    @Test
    void countLogPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders("some text without placeholders"),
                "text without placeholders should return 0");
    }

    @Test
    void countLogPlaceholders_slf4jStyle() {
        assertEquals(1, extractor.countLogPlaceholders("Value is {}"),
                "one {} should return 1");
    }

    @Test
    void countLogPlaceholders_multipleSlf4j() {
        assertEquals(3, extractor.countLogPlaceholders("a={} b={} c={}"),
                "three {} should return 3");
    }

    // --- countFormatPlaceholders tests ---

    @Test
    void countFormatPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders(null),
                "null should return 0");
    }

    @Test
    void countFormatPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders(""),
                "empty string should return 0");
    }

    @Test
    void countFormatPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders("some text"),
                "text without placeholders should return 0");
    }

    @Test
    void countFormatPlaceholders_percentS() {
        assertEquals(1, extractor.countFormatPlaceholders("Value is %s"),
                "one %s should return 1");
    }

    @Test
    void countFormatPlaceholders_percentD() {
        assertEquals(1, extractor.countFormatPlaceholders("Count: %d"),
                "one %d should return 1");
    }

    @Test
    void countFormatPlaceholders_percentF() {
        assertEquals(1, extractor.countFormatPlaceholders("Value: %f"),
                "one %f should return 1");
    }

    @Test
    void countFormatPlaceholders_multipleStringFormat() {
        assertEquals(3, extractor.countFormatPlaceholders("a=%s b=%d c=%f"),
                "three format placeholders should return 3");
    }

    @Test
    void countFormatPlaceholders_messageFormat() {
        assertEquals(1, extractor.countFormatPlaceholders("Value is {0}"),
                "one {0} should return 1");
    }

    @Test
    void countFormatPlaceholders_multipleMessageFormat() {
        assertEquals(3, extractor.countFormatPlaceholders("a={0} b={1} c={2}"),
                "three {n} should return 3");
    }

    @Test
    void countFormatPlaceholders_mixedFormats_returnsMax() {
        // If mixed, returns max of either style
        assertEquals(2, extractor.countFormatPlaceholders("{0} {1} %s"),
                "should return max count between styles");
    }

    @Test
    void countFormatPlaceholders_escapedPercent() {
        // Note: the pattern may still match if % is followed by valid format chars
        // Testing that first % is skipped when followed by another %
        assertEquals(0, extractor.countFormatPlaceholders("progress is %%"),
                "escaped %% should not count when nothing follows");
    }

    // --- extractStringLiteral null handling ---

    @Test
    void extractStringLiteral_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractStringLiteral(null),
                "should throw NPE for null");
    }

    @Test
    void extractStringLiteral_allowMethodCall_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractStringLiteral(null, true),
                "should throw NPE for null");
    }

    // --- isConstantStringExpression null handling ---

    @Test
    void isConstantStringExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.isConstantStringExpression(null),
                "should throw NPE for null");
    }

    // --- extractConstantString null handling ---

    @Test
    void extractConstantString_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractConstantString(null),
                "should throw NPE for null");
    }

    // --- countPlaceholderTokens null handling ---

    @Test
    void countPlaceholderTokens_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.countPlaceholderTokens(null),
                "should throw NPE for null");
    }

    // --- extractMessageTemplate null handling ---

    @Test
    void extractMessageTemplate_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractMessageTemplate(null),
                "should throw NPE for null");
    }
}
