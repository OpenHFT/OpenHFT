/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageExtractionContext}.
 */
@SuppressWarnings("MMDisplayName")
@DisplayName("Message extraction context tests scenario case")
class MessageExtractionContextTest {

    @TempDir
    Path tempDir;

    private MessageExtractionContext context;

    private static DetailAST ident(String text) {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(text);
        return ident;
    }

    // --- Basic accessors ---

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

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
    }

    @Test
    @DisplayName("Ast support not null scenario case")
    void astSupportNotNull() {
        assertNotNull(context.astSupport());
    }

    @Test
    @DisplayName("File contents null initially scenario case")
    void fileContentsNullInitially() {
        assertNull(context.fileContents());
    }

    // --- reset ---

    @Test
    @DisplayName("Template extractor null initially scenario case")
    void templateExtractorNullInitially() {
        assertNull(context.templateExtractor());
    }

    // --- setIgnoredExceptionClassNames ---

    @Test
    @DisplayName("Set template extractor scenario case detail")
    void setTemplateExtractor() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);
        context.setTemplateExtractor(extractor);
        assertEquals(extractor, context.templateExtractor());
    }

    @Test
    @DisplayName("Current class name null initially scenario")
    void currentClassNameNullInitially() {
        assertNull(context.currentClassName());
    }

    @Test
    @DisplayName("Current method name null initially scenario")
    void currentMethodNameNullInitially() {
        assertNull(context.currentMethodName());
    }

    @Test
    @DisplayName("Reset clears state scenario case detail")
    void resetClearsState() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(null);
        assertNull(context.fileContents());
        assertNull(context.currentClassName());
        assertNull(context.currentMethodName());
    }

    @Test
    @DisplayName("Reset clears recorded types and imports")
    void resetClearsRecordedTypesAndImports() {
        MessageExtractionContext local = new MessageExtractionContext(new MessageAstSupport());
        local.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));

        local.recordImport(createImportAst("java.util.List"));
        local.recordStaticJUnitImport("org.junit.Assert.assertEquals", "org.junit.Assert", true);
        local.setDeclaredMethodNames(new HashSet<>(Arrays.asList("helperMethod")));

        DetailAstImpl varDef = createVariableDefWithInitializer("field", "String",
                createStringLiteral("Stored"));
        local.recordVariableType(varDef);

        assertEquals("String", local.getVariableType("field"));
        assertEquals("java.util.List", local.importedClass("List"));
        assertTrue(local.isStaticJUnit4Method("assertEquals"));
        assertTrue(local.isDeclaredMethodName("helperMethod"));

        local.reset(null);

        assertNull(local.getVariableType("field"));
        assertNull(local.importedClass("List"));
        assertFalse(local.isStaticJUnit4Method("assertEquals"));
        assertFalse(local.isDeclaredMethodName("helperMethod"));
    }

    @Test
    @DisplayName("Resolve message template from dot expression uses recorded template")
    void resolveMessageTemplateFromDotUsesRecordedTemplate() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        DetailAstImpl varDef = createVariableDefWithInitializer("message", "String",
                createStringLiteral("Resolved"));
        context.recordVariableType(varDef);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(createDot(createIdent("holder"), createIdent("message")));

        MessageTemplate template = context.resolveMessageTemplate(expr);
        assertNotNull(template, "Template should resolve from field reference");
        assertEquals("Resolved", template.message());
    }

    // --- isIgnoredExceptionClass edge cases ---

    @Test
    @DisplayName("Set ignored exception class names null scenario")
    void setIgnoredExceptionClassNamesNull() {
        context.setIgnoredExceptionClassNames(null);
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    @Test
    @DisplayName("Set ignored exception class names empty scenario")
    void setIgnoredExceptionClassNamesEmpty() {
        context.setIgnoredExceptionClassNames(Collections.emptySet());
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    // --- JUnit static method detection ---

    @Test
    @DisplayName("Set ignored exception class names with values")
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
    @DisplayName("Set ignored exception class names normalises fully qualified")
    void setIgnoredExceptionClassNamesNormalisesFullyQualified() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        // Fully qualified name should be normalised
        assertTrue(context.isIgnoredExceptionClass("java.io.IOException"));
    }

    // --- resolveTypeName ---

    @Test
    @DisplayName("Is ignored exception class null scenario case")
    void isIgnoredExceptionClassNull() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(null));
    }

    @Test
    @DisplayName("Is ignored exception class empty scenario case")
    void isIgnoredExceptionClassEmpty() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(""));
    }

    @Test
    @DisplayName("Declared method names default to empty")
    void declaredMethodNamesDefaultToEmpty() {
        assertFalse(context.isDeclaredMethodName("assertTrue"));
    }

    @Test
    @DisplayName("Declared method names handle null and empty inputs")
    void declaredMethodNamesHandleNullAndEmptyInputs() {
        context.setDeclaredMethodNames(null);
        assertFalse(context.isDeclaredMethodName("assertTrue"));

        context.setDeclaredMethodNames(Collections.emptySet());
        assertFalse(context.isDeclaredMethodName("assertTrue"));
    }

    @Test
    @DisplayName("Declared method names overwrite previous values")
    void declaredMethodNamesOverwritePreviousValues() {
        Set<String> first = new HashSet<>(Arrays.asList("alpha"));
        context.setDeclaredMethodNames(first);
        assertTrue(context.isDeclaredMethodName("alpha"));

        Set<String> second = new HashSet<>(Arrays.asList("beta"));
        context.setDeclaredMethodNames(second);
        assertFalse(context.isDeclaredMethodName("alpha"));
        assertTrue(context.isDeclaredMethodName("beta"));
    }

    @Test
    @DisplayName("Declared method names reflect supplied entries")
    void declaredMethodNamesReflectSuppliedEntries() {
        Set<String> names = new HashSet<>();
        names.add("assertTrue");
        names.add("verifyOrder");
        context.setDeclaredMethodNames(names);

        assertTrue(context.isDeclaredMethodName("assertTrue"));
        assertTrue(context.isDeclaredMethodName("verifyOrder"));
        assertFalse(context.isDeclaredMethodName("missingMethod"));
        assertFalse(context.isDeclaredMethodName(null));
    }

    @Test
    @DisplayName("Declared method signatures default to empty")
    void declaredMethodSignaturesDefaultToEmpty() {
        assertFalse(context.isDeclaredMethodSignature("wait", 1));
    }

    @Test
    @DisplayName("Declared method signatures reflect supplied arities")
    void declaredMethodSignaturesReflectSuppliedArities() {
        context.setDeclaredMethodArities(Map.of(
                "wait", Set.of(1, 2),
                "notifyAll", Set.of(1)));

        assertTrue(context.isDeclaredMethodSignature("wait", 1));
        assertTrue(context.isDeclaredMethodSignature("wait", 2));
        assertTrue(context.isDeclaredMethodSignature("notifyAll", 1));
        assertFalse(context.isDeclaredMethodSignature("wait", 0));
        assertFalse(context.isDeclaredMethodSignature("missingMethod", 1));
        assertFalse(context.isDeclaredMethodSignature(null, 1));
    }

    @Test
    @DisplayName("Declared method signatures clear when null input arrives so stale helper overloads do not leak")
    void declaredMethodSignaturesClearWhenNullInputArrives() {
        context.setDeclaredMethodArities(Map.of("wait", Set.of(1)));
        assertTrue(context.isDeclaredMethodSignature("wait", 1));

        context.setDeclaredMethodArities(null);

        assertFalse(context.isDeclaredMethodSignature("wait", 1));
    }

    @Test
    @DisplayName("Declared method signatures ignore blank entries so only concrete helper overloads match")
    void declaredMethodSignaturesIgnoreBlankEntries() {
        java.util.Map<String, Set<Integer>> methodArities = new java.util.HashMap<>();
        methodArities.put(null, Set.of(1));
        methodArities.put("notifyAll", null);
        methodArities.put("wait", Collections.emptySet());
        methodArities.put("sleep", Set.of(2));

        context.setDeclaredMethodArities(methodArities);

        assertFalse(context.isDeclaredMethodSignature("wait", 1));
        assertFalse(context.isDeclaredMethodSignature("notifyAll", 0));
        assertTrue(context.isDeclaredMethodSignature("sleep", 2));
    }

    // --- getVariableType ---

    @Test
    @DisplayName("Is static J unit 4 method false initially scenario")
    void isStaticJUnit4MethodFalseInitially() {
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    // --- importedClass ---

    @Test
    @DisplayName("Is static J unit 5 method false initially scenario")
    void isStaticJUnit5MethodFalseInitially() {
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    // --- leaveMethod ---

    @Test
    @DisplayName("Resolve type name simple scenario case")
    void resolveTypeNameSimple() {
        assertEquals("String", context.resolveTypeName("String"));
    }

    // --- JUnit 5 test annotation detection ---

    @Test
    @DisplayName("Resolve type name fully qualified scenario")
    void resolveTypeNameFullyQualified() {
        assertEquals("java.lang.String", context.resolveTypeName("java.lang.String"));
    }

    @Test
    @DisplayName("Get variable type unknown scenario case")
    void getVariableTypeUnknown() {
        assertNull(context.getVariableType("unknown"));
    }

    @Test
    @DisplayName("Imported class unknown scenario case detail")
    void importedClassUnknown() {
        assertNull(context.importedClass("Unknown"));
    }

    @Test
    @DisplayName("Leave method clears method name scenario")
    void leaveMethodClearsMethodName() {
        context.leaveMethod();
        assertNull(context.currentMethodName());
    }

    @Test
    @DisplayName("Enter method clears method-local variables and templates")
    void enterMethodClearsMethodLocalState() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));

        DetailAstImpl varDef = createVariableDefWithInitializer("message", "String",
                createStringLiteral("Method template"));
        DetailAstImpl methodDef = createMethodDef("firstMethod", 7);
        methodDef.addChild(varDef);
        context.recordVariableType(varDef);

        assertEquals("String", context.getVariableType("message"));
        MessageTemplate template = context.resolveMessageTemplate(createIdent("message"));
        assertNotNull(template, "Template should be recorded for method variable");
        assertEquals("Method template", template.message());

        context.enterMethod(createMethodDef("secondMethod", 12));
        assertNull(context.getVariableType("message"));
        assertNull(context.resolveMessageTemplate(createIdent("message")));
    }

    @Test
    @DisplayName("Leave method clears method-local variables and templates")
    void leaveMethodClearsMethodLocalState() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));

        DetailAstImpl varDef = createVariableDefWithInitializer("message", "String",
                createStringLiteral("Scoped template"));
        DetailAstImpl methodDef = createMethodDef("method", 3);
        methodDef.addChild(varDef);
        context.recordVariableType(varDef);

        assertEquals("String", context.getVariableType("message"));
        assertNotNull(context.resolveMessageTemplate(createIdent("message")));

        context.leaveMethod();
        assertNull(context.getVariableType("message"));
        assertNull(context.resolveMessageTemplate(createIdent("message")));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation null returns false scenario case")
    void isJUnit5TestAnnotation_null_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation(null));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation parameterized test returns true scenario")
    void isJUnit5TestAnnotation_parameterizedTest_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("ParameterizedTest"));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation repeated test returns true scenario")
    void isJUnit5TestAnnotation_repeatedTest_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("RepeatedTest"));
    }

    // --- Current method test and DisplayName tracking ---

    @Test
    @DisplayName("Is J unit 5 test annotation test factory returns true scenario")
    void isJUnit5TestAnnotation_testFactory_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("TestFactory"));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation test template returns true scenario")
    void isJUnit5TestAnnotation_testTemplate_returnsTrue() {
        assertTrue(context.isJUnit5TestAnnotation("TestTemplate"));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation test without import returns false scenario")
    void isJUnit5TestAnnotation_testWithoutImport_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation("Test"));
    }

    @Test
    @DisplayName("Is J unit 5 test annotation unknown returns false scenario")
    void isJUnit5TestAnnotation_unknown_returnsFalse() {
        assertFalse(context.isJUnit5TestAnnotation("Unknown"));
    }

    @Test
    @DisplayName("Is current method test initially false scenario case")
    void isCurrentMethodTest_initiallyFalse() {
        assertFalse(context.isCurrentMethodTest());
    }

    @Test
    @DisplayName("Mark current method as test sets flag")
    void markCurrentMethodAsTest_setsFlag() {
        context.markCurrentMethodAsTest();
        assertTrue(context.isCurrentMethodTest());
    }

    // --- isLocaleExpression ---

    @Test
    @DisplayName("Current method has display name initially false")
    void currentMethodHasDisplayName_initiallyFalse() {
        assertFalse(context.currentMethodHasDisplayName());
    }

    @Test
    @DisplayName("Mark current method has display name sets flag")
    void markCurrentMethodHasDisplayName_setsFlag() {
        context.markCurrentMethodHasDisplayName();
        assertTrue(context.currentMethodHasDisplayName());
    }

    @Test
    @DisplayName("Current method line no initially zero scenario")
    void currentMethodLineNo_initiallyZero() {
        assertEquals(0, context.currentMethodLineNo());
    }

    @Test
    @DisplayName("Leave method clears flags and line no scenario")
    void leaveMethod_clearsFlagsAndLineNo() {
        context.markCurrentMethodAsTest();
        context.markCurrentMethodHasDisplayName();
        context.leaveMethod();

        assertFalse(context.isCurrentMethodTest());
        assertFalse(context.currentMethodHasDisplayName());
        assertEquals(0, context.currentMethodLineNo());
        assertNull(context.currentMethodName());
    }

    @Test
    @DisplayName("Is locale expression ident with locale type")
    void isLocaleExpression_identWithLocaleType() {
        recordVariableType("localeVar", "Locale");
        DetailAST ident = ident("localeVar");

        assertTrue(context.isLocaleExpression(ident));
    }

    @Test
    @DisplayName("Is locale expression ident without locale type")
    void isLocaleExpression_identWithoutLocaleType() {
        DetailAST ident = ident("other");

        assertFalse(context.isLocaleExpression(ident));
    }

    @Test
    @DisplayName("Is locale expression dot with locale prefix")
    void isLocaleExpression_dotWithLocalePrefix() {
        DetailAST dot = dot(ident("Locale"), ident("US"));

        assertTrue(context.isLocaleExpression(dot));
    }

    @Test
    @DisplayName("Is locale expression dot with typed qualifier")
    void isLocaleExpression_dotWithTypedQualifier() {
        recordVariableType("localeVar", "java.util.Locale");
        DetailAST dot = dot(ident("localeVar"), ident("US"));

        assertTrue(context.isLocaleExpression(dot));
    }

    @Test
    @DisplayName("Is locale expression dot with non locale qualifier")
    void isLocaleExpression_dotWithNonLocaleQualifier() {
        recordVariableType("otherVar", "String");
        DetailAST dot = dot(ident("otherVar"), ident("US"));

        assertFalse(context.isLocaleExpression(dot));
    }

    // --- package-local accessors ---

    @Test
    @DisplayName("Is locale expression literal new locale scenario")
    void isLocaleExpression_literalNewLocale() {
        DetailAST literalNew = literalNew(ident("Locale"));

        assertTrue(context.isLocaleExpression(literalNew));
    }

    @Test
    @DisplayName("Is locale expression literal new non locale scenario")
    void isLocaleExpression_literalNewNonLocale() {
        DetailAST literalNew = literalNew(ident("String"));

        assertFalse(context.isLocaleExpression(literalNew));
    }

    @Test
    @DisplayName("Is locale expression method call qualifier locale scenario")
    void isLocaleExpression_methodCallQualifierLocale() {
        DetailAST dot = dot(ident("Locale"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertTrue(context.isLocaleExpression(methodCall));
    }

    @Test
    @DisplayName("Is locale expression method call qualifier typed")
    void isLocaleExpression_methodCallQualifierTyped() {
        recordVariableType("localeVar", "Locale");
        DetailAST dot = dot(ident("localeVar"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertTrue(context.isLocaleExpression(methodCall));
    }

    @Test
    @DisplayName("Is locale expression method call qualifier unknown")
    void isLocaleExpression_methodCallQualifierUnknown() {
        DetailAST dot = dot(ident("other"), ident("getDefault"));
        DetailAST methodCall = methodCall(dot);

        assertFalse(context.isLocaleExpression(methodCall));
    }

    @Test
    @DisplayName("Record static J unit import junit 4 specific")
    void recordStaticJUnitImport_junit4Specific() {
        context.recordStaticJUnitImport("org.junit.Assert.assertEquals", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("assertEquals"));
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    @Test
    @DisplayName("Record static J unit import junit 4 wildcard")
    void recordStaticJUnitImport_junit4Wildcard() {
        context.recordStaticJUnitImport("org.junit.Assert.*", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("anyMethod"));
    }

    @Test
    @DisplayName("Record static J unit import junit 5 specific")
    void recordStaticJUnitImport_junit5Specific() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertThrows",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("assertThrows"));
        assertFalse(context.isStaticJUnit4Method("assertThrows"));
    }

    @Test
    @DisplayName("Record static J unit import junit 5 wildcard")
    void recordStaticJUnitImport_junit5Wildcard() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.*",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("anyMethod"));
    }

    @Test
    @DisplayName("Record static J unit import wrong prefix")
    void recordStaticJUnitImport_wrongPrefix() {
        context.recordStaticJUnitImport("org.other.Assert.assertEquals", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    @DisplayName("Record static J unit import empty suffix")
    void recordStaticJUnitImport_emptySuffix() {
        context.recordStaticJUnitImport("org.junit.Assert.", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method(""));
    }

    @Test
    @DisplayName("Record static import captures J unit 4 methods")
    void recordStaticImportCapturesJUnit4Methods() {
        MessageExtractionContext local = new MessageExtractionContext(new MessageAstSupport());
        DetailAstImpl importAst = createImportAst("org.junit.Assert.assertEquals");
        importAst.setType(TokenTypes.STATIC_IMPORT);
        local.recordStaticImport(importAst);

        assertTrue(local.isStaticJUnit4Method("assertEquals"));
        assertFalse(local.isStaticJUnit5Method("assertEquals"));
    }

    @Test
    @DisplayName("Record static import captures J unit 5 methods")
    void recordStaticImportCapturesJUnit5Methods() {
        MessageExtractionContext local = new MessageExtractionContext(new MessageAstSupport());
        DetailAstImpl importAst = createImportAst("org.junit.jupiter.api.Assertions.assertThrows");
        importAst.setType(TokenTypes.STATIC_IMPORT);
        local.recordStaticImport(importAst);

        assertTrue(local.isStaticJUnit5Method("assertThrows"));
        assertFalse(local.isStaticJUnit4Method("assertThrows"));
    }

    @Test
    @DisplayName("Normalize class name simple scenario case")
    void normalizeClassName_simple() {
        assertEquals("ClassName", context.normalizeClassName("ClassName"));
    }

    @Test
    @DisplayName("Normalize class name qualified scenario case")
    void normalizeClassName_qualified() {
        assertEquals("ClassName", context.normalizeClassName("com.example.ClassName"));
    }

    @Test
    @DisplayName("Normalize class name whitespace scenario case")
    void normalizeClassName_whitespace() {
        assertEquals("ClassName", context.normalizeClassName("  com.example.ClassName  "));
    }

    @Test
    @DisplayName("Normalize class name empty after trim scenario")
    void normalizeClassName_emptyAfterTrim() {
        assertNull(context.normalizeClassName("  "));
    }

    @Test
    @DisplayName("Is locale type name null scenario")
    void isLocaleTypeName_null() {
        assertFalse(context.isLocaleTypeName(null));
    }

    @Test
    @DisplayName("Is locale type name locale scenario")
    void isLocaleTypeName_locale() {
        assertTrue(context.isLocaleTypeName("Locale"));
    }

    @Test
    @DisplayName("Is locale type name fully qualified")
    void isLocaleTypeName_fullyQualified() {
        assertTrue(context.isLocaleTypeName("java.util.Locale"));
    }

    @Test
    @DisplayName("Is locale type name other scenario")
    void isLocaleTypeName_other() {
        assertFalse(context.isLocaleTypeName("String"));
    }

    @Test
    @DisplayName("Record import ignores trailing dot")
    void recordImportIgnoresTrailingDot() {
        MessageExtractionContext local = new MessageExtractionContext(new MessageAstSupport());
        local.recordImport(createImportAst("com.example."));

        assertNull(local.importedClass("example"));
    }

    @Test
    @DisplayName("Record import captures simple name")
    void recordImportCapturesSimpleName() {
        MessageExtractionContext local = new MessageExtractionContext(new MessageAstSupport());
        local.recordImport(createImportAst("java.util.List"));

        assertEquals("java.util.List", local.importedClass("List"));
    }

    @Test
    @DisplayName("Is before line before scenario case detail path")
    void isBefore_lineBefore() {
        assertTrue(context.isBefore(1, 1, 2, 1));
    }

    // --- blockCommentHasWord tests ---

    @Test
    @DisplayName("Is before col before scenario case detail")
    void isBefore_colBefore() {
        assertTrue(context.isBefore(1, 1, 1, 2));
    }

    @Test
    @DisplayName("Is before line after scenario case detail path")
    void isBefore_lineAfter() {
        assertFalse(context.isBefore(2, 1, 1, 1));
    }

    @Test
    @DisplayName("Is before col after scenario case detail")
    void isBefore_colAfter() {
        assertFalse(context.isBefore(1, 2, 1, 1));
    }

    @Test
    @DisplayName("Is before same scenario case detail")
    void isBefore_same() {
        assertFalse(context.isBefore(1, 1, 1, 1));
    }

    // --- isInMethodOrCtor tests ---

    @Test
    @DisplayName("Block comment has word empty lines")
    void blockCommentHasWord_emptyLines() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{});
        assertFalse(context.blockCommentHasWord(block));
    }

    @Test
    @DisplayName("Block comment has word no letters")
    void blockCommentHasWord_noLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* 123 */"});
        assertFalse(context.blockCommentHasWord(block));
    }

    @Test
    @DisplayName("Block comment has word has letters")
    void blockCommentHasWord_hasLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* reason */"});
        assertTrue(context.blockCommentHasWord(block));
    }

    @Test
    @DisplayName("Block comment has word multi line")
    void blockCommentHasWord_multiLine() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/*", " * reason", " */"});
        assertTrue(context.blockCommentHasWord(block));
    }

    @Test
    @DisplayName("Is in method or ctor method def scenario case")
    void isInMethodOrCtor_methodDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.METHOD_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor ctor def scenario case")
    void isInMethodOrCtor_ctorDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CTOR_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor class def scenario case")
    void isInMethodOrCtor_classDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CLASS_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor interface def scenario case")
    void isInMethodOrCtor_interfaceDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.INTERFACE_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor enum def scenario case")
    void isInMethodOrCtor_enumDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.ENUM_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor object block scenario case")
    void isInMethodOrCtor_objBlock() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.OBJBLOCK);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Is in method or ctor null parent scenario case")
    void isInMethodOrCtor_nullParent() {
        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(null);

        assertFalse(context.isInMethodOrCtor(ast));
    }

    @Test
    @DisplayName("Find argument range returns expected columns")
    void findArgumentRangeReturnsExpectedColumns() throws Exception {
        DetailAstImpl call = createNodeWithParens(1, 4, 10);
        FileContents contents = createFileContents("InputArgs.java", "call( value )");

        int[] range = invokeFindArgumentListRange(call, contents);

        assertArrayEquals(new int[]{1, 5, 1, 9}, range);
    }

    @Test
    @DisplayName("Find argument range returns null for empty args")
    void findArgumentRangeReturnsNullForEmptyArgs() throws Exception {
        DetailAstImpl call = createNodeWithParens(1, 5, 5);
        FileContents contents = createFileContents("InputArgsEmpty.java", "call()");

        assertNull(invokeFindArgumentListRange(call, contents));
    }

    @Test
    @DisplayName("Find argument range returns null for zero line")
    void findArgumentRangeReturnsNullForZeroLine() throws Exception {
        DetailAstImpl call = createNodeWithParens(0, 2, 4);
        FileContents contents = createFileContents("InputArgsLine.java", "call(1)");

        assertNull(invokeFindArgumentListRange(call, contents));
    }

    @Test
    @DisplayName("Scan argument range returns null for missing parens")
    void scanArgumentRangeReturnsNullForMissingParens() throws Exception {
        String line = "no parens here";
        FileContents contents = createFileContents("InputNoParens.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        assertNull(invokeFindArgumentListRangeByScan(methodCall, contents));
    }

    @Test
    @DisplayName("Scan argument range returns null for empty args")
    void scanArgumentRangeReturnsNullForEmptyArgs() throws Exception {
        String line = "call()";
        FileContents contents = createFileContents("InputEmptyCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        assertNull(invokeFindArgumentListRangeByScan(methodCall, contents));
    }

    @Test
    @DisplayName("Scan argument range detects block comment in args")
    void scanArgumentRangeDetectsBlockCommentInArgs() throws Exception {
        String line = "call(/* reason */ value)";
        FileContents contents = createFileContents("InputCommentCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.indexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range ignores parens inside block comment")
    void scanArgumentRangeIgnoresParensInsideBlockComment() throws Exception {
        String line = "call(/* ) */ value)";
        FileContents contents = createFileContents("InputCommentParenCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range ignores parens inside string literal")
    void scanArgumentRangeIgnoresParensInsideStringLiteral() throws Exception {
        String line = "call(\"value )\", other)";
        FileContents contents = createFileContents("InputStringParenCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range ignores escaped quote inside string literal")
    void scanArgumentRangeIgnoresEscapedQuoteInsideStringLiteral() throws Exception {
        String line = "call(\"value \\\" )\", other)";
        FileContents contents = createFileContents("InputStringEscapeCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range ignores parens inside char literal")
    void scanArgumentRangeIgnoresParensInsideCharLiteral() throws Exception {
        String line = "call(')', value)";
        FileContents contents = createFileContents("InputCharParenCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range ignores escaped quote inside char literal")
    void scanArgumentRangeIgnoresEscapedQuoteInsideCharLiteral() throws Exception {
        String line = "call('\\'', value)";
        FileContents contents = createFileContents("InputCharEscapeCall.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range uses method call column for nested call")
    void scanArgumentRangeUsesMethodCallColumnForNestedCall() throws Exception {
        String line = "other(call(value))";
        FileContents contents = createFileContents("InputNestedCall.java", line);
        int callCol = line.indexOf("call");
        DetailAstImpl methodCall = createMethodCall("call", 1, callCol);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(', callCol) + 1;
        int closeCol = line.indexOf(')', callCol) - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range uses method ident column for qualified call")
    void scanArgumentRangeUsesMethodIdentColumnForQualifiedCall() throws Exception {
        String line = "LoggerFactory.getLogger(\"x\")";
        FileContents contents = createFileContents("InputQualifiedCall.java", line);
        int qualifierCol = line.indexOf("LoggerFactory");
        int methodCol = line.indexOf("getLogger");
        DetailAstImpl methodCall = createMethodCallWithDot("LoggerFactory", "getLogger",
                1, qualifierCol, methodCol);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range continues after line comment")
    void scanArgumentRangeContinuesAfterLineComment() throws Exception {
        FileContents contents = createFileContents("InputLineCommentCall.java",
                "call(value // )",
                "  )");
        DetailAstImpl methodCall = createMethodCall("call", 1, 0);

        int[] range = invokeFindArgumentListRangeByScan(methodCall, contents);

        int openCol = "call(value // )".indexOf('(') + 1;
        int closeCol = "  )".indexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 2, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range handles annotation arguments")
    void scanArgumentRangeHandlesAnnotationArguments() throws Exception {
        String line = "@Tag(value = \"x\")";
        FileContents contents = createFileContents("InputAnnotationArgs.java", line);
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        DetailAstImpl ident = createIdent("Tag", 1, line.indexOf("Tag"));
        annotation.addChild(ident);

        int[] range = invokeFindArgumentListRangeByScan(annotation, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range handles qualified annotation arguments")
    void scanArgumentRangeHandlesQualifiedAnnotationArguments() throws Exception {
        String line = "@org.junit.jupiter.api.Tag(\"x\")";
        FileContents contents = createFileContents("InputQualifiedAnnotation.java", line);
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        int tagCol = line.indexOf("Tag");
        annotation.addChild(createDotChainWithPosition(
                new String[]{"org", "junit", "jupiter", "api", "Tag"}, 1, tagCol));

        int[] range = invokeFindArgumentListRangeByScan(annotation, contents);

        int openCol = line.indexOf('(') + 1;
        int closeCol = line.lastIndexOf(')') - 1;
        assertArrayEquals(new int[]{1, openCol, 1, closeCol}, range);
    }

    @Test
    @DisplayName("Scan argument range returns null for out of range line")
    void scanArgumentRangeReturnsNullForOutOfRangeLine() throws Exception {
        String line = "call(value)";
        FileContents contents = createFileContents("InputOutOfRange.java", line);
        DetailAstImpl methodCall = createMethodCall("call", 2, 0);

        assertNull(invokeFindArgumentListRangeByScan(methodCall, contents));
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

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private DetailAstImpl createIdent(String name, int lineNo, int columnNo) {
        DetailAstImpl ident = createIdent(name);
        ident.setLineNo(lineNo);
        ident.setColumnNo(columnNo);
        return ident;
    }

    private DetailAstImpl createDot(DetailAstImpl left, DetailAstImpl right) {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(left);
        dot.addChild(right);
        return dot;
    }

    private DetailAstImpl createStringLiteral(String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + value + "\"");
        return literal;
    }

    private DetailAstImpl createVariableDefWithInitializer(String name, String typeName, DetailAstImpl initializer) {
        DetailAstImpl varDef = new DetailAstImpl();
        varDef.setType(TokenTypes.VARIABLE_DEF);

        DetailAstImpl type = new DetailAstImpl();
        type.setType(TokenTypes.TYPE);
        DetailAstImpl typeIdent = new DetailAstImpl();
        typeIdent.setType(TokenTypes.IDENT);
        typeIdent.setText(typeName);
        type.addChild(typeIdent);
        varDef.addChild(type);

        varDef.addChild(createIdent(name));

        DetailAstImpl assign = new DetailAstImpl();
        assign.setType(TokenTypes.ASSIGN);
        assign.addChild(createIdent(name));
        assign.addChild(initializer);
        varDef.addChild(assign);
        return varDef;
    }

    private DetailAstImpl createMethodDef(String name, int lineNo) {
        DetailAstImpl methodDef = new DetailAstImpl();
        methodDef.setType(TokenTypes.METHOD_DEF);
        methodDef.setLineNo(lineNo);
        methodDef.addChild(createIdent(name, lineNo, 0));
        return methodDef;
    }

    private DetailAstImpl createMethodCall(String name, int lineNo, int columnNo) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setLineNo(lineNo);
        methodCall.setColumnNo(columnNo);
        methodCall.addChild(createIdent(name, lineNo, columnNo));
        return methodCall;
    }

    private DetailAstImpl createMethodCallWithDot(String qualifier, String methodName,
                                                  int lineNo, int qualifierCol, int methodCol) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setLineNo(lineNo);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(qualifier, lineNo, qualifierCol));
        dot.addChild(createIdent(methodName, lineNo, methodCol));
        methodCall.addChild(dot);
        return methodCall;
    }

    private DetailAstImpl createNodeWithParens(int lineNo, int lparenCol, int rparenCol) {
        DetailAstImpl node = new DetailAstImpl();
        node.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl lparen = new DetailAstImpl();
        lparen.setType(TokenTypes.LPAREN);
        lparen.setLineNo(lineNo);
        lparen.setColumnNo(lparenCol);
        DetailAstImpl rparen = new DetailAstImpl();
        rparen.setType(TokenTypes.RPAREN);
        rparen.setLineNo(lineNo);
        rparen.setColumnNo(rparenCol);
        node.addChild(lparen);
        node.addChild(rparen);
        return node;
    }

    private int[] invokeFindArgumentListRange(DetailAST node, FileContents contents) throws Exception {
        return context.findArgumentListRange(node, contents);
    }

    private int[] invokeFindArgumentListRangeByScan(DetailAST node, FileContents contents) throws Exception {
        return context.findArgumentListRangeByScan(node, contents);
    }

    private FileContents createFileContents(String fileName, String... lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), Arrays.asList(lines));
        return new FileContents(text);
    }

    private DetailAstImpl createImportAst(String qualifiedName) {
        DetailAstImpl importAst = new DetailAstImpl();
        importAst.setType(TokenTypes.IMPORT);
        String[] parts = qualifiedName.split("\\.", -1);
        importAst.addChild(createDotChain(parts));
        return importAst;
    }

    private DetailAstImpl createDotChain(String[] parts) {
        DetailAstImpl current = new DetailAstImpl();
        current.setType(TokenTypes.IDENT);
        current.setText(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            DetailAstImpl ident = new DetailAstImpl();
            ident.setType(TokenTypes.IDENT);
            ident.setText(parts[i]);
            dot.addChild(ident);
            current = dot;
        }
        return current;
    }

    private DetailAstImpl createDotChainWithPosition(String[] parts, int lineNo, int lastCol) {
        DetailAstImpl current = null;
        for (int i = 0; i < parts.length; i++) {
            DetailAstImpl ident = i == parts.length - 1
                    ? createIdent(parts[i], lineNo, lastCol)
                    : createIdent(parts[i]);
            if (current == null) {
                current = ident;
            } else {
                DetailAstImpl dot = new DetailAstImpl();
                dot.setType(TokenTypes.DOT);
                dot.addChild(current);
                dot.addChild(ident);
                current = dot;
            }
        }
        return current;
    }
}
