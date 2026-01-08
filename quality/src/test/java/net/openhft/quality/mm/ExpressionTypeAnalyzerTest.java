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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExpressionTypeAnalyzer}.
 *
 * <p>Tests focus on the type name checking methods which have simple string-based logic.
 * Expression analysis methods require AST nodes and are covered by integration tests.
 */
@SuppressWarnings("MMDisplayName")
@DisplayName("Expression type analyzer tests scenario case")
class ExpressionTypeAnalyzerTest {

    private ExpressionTypeAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        MessageAstSupport astSupport = new MessageAstSupport();
        MessageExtractionContext context = new MessageExtractionContext(astSupport);
        analyzer = new ExpressionTypeAnalyzer(context);
    }

    @Test
    @DisplayName("Is string type name string returns true scenario")
    void isStringTypeName_String_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("String"));
    }

    @Test
    @DisplayName("Is string type name java lang string returns true")
    void isStringTypeName_javaLangString_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("java.lang.String"));
    }

    @Test
    @DisplayName("Is string type name null returns false scenario")
    void isStringTypeName_null_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(null));
    }

    @Test
    @DisplayName("Is string type name integer returns false")
    void isStringTypeName_Integer_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("Integer"));
    }

    @Test
    @DisplayName("Is string type name java lang integer returns false")
    void isStringTypeName_javaLangInteger_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("java.lang.Integer"));
    }

    @Test
    @DisplayName("Is string type name empty string returns false scenario")
    void isStringTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(""));
    }

    @Test
    @DisplayName("Is supplier type name supplier returns true scenario")
    void isSupplierTypeName_Supplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("Supplier"));
    }

    @Test
    @DisplayName("Is supplier type name java util function supplier returns true")
    void isSupplierTypeName_javaUtilFunctionSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("java.util.function.Supplier"));
    }

    @Test
    @DisplayName("Is supplier type name custom package supplier returns true")
    void isSupplierTypeName_customPackageSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("com.example.Supplier"));
    }

    @Test
    @DisplayName("Is supplier type name null returns false scenario")
    void isSupplierTypeName_null_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(null));
    }

    @Test
    @DisplayName("Is supplier type name string returns false")
    void isSupplierTypeName_String_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("String"));
    }

    @Test
    @DisplayName("Is supplier type name empty string returns false")
    void isSupplierTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(""));
    }

    @Test
    @DisplayName("Is supplier type name supplier prefix returns false")
    void isSupplierTypeName_SupplierPrefix_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("SupplierFactory"));
    }

    // --- isLambdaArgument tests ---

    @Test
    @DisplayName("Is lambda argument lambda returns true scenario case")
    void isLambdaArgument_lambda_returnsTrue() {
        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);
        assertTrue(analyzer.isLambdaArgument(lambda), "LAMBDA should return true");
    }

    @Test
    @DisplayName("Is lambda argument method ref returns true scenario")
    void isLambdaArgument_methodRef_returnsTrue() {
        DetailAST methodRef = mock(DetailAST.class);
        when(methodRef.getType()).thenReturn(TokenTypes.METHOD_REF);
        assertTrue(analyzer.isLambdaArgument(methodRef), "METHOD_REF should return true");
    }

    @Test
    @DisplayName("Is lambda argument string literal returns false")
    void isLambdaArgument_stringLiteral_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertFalse(analyzer.isLambdaArgument(literal), "STRING_LITERAL should return false");
    }

    @Test
    @DisplayName("Is lambda argument null throws NPE")
    void isLambdaArgument_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isLambdaArgument(null),
                "null should throw NPE");
    }

    // --- isStringTypedExpression tests ---

    @Test
    @DisplayName("Is string typed expression string literal returns true")
    void isStringTypedExpression_stringLiteral_returnsTrue() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertTrue(analyzer.isStringTypedExpression(literal),
                "STRING_LITERAL should return true");
    }

    @Test
    @DisplayName("Is string typed expression numeric literal returns false")
    void isStringTypedExpression_numericLiteral_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.NUM_INT);
        assertFalse(analyzer.isStringTypedExpression(literal),
                "NUM_INT should return false");
    }

    @Test
    @DisplayName("Is string typed expression null throws NPE")
    void isStringTypedExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isStringTypedExpression(null),
                "null should throw NPE");
    }

    @Test
    @DisplayName("Is string typed expression typecast without type returns false")
    void isStringTypedExpression_typecastWithoutType_returnsFalse() {
        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.findFirstToken(TokenTypes.TYPE)).thenReturn(null);
        assertFalse(analyzer.isStringTypedExpression(typecast),
                "TYPECAST without TYPE child should return false");
    }

    @Test
    @DisplayName("Is string typed expression ident with unknown type returns false")
    void isStringTypedExpression_identWithUnknownType_returnsFalse() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVariable");
        assertFalse(analyzer.isStringTypedExpression(ident),
                "IDENT with unknown variable type should return false");
    }

    @Test
    @DisplayName("Is string typed expression plus without string literal returns false")
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
    @DisplayName("Is supplier typed expression ident with unknown type returns false")
    void isSupplierTypedExpression_identWithUnknownType_returnsFalse() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVariable");
        assertFalse(analyzer.isSupplierTypedExpression(ident),
                "IDENT with unknown variable type should return false");
    }

    @Test
    @DisplayName("Is supplier typed expression non ident returns false")
    void isSupplierTypedExpression_nonIdent_returnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        assertFalse(analyzer.isSupplierTypedExpression(literal),
                "non-IDENT should return false");
    }

    @Test
    @DisplayName("Is supplier typed expression null throws NPE")
    void isSupplierTypedExpression_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> analyzer.isSupplierTypedExpression(null),
                "null should throw NPE");
    }
}
