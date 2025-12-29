/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Extracts message templates and placeholder counts from expressions.
 */
public final class MessageTemplateExtractor {

    private static final String CONCAT_PLACEHOLDER = "{}";
    private final LocaleDetector localeDetector;

    /**
     * Create a message template extractor.
     *
     * @param localeDetector optional Locale detector, or {@code null}.
     */
    public MessageTemplateExtractor(LocaleDetector localeDetector) {
        this.localeDetector = localeDetector;
    }

    /**
     * Extract a message template from an expression.
     *
     * @param expr expression to inspect.
     * @return extracted template, or {@code null} if none is found.
     */
    public MessageTemplate extractMessageTemplate(DetailAST expr) {
        Objects.requireNonNull(expr);
        DetailAST content = unwrapExpr(expr);
        if (content != null && content.getType() == TokenTypes.METHOD_CALL) {
            MessageTemplate template = extractTemplateFromMethodCall(content);
            if (template != null) {
                return template;
            }
        }
        String message = extractStringLiteral(expr, false);
        if (message == null) {
            return null;
        }
        int placeholderCount = countPlaceholderTokens(content);
        return new MessageTemplate(message, placeholderCount, false);
    }

    /**
     * Extract a string literal from an expression, allowing method calls.
     *
     * @param expr expression to inspect.
     * @return string literal content, or {@code null} if not found.
     */
    public String extractStringLiteral(DetailAST expr) {
        return extractStringLiteral(expr, true);
    }

    /**
     * Extract a string literal from an expression.
     *
     * @param expr            expression to inspect.
     * @param allowMethodCall {@code true} to allow format calls.
     * @return string literal content, or {@code null} if not found.
     */
    public String extractStringLiteral(DetailAST expr, boolean allowMethodCall) {
        Objects.requireNonNull(expr);
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }
        if (content != null && content.getType() == TokenTypes.METHOD_CALL) {
            if (!allowMethodCall) {
                return null;
            }
            MessageTemplate template = extractTemplateFromMethodCall(content);
            return template != null ? template.message() : null;
        }
        if (isConstantStringExpression(expr)) {
            return extractConstantString(expr);
        }
        String constantPart = extractConstantStringParts(expr);
        if (constantPart != null && !constantPart.isEmpty()) {
            return constantPart;
        }
        DetailAST literal = findStringLiteral(expr);
        if (literal != null) {
            String text = literal.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
        }
        return null;
    }

    /**
     * Determine whether an expression is a constant string expression.
     *
     * @param expr expression to inspect.
     * @return {@code true} if the expression is a constant string.
     */
    public boolean isConstantStringExpression(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (expr.getType() == TokenTypes.PLUS) {
            DetailAST left = expr.getFirstChild();
            DetailAST right = expr.getLastChild();
            return isConstantStringExpression(left) && isConstantStringExpression(right);
        }
        return false;
    }

    /**
     * Extract the constant string value from an expression.
     *
     * @param expr expression to inspect.
     * @return constant string value, or {@code null} if not constant.
     */
    public String extractConstantString(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            String text = expr.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return "";
        }
        if (expr.getType() == TokenTypes.PLUS) {
            DetailAST left = expr.getFirstChild();
            DetailAST right = expr.getLastChild();
            String leftStr = extractConstantString(left);
            String rightStr = extractConstantString(right);
            if (leftStr != null && rightStr != null) {
                return leftStr + rightStr;
            }
        }
        return null;
    }

    /**
     * Count placeholder tokens in a string expression.
     *
     * @param expr expression to inspect.
     * @return number of placeholder tokens.
     */
    public int countPlaceholderTokens(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return 0;
        }
        if (expr.getType() == TokenTypes.PLUS) {
            return countPlaceholderTokens(expr.getFirstChild())
                    + countPlaceholderTokens(expr.getLastChild());
        }
        if (expr.getType() == TokenTypes.TYPECAST) {
            return countPlaceholderTokens(expr.getLastChild());
        }
        if (expr.getType() == TokenTypes.IDENT
                || expr.getType() == TokenTypes.METHOD_CALL
                || expr.getType() == TokenTypes.NUM_INT
                || expr.getType() == TokenTypes.NUM_LONG
                || expr.getType() == TokenTypes.NUM_FLOAT
                || expr.getType() == TokenTypes.NUM_DOUBLE
                || expr.getType() == TokenTypes.DOT) {
            return 1;
        }
        if (expr.getType() == TokenTypes.EXPR) {
            int total = 0;
            DetailAST child = expr.getFirstChild();
            while (child != null) {
                total += countPlaceholderTokens(child);
                child = child.getNextSibling();
            }
            return total;
        }
        int total = 0;
        DetailAST child = expr.getFirstChild();
        while (child != null) {
            total += countPlaceholderTokens(child);
            child = child.getNextSibling();
        }
        return total;
    }

    /**
     * Count key-value label patterns in a constant message.
     *
     * @param constantParts constant message text.
     * @return number of key-value labels.
     */
    public int countKeyValueLabels(String constantParts) {
        if (constantParts == null || constantParts.isEmpty()) {
            return 0;
        }
        int count = 0;
        Matcher matcher = PlaceholderPatterns.KEY_VALUE_LABEL.matcher(constantParts);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Count annotation placeholders (for example, {@code {index}}).
     *
     * @param message annotation message.
     * @return number of placeholders found.
     */
    public int countAnnotationPlaceholders(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        int count = 0;
        Matcher matcher = PlaceholderPatterns.ANNOTATION.matcher(message);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Count logging placeholders in a message.
     *
     * @param message log message text.
     * @return number of log placeholders.
     */
    public int countLogPlaceholders(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        int count = 0;
        Matcher matcher = PlaceholderPatterns.LOG.matcher(message);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Count format placeholders for String.format or MessageFormat styles.
     *
     * @param message format string.
     * @return number of placeholders.
     */
    public int countFormatPlaceholders(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }
        int stringCount = 0;
        Matcher stringMatcher = PlaceholderPatterns.STRING_FORMAT.matcher(message);
        while (stringMatcher.find()) {
            stringCount++;
        }
        int messageCount = 0;
        Matcher messageMatcher = PlaceholderPatterns.MESSAGE_FORMAT.matcher(message);
        while (messageMatcher.find()) {
            messageCount++;
        }
        return Math.max(stringCount, messageCount);
    }

    private MessageTemplate extractTemplateFromMethodCall(DetailAST methodCall) {
        String methodName = extractMethodName(methodCall);
        Objects.requireNonNull(methodName);
        if ("formatted".equals(methodName)) {
            return extractFormattedTemplate(methodCall);
        }
        if (!"format".equals(methodName)) {
            return null;
        }
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return null;
        }
        List<DetailAST> args = collectArguments(elist);
        if (args.isEmpty()) {
            return null;
        }
        int templateIndex = 0;
        if (args.size() > 1 && isLocaleExpression(args.get(0))) {
            templateIndex = 1;
        }
        if (templateIndex >= args.size()) {
            return null;
        }
        String template = extractStringLiteral(args.get(templateIndex), false);
        if (template == null) {
            return null;
        }
        int placeholderCount = countFormatPlaceholders(template);
        return new MessageTemplate(template, placeholderCount, true);
    }

    private MessageTemplate extractFormattedTemplate(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        Objects.requireNonNull(dot);
        DetailAST receiver = dot.getFirstChild();
        Objects.requireNonNull(receiver);
        String template = extractStringLiteral(receiver, false);
        if (template == null) {
            return null;
        }
        int placeholderCount = countFormatPlaceholders(template);
        return new MessageTemplate(template, placeholderCount, true);
    }

    private DetailAST unwrapExpr(DetailAST expr) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.EXPR && expr.getChildCount() == 1) {
            return expr.getFirstChild();
        }
        return expr;
    }

    private List<DetailAST> collectArguments(DetailAST elist) {
        List<DetailAST> args = new ArrayList<>();
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.LAMBDA) {
                args.add(child);
            }
            child = child.getNextSibling();
        }
        return args;
    }

    private String extractMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            DetailAST rightmost = findRightmostIdent(dot);
            return rightmost != null ? rightmost.getText() : null;
        }
        DetailAST ident = methodCall.findFirstToken(TokenTypes.IDENT);
        if (ident != null) {
            return ident.getText();
        }
        return null;
    }

    private DetailAST findRightmostIdent(DetailAST dot) {
        Objects.requireNonNull(dot);
        DetailAST lastChild = dot.getLastChild();
        Objects.requireNonNull(lastChild);
        if (lastChild.getType() == TokenTypes.IDENT) {
            return lastChild;
        }
        if (lastChild.getType() == TokenTypes.DOT) {
            return findRightmostIdent(lastChild);
        }
        return null;
    }

    private String extractConstantStringParts(DetailAST expr) {
        return extractConstantStringParts(expr, false);
    }

    private String extractConstantStringParts(DetailAST expr, boolean allowPlaceholder) {
        Objects.requireNonNull(expr);
        if (expr.getType() == TokenTypes.EXPR && expr.getChildCount() == 1) {
            return extractConstantStringParts(expr.getFirstChild(), allowPlaceholder);
        }
        if (expr.getType() == TokenTypes.TYPECAST) {
            return extractConstantStringParts(expr.getLastChild(), allowPlaceholder);
        }
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            String text = expr.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return "";
        }
        if (expr.getType() == TokenTypes.PLUS) {
            String left = Objects.requireNonNull(extractConstantStringParts(expr.getFirstChild(), true));
            String right = Objects.requireNonNull(extractConstantStringParts(expr.getLastChild(), true));
            return left + right;
        }
        if (allowPlaceholder) {
            return CONCAT_PLACEHOLDER;
        }
        if (expr.getType() == TokenTypes.METHOD_CALL) {
            return "";
        }
        StringBuilder parts = new StringBuilder();
        DetailAST child = expr.getFirstChild();
        while (child != null) {
            String part = extractConstantStringParts(child, false);
            if (part != null) {
                parts.append(part);
            }
            child = child.getNextSibling();
        }
        return parts.toString();
    }

    private DetailAST findStringLiteral(DetailAST ast) {
        Objects.requireNonNull(ast);
        if (ast.getType() == TokenTypes.STRING_LITERAL) {
            return ast;
        }
        if (ast.getType() == TokenTypes.METHOD_CALL) {
            return null;
        }
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            DetailAST found = findStringLiteral(child);
            if (found != null) {
                return found;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private boolean isLocaleExpression(DetailAST expr) {
        return localeDetector != null && localeDetector.isLocaleExpression(expr);
    }

    /**
     * Resolves whether an expression represents a Locale argument.
     */
    public interface LocaleDetector {
        /**
         * Determine whether the expression is a Locale.
         *
         * @param expr expression to inspect.
         * @return {@code true} if the expression represents a Locale.
         */
        boolean isLocaleExpression(DetailAST expr);
    }
}
