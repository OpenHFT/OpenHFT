/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Tracks @SuppressWarnings tokens for MeaningfulMessage rule suppression.
 */
public class SuppressionTracker {

    private static final String SUPPRESS_WARNINGS = "SuppressWarnings";
    private static final String CHECKSTYLE_PREFIX = "checkstyle:";
    private static final String ALL_TOKEN = "MM-all";
    private static final String CHECK_ID = "MeaningfulMessage";
    private static final String CHECK_CLASS = "MeaningfulMessageCheck";

    private final Deque<SuppressionScope> scopes = new ArrayDeque<>();
    private final SuppressionScope fileScope = new SuppressionScope();

    /**
     * Create a suppression tracker.
     */
    public SuppressionTracker() {
    }

    /**
     * Enter a new suppression scope based on a type or method definition.
     *
     * @param scopeAst AST node that defines the scope.
     */
    public void enterScope(DetailAST scopeAst) {
        SuppressionScope parent = scopes.peek();
        SuppressionScope current = parent == null
                ? new SuppressionScope()
                : new SuppressionScope(parent);
        for (String token : extractSuppressWarnings(scopeAst)) {
            current.addToken(token);
        }
        scopes.push(current);
    }

    /**
     * Record suppressions declared on a top-level type for file-level checks.
     *
     * @param scopeAst AST node that defines the top-level type.
     */
    public void recordFileSuppressions(DetailAST scopeAst) {
        for (String token : extractSuppressWarnings(scopeAst)) {
            fileScope.addToken(token);
        }
    }

    /**
     * Leave the current suppression scope.
     */
    public void leaveScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    /**
     * Check whether a rule is suppressed in the current scope.
     *
     * @param ruleId rule identifier to check.
     * @return {@code true} if the rule is suppressed.
     */
    public boolean isSuppressed(RuleId ruleId) {
        requireNonNull(ruleId);
        SuppressionScope scope = scopes.peek();
        if (scope == null) {
            return false;
        }
        if (scope.suppressAll) {
            return true;
        }
        return scope.suppressedCodes.contains(ruleId.code());
    }

    /**
     * Check whether a rule is suppressed at the file level.
     *
     * @param ruleId rule identifier to check.
     * @return {@code true} if the rule is suppressed for the file.
     */
    public boolean isSuppressedInFile(RuleId ruleId) {
        requireNonNull(ruleId);
        if (fileScope.suppressAll) {
            return true;
        }
        return fileScope.suppressedCodes.contains(ruleId.code());
    }

    private List<String> extractSuppressWarnings(DetailAST scopeAst) {
        requireNonNull(scopeAst);
        DetailAST modifiers = scopeAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return java.util.Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                String name = extractAnnotationName(child);
                if (name != null && name.endsWith(SUPPRESS_WARNINGS)) {
                    DetailAST valueExpr = findAnnotationValue(child);
                    if (valueExpr != null) {
                        collectStringValues(valueExpr, tokens);
                    }
                }
            }
            child = child.getNextSibling();
        }
        return tokens;
    }

    void collectStringValues(DetailAST expr, List<String> tokens) {
        DetailAST content = unwrapExpr(expr);
        requireNonNull(content);
        if (content.getType() == TokenTypes.EXPR) {
            DetailAST child = content.getFirstChild();
            while (child != null) {
                collectStringValues(child, tokens);
                child = child.getNextSibling();
            }
            return;
        }
        if (content.getType() == TokenTypes.STRING_LITERAL) {
            String literal = stripQuotes(content.getText());
            if (literal != null) {
                tokens.add(literal);
            }
            return;
        }
        if (content.getType() == TokenTypes.ARRAY_INIT
                || content.getType() == TokenTypes.ANNOTATION_ARRAY_INIT) {
            DetailAST child = content.getFirstChild();
            while (child != null) {
                collectStringValues(child, tokens);
                child = child.getNextSibling();
            }
        }
    }

    DetailAST unwrapExpr(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.EXPR && expr.getChildCount() == 1) {
            return expr.getFirstChild();
        }
        return expr;
    }

    DetailAST findAnnotationValue(DetailAST annotationAst) {
        DetailAST child = annotationAst.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                DetailAST ident = child.findFirstToken(TokenTypes.IDENT);
                if (ident != null && "value".equals(ident.getText())) {
                    DetailAST expr = child.findFirstToken(TokenTypes.EXPR);
                    if (expr != null) {
                        return expr;
                    }
                    DetailAST arrayInit = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
                    if (arrayInit != null) {
                        return arrayInit;
                    }
                    DetailAST literal = child.findFirstToken(TokenTypes.STRING_LITERAL);
                    if (literal != null) {
                        return literal;
                    }
                }
            } else if (child.getType() == TokenTypes.EXPR
                    || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT
                    || child.getType() == TokenTypes.ARRAY_INIT
                    || child.getType() == TokenTypes.STRING_LITERAL) {
                return child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private String extractAnnotationName(DetailAST annotationAst) {
        DetailAST dot = annotationAst.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            DetailAST ident = findRightmostIdent(dot);
            if (ident != null) {
                return ident.getText();
            }
        }
        DetailAST ident = annotationAst.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    private DetailAST findRightmostIdent(DetailAST dot) {
        DetailAST current = dot;
        while (current != null && current.getType() == TokenTypes.DOT) {
            current = current.getLastChild();
        }
        return current != null && current.getType() == TokenTypes.IDENT ? current : null;
    }


    String stripQuotes(String text) {
        requireNonNull(text);
        if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    static final class SuppressionScope {
        final Set<String> suppressedCodes = new HashSet<>();
        boolean suppressAll;

        SuppressionScope() {
        }

        SuppressionScope(SuppressionScope parent) {
            suppressedCodes.addAll(parent.suppressedCodes);
            suppressAll = parent.suppressAll;
        }

        void addToken(String token) {
            requireNonNull(token);
            String cleaned = cleanToken(token);
            if (cleaned.isEmpty()) {
                return;
            }
            if (ALL_TOKEN.equals(cleaned) || CHECK_ID.equals(cleaned) || CHECK_CLASS.equals(cleaned)) {
                suppressAll = true;
                return;
            }
            RuleId ruleId = RuleRegistry.forCode(cleaned);
            if (ruleId != null) {
                suppressedCodes.add(ruleId.code());
            }
        }

        String cleanToken(String token) {
            String trimmed = token.trim();
            if (trimmed.startsWith(CHECKSTYLE_PREFIX)) {
                return trimmed.substring(CHECKSTYLE_PREFIX.length());
            }
            return trimmed;
        }
    }

    void pushScopeForTesting(SuppressionScope scope) {
        scopes.push(scope);
    }
}
