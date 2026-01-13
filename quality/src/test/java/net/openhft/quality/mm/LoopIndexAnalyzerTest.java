/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoopIndexAnalyzer}.
 * Targets surviving mutations in containsLoopIndexInAst.
 */
@DisplayName("Loop index analyzer tests scenario case")
class LoopIndexAnalyzerTest {

    private LoopIndexAnalyzer analyzer;
    private MessageAstSupport astSupport;

    @BeforeEach
    void setUp() {
        astSupport = new MessageAstSupport();
        analyzer = new LoopIndexAnalyzer(astSupport);
    }

    @Test
    @DisplayName("Constructor requires non null ast support")
    void constructor_requiresNonNullAstSupport() {
        assertThrows(NullPointerException.class, () -> new LoopIndexAnalyzer(null));
    }

    @Test
    @DisplayName("Find loop index info returns null for non loop assertion methods")
    void findLoopIndexInfo_returnsNullForNonLoopAssertionMethods() {
        // Create a mock method call for assertNotNull (not a loop index method)
        DetailAST methodCall = createMethodCall("assertNotNull");
        DetailAST messageExpr = createStringLiteralExpr("test message");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test message");

        assertNull(result, "Should return null for non-loop assertion method");
    }

    @Test
    @DisplayName("Find loop index info returns null when not in loop")
    void findLoopIndexInfo_returnsNullWhenNotInLoop() {
        DetailAST methodCall = createMethodCall("assertEquals");
        DetailAST messageExpr = createStringLiteralExpr("test");

        // No parent means not in a loop
        when(methodCall.getParent()).thenReturn(null);

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNull(result, "Should return null when not inside a loop");
    }

    @Test
    @DisplayName("Find loop index info detects loop index in message text")
    void findLoopIndexInfo_detectsLoopIndexInMessageText() {
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("element at i");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "element at i");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index 'i' should be found in message");
    }

    @Test
    @DisplayName("Find loop index info detects missing loop index")
    void findLoopIndexInfo_detectsMissingLoopIndex() {
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("element check");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "element check");

        assertNotNull(result);
        assertTrue(result.isMissing(), "Loop index 'i' should be missing from message");
        assertEquals(1, result.loopNames().size());
        assertEquals("i", result.loopNames().get(0));
    }

    @Test
    @DisplayName("Find loop index info detects loop index with key value format")
    void findLoopIndexInfo_detectsLoopIndexWithKeyValueFormat() {
        DetailAST forLoop = createForLoopWithIndex("idx");
        DetailAST methodCall = createMethodCallInForLoop("assertTrue", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("idx=5 check");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "idx=5 check");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index with key-value format should be detected");
    }

    @Test
    @DisplayName("Find loop index info detects loop index with colon format")
    void findLoopIndexInfo_detectsLoopIndexWithColonFormat() {
        DetailAST forLoop = createForLoopWithIndex("j");
        DetailAST methodCall = createMethodCallInForLoop("assertFalse", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("j: 10");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "j: 10");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index with colon format should be detected");
    }

    @Test
    @DisplayName("Loop index info accessor methods scenario")
    void loopIndexInfo_accessorMethods() {
        DetailAST forLoop = createForLoopWithIndex("counter");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("test");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertNotNull(result.loopNames());
        assertTrue(result.loopNames().contains("counter"));
    }

    @Test
    @DisplayName("Find loop index info for enhanced for loop scenario")
    void findLoopIndexInfo_enhancedForLoop() {
        DetailAST forEachLoop = createEnhancedForLoopWithVariable("item");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forEachLoop);
        DetailAST messageExpr = createStringLiteralExpr("checking item value");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "checking item value");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop variable 'item' should be found in message");
    }

    @Test
    @DisplayName("Find loop index info for assignment init scenario")
    void findLoopIndexInfo_assignmentInit() {
        DetailAST forLoop = createForLoopWithAssignInit("idx");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("checking");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "checking");

        assertNotNull(result);
        assertTrue(result.isMissing(), "Loop index 'idx' should be missing from message");
        assertTrue(result.loopNames().contains("idx"));
    }

    @Test
    @DisplayName("Find loop index info with empty name skips scenario")
    void findLoopIndexInfo_emptyLoopNameSkipped() {
        DetailAST forLoop = createForLoopWithIndex("");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("test message");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test message");

        // Empty names should be skipped, so should still check for missing
        assertNotNull(result);
    }

    @Test
    @DisplayName("Find loop index info detects index in expression")
    void findLoopIndexInfo_detectsIndexInExpression() {
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithIdent("i");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index should be found in expression");
    }

    @Test
    @DisplayName("Find loop index info nested for loops")
    void findLoopIndexInfo_nestedForLoops() {
        DetailAST outerLoop = createForLoopWithIndex("i");
        DetailAST innerLoop = createForLoopWithIndex("j");
        when(innerLoop.getParent()).thenReturn(outerLoop);
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", innerLoop);
        DetailAST messageExpr = createStringLiteralExpr("i and j");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "i and j");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Both loop indices should be found");
        assertEquals(2, result.loopNames().size());
    }

    @Test
    @DisplayName("Find loop index info with null message throws NPE")
    void findLoopIndexInfo_nullMessageThrowsNPE() {
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createStringLiteralExpr("test");

        assertThrows(NullPointerException.class,
                () -> analyzer.findLoopIndexInfo(methodCall, messageExpr, null));
    }

    @Test
    @DisplayName("Find loop index info with null method call throws NPE")
    void findLoopIndexInfo_nullMethodCallThrowsNPE() {
        DetailAST messageExpr = createStringLiteralExpr("test");

        assertThrows(NullPointerException.class,
                () -> analyzer.findLoopIndexInfo(null, messageExpr, "test"));
    }

    @Test
    @DisplayName("Find loop index info with null message expr throws NPE")
    void findLoopIndexInfo_nullMessageExprThrowsNPE() {
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);

        assertThrows(NullPointerException.class,
                () -> analyzer.findLoopIndexInfo(methodCall, null, "test"));
    }

    @Test
    @DisplayName("Message contains loop index detects word and key value forms")
    void messageContainsLoopIndexDetectsWordAndKeyValueForms() throws Exception {
        Set<String> loopNames = new HashSet<>(Collections.singleton("idx"));

        assertTrue(invokeMessageContainsLoopIndex("index idx is set", loopNames));
        assertTrue(invokeMessageContainsLoopIndex("idx=4 value", loopNames));
        assertFalse(invokeMessageContainsLoopIndex("index is set", loopNames));
    }

    // --- Helper methods ---

    private DetailAST createMethodCall(String methodName) {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(methodName);

        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(methodCall.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);
        when(methodCall.getFirstChild()).thenReturn(ident);
        when(methodCall.getParent()).thenReturn(null);

        return methodCall;
    }

    private DetailAST createStringLiteralExpr(String value) {
        DetailAST strLiteral = mock(DetailAST.class);
        when(strLiteral.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(strLiteral.getText()).thenReturn("\"" + value + "\"");

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(strLiteral);
        when(expr.getChildCount()).thenReturn(1);

        return expr;
    }

    private DetailAST createForLoopWithIndex(String indexName) {
        // Create IDENT for index
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(indexName);

        // Create VARIABLE_DEF
        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);

        // Create FOR_INIT
        DetailAST forInit = mock(DetailAST.class);
        when(forInit.getType()).thenReturn(TokenTypes.FOR_INIT);
        when(forInit.getFirstChild()).thenReturn(varDef);

        // Create FOR loop
        DetailAST forLoop = mock(DetailAST.class);
        when(forLoop.getType()).thenReturn(TokenTypes.LITERAL_FOR);
        when(forLoop.findFirstToken(TokenTypes.FOR_INIT)).thenReturn(forInit);
        when(forLoop.findFirstToken(TokenTypes.FOR_EACH_CLAUSE)).thenReturn(null);
        when(forLoop.getParent()).thenReturn(null);

        return forLoop;
    }

    private DetailAST createMethodCallInForLoop(String methodName, DetailAST forLoop) {
        DetailAST methodCall = createMethodCall(methodName);
        when(methodCall.getParent()).thenReturn(forLoop);
        return methodCall;
    }

    private boolean invokeMessageContainsLoopIndex(String message, Set<String> names) throws Exception {
        Method method = LoopIndexAnalyzer.class
                .getDeclaredMethod("messageContainsLoopIndex", String.class, Set.class);
        method.setAccessible(true);
        return (boolean) method.invoke(analyzer, message, names);
    }

    private DetailAST createEnhancedForLoopWithVariable(String varName) {
        // Create IDENT for variable
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(varName);

        // Create VARIABLE_DEF
        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);

        // Create FOR_EACH_CLAUSE
        DetailAST forEachClause = mock(DetailAST.class);
        when(forEachClause.getType()).thenReturn(TokenTypes.FOR_EACH_CLAUSE);
        when(forEachClause.findFirstToken(TokenTypes.VARIABLE_DEF)).thenReturn(varDef);

        // Create FOR loop with FOR_EACH_CLAUSE
        DetailAST forLoop = mock(DetailAST.class);
        when(forLoop.getType()).thenReturn(TokenTypes.LITERAL_FOR);
        when(forLoop.findFirstToken(TokenTypes.FOR_EACH_CLAUSE)).thenReturn(forEachClause);
        when(forLoop.findFirstToken(TokenTypes.FOR_INIT)).thenReturn(null);
        when(forLoop.getParent()).thenReturn(null);

        return forLoop;
    }

    private DetailAST createForLoopWithAssignInit(String varName) {
        // Create IDENT for left side of assignment
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(varName);

        // Create ASSIGN
        DetailAST assign = mock(DetailAST.class);
        when(assign.getType()).thenReturn(TokenTypes.ASSIGN);
        when(assign.getFirstChild()).thenReturn(ident);

        // Create FOR_INIT with ASSIGN
        DetailAST forInit = mock(DetailAST.class);
        when(forInit.getType()).thenReturn(TokenTypes.FOR_INIT);
        when(forInit.getFirstChild()).thenReturn(assign);

        // Create FOR loop
        DetailAST forLoop = mock(DetailAST.class);
        when(forLoop.getType()).thenReturn(TokenTypes.LITERAL_FOR);
        when(forLoop.findFirstToken(TokenTypes.FOR_INIT)).thenReturn(forInit);
        when(forLoop.findFirstToken(TokenTypes.FOR_EACH_CLAUSE)).thenReturn(null);
        when(forLoop.getParent()).thenReturn(null);

        return forLoop;
    }

    private DetailAST createExprWithIdent(String identName) {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn(identName);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(ident);
        when(expr.getChildCount()).thenReturn(1);

        return expr;
    }

    // --- Mutation killing tests for containsLoopIndexInAst ---

    @Test
    @DisplayName("Find loop index info ident not matching loop name returns missing")
    void findLoopIndexInfo_identNotMatchingLoopName_returnsMissing() {
        // This test kills the "negated conditional" mutation on IDENT type check
        // The expr contains an IDENT "x" but loop index is "i"
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithIdent("x"); // Different from loop index

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertTrue(result.isMissing(), "Loop index 'i' should be missing when expr has 'x'");
    }

    @Test
    @DisplayName("Find loop index info method call with dot and loop index in qualifier")
    void findLoopIndexInfo_methodCallWithDotAndLoopIndex() {
        // This test kills the "negated conditional" on DOT type check
        DetailAST forLoop = createForLoopWithIndex("item");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithDotMethodCall("item", "toString");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index should be found in method call qualifier");
    }

    @Test
    @DisplayName("Find loop index info method call with elist containing loop index")
    void findLoopIndexInfo_methodCallWithElistContainingLoopIndex() {
        // This test kills the "replaced boolean return" mutation on ELIST handling
        DetailAST forLoop = createForLoopWithIndex("idx");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithMethodCallWithArg("format", "idx");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertFalse(result.isMissing(), "Loop index should be found in method call arguments");
    }

    @Test
    @DisplayName("Find loop index info recursion returns false when nothing matches")
    void findLoopIndexInfo_recursionReturnsFalse() {
        // This test kills the "replaced return false with true" mutation
        DetailAST forLoop = createForLoopWithIndex("i");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithNestedNonMatchingContent();

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "no index here");

        assertNotNull(result);
        assertTrue(result.isMissing(), "Loop index should be missing when no content matches");
    }

    @Test
    @DisplayName("Find loop index info method call without dot target")
    void findLoopIndexInfo_methodCallWithoutDotTarget() {
        // Method call where first child is not DOT (e.g., simple method call)
        DetailAST forLoop = createForLoopWithIndex("j");
        DetailAST methodCall = createMethodCallInForLoop("assertEquals", forLoop);
        DetailAST messageExpr = createExprWithSimpleMethodCall("getValue");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test");

        assertNotNull(result);
        assertTrue(result.isMissing(), "Loop index should be missing for simple method call");
    }

    private DetailAST createExprWithDotMethodCall(String qualifierName, String methodName) {
        // Create: qualifier.method() where qualifier is an IDENT matching loop index
        DetailAST qualifierIdent = mock(DetailAST.class);
        when(qualifierIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(qualifierIdent.getText()).thenReturn(qualifierName);
        when(qualifierIdent.getFirstChild()).thenReturn(null);

        DetailAST methodIdent = mock(DetailAST.class);
        when(methodIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(methodIdent.getText()).thenReturn(methodName);

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(qualifierIdent);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getType()).thenReturn(TokenTypes.ELIST);
        when(elist.getFirstChild()).thenReturn(null);

        DetailAST innerMethodCall = mock(DetailAST.class);
        when(innerMethodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(innerMethodCall.getFirstChild()).thenReturn(dot);
        when(innerMethodCall.findFirstToken(TokenTypes.ELIST)).thenReturn(elist);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(innerMethodCall);

        return expr;
    }

    private DetailAST createExprWithMethodCallWithArg(String methodName, String argName) {
        // Create: method(argIdent) where argIdent matches loop index
        DetailAST argIdent = mock(DetailAST.class);
        when(argIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(argIdent.getText()).thenReturn(argName);
        when(argIdent.getFirstChild()).thenReturn(null);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getType()).thenReturn(TokenTypes.ELIST);
        when(elist.getFirstChild()).thenReturn(argIdent);

        DetailAST methodIdent = mock(DetailAST.class);
        when(methodIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(methodIdent.getText()).thenReturn(methodName);
        when(methodIdent.getFirstChild()).thenReturn(null);

        DetailAST innerMethodCall = mock(DetailAST.class);
        when(innerMethodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(innerMethodCall.getFirstChild()).thenReturn(methodIdent);
        when(innerMethodCall.findFirstToken(TokenTypes.ELIST)).thenReturn(elist);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(innerMethodCall);

        return expr;
    }

    private DetailAST createExprWithNestedNonMatchingContent() {
        // Create nested structure with no matching loop index
        DetailAST literalStr = mock(DetailAST.class);
        when(literalStr.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(literalStr.getText()).thenReturn("\"constant\"");
        when(literalStr.getFirstChild()).thenReturn(null);

        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);
        when(plus.getFirstChild()).thenReturn(literalStr);
        when(literalStr.getNextSibling()).thenReturn(null);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(plus);

        return expr;
    }

    private DetailAST createExprWithSimpleMethodCall(String methodName) {
        // Method call without DOT (just IDENT as first child)
        DetailAST methodIdent = mock(DetailAST.class);
        when(methodIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(methodIdent.getText()).thenReturn(methodName);
        when(methodIdent.getFirstChild()).thenReturn(null);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getType()).thenReturn(TokenTypes.ELIST);
        when(elist.getFirstChild()).thenReturn(null);

        DetailAST innerMethodCall = mock(DetailAST.class);
        when(innerMethodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(innerMethodCall.getFirstChild()).thenReturn(methodIdent);
        when(innerMethodCall.findFirstToken(TokenTypes.ELIST)).thenReturn(elist);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(innerMethodCall);

        return expr;
    }
}
