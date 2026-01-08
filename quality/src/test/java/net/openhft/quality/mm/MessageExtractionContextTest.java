/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Message extraction context tests scenario case")
class MessageExtractionContextTest {

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

    // --- package-local accessors ---

    @Test
    @DisplayName("Is locale expression literal new locale scenario")
    void isLocaleExpression_literalNewLocale() {
        DetailAST literalNew = literalNew(ident("Locale"));

        assertTrue(context.isLocaleExpression(literalNew));
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
        context.recordStaticJUnitImportForTesting("org.junit.Assert.assertEquals", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("assertEquals"));
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    @Test
    @DisplayName("Record static J unit import junit 4 wildcard")
    void recordStaticJUnitImport_junit4Wildcard() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.*", "org.junit.Assert", true);
        assertTrue(context.isStaticJUnit4Method("anyMethod"));
    }

    @Test
    @DisplayName("Record static J unit import junit 5 specific")
    void recordStaticJUnitImport_junit5Specific() {
        context.recordStaticJUnitImportForTesting("org.junit.jupiter.api.Assertions.assertThrows",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("assertThrows"));
        assertFalse(context.isStaticJUnit4Method("assertThrows"));
    }

    @Test
    @DisplayName("Record static J unit import junit 5 wildcard")
    void recordStaticJUnitImport_junit5Wildcard() {
        context.recordStaticJUnitImportForTesting("org.junit.jupiter.api.Assertions.*",
                "org.junit.jupiter.api.Assertions", false);
        assertTrue(context.isStaticJUnit5Method("anyMethod"));
    }

    @Test
    @DisplayName("Record static J unit import wrong prefix")
    void recordStaticJUnitImport_wrongPrefix() {
        context.recordStaticJUnitImportForTesting("org.other.Assert.assertEquals", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    @DisplayName("Record static J unit import empty suffix")
    void recordStaticJUnitImport_emptySuffix() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.", "org.junit.Assert", true);
        assertFalse(context.isStaticJUnit4Method(""));
    }

    @Test
    @DisplayName("Normalize class name simple scenario case")
    void normalizeClassName_simple() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("ClassName"));
    }

    @Test
    @DisplayName("Normalize class name qualified scenario case")
    void normalizeClassName_qualified() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("com.example.ClassName"));
    }

    @Test
    @DisplayName("Normalize class name whitespace scenario case")
    void normalizeClassName_whitespace() {
        assertEquals("ClassName", context.normalizeClassNameForTesting("  com.example.ClassName  "));
    }

    @Test
    @DisplayName("Normalize class name empty after trim scenario")
    void normalizeClassName_emptyAfterTrim() {
        assertNull(context.normalizeClassNameForTesting("  "));
    }

    @Test
    @DisplayName("Is locale type name null scenario")
    void isLocaleTypeName_null() {
        assertFalse(context.isLocaleTypeNameForTesting(null));
    }

    @Test
    @DisplayName("Is locale type name locale scenario")
    void isLocaleTypeName_locale() {
        assertTrue(context.isLocaleTypeNameForTesting("Locale"));
    }

    @Test
    @DisplayName("Is locale type name fully qualified")
    void isLocaleTypeName_fullyQualified() {
        assertTrue(context.isLocaleTypeNameForTesting("java.util.Locale"));
    }

    @Test
    @DisplayName("Is locale type name other scenario")
    void isLocaleTypeName_other() {
        assertFalse(context.isLocaleTypeNameForTesting("String"));
    }

    @Test
    @DisplayName("Is before line before scenario case detail path")
    void isBefore_lineBefore() {
        assertTrue(context.isBeforeForTesting(1, 1, 2, 1));
    }

    // --- blockCommentHasWord tests ---

    @Test
    @DisplayName("Is before col before scenario case detail")
    void isBefore_colBefore() {
        assertTrue(context.isBeforeForTesting(1, 1, 1, 2));
    }

    @Test
    @DisplayName("Is before line after scenario case detail path")
    void isBefore_lineAfter() {
        assertFalse(context.isBeforeForTesting(2, 1, 1, 1));
    }

    @Test
    @DisplayName("Is before col after scenario case detail")
    void isBefore_colAfter() {
        assertFalse(context.isBeforeForTesting(1, 2, 1, 1));
    }

    @Test
    @DisplayName("Is before same scenario case detail")
    void isBefore_same() {
        assertFalse(context.isBeforeForTesting(1, 1, 1, 1));
    }

    // --- isInMethodOrCtor tests ---

    @Test
    @DisplayName("Block comment has word empty lines")
    void blockCommentHasWord_emptyLines() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{});
        assertFalse(context.blockCommentHasWordForTesting(block));
    }

    @Test
    @DisplayName("Block comment has word no letters")
    void blockCommentHasWord_noLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* 123 */"});
        assertFalse(context.blockCommentHasWordForTesting(block));
    }

    @Test
    @DisplayName("Block comment has word has letters")
    void blockCommentHasWord_hasLetters() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/* reason */"});
        assertTrue(context.blockCommentHasWordForTesting(block));
    }

    @Test
    @DisplayName("Block comment has word multi line")
    void blockCommentHasWord_multiLine() {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{"/*", " * reason", " */"});
        assertTrue(context.blockCommentHasWordForTesting(block));
    }

    @Test
    @DisplayName("Is in method or ctor method def scenario case")
    void isInMethodOrCtor_methodDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.METHOD_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    @DisplayName("Is in method or ctor ctor def scenario case")
    void isInMethodOrCtor_ctorDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CTOR_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertTrue(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    @DisplayName("Is in method or ctor class def scenario case")
    void isInMethodOrCtor_classDef() {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(TokenTypes.CLASS_DEF);

        DetailAST ast = mock(DetailAST.class);
        when(ast.getParent()).thenReturn(parent);

        assertFalse(context.isInMethodOrCtorForTesting(ast));
    }

    @Test
    @DisplayName("Is in method or ctor null parent scenario case")
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

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
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
}
