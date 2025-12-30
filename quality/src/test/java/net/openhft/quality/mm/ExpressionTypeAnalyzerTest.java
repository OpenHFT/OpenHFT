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
 * Unit tests for {@link ExpressionTypeAnalyzer}.
 *
 * <p>Tests focus on the type name checking methods which have simple string-based logic.
 * Expression analysis methods require AST nodes and are covered by integration tests.
 */
@SuppressWarnings("MMDisplayName")
class ExpressionTypeAnalyzerTest {

    private ExpressionTypeAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MessageAstSupport astSupport = new MessageAstSupport();
        MessageExtractionContext context = new MessageExtractionContext(astSupport);
        analyzer = new ExpressionTypeAnalyzer(context);
    }

    @Test
    void isStringTypeName_String_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("String"));
    }

    @Test
    void isStringTypeName_javaLangString_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("java.lang.String"));
    }

    @Test
    void isStringTypeName_null_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(null));
    }

    @Test
    void isStringTypeName_Integer_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("Integer"));
    }

    @Test
    void isStringTypeName_javaLangInteger_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("java.lang.Integer"));
    }

    @Test
    void isStringTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(""));
    }

    @Test
    void isSupplierTypeName_Supplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("Supplier"));
    }

    @Test
    void isSupplierTypeName_javaUtilFunctionSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("java.util.function.Supplier"));
    }

    @Test
    void isSupplierTypeName_customPackageSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("com.example.Supplier"));
    }

    @Test
    void isSupplierTypeName_null_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(null));
    }

    @Test
    void isSupplierTypeName_String_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("String"));
    }

    @Test
    void isSupplierTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(""));
    }

    @Test
    void isSupplierTypeName_SupplierPrefix_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("SupplierFactory"));
    }

    // --- isLambdaArgument tests ---

    @Test
    void isLambdaArgument_lambda_returnsTrue() {
        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);
        assertTrue(analyzer.isLambdaArgument(lambda), "LAMBDA should return true");
    }

    @Test
    void isLambdaArgument_methodRef_returnsTrue() {
        DetailAST methodRef = mock(DetailAST.class);
        when(methodRef.getType()).thenReturn(TokenTypes.METHOD_REF);
        assertTrue(analyzer.isLambdaArgument(methodRef), "METHOD_REF should return true");
    }

    @Test
    void isLambdaArgument_stringLiteral_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertFalse(analyzer.isLambdaArgument(literal), "STRING_LITERAL should return false");
    }

    @Test
    void isLambdaArgument_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isLambdaArgument(null),
                "null should throw NPE");
    }

    // --- isStringTypedExpression tests ---

    @Test
    void isStringTypedExpression_stringLiteral_returnsTrue() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertTrue(analyzer.isStringTypedExpression(literal),
                "STRING_LITERAL should return true");
    }

    @Test
    void isStringTypedExpression_numericLiteral_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.NUM_INT);
        assertFalse(analyzer.isStringTypedExpression(literal),
                "NUM_INT should return false");
    }

    @Test
    void isStringTypedExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isStringTypedExpression(null),
                "null should throw NPE");
    }

    @Test
    void isStringTypedExpression_typecastWithoutType_returnsFalse() {
        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.findFirstToken(TokenTypes.TYPE)).thenReturn(null);
        assertFalse(analyzer.isStringTypedExpression(typecast),
                "TYPECAST without TYPE child should return false");
    }

    @Test
    void isStringTypedExpression_identWithUnknownType_returnsFalse() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVariable");
        assertFalse(analyzer.isStringTypedExpression(ident),
                "IDENT with unknown variable type should return false");
    }

    @Test
    void isStringTypedExpression_plusWithoutStringLiteral_returnsFalse() {
        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);
        // Mock the AST tree traversal - no string literal found
        when(plus.getFirstChild()).thenReturn(null);
        assertFalse(analyzer.isStringTypedExpression(plus),
                "PLUS without string literal should return false");
    }

    // --- isSupplierTypedExpression tests ---

    @Test
    void isSupplierTypedExpression_identWithUnknownType_returnsFalse() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVariable");
        assertFalse(analyzer.isSupplierTypedExpression(ident),
                "IDENT with unknown variable type should return false");
    }

    @Test
    void isSupplierTypedExpression_nonIdent_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertFalse(analyzer.isSupplierTypedExpression(literal),
                "non-IDENT should return false");
    }

    @Test
    void isSupplierTypedExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isSupplierTypedExpression(null),
                "null should throw NPE");
    }
}
