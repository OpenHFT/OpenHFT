/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Objects;

/**
 * Analyzes expression types to determine if they are String or Supplier types.
 */
public final class ExpressionTypeAnalyzer {

    private final MessageExtractionContext context;
    private final MessageAstSupport astSupport;

    /**
     * Create an expression type analyzer.
     *
     * @param context extraction context with type information.
     */
    public ExpressionTypeAnalyzer(MessageExtractionContext context) {
        this.context = Objects.requireNonNull(context);
        this.astSupport = context.astSupport();
    }

    /**
     * Check if an expression is a lambda or method reference.
     *
     * @param expr expression to check.
     * @return {@code true} if the expression is a lambda or method reference.
     */
    public boolean isLambdaArgument(DetailAST expr) {
        DetailAST content = astSupport.unwrapExpr(expr);
        return content != null
                && (content.getType() == TokenTypes.LAMBDA || content.getType() == TokenTypes.METHOD_REF);
    }

    /**
     * Check if an expression has String type.
     *
     * @param expr expression to check.
     * @return {@code true} if the expression has String type.
     */
    public boolean isStringTypedExpression(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (expr.getType() == TokenTypes.TYPECAST) {
            DetailAST type = expr.findFirstToken(TokenTypes.TYPE);
            return type != null && isStringTypeName(astSupport.extractTypeName(type));
        }
        if (expr.getType() == TokenTypes.IDENT) {
            return isStringTypeName(context.getVariableType(expr.getText()));
        }
        if (expr.getType() == TokenTypes.PLUS) {
            return astSupport.containsStringLiteralDeep(expr);
        }
        return false;
    }

    /**
     * Check if an expression has Supplier type.
     *
     * @param expr expression to check.
     * @return {@code true} if the expression has Supplier type.
     */
    public boolean isSupplierTypedExpression(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.IDENT) {
            return isSupplierTypeName(context.getVariableType(expr.getText()));
        }
        return false;
    }

    /**
     * Check if a type name represents String.
     *
     * @param typeName type name to check.
     * @return {@code true} if the type is String.
     */
    public boolean isStringTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = context.resolveTypeName(typeName);
        return "java.lang.String".equals(resolved) || "String".equals(resolved);
    }

    /**
     * Check if a type name represents Supplier.
     *
     * @param typeName type name to check.
     * @return {@code true} if the type is Supplier.
     */
    public boolean isSupplierTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = context.resolveTypeName(typeName);
        return "java.util.function.Supplier".equals(resolved)
                || "Supplier".equals(resolved)
                || (resolved != null && resolved.endsWith(".Supplier"));
    }
}
