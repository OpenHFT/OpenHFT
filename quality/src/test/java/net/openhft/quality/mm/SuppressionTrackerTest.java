/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SuppressionTracker}.
 */
@SuppressWarnings("MMDisplayName")
@DisplayName("Suppression tracker tests scenario case detail")
class SuppressionTrackerTest {

    private SuppressionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new SuppressionTracker();
    }

    @Test
    @DisplayName("Constructor creates empty tracker scenario case")
    void constructorCreatesEmptyTracker() {
        assertNotNull(tracker, "tracker should not be null");
    }

    @Test
    @DisplayName("Is suppressed empty scope returns false scenario case")
    void isSuppressed_emptyScope_returnsFalse() {
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return false when no scope entered");
    }

    @Test
    @DisplayName("Is suppressed null rule id throws NPE")
    void isSuppressed_nullRuleId_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> tracker.isSuppressed(null),
                "should throw NPE for null ruleId");
    }

    @Test
    @DisplayName("Leave scope empty stack does not throw")
    void leaveScope_emptyStack_doesNotThrow() {
        // Should not throw even with empty stack
        tracker.leaveScope();
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should still return false after leaveScope on empty stack");
    }

    // --- stripQuotes tests ---

    @Test
    @DisplayName("Strip quotes normal string scenario case")
    void stripQuotes_normalString() {
        assertEquals("test", tracker.stripQuotes("\"test\""),
                "should strip surrounding quotes");
    }

    @Test
    @DisplayName("Strip quotes no quotes scenario case")
    void stripQuotes_noQuotes() {
        assertEquals("test", tracker.stripQuotes("test"),
                "should return unchanged if no quotes");
    }

    @Test
    @DisplayName("Strip quotes empty quotes scenario case")
    void stripQuotes_emptyQuotes() {
        assertEquals("", tracker.stripQuotes("\"\""),
                "should return empty string for empty quotes");
    }

    @Test
    @DisplayName("Strip quotes single char scenario case")
    void stripQuotes_singleChar() {
        assertEquals("x", tracker.stripQuotes("x"),
                "should return unchanged for single char");
    }

    // --- SuppressionScope inner class tests ---

    @Test
    @DisplayName("Suppression scope add token mm all sets flag")
    void suppressionScope_addToken_mmAll_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");

        assertTrue(scope.suppressAll,
                "MM-all should set suppressAll flag");
    }

    @Test
    @DisplayName("Suppression scope add token meaningful message sets flag")
    void suppressionScope_addToken_meaningfulMessage_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MeaningfulMessage");

        assertTrue(scope.suppressAll,
                "MeaningfulMessage should set suppressAll flag");
    }

    @Test
    @DisplayName("Suppression scope add token meaningful message check sets flag")
    void suppressionScope_addToken_meaningfulMessageCheck_setsFlag() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MeaningfulMessageCheck");

        assertTrue(scope.suppressAll,
                "MeaningfulMessageCheck should set suppressAll flag");
    }

    @Test
    @DisplayName("Suppression scope add token known code adds to set")
    void suppressionScope_addToken_knownCode_addsToSet() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MMTooShort");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "known code should be added to suppressedCodes");
    }

    @Test
    @DisplayName("Suppression scope add token unknown code not added")
    void suppressionScope_addToken_unknownCode_notAdded() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("UnknownCode");

        Set<String> codes = scope.suppressedCodes;
        assertFalse(codes.contains("UnknownCode"),
                "unknown code should not be added");
    }

    @Test
    @DisplayName("Suppression scope add token empty string not added")
    void suppressionScope_addToken_emptyString_notAdded() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.isEmpty(),
                "empty string should not be added");
    }

    @Test
    @DisplayName("Suppression scope add token checkstyle prefix stripped")
    void suppressionScope_addToken_checkstylePrefix_stripped() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("checkstyle:MMTooShort");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "checkstyle: prefix should be stripped");
    }

    @Test
    @DisplayName("Suppression scope add token whitespace trimmed")
    void suppressionScope_addToken_whitespace_trimmed() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("  MMTooShort  ");

        Set<String> codes = scope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "whitespace should be trimmed");
    }

    @Test
    @DisplayName("Suppression scope copy constructor inherits codes")
    void suppressionScope_copyConstructor_inheritsCodes() {
        SuppressionTracker.SuppressionScope parentScope = new SuppressionTracker.SuppressionScope();
        parentScope.addToken("MMTooShort");

        SuppressionTracker.SuppressionScope childScope = new SuppressionTracker.SuppressionScope(parentScope);

        Set<String> codes = childScope.suppressedCodes;
        assertTrue(codes.contains("MMTooShort"),
                "child scope should inherit parent codes");
    }

    @Test
    @DisplayName("Suppression scope copy constructor inherits suppress all")
    void suppressionScope_copyConstructor_inheritsSuppressAll() {
        SuppressionTracker.SuppressionScope parentScope = new SuppressionTracker.SuppressionScope();
        parentScope.addToken("MM-all");

        SuppressionTracker.SuppressionScope childScope = new SuppressionTracker.SuppressionScope(parentScope);

        assertTrue(childScope.suppressAll,
                "child scope should inherit suppressAll flag");
    }

    @Test
    @DisplayName("Clean token strips checkstyle prefix scenario")
    void cleanToken_stripsCheckstylePrefix() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("checkstyle:MMTooShort"),
                "should strip checkstyle: prefix");
    }

    @Test
    @DisplayName("Clean token trims whitespace scenario case")
    void cleanToken_trimsWhitespace() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("  MMTooShort  "),
                "should trim whitespace");
    }

    @Test
    @DisplayName("Clean token preserves non prefixed scenario")
    void cleanToken_preservesNonPrefixed() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();

        assertEquals("MMTooShort", scope.cleanToken("MMTooShort"),
                "should preserve non-prefixed token");
    }

    // --- Test isSuppressed with manually manipulated scope stack ---

    @Test
    @DisplayName("Is suppressed with suppress all returns true scenario case")
    void isSuppressed_withSuppressAll_returnsTrue() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MM-all");
        tracker.pushScopeForTesting(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true when suppressAll is set");
        assertTrue(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return true for any rule when suppressAll is set");
    }

    @Test
    @DisplayName("Is suppressed with specific code returns true for match")
    void isSuppressed_withSpecificCode_returnsTrueForMatch() {
        SuppressionTracker.SuppressionScope scope = new SuppressionTracker.SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTesting(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true for suppressed rule");
        assertFalse(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return false for non-suppressed rule");
    }

    // --- enterScope tests with real AST structures ---

    @Test
    @DisplayName("Enter scope with no modifiers does not throw")
    void enterScope_noModifiers_doesNotThrow() {
        DetailAstImpl classDef = new DetailAstImpl();
        classDef.setType(TokenTypes.CLASS_DEF);

        tracker.enterScope(classDef);
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should not suppress when no modifiers present");
    }

    @Test
    @DisplayName("Enter scope with suppress warnings array extracts all tokens")
    void enterScope_suppressWarningsArray_extractsAllTokens() {
        // Build: @SuppressWarnings({"MMTooShort", "MMTooLong"})
        DetailAstImpl classDef = createClassDefWithSuppressWarnings("MMTooShort", "MMTooLong");

        tracker.enterScope(classDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should suppress MMTooShort from array");
        assertTrue(tracker.isSuppressed(RuleId.TOO_LONG),
                "should suppress MMTooLong from array");
        assertFalse(tracker.isSuppressed(RuleId.DUPLICATE),
                "should not suppress MMDuplicate");
    }

    @Test
    @DisplayName("Enter scope with single suppress warning extracts token")
    void enterScope_singleSuppressWarning_extractsToken() {
        // Build: @SuppressWarnings("MMTooShort")
        DetailAstImpl classDef = createClassDefWithSuppressWarnings("MMTooShort");

        tracker.enterScope(classDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should suppress MMTooShort from single value");
    }

    @Test
    @DisplayName("Enter scope with MM-all suppresses all rules")
    void enterScope_mmAll_suppressesAllRules() {
        DetailAstImpl classDef = createClassDefWithSuppressWarnings("MM-all");

        tracker.enterScope(classDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "MM-all should suppress any rule");
        assertTrue(tracker.isSuppressed(RuleId.DUPLICATE),
                "MM-all should suppress any rule");
    }

    @Test
    @DisplayName("Enter scope with MeaningfulMessage suppresses all rules")
    void enterScope_meaningfulMessage_suppressesAllRules() {
        DetailAstImpl classDef = createClassDefWithSuppressWarnings("MeaningfulMessage");

        tracker.enterScope(classDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "MeaningfulMessage should suppress any rule");
    }

    @Test
    @DisplayName("Enter scope with named value attribute extracts token")
    void enterScope_namedValueAttribute_extractsToken() {
        // Build: @SuppressWarnings(value = "MMTooShort")
        DetailAstImpl classDef = createClassDefWithNamedSuppressWarnings("MMTooShort");

        tracker.enterScope(classDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should extract token from named value attribute");
    }

    @Test
    @DisplayName("Enter scope nested scopes inherit suppressions")
    void enterScope_nestedScopes_inheritSuppressions() {
        DetailAstImpl classDef = createClassDefWithSuppressWarnings("MMTooShort");
        DetailAstImpl methodDef = new DetailAstImpl();
        methodDef.setType(TokenTypes.METHOD_DEF);
        DetailAstImpl modifiers = new DetailAstImpl();
        modifiers.setType(TokenTypes.MODIFIERS);
        methodDef.addChild(modifiers);

        tracker.enterScope(classDef);
        tracker.enterScope(methodDef);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "inner scope should inherit parent suppressions");

        tracker.leaveScope();
        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "outer scope should still have suppression");
    }

    @Test
    @DisplayName("Collect string values traverses expr and array init")
    void collectStringValuesTraversesExprAndArrayInit() throws Exception {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl arrayInit = new DetailAstImpl();
        arrayInit.setType(TokenTypes.ANNOTATION_ARRAY_INIT);
        expr.addChild(arrayInit);
        arrayInit.addChild(createExprWithLiteral("MMTooShort"));
        arrayInit.addChild(createExprWithLiteral("MMTooLong"));

        List<String> tokens = new ArrayList<>();
        invokeCollectStringValues(expr, tokens);

        assertEquals(Arrays.asList("MMTooShort", "MMTooLong"), tokens,
                "Should collect tokens from array init");
    }

    @Test
    @DisplayName("Unwrap expr returns child only when single child")
    void unwrapExprReturnsChildOnlyWhenSingleChild() throws Exception {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"MMTooShort\"");
        expr.addChild(literal);

        assertSame(literal, invokeUnwrapExpr(expr), "Single child should be unwrapped");

        DetailAstImpl exprWithTwo = new DetailAstImpl();
        exprWithTwo.setType(TokenTypes.EXPR);
        exprWithTwo.addChild(createIdent("first"));
        exprWithTwo.addChild(createIdent("second"));

        assertSame(exprWithTwo, invokeUnwrapExpr(exprWithTwo),
                "Expr with multiple children should return itself");
    }

    @Test
    @DisplayName("Find annotation value handles literal and array init")
    void findAnnotationValueHandlesLiteralAndArrayInit() throws Exception {
        DetailAstImpl literalAnnotation = createAnnotationWithValueLiteral("MMTooShort");
        DetailAstImpl arrayAnnotation = createAnnotationWithValueArray("MMTooShort", "MMTooLong");

        DetailAST literalValue = invokeFindAnnotationValue(literalAnnotation);
        DetailAST arrayValue = invokeFindAnnotationValue(arrayAnnotation);

        assertNotNull(literalValue, "Literal value should be found");
        assertEquals(TokenTypes.EXPR, literalValue.getType());
        assertNotNull(arrayValue, "Array value should be found");
        assertEquals(TokenTypes.ANNOTATION_ARRAY_INIT, arrayValue.getType());
    }

    @Test
    @DisplayName("Find annotation value handles direct expr and missing value")
    void findAnnotationValueHandlesDirectExprAndMissingValue() throws Exception {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        annotation.addChild(expr);

        assertSame(expr, invokeFindAnnotationValue(annotation),
                "Direct expr child should be returned");

        DetailAstImpl missingValue = createAnnotationWithNamedValue("ignored", "MMTooShort");
        assertNull(invokeFindAnnotationValue(missingValue),
                "Non-value attribute should return null");
    }

    // --- Helper methods to build AST structures ---

    private DetailAstImpl createClassDefWithSuppressWarnings(String... tokens) {
        DetailAstImpl classDef = new DetailAstImpl();
        classDef.setType(TokenTypes.CLASS_DEF);

        DetailAstImpl modifiers = new DetailAstImpl();
        modifiers.setType(TokenTypes.MODIFIERS);
        classDef.addChild(modifiers);

        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        modifiers.addChild(annotation);

        DetailAstImpl annotationName = new DetailAstImpl();
        annotationName.setType(TokenTypes.IDENT);
        annotationName.setText("SuppressWarnings");
        annotation.addChild(annotationName);

        if (tokens.length == 1) {
            // Single value: @SuppressWarnings("token")
            DetailAstImpl expr = new DetailAstImpl();
            expr.setType(TokenTypes.EXPR);
            annotation.addChild(expr);

            DetailAstImpl literal = new DetailAstImpl();
            literal.setType(TokenTypes.STRING_LITERAL);
            literal.setText("\"" + tokens[0] + "\"");
            expr.addChild(literal);
        } else {
            // Array value: @SuppressWarnings({"token1", "token2"})
            DetailAstImpl arrayInit = new DetailAstImpl();
            arrayInit.setType(TokenTypes.ANNOTATION_ARRAY_INIT);
            annotation.addChild(arrayInit);

            for (String token : tokens) {
                DetailAstImpl expr = new DetailAstImpl();
                expr.setType(TokenTypes.EXPR);
                arrayInit.addChild(expr);

                DetailAstImpl literal = new DetailAstImpl();
                literal.setType(TokenTypes.STRING_LITERAL);
                literal.setText("\"" + token + "\"");
                expr.addChild(literal);
            }
        }

        return classDef;
    }

    private DetailAstImpl createClassDefWithNamedSuppressWarnings(String token) {
        DetailAstImpl classDef = new DetailAstImpl();
        classDef.setType(TokenTypes.CLASS_DEF);

        DetailAstImpl modifiers = new DetailAstImpl();
        modifiers.setType(TokenTypes.MODIFIERS);
        classDef.addChild(modifiers);

        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        modifiers.addChild(annotation);

        DetailAstImpl annotationName = new DetailAstImpl();
        annotationName.setType(TokenTypes.IDENT);
        annotationName.setText("SuppressWarnings");
        annotation.addChild(annotationName);

        // Named value: @SuppressWarnings(value = "token")
        DetailAstImpl pair = new DetailAstImpl();
        pair.setType(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        annotation.addChild(pair);

        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText("value");
        pair.addChild(ident);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        pair.addChild(expr);

        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + token + "\"");
        expr.addChild(literal);

        return classDef;
    }

    private DetailAstImpl createExprWithLiteral(String token) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + token + "\"");
        expr.addChild(literal);
        return expr;
    }

    private DetailAstImpl createAnnotationWithValueLiteral(String token) {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        DetailAstImpl pair = new DetailAstImpl();
        pair.setType(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        annotation.addChild(pair);
        pair.addChild(createIdent("value"));
        pair.addChild(createExprWithLiteral(token));
        return annotation;
    }

    private DetailAstImpl createAnnotationWithValueArray(String... tokens) {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        DetailAstImpl pair = new DetailAstImpl();
        pair.setType(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        annotation.addChild(pair);
        pair.addChild(createIdent("value"));
        DetailAstImpl arrayInit = new DetailAstImpl();
        arrayInit.setType(TokenTypes.ANNOTATION_ARRAY_INIT);
        pair.addChild(arrayInit);
        for (String token : tokens) {
            arrayInit.addChild(createExprWithLiteral(token));
        }
        return annotation;
    }

    private DetailAstImpl createAnnotationWithNamedValue(String name, String token) {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        DetailAstImpl pair = new DetailAstImpl();
        pair.setType(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        annotation.addChild(pair);
        pair.addChild(createIdent(name));
        pair.addChild(createExprWithLiteral(token));
        return annotation;
    }

    private DetailAstImpl createIdent(String text) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(text);
        return ident;
    }

    private void invokeCollectStringValues(DetailAstImpl expr, List<String> tokens) throws Exception {
        tracker.collectStringValues(expr, tokens);
    }

    private DetailAST invokeFindAnnotationValue(DetailAstImpl annotation) throws Exception {
        return tracker.findAnnotationValue(annotation);
    }

    private DetailAST invokeUnwrapExpr(DetailAstImpl expr) throws Exception {
        return tracker.unwrapExpr(expr);
    }
}
