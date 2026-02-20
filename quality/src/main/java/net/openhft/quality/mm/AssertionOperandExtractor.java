/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import static java.util.Objects.requireNonNull;

/**
 * Extracts operand information from comparison and string search expressions.
 */
public final class AssertionOperandExtractor {

    private final MessageAstSupport astSupport;

    /**
     * Create an operand extractor.
     *
     * @param astSupport AST support utilities.
     */
    public AssertionOperandExtractor(MessageAstSupport astSupport) {
        this.astSupport = requireNonNull(astSupport);
    }

    /**
     * Resolve operands for a boolean assertion based on argument order.
     *
     * @param elist expression list AST.
     * @param style assertion style (determines argument order).
     * @return operands, or {@code null} if not resolvable.
     */
    public BooleanAssertionOperands resolveBooleanAssertionOperands(DetailAST elist,
                                                                    AssertionStyle style) {
        DetailAST firstExpr = null;
        DetailAST secondExpr = null;
        int argCount = 0;

        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                argCount++;
                if (argCount == 1) {
                    firstExpr = child;
                } else if (argCount == 2) {
                    secondExpr = child;
                }
            }
            child = child.getNextSibling();
        }

        requireNonNull(firstExpr);
        requireNonNull(secondExpr);
        if (style == AssertionStyle.JUNIT4) {
            return new BooleanAssertionOperands(secondExpr, firstExpr);
        }
        return new BooleanAssertionOperands(firstExpr, secondExpr);
    }

    /**
     * Extract string search information from a condition expression.
     *
     * @param expr condition expression.
     * @return string search info, or {@code null} if not a string search.
     */
    public StringSearchInfo extractStringSearch(DetailAST expr) {
        requireNonNull(expr);

        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() > 0) {
            content = content.getFirstChild();
        }

        if (content.getType() != TokenTypes.METHOD_CALL) {
            return null;
        }

        DetailAST dot = content.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return null;
        }

        DetailAST methodIdent = astSupport.findRightmostIdent(dot);
        if (methodIdent == null) {
            return null;
        }

        String methodName = methodIdent.getText();

        if (!methodName.equals("contains")
                && !methodName.equals("startsWith")
                && !methodName.equals("endsWith")) {
            return null;
        }

        DetailAST objectExpr = dot.getFirstChild();
        if (objectExpr == null) {
            return null;
        }
        String stringVar = extractOperandName(objectExpr);
        if (stringVar == null) {
            return null;
        }

        DetailAST elist = content.findFirstToken(TokenTypes.ELIST);
        String searchArg = "pattern";
        if (elist != null) {
            DetailAST argExpr = elist.getFirstChild();
            if (argExpr != null && argExpr.getType() == TokenTypes.EXPR) {
                DetailAST argChild = argExpr.getFirstChild();
                if (argChild != null) {
                    String extracted = extractOperandName(argChild);
                    if (extracted != null) {
                        searchArg = extracted;
                    }
                }
            }
        }

        return new StringSearchInfo(methodName, stringVar, searchArg);
    }

    /**
     * Extract comparison information from a condition expression.
     *
     * @param expr condition expression.
     * @return comparison info, or {@code null} if not a comparison.
     */
    public ComparisonInfo extractComparison(DetailAST expr) {
        requireNonNull(expr);
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() > 0) {
            content = content.getFirstChild();
        }

        String operator;
        DetailAST left;
        DetailAST right;

        switch (content.getType()) {
            case TokenTypes.EQUAL:
                operator = "==";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            case TokenTypes.NOT_EQUAL:
                operator = "!=";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            case TokenTypes.LT:
                operator = "<";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            case TokenTypes.GT:
                operator = ">";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            case TokenTypes.LE:
                operator = "<=";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            case TokenTypes.GE:
                operator = ">=";
                left = content.getFirstChild();
                right = content.getLastChild();
                break;
            default:
                return null;
        }

        String leftOperand = extractOperandName(left);
        String rightOperand = extractOperandName(right);
        if (leftOperand == null || rightOperand == null) {
            return null;
        }

        return new ComparisonInfo(operator, leftOperand, rightOperand);
    }

    /**
     * Extract the name of an operand expression.
     *
     * @param operand operand expression.
     * @return operand name, or {@code null} if not extractable.
     */
    public String extractOperandName(DetailAST operand) {
        requireNonNull(operand);
        DetailAST content = astSupport.unwrapExpr(operand);
        requireNonNull(content);

        if (content.getType() == TokenTypes.IDENT) {
            return content.getText();
        }
        if (content.getType() == TokenTypes.NUM_INT) {
            return content.getText();
        }
        if (content.getType() == TokenTypes.STRING_LITERAL) {
            return content.getText();
        }
        if (content.getType() == TokenTypes.METHOD_CALL) {
            DetailAST ident = content.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                return ident.getText() + "()";
            }
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST ident = astSupport.findRightmostIdent(content);
            if (ident != null) {
                return ident.getText();
            }
            DetailAST rightmost = content;
            while (rightmost.getType() == TokenTypes.DOT) {
                rightmost = rightmost.getLastChild();
            }
            if (rightmost.getType() == TokenTypes.METHOD_CALL) {
                DetailAST methodIdent = rightmost.findFirstToken(TokenTypes.IDENT);
                if (methodIdent != null) {
                    return methodIdent.getText() + "()";
                }
            }
        }
        return null;
    }

    /**
     * Assertion style for JUnit 4 vs JUnit 5 argument ordering.
     */
    public enum AssertionStyle {
        /**
         * JUnit 4 style (message first, then condition).
         */
        JUNIT4,
        /**
         * JUnit 5 style (condition first, then message).
         */
        JUNIT5,
        /**
         * Unknown assertion style.
         */
        UNKNOWN
    }

    /**
     * Operands for a boolean assertion (condition and message).
     */
    public static final class BooleanAssertionOperands {
        private final DetailAST conditionExpr;
        private final DetailAST messageExpr;

        BooleanAssertionOperands(DetailAST conditionExpr, DetailAST messageExpr) {
            this.conditionExpr = conditionExpr;
            this.messageExpr = messageExpr;
        }

        /**
         * Return the condition expression AST.
         *
         * @return condition expression.
         */
        public DetailAST conditionExpr() {
            return conditionExpr;
        }

        /**
         * Return the message expression AST.
         *
         * @return message expression.
         */
        public DetailAST messageExpr() {
            return messageExpr;
        }
    }

    /**
     * Information about a string search method call.
     */
    public static final class StringSearchInfo {
        private final String methodName;
        private final String stringVar;
        private final String searchArg;

        StringSearchInfo(String methodName, String stringVar, String searchArg) {
            this.methodName = methodName;
            this.stringVar = stringVar;
            this.searchArg = searchArg;
        }

        /**
         * Return the method name (contains, startsWith, endsWith).
         *
         * @return method name.
         */
        public String methodName() {
            return methodName;
        }

        /**
         * Return the string variable being searched.
         *
         * @return string variable name.
         */
        public String stringVar() {
            return stringVar;
        }

        /**
         * Return the search argument.
         *
         * @return search argument.
         */
        public String searchArg() {
            return searchArg;
        }
    }

    /**
     * Information about a comparison expression.
     */
    public static final class ComparisonInfo {
        private final String operator;
        private final String leftOperand;
        private final String rightOperand;

        ComparisonInfo(String operator, String leftOperand, String rightOperand) {
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        /**
         * Return the comparison operator.
         *
         * @return operator string (==, !=, &lt;, &gt;, &lt;=, &gt;=).
         */
        public String operator() {
            return operator;
        }

        /**
         * Return the left operand name.
         *
         * @return left operand.
         */
        public String leftOperand() {
            return leftOperand;
        }

        /**
         * Return the right operand name.
         *
         * @return right operand.
         */
        public String rightOperand() {
            return rightOperand;
        }
    }
}
