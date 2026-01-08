/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Helper methods for traversing Checkstyle AST nodes during message extraction.
 */
public final class MessageAstSupport {
    /**
     * Create a helper for AST extraction routines.
     */
    public MessageAstSupport() {
    }

    /**
     * Extract the identifier text from a node that contains an {@code IDENT}.
     *
     * @param ast node to inspect.
     * @return identifier text, or {@code null} if none is present.
     */
    public String extractName(DetailAST ast) {
        requireNonNull(ast);
        DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    /**
     * Extract the fully qualified import text, including wildcard imports.
     *
     * @param importAst import AST node.
     * @return import text, or {@code null} if not available.
     */
    public String extractImportText(DetailAST importAst) {
        DetailAST dot = importAst.findFirstToken(TokenTypes.DOT);
        return flattenDot(dot);
    }

    /**
     * Extract a simple or qualified type name.
     *
     * @param typeAst type AST node.
     * @return type name, or {@code null} if not available.
     */
    public String extractTypeName(DetailAST typeAst) {
        requireNonNull(typeAst);
        DetailAST dot = typeAst.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return flattenDot(dot);
        }
        DetailAST ident = typeAst.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    /**
     * Flatten a {@code DOT} subtree into a dot-separated name.
     *
     * @param dotAst node to flatten.
     * @return flattened name, or {@code null} if any part is missing.
     */
    public String flattenDot(DetailAST dotAst) {
        if (dotAst.getType() != TokenTypes.DOT) {
            return dotAst.getText();
        }
        return flattenDot(dotAst.getFirstChild()) + "." + flattenDot(dotAst.getLastChild());
    }

    /**
     * Locate the rightmost {@code IDENT} within a dot chain.
     *
     * @param dot dot subtree to inspect.
     * @return rightmost identifier, or {@code null} if none is present.
     */
    public DetailAST findRightmostIdent(DetailAST dot) {
        // Traverse down the right side until we hit an IDENT
        DetailAST current = dot;
        while (current.getType() == TokenTypes.DOT) {
            current = current.getLastChild();
        }
        return current.getType() == TokenTypes.IDENT ? current : null;
    }

    /**
     * Extract a method name from a {@code METHOD_CALL} node.
     *
     * @param methodCall method call AST node.
     * @return method name, or {@code null} if not available.
     */
    public String extractMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return findRightmostIdent(dot).getText();
        }
        return methodCall.findFirstToken(TokenTypes.IDENT).getText();
    }

    /**
     * Extract the qualifier identifier from the left-hand side of a dot chain.
     *
     * @param dot dot subtree to inspect.
     * @return qualifier identifier text.
     */
    public String extractQualifierIdent(DetailAST dot) {
        DetailAST owner = dot.getFirstChild();
        while (owner.getType() == TokenTypes.DOT) {
            owner = owner.getLastChild();
        }
        return owner.getText();
    }

    /**
     * Collect expression and lambda arguments from an {@code ELIST}.
     *
     * @param elist expression list node.
     * @return ordered list of argument nodes.
     */
    public List<DetailAST> collectArguments(DetailAST elist) {
        List<DetailAST> args = new ArrayList<>();
        requireNonNull(elist);
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.LAMBDA) {
                args.add(child);
            }
            child = child.getNextSibling();
        }
        return args;
    }

    /**
     * Unwrap a single-child {@code EXPR} node.
     *
     * @param expr expression node.
     * @return unwrapped child or the original node.
     */
    public DetailAST unwrapExpr(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.EXPR && expr.getChildCount() == 1) {
            return expr.getFirstChild();
        }
        return expr;
    }

    /**
     * Check whether an expression resolves to a {@code null} literal.
     *
     * @param expr expression node.
     * @return {@code true} if the expression is {@code null} or casted {@code null}.
     */
    public boolean isNullLiteral(DetailAST expr) {
        DetailAST content = unwrapExpr(expr);
        requireNonNull(content);
        if (content.getType() == TokenTypes.LITERAL_NULL) {
            return true;
        }
        if (content.getType() == TokenTypes.TYPECAST) {
            return isNullLiteral(content.getLastChild());
        }
        return false;
    }

    /**
     * Find the first lambda expression within a subtree.
     *
     * @param ast subtree to inspect.
     * @return the lambda node, or {@code null} if none is found.
     */
    public DetailAST findLambda(DetailAST ast) {
        requireNonNull(ast);
        if (ast.getType() == TokenTypes.LAMBDA) {
            return ast;
        }
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            DetailAST found = findLambda(child);
            if (found != null) {
                return found;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Check whether an expression contains a string literal, excluding method calls.
     *
     * @param expr expression node.
     * @return {@code true} if a string literal is found.
     */
    public boolean containsStringLiteral(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (expr.getType() == TokenTypes.METHOD_CALL) {
            return false;
        }
        DetailAST child = expr.getFirstChild();
        while (child != null) {
            if (containsStringLiteral(child)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Check whether an expression contains a string literal anywhere in the subtree.
     *
     * @param expr expression node.
     * @return {@code true} if a string literal is found.
     */
    public boolean containsStringLiteralDeep(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        DetailAST child = expr.getFirstChild();
        while (child != null) {
            if (containsStringLiteralDeep(child)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Locate the first {@code PLUS} operator in the subtree, skipping method calls.
     *
     * @param ast subtree to inspect.
     * @return plus node, or {@code null} if not found.
     */
    public DetailAST findPlus(DetailAST ast) {
        requireNonNull(ast);
        if (ast.getType() == TokenTypes.PLUS) {
            return ast;
        }
        // Don't descend into method calls - concatenations inside them are not message parts
        if (ast.getType() == TokenTypes.METHOD_CALL) {
            return null;
        }
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            DetailAST found = findPlus(child);
            if (found != null) {
                return found;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Find the first argument expression that resolves to a string literal.
     *
     * @param elist             expression list node.
     * @param templateExtractor template extractor to identify string arguments.
     * @return matching expression node, or {@code null} if none is found.
     */
    public DetailAST findFirstStringArgument(DetailAST elist, MessageTemplateExtractor templateExtractor) {
        requireNonNull(elist);
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                DetailAST match = findStringArgumentExpression(child, templateExtractor);
                if (match != null) {
                    return match;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Resolve whether an expression should be treated as a string argument.
     *
     * @param expr              expression node.
     * @param templateExtractor template extractor to identify string literals.
     * @return the expression to treat as a string argument, or {@code null}.
     */
    public DetailAST findStringArgumentExpression(DetailAST expr, MessageTemplateExtractor templateExtractor) {
        requireNonNull(expr);
        requireNonNull(templateExtractor);
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }
        if (content != null && content.getType() == TokenTypes.LITERAL_NEW) {
            DetailAST innerElist = content.findFirstToken(TokenTypes.ELIST);
            DetailAST innerMatch = findFirstStringArgument(innerElist, templateExtractor);
            if (innerMatch != null) {
                return innerMatch;
            }
        }
        return templateExtractor.extractStringLiteral(expr) != null ? expr : null;
    }

    /**
     * Extract a class name from a {@code new} expression.
     *
     * @param literalNew {@code LITERAL_NEW} node.
     * @return simple or qualified class name, or {@code null} if not found.
     */
    public String extractNewClassName(DetailAST literalNew) {
        requireNonNull(literalNew);
        DetailAST child = literalNew.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.IDENT) {
                return child.getText();
            }
            if (child.getType() == TokenTypes.DOT) {
                DetailAST ident = findRightmostIdent(child);
                return ident != null ? ident.getText() : null;
            }
            if (child.getType() == TokenTypes.LPAREN || child.getType() == TokenTypes.ELIST) {
                break;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Extract the class name from a class literal expression (for example, {@code Foo.class}).
     *
     * @param expr expression node.
     * @return class literal name, or {@code null} if not found.
     */
    public String extractClassLiteralName(DetailAST expr) {
        requireNonNull(expr);
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST lastChild = content.getLastChild();
            if (lastChild != null && "class".equals(lastChild.getText())) {
                DetailAST left = content.getFirstChild();
                requireNonNull(left);
                DetailAST ident = left.getType() == TokenTypes.IDENT
                        ? left
                        : findRightmostIdent(left);
                return ident != null ? ident.getText() : null;
            }
        }
        return null;
    }

    /**
     * Extract the identifier assigned by a simple or qualified expression.
     *
     * @param expr expression node.
     * @return identifier text, or {@code null} if not available.
     */
    public String extractAssignedIdent(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.IDENT) {
            return expr.getText();
        }
        if (expr.getType() == TokenTypes.DOT) {
            DetailAST ident = findRightmostIdent(expr);
            return ident != null ? ident.getText() : null;
        }
        return null;
    }
}
