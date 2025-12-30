/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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

    // --- Reflection tests for private methods ---

    @Test
    void recordStaticJUnitImport_junit4Specific() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.junit.Assert.assertEquals", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("assertEquals"));
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    @Test
    void recordStaticJUnitImport_junit4Wildcard() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.junit.Assert.*", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("anyMethod"));
    }

    @Test
    void recordStaticJUnitImport_junit5Specific() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.junit.jupiter.api.Assertions.assertThrows",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("assertThrows"));
        assertFalse(context.isStaticJUnit4Method("assertThrows"));
    }

    @Test
    void recordStaticJUnitImport_junit5Wildcard() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.junit.jupiter.api.Assertions.*",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("anyMethod"));
    }

    @Test
    void recordStaticJUnitImport_wrongPrefix() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.other.Assert.assertEquals", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    void recordStaticJUnitImport_emptySuffix() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("recordStaticJUnitImport", String.class, String.class, boolean.class);
        method.setAccessible(true);

        method.invoke(context, "org.junit.Assert.", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method(""));
    }

    @Test
    void normalizeClassName_simple() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertEquals("ClassName", method.invoke(context, "ClassName"));
    }

    @Test
    void normalizeClassName_qualified() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertEquals("ClassName", method.invoke(context, "com.example.ClassName"));
    }

    @Test
    void normalizeClassName_whitespace() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertEquals("ClassName", method.invoke(context, "  com.example.ClassName  "));
    }

    @Test
    void normalizeClassName_emptyAfterTrim() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(context, "  "));
    }

    @Test
    void isLocaleTypeName_null() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isLocaleTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(context, (Object) null));
    }

    @Test
    void isLocaleTypeName_locale() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isLocaleTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(context, "Locale"));
    }

    @Test
    void isLocaleTypeName_fullyQualified() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isLocaleTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(context, "java.util.Locale"));
    }

    @Test
    void isLocaleTypeName_other() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isLocaleTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(context, "String"));
    }

    @Test
    void isBefore_lineBefore() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isBefore", int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(context, 1, 1, 2, 1));
    }

    @Test
    void isBefore_colBefore() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isBefore", int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(context, 1, 1, 1, 2));
    }

    @Test
    void isBefore_lineAfter() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isBefore", int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(context, 2, 1, 1, 1));
    }

    @Test
    void isBefore_colAfter() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isBefore", int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(context, 1, 2, 1, 1));
    }

    @Test
    void isBefore_same() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isBefore", int.class, int.class, int.class, int.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(context, 1, 1, 1, 1));
    }

    // --- blockCommentHasWord tests via reflection ---

    @Test
    void blockCommentHasWord_emptyLines() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("blockCommentHasWord", TextBlock.class);
        method.setAccessible(true);

        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{});
        assertFalse((Boolean) method.invoke(context, block));
    }

    @Test
    void blockCommentHasWord_noLetters() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("blockCommentHasWord", TextBlock.class);
        method.setAccessible(true);

        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* 123 */"});
        assertFalse((Boolean) method.invoke(context, block));
    }

    @Test
    void blockCommentHasWord_hasLetters() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("blockCommentHasWord", TextBlock.class);
        method.setAccessible(true);

        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* reason */"});
        assertTrue((Boolean) method.invoke(context, block));
    }

    @Test
    void blockCommentHasWord_multiLine() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("blockCommentHasWord", TextBlock.class);
        method.setAccessible(true);

        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/*", " * reason", " */"});
        assertTrue((Boolean) method.invoke(context, block));
    }

    // --- isInMethodOrCtor tests via reflection ---

    @Test
    void isInMethodOrCtor_methodDef() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isInMethodOrCtor", DetailAST.class);
        method.setAccessible(true);

        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.METHOD_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue((Boolean) method.invoke(context, ast));
    }

    @Test
    void isInMethodOrCtor_ctorDef() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isInMethodOrCtor", DetailAST.class);
        method.setAccessible(true);

        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CTOR_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue((Boolean) method.invoke(context, ast));
    }

    @Test
    void isInMethodOrCtor_classDef() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isInMethodOrCtor", DetailAST.class);
        method.setAccessible(true);

        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CLASS_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse((Boolean) method.invoke(context, ast));
    }

    @Test
    void isInMethodOrCtor_nullParent() throws Exception {
        Method method = MessageExtractionContext.class
                .getDeclaredMethod("isInMethodOrCtor", DetailAST.class);
        method.setAccessible(true);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(null);

        assertFalse((Boolean) method.invoke(context, ast));
    }

}
