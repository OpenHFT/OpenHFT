/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageTemplateExtractor}.
 */
@SuppressWarnings("MMDisplayName")
@DisplayName("Message template extractor tests scenario case")
class MessageTemplateExtractorTest {

    private MessageTemplateExtractor extractor;

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

    // --- countKeyValueLabels tests ---

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

    private static DetailAST numberLiteral(int type, String text) {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(type);
        when(literal.getText()).thenReturn(text);
        return literal;
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

    private static DetailAST typecast(DetailAST expr) {
        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.getLastChild()).thenReturn(expr);
        return typecast;
    }

    private static DetailAST exprWithChildren(DetailAST first, DetailAST second) {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(first);
        when(first.getNextSibling()).thenReturn(second);
        when(second.getNextSibling()).thenReturn(null);
        return expr;
    }

    private static DetailAST nodeWithChildren(int type, DetailAST... children) {
        DetailAST node = mock(DetailAST.class);
        when(node.getType()).thenReturn(type);
        if (children.length == 0) {
            when(node.getFirstChild()).thenReturn(null);
            return node;
        }
        for (int i = 0; i < children.length - 1; i++) {
            when(children[i].getNextSibling()).thenReturn(children[i + 1]);
        }
        when(children[children.length - 1].getNextSibling()).thenReturn(null);
        when(node.getFirstChild()).thenReturn(children[0]);
        return node;
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

    // --- countAnnotationPlaceholders tests ---

    @BeforeEach
    void setUp() {
        extractor = new MessageTemplateExtractor(null);
    }

    @Test
    @DisplayName("Constructor with null locale detector scenario")
    void constructorWithNullLocaleDetector() {
        MessageTemplateExtractor ext = new MessageTemplateExtractor(null);
        assertNotNull(ext, "should create extractor with null locale detector");
    }

    @Test
    @DisplayName("Constructor with locale detector scenario case")
    void constructorWithLocaleDetector() {
        MessageTemplateExtractor ext = new MessageTemplateExtractor(expr -> true);
        assertNotNull(ext, "should create extractor with locale detector");
    }

    @Test
    @DisplayName("Count key value labels null returns zero")
    void countKeyValueLabels_null_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels(null),
                "null should return 0");
    }

    @Test
    @DisplayName("Count key value labels empty returns zero")
    void countKeyValueLabels_empty_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels(""),
                "empty string should return 0");
    }

    @Test
    @DisplayName("Count key value labels no labels returns zero")
    void countKeyValueLabels_noLabels_returnsZero() {
        assertEquals(0, extractor.countKeyValueLabels("some text without labels"),
                "text without labels should return 0");
    }

    @Test
    @DisplayName("Count key value labels one label")
    void countKeyValueLabels_oneLabel() {
        assertEquals(1, extractor.countKeyValueLabels("key="),
                "one label should return 1");
    }

    @Test
    @DisplayName("Count key value labels one label with colon")
    void countKeyValueLabels_oneLabel_withColon() {
        assertEquals(1, extractor.countKeyValueLabels("key:"),
                "one label with colon should return 1");
    }

    // --- countLogPlaceholders tests ---

    @Test
    @DisplayName("Count key value labels multiple labels")
    void countKeyValueLabels_multipleLabels() {
        assertEquals(2, extractor.countKeyValueLabels("index= size="),
                "two labels should return 2");
    }

    @Test
    @DisplayName("Count annotation placeholders null returns zero")
    void countAnnotationPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders(null),
                "null should return 0");
    }

    @Test
    @DisplayName("Count annotation placeholders empty returns zero")
    void countAnnotationPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders(""),
                "empty string should return 0");
    }

    @Test
    @DisplayName("Count annotation placeholders no placeholders returns zero")
    void countAnnotationPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countAnnotationPlaceholders("some text"),
                "text without placeholders should return 0");
    }

    @Test
    @DisplayName("Count annotation placeholders index placeholder scenario")
    void countAnnotationPlaceholders_indexPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{index}]"),
                "one {index} should return 1");
    }

    // --- countFormatPlaceholders tests ---

    @Test
    @DisplayName("Count annotation placeholders display name placeholder")
    void countAnnotationPlaceholders_displayNamePlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{displayName}]"),
                "one {displayName} should return 1");
    }

    @Test
    @DisplayName("Count annotation placeholders arguments placeholder scenario")
    void countAnnotationPlaceholders_argumentsPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{arguments}]"),
                "one {arguments} should return 1");
    }

    @Test
    @DisplayName("Count annotation placeholders numbered placeholder scenario")
    void countAnnotationPlaceholders_numberedPlaceholder() {
        assertEquals(1, extractor.countAnnotationPlaceholders("Test [{0}]"),
                "one {0} should return 1");
    }

    @Test
    @DisplayName("Count annotation placeholders multiple placeholders scenario")
    void countAnnotationPlaceholders_multiplePlaceholders() {
        assertEquals(2, extractor.countAnnotationPlaceholders("Test [{index}] [{displayName}]"),
                "two placeholders should return 2");
    }

    @Test
    @DisplayName("Count log placeholders null returns zero")
    void countLogPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders(null),
                "null should return 0");
    }

    @Test
    @DisplayName("Count log placeholders empty returns zero")
    void countLogPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders(""),
                "empty string should return 0");
    }

    @Test
    @DisplayName("Count log placeholders no placeholders returns zero")
    void countLogPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countLogPlaceholders("some text without placeholders"),
                "text without placeholders should return 0");
    }

    @Test
    @DisplayName("Count log placeholders slf 4 j style")
    void countLogPlaceholders_slf4jStyle() {
        assertEquals(1, extractor.countLogPlaceholders("Value is {}"),
                "one {} should return 1");
    }

    @Test
    @DisplayName("Count log placeholders multiple slf 4 j")
    void countLogPlaceholders_multipleSlf4j() {
        assertEquals(3, extractor.countLogPlaceholders("a={} b={} c={}"),
                "three {} should return 3");
    }

    @Test
    @DisplayName("Count format placeholders null returns zero")
    void countFormatPlaceholders_null_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders(null),
                "null should return 0");
    }

    @Test
    @DisplayName("Count format placeholders empty returns zero")
    void countFormatPlaceholders_empty_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders(""),
                "empty string should return 0");
    }

    // --- extractStringLiteral null handling ---

    @Test
    @DisplayName("Count format placeholders no placeholders returns zero")
    void countFormatPlaceholders_noPlaceholders_returnsZero() {
        assertEquals(0, extractor.countFormatPlaceholders("some text"),
                "text without placeholders should return 0");
    }

    @Test
    @DisplayName("Count format placeholders percent S scenario")
    void countFormatPlaceholders_percentS() {
        assertEquals(1, extractor.countFormatPlaceholders("Value is %s"),
                "one %s should return 1");
    }

    // --- isConstantStringExpression null handling ---

    @Test
    @DisplayName("Count format placeholders percent D scenario")
    void countFormatPlaceholders_percentD() {
        assertEquals(1, extractor.countFormatPlaceholders("Count: %d"),
                "one %d should return 1");
    }

    // --- extractConstantString null handling ---

    @Test
    @DisplayName("Count format placeholders percent F scenario")
    void countFormatPlaceholders_percentF() {
        assertEquals(1, extractor.countFormatPlaceholders("Value: %f"),
                "one %f should return 1");
    }

    // --- countPlaceholderTokens null handling ---

    @Test
    @DisplayName("Count format placeholders multiple string format")
    void countFormatPlaceholders_multipleStringFormat() {
        assertEquals(3, extractor.countFormatPlaceholders("a=%s b=%d c=%f"),
                "three format placeholders should return 3");
    }

    // --- extractMessageTemplate null handling ---

    @Test
    @DisplayName("Count format placeholders message format scenario")
    void countFormatPlaceholders_messageFormat() {
        assertEquals(1, extractor.countFormatPlaceholders("Value is {0}"),
                "one {0} should return 1");
    }

    @Test
    @DisplayName("Count format placeholders multiple message format")
    void countFormatPlaceholders_multipleMessageFormat() {
        assertEquals(3, extractor.countFormatPlaceholders("a={0} b={1} c={2}"),
                "three {n} should return 3");
    }

    @Test
    @DisplayName("Count format placeholders mixed formats returns max")
    void countFormatPlaceholders_mixedFormats_returnsMax() {
        // If mixed, returns max of either style
        assertEquals(2, extractor.countFormatPlaceholders("{0} {1} %s"),
                "should return max count between styles");
    }

    @Test
    @DisplayName("Count format placeholders escaped percent scenario")
    void countFormatPlaceholders_escapedPercent() {
        // Note: the pattern may still match if % is followed by valid format chars
        // Testing that first % is skipped when followed by another %
        assertEquals(0, extractor.countFormatPlaceholders("progress is %%"),
                "escaped %% should not count when nothing follows");
    }

    @Test
    @DisplayName("Extract string literal null throws NPE")
    void extractStringLiteral_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractStringLiteral(null),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Extract string literal allow method call null throws NPE")
    void extractStringLiteral_allowMethodCall_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractStringLiteral(null, true),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Is constant string expression null throws NPE")
    void isConstantStringExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.isConstantStringExpression(null),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Extract constant string null throws NPE")
    void extractConstantString_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractConstantString(null),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Count placeholder tokens null throws NPE")
    void countPlaceholderTokens_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.countPlaceholderTokens(null),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Count placeholder tokens string literal returns zero")
    void countPlaceholderTokens_stringLiteral_returnsZero() {
        assertEquals(0, extractor.countPlaceholderTokens(stringLiteral("text")),
                "string literal should not count as placeholder");
    }

    @Test
    @DisplayName("Count placeholder tokens dot counts as placeholder")
    void countPlaceholderTokens_dot_countsAsPlaceholder() {
        DetailAST expr = dot(ident("owner"), ident("field"));
        assertEquals(1, extractor.countPlaceholderTokens(expr),
                "dot expression should count as one placeholder");
    }

    @Test
    @DisplayName("Count placeholder tokens typecast uses last child")
    void countPlaceholderTokens_typecast_usesLastChild() {
        DetailAST expr = typecast(ident("value"));
        assertEquals(1, extractor.countPlaceholderTokens(expr),
                "typecast should count placeholder from casted expression");
    }

    @Test
    @DisplayName("Count placeholder tokens expr sums child tokens")
    void countPlaceholderTokens_expr_sumsChildTokens() {
        DetailAST expr = exprWithChildren(ident("first"), stringLiteral("text"));
        assertEquals(1, extractor.countPlaceholderTokens(expr),
                "expr should sum placeholder tokens from children");
    }

    @Test
    @DisplayName("Count placeholder tokens numeric literal counts as placeholder")
    void countPlaceholderTokens_numericLiteral_countsAsPlaceholder() {
        assertEquals(1, extractor.countPlaceholderTokens(numberLiteral(TokenTypes.NUM_INT, "1")),
                "numeric literal should count as placeholder");
    }

    @Test
    @DisplayName("Count placeholder tokens method call counts as placeholder")
    void countPlaceholderTokens_methodCall_countsAsPlaceholder() {
        DetailAST dot = dot(ident("value"), ident("toString"));
        DetailAST methodCall = methodCall(dot, null);
        assertEquals(1, extractor.countPlaceholderTokens(methodCall),
                "method call should count as placeholder");
    }

    @Test
    @DisplayName("Count placeholder tokens unknown node sums children")
    void countPlaceholderTokens_unknownNode_sumsChildren() {
        DetailAST expr = nodeWithChildren(TokenTypes.ELIST, ident("left"), ident("right"));
        assertEquals(2, extractor.countPlaceholderTokens(expr),
                "unknown node should sum placeholder counts from children");
    }

    @Test
    @DisplayName("Extract message template null throws NPE")
    void extractMessageTemplate_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> extractor.extractMessageTemplate(null),
                "should throw NPE for null");
    }

    @Test
    @DisplayName("Extract message template format with locale uses second argument")
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
    @DisplayName("Extract message template formatted uses receiver template")
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
    @DisplayName("Extract message template concatenation produces placeholder")
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
    @DisplayName("Extract message template non format method call returns null")
    void extractMessageTemplate_nonFormatMethodCallReturnsNull() {
        DetailAST dot = dot(ident("value"), ident("toString"));
        DetailAST methodCall = methodCall(dot, null);

        MessageTemplate template = extractor.extractMessageTemplate(methodCall);

        assertNull(template, "non-format method call should not produce a template");
    }

    @Test
    @DisplayName("Extract constant string parts typecast returns literal")
    void extractConstantStringParts_typecast_returnsLiteral() throws Exception {
        DetailAST expr = typecast(stringLiteral("value"));
        assertEquals("value", invokeExtractConstantStringParts(expr, false),
                "typecast should keep literal text");
    }

    @Test
    @DisplayName("Extract constant string parts expr unwraps single child")
    void extractConstantStringParts_exprUnwrapsSingleChild() throws Exception {
        DetailAST expr = expr(stringLiteral("value"));
        assertEquals("value", invokeExtractConstantStringParts(expr, false),
                "expr with single child should unwrap");
    }

    @Test
    @DisplayName("Extract constant string parts returns placeholder when allowed")
    void extractConstantStringParts_returnsPlaceholderWhenAllowed() throws Exception {
        DetailAST expr = ident("value");
        assertEquals("{}", invokeExtractConstantStringParts(expr, true),
                "placeholder should be returned when allowed");
    }

    @Test
    @DisplayName("Extract constant string parts method call returns empty when placeholders disallowed")
    void extractConstantStringParts_methodCall_returnsEmptyWhenPlaceholdersDisallowed() throws Exception {
        DetailAST dot = dot(ident("value"), ident("toString"));
        DetailAST methodCall = methodCall(dot, null);
        assertEquals("", invokeExtractConstantStringParts(methodCall, false),
                "method call should return empty when placeholders disallowed");
    }

    @Test
    @DisplayName("Extract string literal includes placeholder for method call concatenation")
    void extractStringLiteral_includesPlaceholderForMethodCallConcatenation() {
        DetailAST dot = dot(ident("value"), ident("toString"));
        DetailAST methodCall = methodCall(dot, null);
        DetailAST expr = expr(plus(stringLiteral("value "), methodCall));

        String message = extractor.extractStringLiteral(expr, false);

        assertEquals("value {}", message,
                "concatenation with method call should include placeholder");
    }

    @Test
    @DisplayName("Extract string literal returns null for method call when disallowed")
    void extractStringLiteral_returnsNullForMethodCallWhenDisallowed() {
        DetailAST dot = dot(ident("String"), ident("valueOf"));
        DetailAST methodCall = methodCall(dot, elist(exprWithIdent("count")));

        assertNull(extractor.extractStringLiteral(methodCall, false),
                "method call should be rejected when disallowed");
    }

    @Test
    @DisplayName("Extract string literal returns format string when allowed")
    void extractStringLiteral_returnsFormatStringWhenAllowed() {
        DetailAST dot = dot(ident("String"), ident("format"));
        DetailAST methodCall = methodCall(dot, elist(exprWithString("value %s"), exprWithIdent("count")));

        assertEquals("value %s", extractor.extractStringLiteral(methodCall, true),
                "format call should return format string");
    }

    @Test
    @DisplayName("Extract string literal finds empty literal in mixed expression")
    void extractStringLiteral_findsEmptyLiteralInMixedExpression() {
        DetailAST expr = nodeWithChildren(TokenTypes.ELIST, stringLiteral(""), ident("value"));

        assertEquals("", extractor.extractStringLiteral(expr, false),
                "empty literal should be returned when found in mixed expression");
    }

    private String invokeExtractConstantStringParts(DetailAST expr, boolean allowPlaceholder) throws Exception {
        java.lang.reflect.Method method = MessageTemplateExtractor.class
                .getDeclaredMethod("extractConstantStringParts", DetailAST.class, boolean.class);
        method.setAccessible(true);
        return (String) method.invoke(extractor, expr, allowPlaceholder);
    }
}
