/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Best-effort renderer for message expressions.
 */
public final class MessageExpressionRenderer {
    private MessageExpressionRenderer() {
    }

    /**
     * Render a message expression using a best-effort AST traversal.
     *
     * @param expr       expression node.
     * @param astSupport AST support utilities.
     * @return rendered expression text, or {@code null} if unavailable.
     */
    public static String render(DetailAST expr, MessageAstSupport astSupport) {
        if (expr == null || astSupport == null) {
            return null;
        }
        DetailAST content = astSupport.unwrapExpr(expr);
        if (content == null) {
            return null;
        }
        String rendered = renderNode(content, astSupport);
        return rendered == null || rendered.trim().isEmpty() ? null : rendered;
    }

    static String renderNode(DetailAST node, MessageAstSupport astSupport) {
        requireNonNull(node);
        switch (node.getType()) {
            case TokenTypes.STRING_LITERAL:
            case TokenTypes.CHAR_LITERAL:
            case TokenTypes.NUM_INT:
            case TokenTypes.NUM_LONG:
            case TokenTypes.NUM_FLOAT:
            case TokenTypes.NUM_DOUBLE:
            case TokenTypes.LITERAL_TRUE:
            case TokenTypes.LITERAL_FALSE:
            case TokenTypes.LITERAL_NULL:
                return node.getText();
            case TokenTypes.IDENT:
                return node.getText();
            case TokenTypes.DOT:
                return renderDot(node, astSupport);
            case TokenTypes.METHOD_CALL:
                return renderMethodCall(node, astSupport);
            case TokenTypes.PLUS:
                return renderBinary(node, astSupport, " + ");
            case TokenTypes.TYPECAST:
                return renderTypecast(node, astSupport);
            case TokenTypes.EXPR:
                return renderNode(node.getFirstChild(), astSupport);
            case TokenTypes.LAMBDA:
            case TokenTypes.METHOD_REF:
                return node.getText();
            default:
                return node.getText();
        }
    }

    private static String renderBinary(DetailAST node, MessageAstSupport astSupport, String op) {
        DetailAST left = node.getFirstChild();
        DetailAST right = node.getLastChild();
        String leftText = left == null ? null : renderNode(left, astSupport);
        String rightText = right == null ? null : renderNode(right, astSupport);
        if (leftText == null || rightText == null) {
            return node.getText();
        }
        return leftText + op + rightText;
    }

    private static String renderDot(DetailAST dot, MessageAstSupport astSupport) {
        DetailAST left = dot.getFirstChild();
        DetailAST right = dot.getLastChild();
        String leftText = left == null ? null : renderNode(left, astSupport);
        String rightText = right == null ? null : renderNode(right, astSupport);
        if (leftText == null || rightText == null) {
            return dot.getText();
        }
        return leftText + "." + rightText;
    }

    private static String renderMethodCall(DetailAST methodCall, MessageAstSupport astSupport) {
        String methodName = astSupport.extractMethodName(methodCall);
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        String qualifier = null;
        if (dot != null) {
            DetailAST left = dot.getFirstChild();
            qualifier = left == null ? null : renderNode(left, astSupport);
        }
        StringBuilder builder = new StringBuilder(32);
        if (qualifier != null && !qualifier.isEmpty()) {
            builder.append(qualifier).append('.');
        }
        if (methodName != null) {
            builder.append(methodName);
        }
        builder.append('(');
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist != null) {
            List<DetailAST> args = astSupport.collectArguments(elist);
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                String argText = renderNode(args.get(i), astSupport);
                builder.append(argText == null ? "?" : argText);
            }
        }
        builder.append(')');
        return builder.toString();
    }

    private static String renderTypecast(DetailAST node, MessageAstSupport astSupport) {
        DetailAST type = node.findFirstToken(TokenTypes.TYPE);
        String typeName = type == null ? null : astSupport.extractTypeName(type);
        DetailAST expr = node.getLastChild();
        String exprText = expr == null ? null : renderNode(expr, astSupport);
        if (exprText == null) {
            return node.getText();
        }
        if (typeName == null) {
            return exprText;
        }
        return "(" + typeName + ") " + exprText;
    }
}
