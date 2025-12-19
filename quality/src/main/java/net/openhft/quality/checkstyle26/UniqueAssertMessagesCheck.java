/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.checkstyle26;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Checkstyle check that enforces unique and meaningful assert messages within each Java file.
 * <p>
 * This check flags:
 * <ul>
 *     <li>Duplicate assert messages within the same file</li>
 *     <li>Messages containing the class name (redundant - shown in stack trace)</li>
 *     <li>Messages containing the method/test name (redundant - shown in stack trace)</li>
 *     <li>Messages containing line numbers (redundant - shown in stack trace)</li>
 *     <li>Trivial Supplier&lt;String&gt; lambdas that just return a constant (defeats lazy evaluation)</li>
 * </ul>
 * <p>
 * Configure in checkstyle.xml:
 * <pre>
 * &lt;module name="net.openhft.quality.checkstyle26.UniqueAssertMessagesCheck"/&gt;
 * </pre>
 */
public class UniqueAssertMessagesCheck extends AbstractCheck {

    /**
     * Message key for duplicate assert messages.
     */
    public static final String MSG_DUPLICATE = "assert.message.duplicate";

    /**
     * Message key for redundant class name in message.
     */
    public static final String MSG_REDUNDANT_CLASS = "assert.message.redundant.class";

    /**
     * Message key for redundant method name in message.
     */
    public static final String MSG_REDUNDANT_METHOD = "assert.message.redundant.method";

    /**
     * Message key for redundant line number in message.
     */
    public static final String MSG_REDUNDANT_LINE = "assert.message.redundant.line";

    /**
     * Message key for trivial Supplier that just returns a constant string.
     */
    public static final String MSG_TRIVIAL_SUPPLIER = "assert.message.trivial.supplier";

    /**
     * Message key for generic single-word messages like "actual", "value".
     */
    public static final String MSG_GENERIC = "assert.message.generic";

    /**
     * Message key for messages that just restate the assertion type.
     */
    public static final String MSG_RESTATES_ASSERTION = "assert.message.restates.assertion";

    /**
     * Message key for index-only messages like "0", "[0]".
     */
    public static final String MSG_INDEX_ONLY = "assert.message.index.only";

    /**
     * Message key for contextless comparison messages.
     */
    public static final String MSG_CONTEXTLESS = "assert.message.contextless";

    /**
     * Message key for messages with too few words.
     */
    public static final String MSG_TOO_SHORT = "assert.message.too.short";

    /**
     * Message key for messages with too many words.
     */
    public static final String MSG_TOO_LONG = "assert.message.too.long";

    /**
     * Message key for messages containing overly long words (likely class/method names).
     */
    public static final String MSG_LONG_WORD = "assert.message.long.word";

    /**
     * Message key for messages that duplicate an assertion input (expected value, variable name).
     */
    public static final String MSG_DUPLICATES_INPUT = "assert.message.duplicates.input";

    /**
     * Message key for low-signal assertAll headings.
     */
    public static final String MSG_ASSERTALL_HEADING = "assert.message.assertall.heading";

    /**
     * Message key for messages that restate derived assertions (empty, present, contains).
     */
    public static final String MSG_RESTATES_DERIVED = "assert.message.restates.derived";

    // ========== Rule Codes (machine-parsable identifiers) ==========

    /** Rule code for duplicate messages. */
    public static final String CODE_DUPLICATE = "AMQ01";
    /** Rule code for redundant class name. */
    public static final String CODE_REDUNDANT_CLASS = "AMQ02";
    /** Rule code for redundant method name. */
    public static final String CODE_REDUNDANT_METHOD = "AMQ03";
    /** Rule code for redundant line number. */
    public static final String CODE_REDUNDANT_LINE = "AMQ04";
    /** Rule code for trivial supplier. */
    public static final String CODE_TRIVIAL_SUPPLIER = "AMQ05";
    /** Rule code for generic message. */
    public static final String CODE_GENERIC = "AMQ06";
    /** Rule code for restating assertion. */
    public static final String CODE_RESTATES_ASSERTION = "AMQ07";
    /** Rule code for index-only message. */
    public static final String CODE_INDEX_ONLY = "AMQ08";
    /** Rule code for contextless message. */
    public static final String CODE_CONTEXTLESS = "AMQ09";
    /** Rule code for too-short message. */
    public static final String CODE_TOO_SHORT = "AMQ10";
    /** Rule code for too-long message. */
    public static final String CODE_TOO_LONG = "AMQ11";
    /** Rule code for long word in message. */
    public static final String CODE_LONG_WORD = "AMQ12";
    /** Rule code for message duplicating assertion input. */
    public static final String CODE_DUPLICATES_INPUT = "AMQ13";
    /** Rule code for low-signal assertAll heading. */
    public static final String CODE_ASSERTALL_HEADING = "AMQ14";
    /** Rule code for restating derived assertion. */
    public static final String CODE_RESTATES_DERIVED = "AMQ15";

    /** Minimum number of words required in a message. */
    private static final int MIN_WORD_COUNT = 3;

    /** Maximum number of words allowed in a message. */
    private static final int MAX_WORD_COUNT = 20;

    /** Maximum length for individual words (longer suggests class/method name). */
    private static final int MAX_WORD_LENGTH = 16;

    /** Pattern to split message into words (alphanumeric sequences). */
    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-zA-Z0-9]+");

    /**
     * Pattern to detect line numbers in messages (e.g., "line 42", "File.java:42", "L42").
     * Refined to avoid false positives on:
     * - Class names like "Kernel32"
     * - Time formats like "01:46:40" or "23:59:59"
     * - Words like "Line1" or "Level2"
     */
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile(
            "(?i)(?:" +
            "\\bline\\s+#?\\d+|" +            // "line 42", "line #42" (requires space)
            "\\bL\\d+\\b|" +                  // "L42" shorthand (word boundary required)
            "\\.java:\\d+|" +                 // "File.java:42"
            "\\.kt:\\d+|" +                   // "File.kt:42"
            "\\.scala:\\d+" +                 // "File.scala:42"
            ")"
    );

    /**
     * Pattern to detect generic single-word messages.
     */
    private static final Pattern GENERIC_MESSAGE_PATTERN = Pattern.compile(
            "(?i)^(actual|expected|value|result|data|object|condition|test|check|message|msg|err|error|fail|ok|true|false)$"
    );

    /**
     * Pattern to detect messages that restate the assertion type.
     * Uses anchors and word boundaries to avoid false positives on longer messages.
     */
    private static final Pattern RESTATES_ASSERTION_PATTERN = Pattern.compile(
            "(?i)(" +
            "^assert(equals|true|false|null|notnull|same|notequals?|that)?$|" +
            "^should (be )?(equal|true|false|null|not null|same|the same)$|" +
            "^should not be null$|" +
            "^must (be )?(equal|true|false|null|not null|same|the same)$|" +
            "^must not be null$|" +
            "^(should|must|expected to) match$|" +
            "^values? (should|must) match$|" +
            "^equals?$|" +
            "^not null$|" +
            "^is null$|" +
            "^is true$|" +
            "^is false$" +
            ")"
    );

    /**
     * Pattern to detect index-only messages.
     */
    private static final Pattern INDEX_ONLY_PATTERN = Pattern.compile(
            "^(\\d+|\\[\\d+\\]|index\\s*\\d+|element\\s*\\d+|item\\s*\\d+|#\\d+|i=\\d+)$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Pattern to detect array/list literal values like "[a, b, c]" or "[1, 2, 3]".
     * Must contain a comma to distinguish from single-element indices like "[0]".
     */
    private static final Pattern ARRAY_VALUE_PATTERN = Pattern.compile(
            "^\\[[^\\[\\]]*,[^\\[\\]]*\\]$"
    );

    /**
     * Pattern to detect pure escape sequences or very short non-descriptive content.
     */
    private static final Pattern ESCAPE_ONLY_PATTERN = Pattern.compile(
            "^(\\\\+|\\\\n|\\\\t|\\\\r|\\s*)+$"
    );

    /**
     * Pattern to detect expected value strings that look like generated class names.
     * These are typically long camelCase strings used as expected values in tests.
     * E.g., "MethodWriterClassNameGeneratorTestJustAnInterfaceYamlMethodWriter"
     */
    private static final Pattern GENERATED_CLASS_NAME_PATTERN = Pattern.compile(
            "^[A-Z][a-zA-Z0-9]*([A-Z][a-z0-9]+){4,}[A-Za-z0-9]*$"
    );

    /**
     * Pattern to detect contextless comparison messages.
     */
    private static final Pattern CONTEXTLESS_PATTERN = Pattern.compile(
            "(?i)^(" +
            "comparison|check|validation|equality|verify|test|" +
            "values? (should |must )?(match|equal|be equal)|" +
            "(should|must) (be )?equal|" +
            "not equal|" +
            "mismatch|" +
            "failed|failure|error" +
            ")$"
    );

    /**
     * Pattern to detect messages that restate derived assertion conditions.
     * These are conditions that assertion frameworks already show in their output.
     */
    private static final Pattern RESTATES_DERIVED_PATTERN = Pattern.compile(
            "(?i)^(" +
            "(is )?empty|" +
            "not empty|" +
            "(is )?blank|" +
            "not blank|" +
            "(is )?present|" +
            "not present|" +
            "contains|" +
            "does not contain|" +
            "matches|" +
            "does not match|" +
            "(has |have )?size|" +
            "(is )?zero|" +
            "(is )?positive|" +
            "(is )?negative" +
            ")$"
    );

    /**
     * Pattern to detect low-signal assertAll headings.
     */
    private static final Pattern LOW_SIGNAL_HEADING_PATTERN = Pattern.compile(
            "(?i)^(" +
            "assert(all|ions?)?|" +
            "grouped? assertions?|" +
            "checks?|" +
            "validat(e|ions?)|" +
            "tests?" +
            ")$"
    );

    /**
     * Map of assert message text to first occurrence line number.
     */
    private Map<String, Integer> messageOccurrences;

    /**
     * Current class name being processed.
     */
    private String currentClassName;

    /**
     * Current method name being processed.
     */
    private String currentMethodName;

    /**
     * Whether to include detailed diagnostic breakdown in messages.
     * Default is false for concise CI output.
     */
    private boolean verbose = false;

    /**
     * Sets verbose mode for detailed diagnostics.
     *
     * @param verbose true for detailed output, false for concise
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[]{
                TokenTypes.CLASS_DEF,
                TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF,
                TokenTypes.METHOD_DEF,
                TokenTypes.LITERAL_ASSERT,
                TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        messageOccurrences = new HashMap<>();
        currentClassName = null;
        currentMethodName = null;
    }

    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
                currentClassName = extractName(ast);
                break;
            case TokenTypes.METHOD_DEF:
                currentMethodName = extractName(ast);
                break;
            case TokenTypes.LITERAL_ASSERT:
                checkJavaAssert(ast);
                break;
            case TokenTypes.METHOD_CALL:
                checkAssertionMethodCall(ast);
                break;
            default:
                break;
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF) {
            currentMethodName = null;
        }
    }

    private String extractName(DetailAST ast) {
        DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    /**
     * Check Java assert statements: {@code assert condition : "message";}
     */
    private void checkJavaAssert(DetailAST assertAst) {
        // The message is the second expression after the colon
        DetailAST expr = assertAst.findFirstToken(TokenTypes.EXPR);
        if (expr != null) {
            // Look for second EXPR (the message part after colon)
            DetailAST nextExpr = expr.getNextSibling();
            while (nextExpr != null) {
                if (nextExpr.getType() == TokenTypes.EXPR) {
                    String message = extractStringLiteral(nextExpr);
                    if (message != null) {
                        checkMessage(message, nextExpr.getLineNo());
                    }
                    break;
                }
                nextExpr = nextExpr.getNextSibling();
            }
        }
    }

    /**
     * Check assertion method calls (JUnit, AssertJ, etc.)
     */
    private void checkAssertionMethodCall(DetailAST methodCall) {
        DetailAST nameAst = methodCall.findFirstToken(TokenTypes.IDENT);
        if (nameAst == null) {
            // Could be a qualified call like Assert.assertEquals or Assertions.assertEquals
            DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
            if (dot != null) {
                // Get the rightmost IDENT in the DOT chain (the method name)
                nameAst = findRightmostIdent(dot);
            }
        }

        if (nameAst == null) {
            return;
        }

        String methodName = nameAst.getText();

        // Check common assertion methods
        if (isAssertionMethod(methodName)) {
            DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
            if (elist != null) {
                checkAssertionArguments(elist, methodName, methodCall.getLineNo());
            }
        }
    }

    private boolean isAssertionMethod(String methodName) {
        // Skip custom assertion utilities that take class/package names, not messages
        if (methodName.equals("assertClassesLoad")
                || methodName.equals("assertPackagePresent")
                || methodName.equals("assertResourcePresent")) {
            return false;
        }
        // JUnit 4/5 and common assertion methods
        return methodName.startsWith("assert")
                || methodName.startsWith("require")
                || methodName.equals("fail")
                || methodName.equals("as")  // AssertJ .as("message")
                || methodName.equals("describedAs")
                || methodName.equals("withFailMessage")
                || methodName.equals("overridingErrorMessage");
    }

    private void checkAssertionArguments(DetailAST elist, String methodName, int lineNo) {
        // JUnit 4: message is FIRST argument: assertEquals(message, expected, actual)
        // JUnit 5: message is LAST argument: assertEquals(expected, actual, message)
        // AssertJ: as("message"), describedAs("message") - only argument
        // assertAll: first string is heading (AMQ14), rest are lambdas

        // Special handling for assertAll (AMQ14)
        if (methodName.equals("assertAll")) {
            checkAssertAllHeading(elist, lineNo);
            return;
        }

        // Count arguments and collect string literals with their positions
        // Also collect potential input values for AMQ13 (message duplicates input)
        int argCount = 0;
        DetailAST firstStringExpr = null;
        DetailAST lastStringExpr = null;
        DetailAST lastLambda = null;
        int firstStringArgIndex = -1;
        int lastStringArgIndex = -1;
        int lastLambdaArgIndex = -1;
        // Map EXPR nodes to their input values (for AMQ13)
        java.util.Map<DetailAST, String> exprToInputValue = new java.util.IdentityHashMap<>();

        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.LAMBDA) {
                argCount++;

                if (child.getType() == TokenTypes.LAMBDA) {
                    lastLambda = child;
                    lastLambdaArgIndex = argCount;
                } else if (child.getType() == TokenTypes.EXPR) {
                    // Check if it contains a lambda
                    DetailAST innerLambda = child.findFirstToken(TokenTypes.LAMBDA);
                    if (innerLambda != null) {
                        lastLambda = innerLambda;
                        lastLambdaArgIndex = argCount;
                    } else {
                        String strLiteral = extractStringLiteral(child);
                        if (strLiteral != null) {
                            if (firstStringExpr == null) {
                                firstStringExpr = child;
                                firstStringArgIndex = argCount;
                            }
                            lastStringExpr = child;
                            lastStringArgIndex = argCount;
                        }
                        // Collect input value for AMQ13 check (associate with EXPR node)
                        String inputValue = extractInputValue(child);
                        if (inputValue != null) {
                            exprToInputValue.put(child, inputValue);
                        }
                    }
                }
            }
            child = child.getNextSibling();
        }

        // For AssertJ methods, any string argument is the message
        if (isAssertJMessageMethod(methodName)) {
            if (lastStringExpr != null) {
                String message = extractStringLiteral(lastStringExpr);
                if (message != null) {
                    checkMessage(message, lineNo);
                }
            }
            return;
        }

        // Check for lambda message supplier (JUnit 5 style - always last)
        // But don't skip checking other string args - assertAll("msg", () -> ...) has both
        if (lastLambda != null && lastLambdaArgIndex == argCount) {
            String trivialLambdaMessage = extractTrivialLambdaMessageDirect(lastLambda);
            if (trivialLambdaMessage == null) {
                trivialLambdaMessage = extractTrivialLambdaMessage(lastLambda.getParent());
            }
            if (trivialLambdaMessage != null) {
                log(lineNo, MSG_TRIVIAL_SUPPLIER, trivialLambdaMessage);
                checkMessage(trivialLambdaMessage, lineNo, true);
                return; // Only return early if we found a trivial supplier
            }
            // Lambda is not a message supplier - fall through to check string args
        }

        // Determine which string is the message based on position heuristics:
        // 1. If last arg is a string AND first arg is also a string: use last (JUnit 5 with string expected)
        // 2. If last arg is a string AND first is NOT a string: use last (JUnit 5 style)
        // 3. If first arg is a string AND last is NOT: use first (JUnit 4 style)
        // 4. If only one string, use it

        DetailAST messageExpr = null;
        if (firstStringExpr != null && lastStringExpr != null) {
            if (firstStringArgIndex == lastStringArgIndex) {
                // Only one string argument - use it
                messageExpr = firstStringExpr;
            } else if (lastStringArgIndex == argCount) {
                // Last arg is string - prefer JUnit 5 style (message last)
                messageExpr = lastStringExpr;
            } else if (firstStringArgIndex == 1) {
                // First arg is string but last is not - JUnit 4 style (message first)
                messageExpr = firstStringExpr;
            } else {
                // Default to last string
                messageExpr = lastStringExpr;
            }
        } else if (firstStringExpr != null) {
            messageExpr = firstStringExpr;
        }

        if (messageExpr != null) {
            String message = extractStringLiteral(messageExpr);
            if (message != null) {
                // Collect input values, excluding the message expression itself (AMQ13)
                java.util.List<String> inputValues = new java.util.ArrayList<>();
                for (java.util.Map.Entry<DetailAST, String> entry : exprToInputValue.entrySet()) {
                    if (entry.getKey() != messageExpr) {
                        inputValues.add(entry.getValue());
                    }
                }
                // Check if message duplicates any input value (AMQ13)
                boolean duplicatesInput = checkDuplicatesInput(message, inputValues, lineNo);
                // Check other message quality rules
                checkMessage(message, lineNo, duplicatesInput);
            }
        }
    }

    private boolean isAssertJMessageMethod(String methodName) {
        return methodName.equals("as")
                || methodName.equals("describedAs")
                || methodName.equals("withFailMessage")
                || methodName.equals("overridingErrorMessage");
    }

    /**
     * Extracts the string from a trivial lambda when the LAMBDA node is passed directly.
     */
    private String extractTrivialLambdaMessageDirect(DetailAST lambda) {
        // Check if it's a no-arg lambda: () -> ...
        DetailAST params = lambda.findFirstToken(TokenTypes.PARAMETERS);
        if (params != null && params.getChildCount() > 0) {
            // Has parameters, not a trivial Supplier
            return null;
        }

        // Find the lambda body - the EXPR child
        DetailAST body = lambda.findFirstToken(TokenTypes.EXPR);
        if (body != null) {
            body = body.getFirstChild();
        }
        if (body == null) {
            // Try the last child if EXPR is not found
            body = lambda.getLastChild();
            if (body != null && body.getType() == TokenTypes.EXPR) {
                body = body.getFirstChild();
            }
        }

        // Check if body is just a string literal
        if (body != null && body.getType() == TokenTypes.STRING_LITERAL) {
            String text = body.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
        }

        // Check if body is a string concatenation with only literals
        if (body != null && isConstantStringExpression(body)) {
            return extractConstantString(body);
        }

        return null;
    }

    /**
     * Extracts the string from a trivial lambda like {@code () -> "message"}.
     * Returns null if the expression is not a trivial lambda.
     */
    private String extractTrivialLambdaMessage(DetailAST expr) {
        DetailAST lambda = findLambda(expr);
        if (lambda == null) {
            return null;
        }

        // Check if it's a no-arg lambda: () -> ...
        DetailAST params = lambda.findFirstToken(TokenTypes.PARAMETERS);
        if (params != null && params.getChildCount() > 0) {
            // Has parameters, not a trivial Supplier
            return null;
        }

        // Find the lambda body - look for the expression after LAMBDA_BODY or directly
        DetailAST body = lambda.getLastChild();
        if (body == null) {
            return null;
        }

        // The body might be wrapped in EXPR
        if (body.getType() == TokenTypes.EXPR) {
            body = body.getFirstChild();
        }

        // Check if body is just a string literal
        if (body != null && body.getType() == TokenTypes.STRING_LITERAL) {
            String text = body.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
        }

        // Check if body is a string concatenation with only literals
        if (body != null && isConstantStringExpression(body)) {
            return extractConstantString(body);
        }

        return null;
    }

    private DetailAST findLambda(DetailAST ast) {
        if (ast == null) {
            return null;
        }
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
     * Checks if an expression is a constant string (literal or concatenation of literals).
     */
    private boolean isConstantStringExpression(DetailAST expr) {
        if (expr == null) {
            return false;
        }
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        // Check for string concatenation: "a" + "b"
        if (expr.getType() == TokenTypes.PLUS) {
            DetailAST left = expr.getFirstChild();
            DetailAST right = expr.getLastChild();
            return isConstantStringExpression(left) && isConstantStringExpression(right);
        }
        return false;
    }

    /**
     * Extracts the constant string value from a constant string expression.
     */
    private String extractConstantString(DetailAST expr) {
        if (expr == null) {
            return null;
        }
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

    private String extractStringLiteral(DetailAST expr) {
        if (expr == null) {
            return null;
        }
        // Get the actual expression content (unwrap EXPR wrapper if present)
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }
        // Don't extract strings from inside method calls - those aren't message arguments
        // e.g., Matchers.startsWith("he") contains "he" but it's not an assertion message
        if (content != null && content.getType() == TokenTypes.METHOD_CALL) {
            return null;
        }
        // First check if it's a constant string (literal or concatenation of literals)
        if (isConstantStringExpression(expr)) {
            return extractConstantString(expr);
        }
        // For expressions with a PLUS (concatenation), try to extract the constant part
        DetailAST plus = findPlus(expr);
        if (plus != null) {
            // Dynamic message - extract just the constant prefix/parts for analysis
            // This is imperfect but better than missing the message entirely
            String constantPart = extractConstantStringParts(plus);
            if (constantPart != null && !constantPart.isEmpty()) {
                return constantPart;
            }
        }
        // Fall back to finding the first string literal (only for direct children)
        DetailAST literal = findStringLiteral(expr);
        if (literal != null) {
            String text = literal.getText();
            // Remove surrounding quotes
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
        }
        return null;
    }

    private DetailAST findPlus(DetailAST ast) {
        if (ast == null) {
            return null;
        }
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
     * Extracts all constant string parts from a concatenation expression.
     * For "prefix " + variable + " suffix", returns "prefix  suffix".
     */
    private String extractConstantStringParts(DetailAST expr) {
        if (expr == null) {
            return null;
        }
        if (expr.getType() == TokenTypes.STRING_LITERAL) {
            String text = expr.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return "";
        }
        if (expr.getType() == TokenTypes.PLUS) {
            String left = extractConstantStringParts(expr.getFirstChild());
            String right = extractConstantStringParts(expr.getLastChild());
            if (left == null) {
                left = "";
            }
            if (right == null) {
                right = "";
            }
            return left + right;
        }
        // Non-string part (variable, method call, etc.) - return empty
        return "";
    }

    private DetailAST findStringLiteral(DetailAST ast) {
        if (ast == null) {
            return null;
        }
        if (ast.getType() == TokenTypes.STRING_LITERAL) {
            return ast;
        }
        // Don't descend into method calls - strings inside them are arguments, not messages
        if (ast.getType() == TokenTypes.METHOD_CALL) {
            return null;
        }
        // Search children
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

    private void checkMessage(String message, int lineNo) {
        checkMessage(message, lineNo, false);
    }

    private void checkMessage(String message, int lineNo, boolean externalWarningFired) {
        if (message == null || message.isEmpty()) {
            return;
        }

        // Skip JSON-like strings (not assertion messages, but JSON payloads)
        // Be careful not to skip index-only messages like "[0]" or "[5]"
        String trimmed = message.trim();
        if (trimmed.startsWith("{")
                || (trimmed.startsWith("[{") || trimmed.startsWith("[\"") || trimmed.startsWith("[["))) {
            return;
        }

        // Skip fully qualified class names (e.g., "com.example.MyClass")
        // These are likely class name arguments, not assertion messages
        if (looksLikeClassName(trimmed)) {
            return;
        }

        // Skip data-like content (YAML, multi-line data, expected values)
        if (looksLikeDataValue(trimmed)) {
            return;
        }

        // Skip array/list literal values like "[a, b, c]"
        if (ARRAY_VALUE_PATTERN.matcher(trimmed).matches()) {
            return;
        }

        // Skip pure escape sequences
        if (ESCAPE_ONLY_PATTERN.matcher(trimmed).matches()) {
            return;
        }

        // Skip strings that look like generated class names (expected values in tests)
        // E.g., "MethodWriterClassNameGeneratorTestJustAnInterfaceYamlMethodWriter"
        if (GENERATED_CLASS_NAME_PATTERN.matcher(trimmed).matches()) {
            return;
        }

        // Track if any warning fired (to avoid redundant "too short" warnings)
        boolean warningFired = externalWarningFired;

        // Check for duplicate messages
        Integer firstOccurrence = messageOccurrences.get(message);
        if (firstOccurrence != null) {
            log(lineNo, MSG_DUPLICATE, message, firstOccurrence);
            warningFired = true;
        } else {
            messageOccurrences.put(message, lineNo);
        }

        // Check if message lacks substance without class name (or its field-name variant)
        if (currentClassName != null) {
            String matchedVariant = findNameVariant(message, currentClassName);
            if (matchedVariant != null) {
                SubstanceAnalysis analysis = analyzeSubstance(message, matchedVariant);
                if (!analysis.hasSubstance) {
                    String diagnosisText = verbose
                            ? analysis.diagnosis + " " + analysis.getVerboseDetails()
                            : analysis.diagnosis;
                    log(lineNo, MSG_REDUNDANT_CLASS, message, currentClassName,
                            diagnosisText);
                    warningFired = true;
                }
            }
        }

        // Check if message lacks substance without method name (or its class-name variant)
        if (currentMethodName != null) {
            String matchedVariant = findNameVariant(message, currentMethodName);
            if (matchedVariant != null) {
                SubstanceAnalysis analysis = analyzeSubstance(message, matchedVariant);
                if (!analysis.hasSubstance) {
                    String diagnosisText = verbose
                            ? analysis.diagnosis + " " + analysis.getVerboseDetails()
                            : analysis.diagnosis;
                    log(lineNo, MSG_REDUNDANT_METHOD, message, currentMethodName,
                            diagnosisText);
                    warningFired = true;
                }
            }
        }

        // Check for line numbers
        String lineMatch = extractMatch(LINE_NUMBER_PATTERN, message);
        if (lineMatch != null) {
            log(lineNo, MSG_REDUNDANT_LINE, message, lineMatch);
            warningFired = true;
        }

        // Check for generic single-word messages
        if (GENERIC_MESSAGE_PATTERN.matcher(message).matches()) {
            log(lineNo, MSG_GENERIC, message);
            warningFired = true;
        }

        // Check for messages that restate the assertion type
        if (RESTATES_ASSERTION_PATTERN.matcher(message).find()) {
            log(lineNo, MSG_RESTATES_ASSERTION, message);
            warningFired = true;
        }

        // Check for index-only messages
        if (INDEX_ONLY_PATTERN.matcher(message).matches()) {
            log(lineNo, MSG_INDEX_ONLY, message);
            warningFired = true;
        }

        // Check for contextless comparison messages
        if (CONTEXTLESS_PATTERN.matcher(message).matches()) {
            log(lineNo, MSG_CONTEXTLESS, message);
            warningFired = true;
        }

        // Check for messages that restate derived assertion conditions (AMQ15)
        String derivedMatch = extractMatch(RESTATES_DERIVED_PATTERN, message);
        if (derivedMatch != null) {
            log(lineNo, MSG_RESTATES_DERIVED, message, derivedMatch);
            warningFired = true;
        }

        // Check word count and word lengths (skip word count if any warning already fired)
        checkMessageWordMetrics(message, lineNo, warningFired);
    }

    /**
     * Checks message word count (3-20) and individual word lengths (max 16 chars).
     *
     * @param message the message to check
     * @param lineNo the line number for reporting
     * @param skipWordCount if true, skip word count checks (a more specific warning already fired)
     */
    private void checkMessageWordMetrics(String message, int lineNo, boolean skipWordCount) {
        String[] words = WORD_SPLITTER.split(message);
        // Filter out empty strings from split
        int wordCount = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount++;
                // Check for long words (likely class/method names)
                // Skip if this word is the current class or method name (already flagged)
                if (word.length() > MAX_WORD_LENGTH
                        && !word.equalsIgnoreCase(currentClassName)
                        && !word.equalsIgnoreCase(currentMethodName)) {
                    log(lineNo, MSG_LONG_WORD, message, word, MAX_WORD_LENGTH);
                }
            }
        }

        // Skip word count checks if a semantic warning already fired
        if (skipWordCount) {
            return;
        }

        if (wordCount < MIN_WORD_COUNT) {
            log(lineNo, MSG_TOO_SHORT, message, wordCount, MIN_WORD_COUNT);
        } else if (wordCount > MAX_WORD_COUNT) {
            log(lineNo, MSG_TOO_LONG, message, wordCount, MAX_WORD_COUNT);
        }
    }

    /**
     * Minimum length for method/class names to consider for substance checks.
     * Short names like "next", "type", "value" are too common to flag.
     */
    private static final int MIN_NAME_LENGTH_FOR_CHECK = 5;

    /**
     * Minimum number of meaningful words required after removing the class/method name.
     * If the message has fewer than this many words without the name, it lacks substance.
     */
    private static final int MIN_WORDS_WITHOUT_NAME = 2;

    /**
     * Words that don't count as meaningful content (too generic).
     * Based on frequency analysis of assertion messages across Chronicle codebase.
     */
    private static final java.util.Set<String> FILLER_WORDS = new java.util.HashSet<>(
            java.util.Arrays.asList(
                    // Articles, prepositions, conjunctions (high frequency)
                    "a", "an", "the", "is", "are", "was", "were", "be", "been",
                    "to", "of", "in", "for", "on", "at", "by", "with", "from",
                    "as", "if", "or", "and", "not", "no", "when", "after", "before",
                    // Test-related generic words (very high frequency)
                    "should", "must", "expected", "actual", "value", "result",
                    "test", "check", "assert", "equals", "return", "returns",
                    "error", "fail", "failed", "failure", "exception",
                    "null", "true", "false", "non", "empty",
                    // Common structural words
                    "have", "has", "does", "contain", "contains", "exist", "exists",
                    "present", "set", "get", "first", "second", "one", "two",
                    "more", "only", "all", "without", "within",
                    // Low-value context words
                    "line", "method", "class", "assertion", "occurred", "here",
                    "call", "new", "instance", "created", "successfully"
            )
    );

    /**
     * Pattern to detect words that are just numbers.
     */
    private static final Pattern NUMERIC_WORD = Pattern.compile("^\\d+$");

    /**
     * Extracts the first match of a pattern from the text.
     *
     * @param pattern the pattern to search for
     * @param text the text to search in
     * @return the matched substring, or null if no match
     */
    private String extractMatch(Pattern pattern, String text) {
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Finds if the message contains the given name or its variant.
     * Checks for: exact name, field-name variant (lowercase first), all-lowercase, all-uppercase.
     *
     * @param text the message text
     * @param name the class/method name to look for
     * @return the variant that was found, or null if none found
     */
    private String findNameVariant(String text, String name) {
        // Skip very short names
        if (name.length() < MIN_NAME_LENGTH_FOR_CHECK) {
            return null;
        }

        // Check for exact name (case-sensitive word boundary match)
        if (containsNameExact(text, name)) {
            return name;
        }

        // Check for variant with swapped first letter case (BytesStore → bytesStore)
        String swappedCase = swapFirstLetterCase(name);
        if (containsNameExact(text, swappedCase)) {
            return swappedCase;
        }

        // Check for all-lowercase variant (InputAllWarningTypes → inputallwarningtypes)
        String lowerCase = name.toLowerCase();
        if (!lowerCase.equals(name) && !lowerCase.equals(swappedCase)
                && containsNameExact(text, lowerCase)) {
            return lowerCase;
        }

        // Check for all-uppercase variant (validateSomething → VALIDATESOMETHING)
        String upperCase = name.toUpperCase();
        if (!upperCase.equals(name) && containsNameExact(text, upperCase)) {
            return upperCase;
        }

        return null;
    }

    /**
     * Swaps the case of the first letter.
     * "BytesStore" → "bytesStore" (class to field)
     * "getValue" → "GetValue" (method to class-style)
     */
    private String swapFirstLetterCase(String name) {
        if (name.isEmpty()) {
            return name;
        }
        char first = name.charAt(0);
        if (Character.isUpperCase(first)) {
            return Character.toLowerCase(first) + name.substring(1);
        } else {
            return Character.toUpperCase(first) + name.substring(1);
        }
    }

    /**
     * Checks if the message contains the given name as a whole word (case-sensitive).
     * Uses word boundary checking to avoid false positives.
     *
     * @param text the message text
     * @param name the exact name to look for (case-sensitive)
     * @return true if the name is found as a whole word in the text
     */
    private boolean containsNameExact(String text, String name) {
        int index = text.indexOf(name);
        while (index >= 0) {
            // Check word boundaries - name must be a standalone word
            boolean startBoundary = (index == 0)
                    || !Character.isLetterOrDigit(text.charAt(index - 1));
            boolean endBoundary = (index + name.length() >= text.length())
                    || !Character.isLetterOrDigit(text.charAt(index + name.length()));

            if (startBoundary && endBoundary) {
                return true;
            }

            // Look for next occurrence
            index = text.indexOf(name, index + 1);
        }
        return false;
    }

    /**
     * Result of analyzing message substance.
     */
    private static class SubstanceAnalysis {
        final boolean hasSubstance;
        final int fillerCount;
        final int meaningfulCount;
        final String remainingWords;
        final String diagnosis;
        // For verbose mode
        final java.util.List<String> meaningfulWords;
        final java.util.List<String> fillerWords;
        final String removedName;

        SubstanceAnalysis(boolean hasSubstance, int fillerCount, int meaningfulCount,
                          String remainingWords, String diagnosis,
                          java.util.List<String> meaningfulWords,
                          java.util.List<String> fillerWords, String removedName) {
            this.hasSubstance = hasSubstance;
            this.fillerCount = fillerCount;
            this.meaningfulCount = meaningfulCount;
            this.remainingWords = remainingWords;
            this.diagnosis = diagnosis;
            this.meaningfulWords = meaningfulWords;
            this.fillerWords = fillerWords;
            this.removedName = removedName;
        }

        /**
         * Returns verbose details for debugging.
         */
        String getVerboseDetails() {
            return String.format("Details: meaningful=[%s] filler=[%s] removed=[%s]",
                    String.join(",", meaningfulWords),
                    String.join(",", fillerWords),
                    removedName);
        }
    }

    /**
     * Analyzes if the message has enough meaningful content without the class/method name.
     * Returns details about which words were treated as filler vs meaningful.
     *
     * @param message the full message text
     * @param name the class/method name to remove
     * @return analysis result with substance flag and word categorization
     */
    private SubstanceAnalysis analyzeSubstance(String message, String name) {
        // Remove the name (case insensitive) from the message
        String withoutName = message.replaceAll("(?i)" + Pattern.quote(name), " ");

        // Split into words and categorize them
        String[] words = WORD_SPLITTER.split(withoutName);
        java.util.List<String> filler = new java.util.ArrayList<>();
        java.util.List<String> meaningful = new java.util.ArrayList<>();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            String lowerWord = word.toLowerCase();

            // Check if filler word
            if (FILLER_WORDS.contains(lowerWord)) {
                filler.add(word);
                continue;
            }
            // Skip pure numbers (like line numbers)
            if (NUMERIC_WORD.matcher(word).matches()) {
                filler.add(word);
                continue;
            }
            // Skip very short words
            if (word.length() < 2) {
                filler.add(word);
                continue;
            }
            meaningful.add(word);
        }

        boolean hasSubstance = meaningful.size() >= MIN_WORDS_WITHOUT_NAME;

        // Build remaining words display
        java.util.List<String> allRemaining = new java.util.ArrayList<>();
        allRemaining.addAll(meaningful);
        allRemaining.addAll(filler);
        String remainingWords = allRemaining.isEmpty() ? "(empty)"
                : String.join(", ", allRemaining);

        // Generate diagnosis based on what we found
        String diagnosis;
        if (meaningful.isEmpty() && filler.isEmpty()) {
            diagnosis = "nothing remains after removing the name";
        } else if (meaningful.isEmpty()) {
            diagnosis = "only filler words remain: " + String.join(", ", filler);
        } else if (filler.isEmpty()) {
            diagnosis = "too short - only " + meaningful.size()
                    + " word(s): " + String.join(", ", meaningful);
        } else {
            diagnosis = "only " + meaningful.size() + " meaningful word(s): "
                    + String.join(", ", meaningful)
                    + " (filler: " + String.join(", ", filler) + ")";
        }

        return new SubstanceAnalysis(hasSubstance, filler.size(), meaningful.size(),
                remainingWords, diagnosis, meaningful, filler, name);
    }

    /**
     * Finds the rightmost IDENT in a DOT chain (the method name in qualified calls).
     * For example, in "org.junit.Assert.assertEquals", returns the IDENT for "assertEquals".
     */
    private DetailAST findRightmostIdent(DetailAST dot) {
        if (dot == null) {
            return null;
        }
        // The rightmost child of a DOT is either an IDENT (the method name)
        // or another DOT (for nested qualifications)
        DetailAST lastChild = dot.getLastChild();
        if (lastChild == null) {
            return null;
        }
        if (lastChild.getType() == TokenTypes.IDENT) {
            return lastChild;
        }
        // Shouldn't happen for method calls, but handle nested DOTs just in case
        if (lastChild.getType() == TokenTypes.DOT) {
            return findRightmostIdent(lastChild);
        }
        return null;
    }

    /**
     * Checks if a string looks like a data value rather than a descriptive message.
     * Data values include YAML, multi-line content, key-value pairs, etc.
     * These are typically expected/actual values, not assertion messages.
     */
    private boolean looksLikeDataValue(String text) {
        // Check for both literal newline and escaped newline (\n in source)
        boolean hasNewline = text.contains("\n") || text.contains("\\n");

        // Multi-line content with colons is likely YAML or data
        if (hasNewline && text.contains(":")) {
            return true;
        }
        // Multi-line content with equals signs is likely properties/config
        if (hasNewline && text.contains("=")) {
            return true;
        }
        // Content starting with YAML type indicator
        if (text.startsWith("!")) {
            return true;
        }
        // Content with multiple newlines (structured data)
        if (hasNewline) {
            // Count newlines - multiple suggests structured data, not a message
            int newlineCount = 0;
            for (int i = 0; i < text.length() - 1; i++) {
                if (text.charAt(i) == '\\' && text.charAt(i + 1) == 'n') {
                    newlineCount++;
                } else if (text.charAt(i) == '\n') {
                    newlineCount++;
                }
            }
            if (newlineCount >= 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a string looks like a fully qualified class name.
     * Used to skip class name arguments that aren't assertion messages.
     */
    private boolean looksLikeClassName(String text) {
        // Must contain at least one dot and no spaces
        if (!text.contains(".") || text.contains(" ")) {
            return false;
        }
        // Should match pattern like "com.example.ClassName" or "com.example.package"
        // All parts should be valid Java identifiers
        String[] parts = text.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        for (String part : parts) {
            if (part.isEmpty() || !Character.isJavaIdentifierStart(part.charAt(0))) {
                return false;
            }
            for (int i = 1; i < part.length(); i++) {
                if (!Character.isJavaIdentifierPart(part.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Extracts an input value from an assertion argument for AMQ13 checking.
     * Returns string literals, identifiers, and simple class references.
     *
     * @param expr the EXPR node containing the argument
     * @return the input value as a string, or null if not extractable
     */
    private String extractInputValue(DetailAST expr) {
        if (expr == null) {
            return null;
        }

        // Unwrap EXPR if needed
        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }

        // String literal - extract the value
        if (content.getType() == TokenTypes.STRING_LITERAL) {
            String text = content.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return null;
        }

        // Simple identifier - return the variable name
        if (content.getType() == TokenTypes.IDENT) {
            return content.getText();
        }

        // Class literal like IllegalArgumentException.class
        if (content.getType() == TokenTypes.DOT) {
            DetailAST lastChild = content.getLastChild();
            if (lastChild != null && "class".equals(lastChild.getText())) {
                DetailAST firstChild = content.getFirstChild();
                if (firstChild != null && firstChild.getType() == TokenTypes.IDENT) {
                    return firstChild.getText();
                }
            }
        }

        // Method call result - extract the method name
        if (content.getType() == TokenTypes.METHOD_CALL) {
            DetailAST ident = content.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                return ident.getText();
            }
        }

        return null;
    }

    /**
     * Checks assertAll heading quality (AMQ14).
     * Flags low-signal headings like "assertAll", "grouped assertions", etc.
     *
     * @param elist the ELIST node containing arguments
     * @param lineNo the line number for reporting
     */
    private void checkAssertAllHeading(DetailAST elist, int lineNo) {
        // Find the first string argument (the heading)
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                String heading = extractStringLiteral(child);
                if (heading != null) {
                    // Check for low-signal headings
                    if (LOW_SIGNAL_HEADING_PATTERN.matcher(heading.trim()).matches()) {
                        log(lineNo, MSG_ASSERTALL_HEADING, heading);
                    } else {
                        // Valid heading - check other quality rules but skip duplicate check
                        checkMessage(heading, lineNo);
                    }
                    return;
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * Checks if a message duplicates an assertion input value (AMQ13).
     *
     * @param message the assertion message
     * @param inputValues list of input values from the assertion
     * @param lineNo the line number for reporting
     * @return true if a warning was logged
     */
    private boolean checkDuplicatesInput(String message, java.util.List<String> inputValues,
                                          int lineNo) {
        if (message == null || inputValues.isEmpty()) {
            return false;
        }

        String messageLower = message.toLowerCase().trim();

        for (String input : inputValues) {
            if (input == null || input.isEmpty()) {
                continue;
            }

            String inputLower = input.toLowerCase().trim();

            // Check for exact match (case-insensitive)
            if (messageLower.equals(inputLower)) {
                log(lineNo, MSG_DUPLICATES_INPUT, message, input);
                return true;
            }

            // Check if message is just the input with common suffixes/prefixes
            // e.g., "expected" as message when expected is a variable name
            if (messageLower.equals(inputLower + " value")
                    || messageLower.equals(inputLower + " result")
                    || messageLower.equals("expected " + inputLower)
                    || messageLower.equals("actual " + inputLower)) {
                log(lineNo, MSG_DUPLICATES_INPUT, message, input);
                return true;
            }
        }

        return false;
    }
}
