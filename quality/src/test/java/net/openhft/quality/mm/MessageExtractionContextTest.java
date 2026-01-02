/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageExtractionContext}.
 */
@SuppressWarnings("MMDisplayName")
class MessageExtractionContextTest {

    private MessageExtractionContext context;

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
    }

    // --- Basic accessors ---

    @Test
    void astSupportNotNull() {
        assertNotNull(context.astSupport());
    }

    @Test
    void fileContentsNullInitially() {
        assertNull(context.fileContents());
    }

    @Test
    void templateExtractorNullInitially() {
        assertNull(context.templateExtractor());
    }

    @Test
    void setTemplateExtractor() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);
        context.setTemplateExtractor(extractor);
        assertEquals(extractor, context.templateExtractor());
    }

    @Test
    void currentClassNameNullInitially() {
        assertNull(context.currentClassName());
    }

    @Test
    void currentMethodNameNullInitially() {
        assertNull(context.currentMethodName());
    }

    // --- reset ---

    @Test
    void resetClearsState() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(null);
        assertNull(context.fileContents());
        assertNull(context.currentClassName());
        assertNull(context.currentMethodName());
    }

    // --- setIgnoredExceptionClassNames ---

    @Test
    void setIgnoredExceptionClassNamesNull() {
        context.setIgnoredExceptionClassNames(null);
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    @Test
    void setIgnoredExceptionClassNamesEmpty() {
        context.setIgnoredExceptionClassNames(Collections.emptySet());
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    @Test
    void setIgnoredExceptionClassNamesWithValues() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        ignored.add("RuntimeException");
        context.setIgnoredExceptionClassNames(ignored);

        assertTrue(context.isIgnoredExceptionClass("IOException"));
        assertTrue(context.isIgnoredExceptionClass("RuntimeException"));
        assertFalse(context.isIgnoredExceptionClass("IllegalStateException"));
    }

    @Test
    void setIgnoredExceptionClassNamesNormalisesFullyQualified() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        // Fully qualified name should be normalised
        assertTrue(context.isIgnoredExceptionClass("java.io.IOException"));
    }

    // --- isIgnoredExceptionClass edge cases ---

    @Test
    void isIgnoredExceptionClassNull() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(null));
    }

    @Test
    void isIgnoredExceptionClassEmpty() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(""));
    }

    // --- JUnit static method detection ---

    @Test
    void isStaticJUnit4MethodFalseInitially() {
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    void isStaticJUnit5MethodFalseInitially() {
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    // --- resolveTypeName ---

    @Test
    void resolveTypeNameSimple() {
        assertEquals("String", context.resolveTypeName("String"));
    }

    @Test
    void resolveTypeNameFullyQualified() {
        assertEquals("java.lang.String", context.resolveTypeName("java.lang.String"));
    }

    // --- getVariableType ---

    @Test
    void getVariableTypeUnknown() {
        assertNull(context.getVariableType("unknown"));
    }

    // --- importedClass ---

    @Test
    void importedClassUnknown() {
        assertNull(context.importedClass("Unknown"));
    }

    // --- leaveMethod ---

    @Test
    void leaveMethodClearsMethodName() {
        context.leaveMethod();
        assertNull(context.currentMethodName());
    }

    // --- JUnit 5 test annotation detection ---

    @Test
    void isJUnit5TestAnnotation_null_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation(null));
    }

    @Test
    void isJUnit5TestAnnotation_parameterizedTest_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("ParameterizedTest"));
    }

    @Test
    void isJUnit5TestAnnotation_repeatedTest_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("RepeatedTest"));
    }

    @Test
    void isJUnit5TestAnnotation_testFactory_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("TestFactory"));
    }

    @Test
    void isJUnit5TestAnnotation_testTemplate_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("TestTemplate"));
    }

    @Test
    void isJUnit5TestAnnotation_testWithoutImport_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation("Test"));
    }

    @Test
    void isJUnit5TestAnnotation_unknown_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation("Unknown"));
    }

    // --- Current method test and DisplayName tracking ---

    @Test
    void isCurrentMethodTest_initiallyFalse() {
        assertFalse(context.isCurrentMethodTest());
    }

    @Test
    void markCurrentMethodAsTest_setsFlag() {
        context.markCurrentMethodAsTest();
        assertTrue(context.isCurrentMethodTest());
    }

    @Test
    void currentMethodHasDisplayName_initiallyFalse() {
        assertFalse(context.currentMethodHasDisplayName());
    }

    @Test
    void markCurrentMethodHasDisplayName_setsFlag() {
        context.markCurrentMethodHasDisplayName();
        assertTrue(context.currentMethodHasDisplayName());
    }

    @Test
    void currentMethodLineNo_initiallyZero() {
        assertEquals(0, context.currentMethodLineNo());
    }

    @Test
    void leaveMethod_clearsFlagsAndLineNo() {
        context.markCurrentMethodAsTest();
        context.markCurrentMethodHasDisplayName();
        context.leaveMethod();

        assertFalse(context.isCurrentMethodTest());
        assertFalse(context.currentMethodHasDisplayName());
        assertEquals(0, context.currentMethodLineNo());
        assertNull(context.currentMethodName());
    }

    // --- isLocaleExpression ---

    @Test
    void isLocaleExpression_identWithLocaleType() {
        recordVariableType("localeVar", "Locale");
        DetailAST ident = ident("localeVar");

        assertTrue(context.isLocaleExpression(ident));
    }

    @Test
    void isLocaleExpression_identWithoutLocaleType() {
        DetailAST ident = ident("other");

        assertFalse(context.isLocaleExpression(ident));
    }

    @Test
    void isLocaleExpression_dotWithLocalePrefix() {
        DetailAST dot = dot(ident("Locale"), ident("US"));

        assertTrue(context.isLocaleExpression(dot));
    }

    @Test
    void isLocaleExpression_dotWithTypedQualifier() {
        recordVariableType("localeVar", "java.util.Locale");
        DetailAST dot = dot(ident("localeVar"), ident("US"));

        assertTrue(context.isLocaleExpression(dot));
    }

    @Test
    void isLocaleExpression_literalNewLocale() {
        DetailAST literalNew = literalNew(ident("Locale"));

        assertTrue(context.isLocaleExpression(literalNew));
    }

    @Test
    void isLocaleExpression_methodCallQualifierLocale() {
        DetailAST dot = dot(ident("Locale"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertTrue(context.isLocaleExpression(methodCall));
    }

    @Test
    void isLocaleExpression_methodCallQualifierTyped() {
        recordVariableType("localeVar", "Locale");
        DetailAST dot = dot(ident("localeVar"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertTrue(context.isLocaleExpression(methodCall));
    }

    @Test
    void isLocaleExpression_methodCallQualifierUnknown() {
        DetailAST dot = dot(ident("other"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertFalse(context.isLocaleExpression(methodCall));
    }

    // --- package-local accessors ---

    @Test
    void recordStaticJUnitImport_junit4Specific() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.assertEquals", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("assertEquals"));
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    @Test
    void recordStaticJUnitImport_junit4Wildcard() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.*", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("anyMethod"));
    }

    @Test
    void recordStaticJUnitImport_junit5Specific() {
        context.recordStaticJUnitImportForTesting("org.junit.jupiter.api.Assertions.assertThrows",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("assertThrows"));
        assertFalse(context.isStaticJUnit4Method("assertThrows"));
    }

    @Test
    void recordStaticJUnitImport_junit5Wildcard() {
        context.recordStaticJUnitImportForTesting("org.junit.jupiter.api.Assertions.*",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("anyMethod"));
    }

    @Test
    void recordStaticJUnitImport_wrongPrefix() {
        context.recordStaticJUnitImportForTesting("org.other.Assert.assertEquals", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    void recordStaticJUnitImport_emptySuffix() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method(""));
    }

    @Test
    void normalizeClassName_simple() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("ClassName"));
    }

    @Test
    void normalizeClassName_qualified() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("com.example.ClassName"));
    }

    @Test
    void normalizeClassName_whitespace() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("  com.example.ClassName  "));
    }

    @Test
    void normalizeClassName_emptyAfterTrim() {
        assertNull(context.normalizeClassNameForTesting("  "));
    }

    @Test
    void isLocaleTypeName_null() {
        assertFalse(context.isLocaleTypeNameForTesting(null));
    }

    @Test
    void isLocaleTypeName_locale() {
        assertTrue(context.isLocaleTypeNameForTesting("Locale"));
    }

    @Test
    void isLocaleTypeName_fullyQualified() {
        assertTrue(context.isLocaleTypeNameForTesting("java.util.Locale"));
    }

    @Test
    void isLocaleTypeName_other() {
        assertFalse(context.isLocaleTypeNameForTesting("String"));
    }

    @Test
    void isBefore_lineBefore() {
        assertTrue(context.isBeforeForTesting(1, 1, 2, 1));
    }

    @Test
    void isBefore_colBefore() {
        assertTrue(context.isBeforeForTesting(1, 1, 1, 2));
    }

    @Test
    void isBefore_lineAfter() {
        assertFalse(context.isBeforeForTesting(2, 1, 1, 1));
    }

    @Test
    void isBefore_colAfter() {
        assertFalse(context.isBeforeForTesting(1, 2, 1, 1));
    }

    @Test
    void isBefore_same() {
        assertFalse(context.isBeforeForTesting(1, 1, 1, 1));
    }

    // --- blockCommentHasWord tests ---

    @Test
    void blockCommentHasWord_emptyLines() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{});
        assertFalse(context.blockCommentHasWordForTesting(block));
    }

    @Test
    void blockCommentHasWord_noLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* 123 */"});
        assertFalse(context.blockCommentHasWordForTesting(block));
    }

    @Test
    void blockCommentHasWord_hasLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* reason */"});
        assertTrue(context.blockCommentHasWordForTesting(block));
    }

    @Test
    void blockCommentHasWord_multiLine() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/*", " * reason", " */"});
        assertTrue(context.blockCommentHasWordForTesting(block));
    }

    // --- isInMethodOrCtor tests ---

    @Test
    void isInMethodOrCtor_methodDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.METHOD_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    void isInMethodOrCtor_ctorDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CTOR_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    void isInMethodOrCtor_classDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CLASS_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    void isInMethodOrCtor_nullParent() {
        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(null);

        assertFalse(context.isInMethodOrCtorForTesting(ast));
    }

    private void recordVariableType(String name, String typeName) {
        DetailAST varDef = mock(DetailAST.class);
        DetailAST type = mock(DetailAST.class);
        DetailAST ident = mock(DetailAST.class);
        DetailAST typeIdent = mock(DetailAST.class);

        when(varDef.findFirstToken(TokenTypes.TYPE)).thenReturn(type);
        when(varDef.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);
        when(varDef.getParent()).thenReturn(null);
        when(ident.getText()).thenReturn(name);
        when(type.findFirstToken(TokenTypes.DOT)).thenReturn(null);
        when(type.findFirstToken(TokenTypes.IDENT)).thenReturn(typeIdent);
        when(typeIdent.getText()).thenReturn(typeName);

        context.recordVariableType(varDef);
    }

    private static DetailAST ident(String text) {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(text);
        return ident;
    }

    private static DetailAST dot(DetailAST left, DetailAST right) {
        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(left);
        when(dot.getLastChild()).thenReturn(right);
        return dot;
    }

    private static DetailAST methodCall(DetailAST dot) {
        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(methodCall.findFirstToken(TokenTypes.DOT)).thenReturn(dot);
        return methodCall;
    }

    private static DetailAST literalNew(DetailAST firstChild) {
        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getType()).thenReturn(TokenTypes.LITERAL_NEW);
        when(literalNew.getFirstChild()).thenReturn(firstChild);
        when(firstChild.getNextSibling()).thenReturn(null);
        return literalNew;
    }
}
