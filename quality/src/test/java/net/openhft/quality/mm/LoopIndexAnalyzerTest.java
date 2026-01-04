/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LoopIndexAnalyzer}.
 * Targets surviving mutations in containsLoopIndexInAst.
 */
class LoopIndexAnalyzerTest {

    private LoopIndexAnalyzer analyzer;
    private MessageAstSupport astSupport;

    @BeforeEach
    void setUp() {
        astSupport = new MessageAstSupport();
        analyzer = new LoopIndexAnalyzer(astSupport);
    }

    @Test
    @DisplayName("constructor requires non-null astSupport")
    void constructor_requiresNonNullAstSupport() {
        assertThrows(NullPointerException.class, () -> new LoopIndexAnalyzer(null));
    }

    @Test
    @DisplayName("findLoopIndexInfo returns null for non-loop assertion methods")
    void findLoopIndexInfo_returnsNullForNonLoopAssertionMethods() {
        // Create a mock method call for assertNotNull (not a loop index method)
        DetailAST methodCall = createMethodCall("assertNotNull");
        DetailAST messageExpr = createStringLiteralExpr("test message");

        LoopIndexAnalyzer.LoopIndexInfo result = analyzer.findLoopIndexInfo(
                methodCall, messageExpr, "test message");

        assertNull(result, "Should return null for non-loop assertion method");
    }

    @Test
    @DisplayName("findLoopIndexInfo returns null when not in a loop")
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
    @DisplayName("findLoopIndexInfo detects loop index in message text")
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
    @DisplayName("findLoopIndexInfo detects missing loop index")
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
    @DisplayName("findLoopIndexInfo detects loop index with key-value format")
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
    @DisplayName("findLoopIndexInfo detects loop index with colon format")
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
    @DisplayName("LoopIndexInfo accessor methods work correctly")
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
}
