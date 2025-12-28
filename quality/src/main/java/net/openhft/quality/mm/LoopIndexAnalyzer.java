/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Analyzes loop index usage in assertion messages.
 */
public final class LoopIndexAnalyzer {

    /**
     * Information about loop index variables and whether they are present in message.
     */
    public static final class LoopIndexInfo {
        private final List<String> loopNames;
        private final boolean missing;

        LoopIndexInfo(List<String> loopNames, boolean missing) {
            this.loopNames = loopNames;
            this.missing = missing;
        }

        /**
         * Return the loop variable names.
         *
         * @return loop variable names.
         */
        public List<String> loopNames() {
            return loopNames;
        }

        /**
         * Return whether loop index is missing from message.
         *
         * @return {@code true} if loop index is missing.
         */
        public boolean isMissing() {
            return missing;
        }
    }

    private final MessageAstSupport astSupport;

    /**
     * Create a loop index analyzer.
     *
     * @param astSupport AST support utilities.
     */
    public LoopIndexAnalyzer(MessageAstSupport astSupport) {
        this.astSupport = Objects.requireNonNull(astSupport);
    }

    /**
     * Find loop index information for an assertion within a loop.
     *
     * @param methodCall method call AST.
     * @param messageExpr message expression AST.
     * @param message message text.
     * @return loop index info, or {@code null} if not in a loop.
     */
    public LoopIndexInfo findLoopIndexInfo(DetailAST methodCall, DetailAST messageExpr, String message) {
        Objects.requireNonNull(methodCall);
        Objects.requireNonNull(messageExpr);
        Objects.requireNonNull(message);
        String methodName = astSupport.extractMethodName(methodCall);
        if (!AssertionMethodClassifier.isLoopIndexAssertionMethod(methodName)) {
            return null;
        }
        LinkedHashSet<String> loopNames = collectLoopIndexNames(methodCall);
        if (loopNames.isEmpty()) {
            return null;
        }
        boolean hasIndex = messageContainsLoopIndex(message, loopNames)
                || expressionContainsLoopIndex(messageExpr, loopNames);
        return new LoopIndexInfo(new ArrayList<>(loopNames), !hasIndex);
    }

    private LinkedHashSet<String> collectLoopIndexNames(DetailAST node) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        DetailAST current = node;
        while (current != null) {
            if (current.getType() == TokenTypes.LITERAL_FOR) {
                collectLoopIndexNamesFromFor(current, names);
            }
            current = current.getParent();
        }
        return names;
    }

    private void collectLoopIndexNamesFromFor(DetailAST forAst, Set<String> names) {
        Objects.requireNonNull(forAst);
        DetailAST forEachClause = forAst.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
        if (forEachClause != null) {
            DetailAST varDef = forEachClause.findFirstToken(TokenTypes.VARIABLE_DEF);
            if (varDef != null) {
                DetailAST ident = varDef.findFirstToken(TokenTypes.IDENT);
                if (ident != null) {
                    names.add(ident.getText());
                }
            }
            return;
        }
        DetailAST forInit = forAst.findFirstToken(TokenTypes.FOR_INIT);
        collectLoopIndexNamesFromForInit(forInit, names);
    }

    private void collectLoopIndexNamesFromForInit(DetailAST node, Set<String> names) {
        if (node == null) {
            return;
        }
        if (node.getType() == TokenTypes.VARIABLE_DEF) {
            DetailAST ident = node.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                names.add(ident.getText());
            }
            return;
        }
        if (node.getType() == TokenTypes.ASSIGN) {
            DetailAST left = node.getFirstChild();
            String name = astSupport.extractAssignedIdent(left);
            if (name != null) {
                names.add(name);
            }
            return;
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            collectLoopIndexNamesFromForInit(child, names);
            child = child.getNextSibling();
        }
    }

    private boolean messageContainsLoopIndex(String message, Set<String> loopNames) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(loopNames);
        for (String name : loopNames) {
            Objects.requireNonNull(name);
            if (name.isEmpty()) {
                continue;
            }
            String quoted = Pattern.quote(name);
            if (Pattern.compile("\\b" + quoted + "\\b").matcher(message).find()) {
                return true;
            }
            if (Pattern.compile("\\b" + quoted + "\\s*[:=]").matcher(message).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean expressionContainsLoopIndex(DetailAST expr, Set<String> loopNames) {
        Objects.requireNonNull(expr);
        Objects.requireNonNull(loopNames);
        DetailAST content = astSupport.unwrapExpr(expr);
        return containsLoopIndexInAst(content, loopNames);
    }

    private boolean containsLoopIndexInAst(DetailAST ast, Set<String> loopNames) {
        Objects.requireNonNull(ast);
        if (ast.getType() == TokenTypes.IDENT && loopNames.contains(ast.getText())) {
            return true;
        }
        if (ast.getType() == TokenTypes.METHOD_CALL) {
            DetailAST target = ast.getFirstChild();
            if (target != null && target.getType() == TokenTypes.DOT) {
                DetailAST qualifier = target.getFirstChild();
                if (containsLoopIndexInAst(qualifier, loopNames)) {
                    return true;
                }
            }
            DetailAST elist = ast.findFirstToken(TokenTypes.ELIST);
            return containsLoopIndexInAst(elist, loopNames);
        }
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            if (containsLoopIndexInAst(child, loopNames)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }
}
