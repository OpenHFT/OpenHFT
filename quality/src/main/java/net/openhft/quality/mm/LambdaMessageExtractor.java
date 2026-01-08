/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import static java.util.Objects.requireNonNull;

/**
 * Extracts message content from lambda expressions and suppliers.
 */
public final class LambdaMessageExtractor {

    private final MessageExtractionContext context;
    private final MessageAstSupport astSupport;

    /**
     * Create a lambda message extractor.
     *
     * @param context extraction context with template extractor.
     */
    public LambdaMessageExtractor(MessageExtractionContext context) {
        this.context = requireNonNull(context);
        this.astSupport = context.astSupport();
    }

    /**
     * Extract a cheap supplier description for a lambda with simple concatenation.
     *
     * @param lambda lambda AST node.
     * @return description string, or {@code null} if not a cheap supplier.
     */
    public String extractCheapSupplierDescription(DetailAST lambda) {
        DetailAST expr = lambda.findFirstToken(TokenTypes.EXPR);
        if (expr == null) {
            return null;
        }
        DetailAST body = expr.getFirstChild();
        if (body == null) {
            return null;
        }
        if (isCheapConcatenation(body)) {
            return describeCheapExpression(body);
        }
        return null;
    }

    /**
     * Extract the trivial message from a lambda expression.
     *
     * @param expr expression containing the lambda.
     * @return message string, or {@code null} if not extractable.
     */
    public String extractTrivialLambdaMessage(DetailAST expr) {
        DetailAST lambda = astSupport.findLambda(expr);
        requireNonNull(lambda);
        DetailAST body = lambda.getLastChild();
        requireNonNull(body);
        if (body.getType() == TokenTypes.EXPR) {
            body = body.getFirstChild();
        }
        return extractTrivialSupplierMessage(body);
    }

    /**
     * Extract a trivial message directly from a lambda AST node.
     *
     * @param lambda lambda AST node.
     * @return message string, or {@code null} if not extractable.
     */
    public String extractTrivialLambdaMessageDirect(DetailAST lambda) {
        DetailAST expr = lambda.findFirstToken(TokenTypes.EXPR);
        if (expr == null) {
            return null;
        }
        DetailAST body = expr.getFirstChild();
        if (body == null) {
            return null;
        }
        MessageTemplateExtractor templateExtractor = context.templateExtractor();
        if (body.getType() == TokenTypes.METHOD_CALL) {
            MessageTemplate template = templateExtractor.extractMessageTemplate(body);
            if (template != null && template.fromFormatCall()) {
                return template.message();
            }
        }
        if (templateExtractor.isConstantStringExpression(body)) {
            return templateExtractor.extractConstantString(body);
        }
        return null;
    }

    /**
     * Extract a trivial message from a supplier body expression.
     *
     * @param body body expression AST.
     * @return message string, or {@code null} if not extractable.
     */
    public String extractTrivialSupplierMessage(DetailAST body) {
        MessageTemplateExtractor templateExtractor = context.templateExtractor();
        if (astSupport.findPlus(body) != null && astSupport.containsStringLiteral(body)) {
            String constantParts = templateExtractor.extractStringLiteral(body);
            return summariseSupplierMessage(constantParts);
        }
        return null;
    }

    /**
     * Summarise a supplier message with placeholder indication.
     *
     * @param constantParts constant string parts.
     * @return summarised message.
     */
    public String summariseSupplierMessage(String constantParts) {
        if (constantParts == null) {
            return "...";
        }
        String normalised = constantParts.replaceAll("\\s+", " ").trim();
        if (normalised.isEmpty()) {
            return "...";
        }
        return normalised + " ...";
    }

    private boolean isCheapConcatenation(DetailAST expr) {
        switch (expr.getType()) {
            case TokenTypes.IDENT:
                return true;
            case TokenTypes.PLUS:
                // Cheap if both children are identifiers (variables)
                return expr.getFirstChild().getType() == TokenTypes.IDENT
                        && expr.getLastChild().getType() == TokenTypes.IDENT;
            default:
                return false;
        }
    }

    private String describeCheapExpression(DetailAST expr) {
        StringBuilder sb = new StringBuilder();
        describeCheapExpressionPart(expr, sb);
        return sb.toString();
    }

    private void describeCheapExpressionPart(DetailAST expr, StringBuilder sb) {
        requireNonNull(expr);
        switch (expr.getType()) {
            case TokenTypes.IDENT:
                sb.append(expr.getText());
                break;
            case TokenTypes.PLUS:
                describeCheapExpressionPart(expr.getFirstChild(), sb);
                sb.append(" + ");
                describeCheapExpressionPart(expr.getLastChild(), sb);
                break;
            default:
                break;
        }
    }
}
