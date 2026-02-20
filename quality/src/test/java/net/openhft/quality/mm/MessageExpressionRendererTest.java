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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link MessageExpressionRenderer}.
 */
@DisplayName("Message expression renderer tests")
class MessageExpressionRendererTest {

    private MessageAstSupport astSupport;

    @BeforeEach
    void setUp() {
        astSupport = new MessageAstSupport();
    }

    @Test
    @DisplayName("render returns null for null expression")
    void render_returnsNullForNullExpression() {
        assertNull(MessageExpressionRenderer.render(null, astSupport),
                "null expression should return null");
    }

    @Test
    @DisplayName("render returns null for null astSupport")
    void render_returnsNullForNullAstSupport() {
        DetailAstImpl expr = createExpr(createStringLiteral("test"));
        assertNull(MessageExpressionRenderer.render(expr, null),
                "null astSupport should return null");
    }

    @Test
    @DisplayName("render handles string literal")
    void render_handlesStringLiteral() {
        DetailAstImpl expr = createExpr(createStringLiteral("hello world"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("\"hello world\"", rendered, "string literal should be rendered with quotes");
    }

    @Test
    @DisplayName("render handles char literal")
    void render_handlesCharLiteral() {
        DetailAstImpl expr = createExpr(createCharLiteral('x'));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("'x'", rendered, "char literal should be rendered with single quotes");
    }

    @Test
    @DisplayName("render handles numeric int literal")
    void render_handlesNumericIntLiteral() {
        DetailAstImpl expr = createExpr(createNumericLiteral(TokenTypes.NUM_INT, "42"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("42", rendered, "int literal should be rendered");
    }

    @Test
    @DisplayName("render handles numeric long literal")
    void render_handlesNumericLongLiteral() {
        DetailAstImpl expr = createExpr(createNumericLiteral(TokenTypes.NUM_LONG, "42L"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("42L", rendered, "long literal should be rendered");
    }

    @Test
    @DisplayName("render handles numeric float literal")
    void render_handlesNumericFloatLiteral() {
        DetailAstImpl expr = createExpr(createNumericLiteral(TokenTypes.NUM_FLOAT, "3.14f"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("3.14f", rendered, "float literal should be rendered");
    }

    @Test
    @DisplayName("render handles numeric double literal")
    void render_handlesNumericDoubleLiteral() {
        DetailAstImpl expr = createExpr(createNumericLiteral(TokenTypes.NUM_DOUBLE, "3.14"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("3.14", rendered, "double literal should be rendered");
    }

    @Test
    @DisplayName("render handles boolean true literal")
    void render_handlesBooleanTrueLiteral() {
        DetailAstImpl expr = createExpr(createBooleanLiteral(true));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("true", rendered, "true literal should be rendered");
    }

    @Test
    @DisplayName("render handles boolean false literal")
    void render_handlesBooleanFalseLiteral() {
        DetailAstImpl expr = createExpr(createBooleanLiteral(false));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("false", rendered, "false literal should be rendered");
    }

    @Test
    @DisplayName("render handles null literal")
    void render_handlesNullLiteral() {
        DetailAstImpl expr = createExpr(createNullLiteral());
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("null", rendered, "null literal should be rendered");
    }

    @Test
    @DisplayName("render handles identifier")
    void render_handlesIdentifier() {
        DetailAstImpl expr = createExpr(createIdent("myVariable"));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("myVariable", rendered, "identifier should be rendered");
    }

    @Test
    @DisplayName("render handles dot expression")
    void render_handlesDotExpression() {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.setText(".");
        DetailAstImpl left = createIdent("obj");
        DetailAstImpl right = createIdent("field");
        dot.addChild(left);
        dot.addChild(right);

        DetailAstImpl expr = createExpr(dot);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("obj.field", rendered, "dot expression should be rendered");
    }

    @Test
    @DisplayName("render handles plus binary expression")
    void render_handlesPlusBinaryExpression() {
        DetailAstImpl plus = new DetailAstImpl();
        plus.setType(TokenTypes.PLUS);
        plus.setText("+");
        plus.addChild(createStringLiteral("hello "));
        plus.addChild(createIdent("name"));

        DetailAstImpl expr = createExpr(plus);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("\"hello \" + name", rendered, "plus expression should be rendered");
    }

    @Test
    @DisplayName("render handles method call without arguments")
    void render_handlesMethodCallWithoutArguments() {
        DetailAstImpl methodCall = createSimpleMethodCall("getMessage");
        DetailAstImpl expr = createExpr(methodCall);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("getMessage()", rendered, "method call without args should be rendered");
    }

    @Test
    @DisplayName("render handles method call with arguments")
    void render_handlesMethodCallWithArguments() {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");
        DetailAstImpl ident = createIdent("format");
        methodCall.addChild(ident);
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        elist.setText("ELIST");
        elist.addChild(createExpr(createStringLiteral("pattern")));
        elist.addChild(createExpr(createIdent("arg1")));
        methodCall.addChild(elist);

        DetailAstImpl expr = createExpr(methodCall);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "method call with args should be rendered");
        assertEquals("format(\"pattern\", arg1)", rendered,
                "method call with args should include all arguments");
    }

    @Test
    @DisplayName("render handles qualified method call")
    void render_handlesQualifiedMethodCall() {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.setText(".");
        dot.addChild(createIdent("String"));
        dot.addChild(createIdent("format"));
        methodCall.addChild(dot);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        elist.setText("ELIST");
        elist.addChild(createExpr(createStringLiteral("msg")));
        methodCall.addChild(elist);

        DetailAstImpl expr = createExpr(methodCall);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "qualified method call should be rendered");
        assertEquals("String.format(\"msg\")", rendered,
                "qualified method call should include qualifier");
    }

    @Test
    @DisplayName("render handles typecast")
    void render_handlesTypecast() {
        DetailAstImpl typecast = new DetailAstImpl();
        typecast.setType(TokenTypes.TYPECAST);
        typecast.setText("(");

        DetailAstImpl type = new DetailAstImpl();
        type.setType(TokenTypes.TYPE);
        type.setText("TYPE");
        DetailAstImpl typeIdent = createIdent("String");
        type.addChild(typeIdent);
        typecast.addChild(type);

        typecast.addChild(createIdent("obj"));

        DetailAstImpl expr = createExpr(typecast);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "typecast should be rendered");
        assertEquals("(String) obj", rendered, "typecast should include type and expression");
    }

    @Test
    @DisplayName("render handles nested expressions")
    void render_handlesNestedExpressions() {
        // "prefix" + obj.getName()
        DetailAstImpl plus = new DetailAstImpl();
        plus.setType(TokenTypes.PLUS);
        plus.setText("+");
        plus.addChild(createStringLiteral("prefix: "));

        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.setText(".");
        dot.addChild(createIdent("obj"));
        dot.addChild(createIdent("getName"));
        methodCall.addChild(dot);
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);

        plus.addChild(methodCall);

        DetailAstImpl expr = createExpr(plus);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "nested expression should be rendered");
        assertEquals("\"prefix: \" + obj.getName()", rendered,
                "nested expression should combine all parts");
    }

    @Test
    @DisplayName("render handles lambda expression")
    void render_handlesLambdaExpression() {
        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        lambda.setText("->");

        DetailAstImpl expr = createExpr(lambda);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("->", rendered, "lambda should render its text");
    }

    @Test
    @DisplayName("render handles method reference")
    void render_handlesMethodReference() {
        DetailAstImpl methodRef = new DetailAstImpl();
        methodRef.setType(TokenTypes.METHOD_REF);
        methodRef.setText("::");

        DetailAstImpl expr = createExpr(methodRef);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("::", rendered, "method reference should render its text");
    }

    @Test
    @DisplayName("render returns null for empty string result")
    void render_returnsNullForEmptyResult() {
        // Create expression that would render to empty/whitespace
        DetailAstImpl expr = createExpr(createIdent(""));
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        // Empty identifier renders as empty string which becomes null
        assertNull(rendered, "empty result should return null");
    }

    @Test
    @DisplayName("render handles deeply nested dot expression")
    void render_handlesDeeplyNestedDotExpression() {
        // a.b.c.d
        DetailAstImpl dot1 = new DetailAstImpl();
        dot1.setType(TokenTypes.DOT);
        dot1.setText(".");
        dot1.addChild(createIdent("a"));
        dot1.addChild(createIdent("b"));

        DetailAstImpl dot2 = new DetailAstImpl();
        dot2.setType(TokenTypes.DOT);
        dot2.setText(".");
        dot2.addChild(dot1);
        dot2.addChild(createIdent("c"));

        DetailAstImpl dot3 = new DetailAstImpl();
        dot3.setType(TokenTypes.DOT);
        dot3.setText(".");
        dot3.addChild(dot2);
        dot3.addChild(createIdent("d"));

        DetailAstImpl expr = createExpr(dot3);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertEquals("a.b.c.d", rendered, "deeply nested dot should be rendered");
    }

    @Test
    @DisplayName("render handles unknown token type")
    void render_handlesUnknownTokenType() {
        // Create a node with an uncommon token type
        DetailAstImpl node = new DetailAstImpl();
        node.setType(TokenTypes.ARRAY_INIT);  // Not explicitly handled
        node.setText("ARRAY_INIT");

        DetailAstImpl expr = createExpr(node);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        // Falls through to default case which returns getText()
        assertEquals("ARRAY_INIT", rendered, "unknown token should use getText()");
    }

    @Test
    @DisplayName("render handles typecast without TYPE node uses expression only")
    void render_handlesTypecastWithoutTypeNode() {
        DetailAstImpl typecast = new DetailAstImpl();
        typecast.setType(TokenTypes.TYPECAST);
        typecast.setText("(");
        // No TYPE child, just expression child
        typecast.addChild(createIdent("obj"));

        DetailAstImpl expr = createExpr(typecast);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "typecast without TYPE should still render expression");
        assertEquals("obj", rendered, "typecast without TYPE should render child expression");
    }

    @Test
    @DisplayName("render handles method call without ELIST omits parenthesised args")
    void render_handlesMethodCallWithoutElist() {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");
        methodCall.addChild(createIdent("doSomething"));
        // No ELIST added

        DetailAstImpl expr = createExpr(methodCall);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "method call without ELIST should still render");
        assertEquals("doSomething()", rendered, "method call without ELIST should render name and empty parens");
    }

    @Test
    @DisplayName("render handles method call with null method name uses empty name")
    void render_handlesMethodCallWithNullMethodName() {
        // Method call where extractMethodName returns null (no IDENT child)
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");
        // Add ELIST but no IDENT
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);

        DetailAstImpl expr = createExpr(methodCall);
        String rendered = MessageExpressionRenderer.render(expr, astSupport);
        assertNotNull(rendered, "method call without name should still render");
        assertEquals("()", rendered, "method call without name should render just parens");
    }

    @Test
    @DisplayName("renderNode directly handles EXPR by delegating to child")
    void renderNode_handlesExprByDelegatingToChild() {
        // Test renderNode with EXPR token
        DetailAstImpl exprNode = new DetailAstImpl();
        exprNode.setType(TokenTypes.EXPR);
        exprNode.addChild(createStringLiteral("inner"));

        String rendered = MessageExpressionRenderer.renderNode(exprNode, astSupport);
        assertEquals("\"inner\"", rendered, "EXPR token should delegate to first child");
    }

    // Helper methods

    private DetailAstImpl createExpr(DetailAST child) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.setText("EXPR");
        if (child != null) {
            expr.addChild(child);
        }
        return expr;
    }

    private DetailAstImpl createStringLiteral(String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + value + "\"");
        return literal;
    }

    private DetailAstImpl createCharLiteral(char value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.CHAR_LITERAL);
        literal.setText("'" + value + "'");
        return literal;
    }

    private DetailAstImpl createNumericLiteral(int type, String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(type);
        literal.setText(value);
        return literal;
    }

    private DetailAstImpl createBooleanLiteral(boolean value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(value ? TokenTypes.LITERAL_TRUE : TokenTypes.LITERAL_FALSE);
        literal.setText(String.valueOf(value));
        return literal;
    }

    private DetailAstImpl createNullLiteral() {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.LITERAL_NULL);
        literal.setText("null");
        return literal;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private DetailAstImpl createSimpleMethodCall(String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setText("(");
        methodCall.addChild(createIdent(name));
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }
}
