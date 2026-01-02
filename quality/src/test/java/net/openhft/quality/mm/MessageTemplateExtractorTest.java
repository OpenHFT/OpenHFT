/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void extractMessageTemplate_formatWithLocaleUsesSecondArgument() {
        DetailAST localeExpr = exprWithIdent("locale");
        DetailAST templateExpr = exprWithString("value %s");
        DetailAST extraExpr = exprWithIdent("count");
        DetailAST elist = elist(localeExpr, templateExpr, extraExpr);
        DetailAST dot = dot(ident("String"), ident("format"));
        DetailAST methodCall = methodCall(dot, elist);

        MessageTemplateExtractor localExtractor = new MessageTemplateExtractor(expr -> expr == localeExpr);
        MessageTemplate template = localExtractor.extractMessageTemplate(methodCall);

        assertNotNull(template, "format call should produce a template");
        assertEquals("value %s", template.message(), "template should use format string argument");
        assertEquals(1, template.placeholderCount(), "template should count format placeholders");
        assertTrue(template.fromFormatCall(), "template should be marked as format call");
    }

    @Test
    void extractMessageTemplate_formattedUsesReceiverTemplate() {
        DetailAST receiver = stringLiteral("count %d");
        DetailAST dot = dot(receiver, ident("formatted"));
        DetailAST methodCall = methodCall(dot, null);

        MessageTemplate template = extractor.extractMessageTemplate(methodCall);

        assertNotNull(template, "formatted call should produce a template");
        assertEquals("count %d", template.message(), "template should use receiver string");
        assertEquals(1, template.placeholderCount(), "template should count format placeholders");
        assertTrue(template.fromFormatCall(), "formatted template should be marked as format call");
    }

    @Test
    void extractMessageTemplate_concatenationProducesPlaceholder() {
        DetailAST plus = plus(stringLiteral("value "), ident("count"));
        DetailAST expr = expr(plus);

        MessageTemplate template = extractor.extractMessageTemplate(expr);

        assertNotNull(template, "concatenation should produce a template");
        assertEquals("value {}", template.message(), "template should include placeholder for concatenated value");
        assertEquals(1, template.placeholderCount(), "template should count concatenation placeholder");
        assertFalse(template.fromFormatCall(), "concatenation template should not be marked as format call");
    }

    @Test
    void extractMessageTemplate_nonFormatMethodCallReturnsNull() {
        DetailAST dot = dot(ident("value"), ident("toString"));
        DetailAST methodCall = methodCall(dot, null);

        MessageTemplate template = extractor.extractMessageTemplate(methodCall);

        assertNull(template, "non-format method call should not produce a template");
    }

    private static DetailAST expr(DetailAST child) {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getChildCount()).thenReturn(1);
        when(expr.getFirstChild()).thenReturn(child);
        when(expr.getLastChild()).thenReturn(child);
        return expr;
    }

    private static DetailAST exprWithString(String text) {
        return expr(stringLiteral(text));
    }

    private static DetailAST exprWithIdent(String name) {
        return expr(ident(name));
    }

    private static DetailAST stringLiteral(String text) {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(literal.getText()).thenReturn("\"" + text + "\"");
        return literal;
    }

    private static DetailAST ident(String text) {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(text);
        return ident;
    }

    private static DetailAST plus(DetailAST left, DetailAST right) {
        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);
        when(plus.getFirstChild()).thenReturn(left);
        when(plus.getLastChild()).thenReturn(right);
        return plus;
    }

    private static DetailAST dot(DetailAST left, DetailAST right) {
        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(left);
        when(dot.getLastChild()).thenReturn(right);
        return dot;
    }

    private static DetailAST methodCall(DetailAST dot, DetailAST elist) {
        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(methodCall.findFirstToken(TokenTypes.DOT)).thenReturn(dot);
        when(methodCall.findFirstToken(TokenTypes.ELIST)).thenReturn(elist);
        return methodCall;
    }

    private static DetailAST elist(DetailAST... exprs) {
        DetailAST elist = mock(DetailAST.class);
        if (exprs.length == 0) {
            when(elist.getFirstChild()).thenReturn(null);
            return elist;
        }
        for (int i = 0; i < exprs.length - 1; i++) {
            when(exprs[i].getNextSibling()).thenReturn(exprs[i + 1]);
        }
        when(exprs[exprs.length - 1].getNextSibling()).thenReturn(null);
        when(elist.getFirstChild()).thenReturn(exprs[0]);
        return elist;
    }
}
