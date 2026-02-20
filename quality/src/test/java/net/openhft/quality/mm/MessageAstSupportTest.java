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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageAstSupport}.
 */
@SuppressWarnings("MMDisplayName")
@DisplayName("Message ast support tests scenario case")
class MessageAstSupportTest {

    private MessageAstSupport support;

    @BeforeEach
    void setUp() {
        support = new MessageAstSupport();
    }

    // --- extractName tests ---

    @Test
    @DisplayName("Extract name with ident returns text")
    void extractName_withIdent_returnsText() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("methodName");

        DetailAST ast = mock(DetailAST.class);
        when(ast.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);

        assertEquals("methodName", support.extractName(ast));
    }

    @Test
    @DisplayName("Extract name no ident returns null scenario")
    void extractName_noIdent_returnsNull() {
        DetailAST ast = mock(DetailAST.class);
        when(ast.findFirstToken(TokenTypes.IDENT)).thenReturn(null);

        assertNull(support.extractName(ast));
    }

    @Test
    @DisplayName("Extract name null throws NPE scenario")
    void extractName_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.extractName(null));
    }

    // --- flattenDot tests ---

    @Test
    @DisplayName("Flatten dot non dot node returns text")
    void flattenDot_nonDotNode_returnsText() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("simpleName");

        assertEquals("simpleName", support.flattenDot(ident));
    }

    @Test
    @DisplayName("Flatten dot simple dot returns concatenated")
    void flattenDot_simpleDot_returnsConcatenated() {
        DetailAST left = mock(DetailAST.class);
        when(left.getType()).thenReturn(TokenTypes.IDENT);
        when(left.getText()).thenReturn("com");

        DetailAST right = mock(DetailAST.class);
        when(right.getType()).thenReturn(TokenTypes.IDENT);
        when(right.getText()).thenReturn("example");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(left);
        when(dot.getLastChild()).thenReturn(right);

        assertEquals("com.example", support.flattenDot(dot));
    }

    // --- findRightmostIdent tests ---

    @Test
    @DisplayName("Find rightmost ident simple ident returns ident")
    void findRightmostIdent_simpleIdent_returnsIdent() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);

        assertEquals(ident, support.findRightmostIdent(ident));
    }

    @Test
    @DisplayName("Find rightmost ident dot chain returns rightmost")
    void findRightmostIdent_dotChain_returnsRightmost() {
        DetailAST rightIdent = mock(DetailAST.class);
        when(rightIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(rightIdent.getText()).thenReturn("rightmost");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(rightIdent);

        assertEquals(rightIdent, support.findRightmostIdent(dot));
    }

    @Test
    @DisplayName("Find rightmost ident not ident returns null scenario")
    void findRightmostIdent_notIdent_returnsNull() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertNull(support.findRightmostIdent(literal));
    }

    // --- extractTypeName tests ---

    @Test
    @DisplayName("Extract type name with dot flattens dot")
    void extractTypeName_withDot_flattensDot() {
        DetailAST left = mock(DetailAST.class);
        when(left.getType()).thenReturn(TokenTypes.IDENT);
        when(left.getText()).thenReturn("java");

        DetailAST right = mock(DetailAST.class);
        when(right.getType()).thenReturn(TokenTypes.IDENT);
        when(right.getText()).thenReturn("String");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(left);
        when(dot.getLastChild()).thenReturn(right);

        DetailAST typeAst = mock(DetailAST.class);
        when(typeAst.findFirstToken(TokenTypes.DOT)).thenReturn(dot);

        assertEquals("java.String", support.extractTypeName(typeAst));
    }

    @Test
    @DisplayName("Extract type name simple ident returns text")
    void extractTypeName_simpleIdent_returnsText() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("String");

        DetailAST typeAst = mock(DetailAST.class);
        when(typeAst.findFirstToken(TokenTypes.DOT)).thenReturn(null);
        when(typeAst.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);

        assertEquals("String", support.extractTypeName(typeAst));
    }

    @Test
    @DisplayName("Extract type name no ident no dot returns null")
    void extractTypeName_noIdentNoDot_returnsNull() {
        DetailAST typeAst = mock(DetailAST.class);
        when(typeAst.findFirstToken(TokenTypes.DOT)).thenReturn(null);
        when(typeAst.findFirstToken(TokenTypes.IDENT)).thenReturn(null);

        assertNull(support.extractTypeName(typeAst));
    }

    @Test
    @DisplayName("Extract type name null throws NPE")
    void extractTypeName_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.extractTypeName(null));
    }

    // --- unwrapExpr tests ---

    @Test
    @DisplayName("Unwrap expr single child returns child")
    void unwrapExpr_singleChild_returnsChild() {
        DetailAST child = mock(DetailAST.class);
        when(child.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getChildCount()).thenReturn(1);
        when(expr.getFirstChild()).thenReturn(child);

        assertEquals(child, support.unwrapExpr(expr));
    }

    @Test
    @DisplayName("Unwrap expr multiple children returns self")
    void unwrapExpr_multipleChildren_returnsSelf() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getChildCount()).thenReturn(2);

        assertEquals(expr, support.unwrapExpr(expr));
    }

    @Test
    @DisplayName("Unwrap expr not expr returns self scenario")
    void unwrapExpr_notExpr_returnsSelf() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(literal.getChildCount()).thenReturn(0);

        assertEquals(literal, support.unwrapExpr(literal));
    }

    @Test
    @DisplayName("Unwrap expr null throws NPE scenario")
    void unwrapExpr_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.unwrapExpr(null));
    }

    // --- isNullLiteral tests ---

    @Test
    @DisplayName("Is null literal literal null returns true scenario case detail")
    void isNullLiteral_literalNull_returnsTrue() {
        DetailAST nullLit = mock(DetailAST.class);
        when(nullLit.getType()).thenReturn(TokenTypes.LITERAL_NULL);

        assertTrue(support.isNullLiteral(nullLit));
    }

    @Test
    @DisplayName("Is null literal casted null returns true scenario case")
    void isNullLiteral_castedNull_returnsTrue() {
        DetailAST nullLit = mock(DetailAST.class);
        when(nullLit.getType()).thenReturn(TokenTypes.LITERAL_NULL);

        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.getLastChild()).thenReturn(nullLit);
        when(typecast.getChildCount()).thenReturn(2);

        assertTrue(support.isNullLiteral(typecast));
    }

    @Test
    @DisplayName("Is null literal not null returns false scenario case detail")
    void isNullLiteral_notNull_returnsFalse() {
        DetailAST stringLit = mock(DetailAST.class);
        when(stringLit.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertFalse(support.isNullLiteral(stringLit));
    }

    @Test
    @DisplayName("Is null literal wrapped expr unwraps")
    void isNullLiteral_wrappedExpr_unwraps() {
        DetailAST nullLit = mock(DetailAST.class);
        when(nullLit.getType()).thenReturn(TokenTypes.LITERAL_NULL);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getChildCount()).thenReturn(1);
        when(expr.getFirstChild()).thenReturn(nullLit);

        assertTrue(support.isNullLiteral(expr));
    }

    // --- containsStringLiteral tests ---

    @Test
    @DisplayName("Contains string literal direct string literal returns true scenario")
    void containsStringLiteral_directStringLiteral_returnsTrue() {
        DetailAST stringLit = mock(DetailAST.class);
        when(stringLit.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(stringLit.getFirstChild()).thenReturn(null);

        assertTrue(support.containsStringLiteral(stringLit));
    }

    @Test
    @DisplayName("Contains string literal method call returns false scenario case")
    void containsStringLiteral_methodCall_returnsFalse() {
        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);

        assertFalse(support.containsStringLiteral(methodCall));
    }

    @Test
    @DisplayName("Contains string literal nested string literal returns true scenario")
    void containsStringLiteral_nestedStringLiteral_returnsTrue() {
        DetailAST stringLit = mock(DetailAST.class);
        when(stringLit.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(stringLit.getFirstChild()).thenReturn(null);
        when(stringLit.getNextSibling()).thenReturn(null);

        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);
        when(plus.getFirstChild()).thenReturn(stringLit);

        assertTrue(support.containsStringLiteral(plus));
    }

    @Test
    @DisplayName("Contains string literal no string literal returns false scenario case")
    void containsStringLiteral_noStringLiteral_returnsFalse() {
        DetailAST numInt = mock(DetailAST.class);
        when(numInt.getType()).thenReturn(TokenTypes.NUM_INT);
        when(numInt.getFirstChild()).thenReturn(null);

        assertFalse(support.containsStringLiteral(numInt));
    }

    @Test
    @DisplayName("Contains string literal null throws NPE")
    void containsStringLiteral_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.containsStringLiteral(null));
    }

    // --- containsStringLiteralDeep tests ---

    @Test
    @DisplayName("Contains string literal deep inside method call returns true")
    void containsStringLiteralDeep_insideMethodCall_returnsTrue() {
        DetailAST stringLit = mock(DetailAST.class);
        when(stringLit.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(stringLit.getFirstChild()).thenReturn(null);
        when(stringLit.getNextSibling()).thenReturn(null);

        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);
        when(methodCall.getFirstChild()).thenReturn(stringLit);

        assertTrue(support.containsStringLiteralDeep(methodCall));
    }

    @Test
    @DisplayName("Contains string literal deep null throws NPE")
    void containsStringLiteralDeep_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.containsStringLiteralDeep(null));
    }

    // --- findLambda tests ---

    @Test
    @DisplayName("Find lambda direct lambda returns lambda scenario")
    void findLambda_directLambda_returnsLambda() {
        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);

        assertEquals(lambda, support.findLambda(lambda));
    }

    @Test
    @DisplayName("Find lambda nested lambda finds it")
    void findLambda_nestedLambda_findsIt() {
        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);
        when(lambda.getFirstChild()).thenReturn(null);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(lambda);
        when(lambda.getNextSibling()).thenReturn(null);

        assertEquals(lambda, support.findLambda(expr));
    }

    @Test
    @DisplayName("Find lambda no lambda returns null scenario case")
    void findLambda_noLambda_returnsNull() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getFirstChild()).thenReturn(null);

        assertNull(support.findLambda(ident));
    }

    @Test
    @DisplayName("Find lambda null throws NPE scenario")
    void findLambda_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.findLambda(null));
    }

    // --- findPlus tests ---

    @Test
    @DisplayName("Find plus direct plus returns plus scenario")
    void findPlus_directPlus_returnsPlus() {
        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);

        assertEquals(plus, support.findPlus(plus));
    }

    @Test
    @DisplayName("Find plus inside method call returns null scenario")
    void findPlus_insideMethodCall_returnsNull() {
        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.getType()).thenReturn(TokenTypes.METHOD_CALL);

        assertNull(support.findPlus(methodCall));
    }

    @Test
    @DisplayName("Find plus nested plus finds it")
    void findPlus_nestedPlus_findsIt() {
        DetailAST plus = mock(DetailAST.class);
        when(plus.getType()).thenReturn(TokenTypes.PLUS);
        when(plus.getFirstChild()).thenReturn(null);
        when(plus.getNextSibling()).thenReturn(null);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getFirstChild()).thenReturn(plus);

        assertEquals(plus, support.findPlus(expr));
    }

    @Test
    @DisplayName("Find plus null throws NPE scenario")
    void findPlus_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.findPlus(null));
    }

    // --- collectArguments tests ---

    @Test
    @DisplayName("Collect arguments expr children collects exprs")
    void collectArguments_exprChildren_collectsExprs() {
        DetailAST expr1 = mock(DetailAST.class);
        when(expr1.getType()).thenReturn(TokenTypes.EXPR);
        DetailAST expr2 = mock(DetailAST.class);
        when(expr2.getType()).thenReturn(TokenTypes.EXPR);
        when(expr1.getNextSibling()).thenReturn(expr2);
        when(expr2.getNextSibling()).thenReturn(null);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getFirstChild()).thenReturn(expr1);

        List<DetailAST> result = support.collectArguments(elist);
        assertEquals(2, result.size());
        assertEquals(expr1, result.get(0));
        assertEquals(expr2, result.get(1));
    }

    @Test
    @DisplayName("Collect arguments lambda child collects lambda")
    void collectArguments_lambdaChild_collectsLambda() {
        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);
        when(lambda.getNextSibling()).thenReturn(null);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getFirstChild()).thenReturn(lambda);

        List<DetailAST> result = support.collectArguments(elist);
        assertEquals(1, result.size());
        assertEquals(lambda, result.get(0));
    }

    @Test
    @DisplayName("Collect arguments comma child skips comma")
    void collectArguments_commaChild_skipsComma() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        DetailAST comma = mock(DetailAST.class);
        when(comma.getType()).thenReturn(TokenTypes.COMMA);
        when(expr.getNextSibling()).thenReturn(comma);
        when(comma.getNextSibling()).thenReturn(null);

        DetailAST elist = mock(DetailAST.class);
        when(elist.getFirstChild()).thenReturn(expr);

        List<DetailAST> result = support.collectArguments(elist);
        assertEquals(1, result.size());
        assertEquals(expr, result.get(0));
    }

    @Test
    @DisplayName("Collect arguments empty elist returns empty list")
    void collectArguments_emptyElist_returnsEmptyList() {
        DetailAST elist = mock(DetailAST.class);
        when(elist.getFirstChild()).thenReturn(null);

        List<DetailAST> result = support.collectArguments(elist);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Collect arguments null throws NPE scenario")
    void collectArguments_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.collectArguments(null));
    }

    // --- extractMethodName tests ---

    @Test
    @DisplayName("Extract method name with dot returns rightmost")
    void extractMethodName_withDot_returnsRightmost() {
        DetailAST rightIdent = mock(DetailAST.class);
        when(rightIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(rightIdent.getText()).thenReturn("methodName");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(rightIdent);

        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.findFirstToken(TokenTypes.DOT)).thenReturn(dot);

        assertEquals("methodName", support.extractMethodName(methodCall));
    }

    @Test
    @DisplayName("Extract method name no dot returns ident")
    void extractMethodName_noDot_returnsIdent() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("simpleMethod");

        DetailAST methodCall = mock(DetailAST.class);
        when(methodCall.findFirstToken(TokenTypes.DOT)).thenReturn(null);
        when(methodCall.findFirstToken(TokenTypes.IDENT)).thenReturn(ident);

        assertEquals("simpleMethod", support.extractMethodName(methodCall));
    }

    // --- extractQualifierIdent tests ---

    @Test
    @DisplayName("Extract qualifier ident simple qualifier returns text")
    void extractQualifierIdent_simpleQualifier_returnsText() {
        DetailAST qualifier = mock(DetailAST.class);
        when(qualifier.getType()).thenReturn(TokenTypes.IDENT);
        when(qualifier.getText()).thenReturn("owner");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getFirstChild()).thenReturn(qualifier);

        assertEquals("owner", support.extractQualifierIdent(dot));
    }

    @Test
    @DisplayName("Extract qualifier ident nested dot traverses to bottom")
    void extractQualifierIdent_nestedDot_traversesToBottom() {
        DetailAST deepIdent = mock(DetailAST.class);
        when(deepIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(deepIdent.getText()).thenReturn("deepOwner");

        DetailAST innerDot = mock(DetailAST.class);
        when(innerDot.getType()).thenReturn(TokenTypes.DOT);
        when(innerDot.getLastChild()).thenReturn(deepIdent);

        DetailAST outerDot = mock(DetailAST.class);
        when(outerDot.getFirstChild()).thenReturn(innerDot);

        assertEquals("deepOwner", support.extractQualifierIdent(outerDot));
    }

    // --- extractNewClassName tests ---

    @Test
    @DisplayName("Extract new class name simple ident returns name")
    void extractNewClassName_simpleIdent_returnsName() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("Exception");
        when(ident.getNextSibling()).thenReturn(null);

        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getFirstChild()).thenReturn(ident);

        assertEquals("Exception", support.extractNewClassName(literalNew));
    }

    @Test
    @DisplayName("Extract new class name qualified name returns rightmost")
    void extractNewClassName_qualifiedName_returnsRightmost() {
        DetailAST rightIdent = mock(DetailAST.class);
        when(rightIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(rightIdent.getText()).thenReturn("IllegalStateException");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(rightIdent);
        when(dot.getNextSibling()).thenReturn(null);

        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getFirstChild()).thenReturn(dot);

        assertEquals("IllegalStateException", support.extractNewClassName(literalNew));
    }

    @Test
    @DisplayName("Extract new class name stops at lparen")
    void extractNewClassName_stopsAtLparen() {
        DetailAST lparen = mock(DetailAST.class);
        when(lparen.getType()).thenReturn(TokenTypes.LPAREN);

        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getFirstChild()).thenReturn(lparen);

        assertNull(support.extractNewClassName(literalNew));
    }

    @Test
    @DisplayName("Extract new class name stops at elist")
    void extractNewClassName_stopsAtElist() {
        DetailAST elist = mock(DetailAST.class);
        when(elist.getType()).thenReturn(TokenTypes.ELIST);

        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getFirstChild()).thenReturn(elist);

        assertNull(support.extractNewClassName(literalNew));
    }

    @Test
    @DisplayName("Extract new class name null throws NPE")
    void extractNewClassName_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.extractNewClassName(null));
    }

    // --- extractClassLiteralName tests ---

    @Test
    @DisplayName("Extract class literal name simple class returns name")
    void extractClassLiteralName_simpleClass_returnsName() {
        DetailAST classIdent = mock(DetailAST.class);
        when(classIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(classIdent.getText()).thenReturn("class");

        DetailAST typeIdent = mock(DetailAST.class);
        when(typeIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(typeIdent.getText()).thenReturn("String");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(classIdent);
        when(dot.getFirstChild()).thenReturn(typeIdent);

        assertEquals("String", support.extractClassLiteralName(dot));
    }

    @Test
    @DisplayName("Extract class literal name not class literal returns null scenario")
    void extractClassLiteralName_notClassLiteral_returnsNull() {
        DetailAST methodIdent = mock(DetailAST.class);
        when(methodIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(methodIdent.getText()).thenReturn("toString");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(methodIdent);

        assertNull(support.extractClassLiteralName(dot));
    }

    @Test
    @DisplayName("Extract class literal name wrapped expr unwraps")
    void extractClassLiteralName_wrappedExpr_unwraps() {
        DetailAST classIdent = mock(DetailAST.class);
        when(classIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(classIdent.getText()).thenReturn("class");

        DetailAST typeIdent = mock(DetailAST.class);
        when(typeIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(typeIdent.getText()).thenReturn("Integer");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(classIdent);
        when(dot.getFirstChild()).thenReturn(typeIdent);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.EXPR);
        when(expr.getChildCount()).thenReturn(1);
        when(expr.getFirstChild()).thenReturn(dot);

        assertEquals("Integer", support.extractClassLiteralName(expr));
    }

    @Test
    @DisplayName("Extract class literal name null throws NPE")
    void extractClassLiteralName_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.extractClassLiteralName(null));
    }

    // --- extractAssignedIdent tests ---

    @Test
    @DisplayName("Extract assigned ident ident returns text")
    void extractAssignedIdent_ident_returnsText() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("varName");

        assertEquals("varName", support.extractAssignedIdent(ident));
    }

    @Test
    @DisplayName("Extract assigned ident dot returns rightmost")
    void extractAssignedIdent_dot_returnsRightmost() {
        DetailAST rightIdent = mock(DetailAST.class);
        when(rightIdent.getType()).thenReturn(TokenTypes.IDENT);
        when(rightIdent.getText()).thenReturn("fieldName");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(rightIdent);

        assertEquals("fieldName", support.extractAssignedIdent(dot));
    }

    @Test
    @DisplayName("Extract assigned ident other type returns null")
    void extractAssignedIdent_otherType_returnsNull() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertNull(support.extractAssignedIdent(literal));
    }

    @Test
    @DisplayName("Extract assigned ident null throws NPE")
    void extractAssignedIdent_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> support.extractAssignedIdent(null));
    }

    // --- extractImportText tests ---

    @Test
    @DisplayName("Extract import text simple dot returns flattened")
    void extractImportText_simpleDot_returnsFlattened() {
        DetailAST left = mock(DetailAST.class);
        when(left.getType()).thenReturn(TokenTypes.IDENT);
        when(left.getText()).thenReturn("java");

        DetailAST right = mock(DetailAST.class);
        when(right.getType()).thenReturn(TokenTypes.IDENT);
        when(right.getText()).thenReturn("util");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getFirstChild()).thenReturn(left);
        when(dot.getLastChild()).thenReturn(right);

        DetailAST importAst = mock(DetailAST.class);
        when(importAst.findFirstToken(TokenTypes.DOT)).thenReturn(dot);

        assertEquals("java.util", support.extractImportText(importAst));
    }

    @Test
    @DisplayName("Extract import text no dot throws NPE")
    void extractImportText_noDot_throwsNPE() {
        DetailAST importAst = mock(DetailAST.class);
        when(importAst.findFirstToken(TokenTypes.DOT)).thenReturn(null);

        // extractImportText calls flattenDot(null) which throws NPE
        assertThrows(NullPointerException.class, () -> support.extractImportText(importAst));
    }

    @Test
    @DisplayName("Find first string argument skips non expr nodes")
    void findFirstStringArgument_skipsNonExprNodes() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);

        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        elist.addChild(lambda);

        DetailAstImpl stringExpr = createExpr(createStringLiteral("alpha"));
        elist.addChild(stringExpr);

        DetailAST match = support.findFirstStringArgument(elist, extractor);
        assertEquals(stringExpr, match, "Should return first matching string expression");
    }

    @Test
    @DisplayName("Find string argument expression returns direct string literal expr")
    void findStringArgumentExpression_directLiteral_returnsExpr() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);
        DetailAstImpl expr = createExpr(createStringLiteral("text"));

        DetailAST match = support.findStringArgumentExpression(expr, extractor);
        assertEquals(expr, match, "Should return the expression containing the string literal");
    }

    @Test
    @DisplayName("Find string argument expression unwraps new expression arguments")
    void findStringArgumentExpression_unwrapsNewExpressionArguments() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);

        DetailAstImpl stringExpr = createExpr(createStringLiteral("message"));
        DetailAstImpl elist = createElist(stringExpr);
        DetailAstImpl literalNew = createNewWithElist(elist);
        DetailAstImpl expr = createExpr(literalNew);

        DetailAST match = support.findStringArgumentExpression(expr, extractor);
        assertEquals(stringExpr, match, "Should return nested string argument expression");
    }

    @Test
    @DisplayName("Find string argument expression returns null for non string")
    void findStringArgumentExpression_nonString_returnsNull() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);
        DetailAstImpl expr = createExpr(createIdent("value"));

        DetailAST match = support.findStringArgumentExpression(expr, extractor);
        assertNull(match, "Should return null when no string literal is found");
    }

    private DetailAstImpl createExpr(DetailAstImpl child) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(child);
        return expr;
    }

    private DetailAstImpl createElist(DetailAstImpl... exprs) {
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        for (DetailAstImpl expr : exprs) {
            elist.addChild(expr);
        }
        return elist;
    }

    private DetailAstImpl createNewWithElist(DetailAstImpl elist) {
        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        literalNew.addChild(elist);
        return literalNew;
    }

    private DetailAstImpl createStringLiteral(String text) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + text + "\"");
        return literal;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }
}
