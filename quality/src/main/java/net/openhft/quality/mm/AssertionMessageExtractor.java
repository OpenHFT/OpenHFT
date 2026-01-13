/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Extracts message candidates from assertion and precondition calls.
 */
public final class AssertionMessageExtractor extends AbstractMessageExtractor {
    private static final String JUNIT4_ASSERTIONS = "org.junit.Assert";
    private static final String JUNIT5_ASSERTIONS = "org.junit.jupiter.api.Assertions";
    private static final String JUNIT4_ASSUME = "org.junit.Assume";
    private static final String JUNIT5_ASSUMPTIONS = "org.junit.jupiter.api.Assumptions";

    private final LoopIndexAnalyzer loopAnalyzer;
    private final LambdaMessageExtractor lambdaExtractor;
    private final ExpressionTypeAnalyzer typeAnalyzer;
    private final AssertionOperandExtractor operandExtractor;

    /**
     * Create an extractor for assertions and preconditions.
     *
     * @param context extraction context with imports and type information.
     * @param sink    sink to receive message candidates.
     */
    public AssertionMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
        this.loopAnalyzer = new LoopIndexAnalyzer(astSupport());
        this.lambdaExtractor = new LambdaMessageExtractor(context);
        this.typeAnalyzer = new ExpressionTypeAnalyzer(context);
        this.operandExtractor = new AssertionOperandExtractor(astSupport());
    }

    /**
     * Process a Java {@code assert} statement and extract its message expression.
     *
     * @param assertAst AST node for the {@code assert} statement.
     */
    public void handleJavaAssert(DetailAST assertAst) {
        DetailAST expr = requireNonNull(assertAst.findFirstToken(TokenTypes.EXPR));
        DetailAST nextExpr = expr.getNextSibling();
        while (nextExpr != null) {
            if (nextExpr.getType() == TokenTypes.EXPR) {
                MessageTemplate template = extractMessageTemplate(nextExpr);
                if (template != null) {
                    int keyValueLabelCount = countKeyValueLabels(template.message());
                    emitMessageCandidate(template.message(), nextExpr.getLineNo(),
                            template.placeholderCount(), keyValueLabelCount);
                } else {
                    DetailAST content = astSupport().unwrapExpr(nextExpr);
                    if (content != null && isNonLiteralMessageExpression(content)) {
                        return;
                    }
                    emitUnhandled(nextExpr, "Java assert message expression not recognised");
                }
                break;
            }
            nextExpr = nextExpr.getNextSibling();
        }
    }

    /**
     * Process assertion/precondition method calls and emit message candidates.
     *
     * @param methodCall AST node for the method call.
     */
    public void handleMethodCall(DetailAST methodCall) {
        String methodName = requireNonNull(astSupport().extractMethodName(methodCall));

        if (AssertionMethodClassifier.isAssertionMethod(methodName)) {
            DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
            if (elist != null) {
                boolean localAssertionHelper = isLocalAssertionHelper(methodCall, methodName);
                MessageSource source = AssertionMethodClassifier.isPreconditionMethod(methodName)
                        ? MessageSource.PRECONDITION
                        : MessageSource.ASSERTION;
                AssertionOperandExtractor.AssertionStyle style = source == MessageSource.ASSERTION
                        ? resolveAssertionStyle(methodCall, methodName)
                        : AssertionOperandExtractor.AssertionStyle.UNKNOWN;
                if (localAssertionHelper) {
                    style = AssertionOperandExtractor.AssertionStyle.UNKNOWN;
                }
                checkAssertionArguments(methodCall, elist, methodName, methodCall.getLineNo(),
                        source, style, localAssertionHelper);
            } else {
                emitUnhandled(methodCall, "Assertion call without argument list: " + methodName);
            }
        }
    }

    private AssertionOperandExtractor.AssertionStyle resolveAssertionStyle(DetailAST methodCall, String methodName) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            String fullCall = astSupport().flattenDot(dot);
            if (fullCall != null) {
                if (fullCall.startsWith(JUNIT4_ASSERTIONS + ".")
                        || fullCall.startsWith(JUNIT4_ASSUME + ".")) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT4;
                }
                if (fullCall.startsWith(JUNIT5_ASSERTIONS + ".")
                        || fullCall.startsWith(JUNIT5_ASSUMPTIONS + ".")) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT5;
                }
            }
            String qualifier = astSupport().extractQualifierIdent(dot);
            if (qualifier != null) {
                String importName = context().importedClass(qualifier);
                if (JUNIT4_ASSERTIONS.equals(importName) || JUNIT4_ASSUME.equals(importName)) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT4;
                }
                if (JUNIT5_ASSERTIONS.equals(importName) || JUNIT5_ASSUMPTIONS.equals(importName)) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT5;
                }
                return AssertionOperandExtractor.AssertionStyle.UNKNOWN;
            }
        }
        boolean junit4 = context().isStaticJUnit4Method(methodName);
        boolean junit5 = context().isStaticJUnit5Method(methodName);
        if (junit4 && !junit5) {
            return AssertionOperandExtractor.AssertionStyle.JUNIT4;
        }
        if (junit5 && !junit4) {
            return AssertionOperandExtractor.AssertionStyle.JUNIT5;
        }
        return AssertionOperandExtractor.AssertionStyle.UNKNOWN;
    }

    private void checkAssertionArguments(DetailAST methodCall, DetailAST elist, String methodName, int lineNo,
                                         MessageSource source, AssertionOperandExtractor.AssertionStyle style,
                                         boolean localAssertionHelper) {
        if (source == MessageSource.ASSERTION && AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            String exceptionClassName = extractExceptionClassLiteral(elist);
            if (context().isIgnoredExceptionClass(exceptionClassName)) {
                return;
            }
        }

        if (source == MessageSource.ASSERTION
                && style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
            context().recordJUnit4AssertionUsage(lineNo);
        }

        int argCount = 0;
        int stringArgCount = 0;
        List<DetailAST> args = new ArrayList<>();
        DetailAST firstStringExpr = null;
        DetailAST lastStringExpr = null;
        DetailAST lastLambda = null;
        DetailAST lastSupplierExpr = null;
        int firstStringArgIndex = -1;
        int lastStringArgIndex = -1;
        int lastLambdaArgIndex = -1;
        int lastSupplierArgIndex = -1;
        Map<DetailAST, String> exprToInputValue = new IdentityHashMap<>();
        Map<DetailAST, MessageTemplate> messageTemplates = new IdentityHashMap<>();

        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.LAMBDA) {
                argCount++;
                args.add(child);

                if (child.getType() == TokenTypes.LAMBDA) {
                    lastLambda = child;
                    lastLambdaArgIndex = argCount;
                } else if (child.getType() == TokenTypes.EXPR) {
                    DetailAST innerLambda = child.findFirstToken(TokenTypes.LAMBDA);
                    if (innerLambda != null) {
                        lastLambda = innerLambda;
                        lastLambdaArgIndex = argCount;
                    } else {
                        DetailAST content = astSupport().unwrapExpr(child);
                        if (content != null && isSupplierExpression(content)) {
                            lastSupplierExpr = child;
                            lastSupplierArgIndex = argCount;
                        }
                        MessageTemplate template = extractMessageTemplate(child);
                        boolean stringCandidate = template != null;
                        if (!stringCandidate && content != null && typeAnalyzer.isStringTypedExpression(content)) {
                            stringCandidate = true;
                        }
                        if (stringCandidate) {
                            stringArgCount++;
                            if (template != null) {
                                messageTemplates.put(child, template);
                            }
                            if (firstStringExpr == null) {
                                firstStringExpr = child;
                                firstStringArgIndex = argCount;
                            }
                            lastStringExpr = child;
                            lastStringArgIndex = argCount;
                        }
                        String inputValue = extractInputValue(child);
                        if (inputValue != null) {
                            exprToInputValue.put(child, inputValue);
                        }
                    }
                }
            }
            child = child.getNextSibling();
        }

        if (source == MessageSource.ASSERTION
                && AssertionMethodClassifier.isAssertThatMethod(methodName)
                && argCount == 2) {
            return;
        }

        if (source == MessageSource.ASSERTION && style != AssertionOperandExtractor.AssertionStyle.UNKNOWN) {
            if (isMissingAssertionMessage(methodName, style, args)) {
                if (!context().hasInlineReasonComment(methodCall)) {
                    sink().emitMissingMessage(lineNo, source);
                }
                return;
            }
        }

        if (source == MessageSource.ASSERTION && methodName.equals("assertAll")) {
            checkAssertAllHeading(methodCall, elist, lineNo);
            return;
        }

        if (source == MessageSource.ASSERTION && AssertionMethodClassifier.isAssertJMessageMethod(methodName)) {
            if (firstStringExpr != null && firstStringArgIndex == 1) {
                MessageTemplate template = extractMessageTemplate(firstStringExpr);
                if (template != null) {
                    int keyValueLabelCount = countKeyValueLabels(template.message());
                    MessageCandidate candidate = new MessageCandidate.Builder()
                            .source(source)
                            .lineNo(lineNo)
                            .message(template.message())
                            .normalisedMessage(MessageNormaliser.normalise(template.message()))
                            .placeholderCount(template.placeholderCount())
                            .keyValueLabelCount(keyValueLabelCount)
                            .assertJOverride(AssertionMethodClassifier.isAssertJOverrideMethod(methodName))
                            .build();
                    sink().emitCandidate(candidate);
                }
            }
            return;
        }

        if (source == MessageSource.ASSERTION
                && style == AssertionOperandExtractor.AssertionStyle.UNKNOWN
                && stringArgCount > 1
                && argCount >= 3
                && AssertionMethodClassifier.isRecognisedAssertionMethod(methodName)) {
            return;
        }

        String cheapSupplierDesc = null;
        boolean lambdaIsMessageArgument = isLambdaMessageArgument(methodName, style, argCount,
                firstStringArgIndex, lastLambdaArgIndex)
                && (!localAssertionHelper || AssertionMethodClassifier.isPreconditionMethod(methodName));
        if (lastLambda != null && lambdaIsMessageArgument) {
            String trivialLambdaMessage = lambdaExtractor.extractTrivialLambdaMessageDirect(lastLambda);
            if (trivialLambdaMessage == null) {
                trivialLambdaMessage = lambdaExtractor.extractTrivialLambdaMessage(lastLambda.getParent());
            }
            if (trivialLambdaMessage != null) {
                MessageCandidate candidate = new MessageCandidate.Builder()
                        .source(source)
                        .lineNo(lineNo)
                        .message(trivialLambdaMessage)
                        .normalisedMessage(MessageNormaliser.normalise(trivialLambdaMessage))
                        .placeholderCount(0)
                        .keyValueLabelCount(0)
                        .trivialSupplierDescription(trivialLambdaMessage)
                        .build();
                sink().emitCandidate(candidate);
                return;
            }
            cheapSupplierDesc = lambdaExtractor.extractCheapSupplierDescription(lastLambda);
            if (cheapSupplierDesc == null) {
                emitUnhandled(lastLambda, "Lambda message argument not recognised for " + methodName);
            }
        }

        DetailAST messageExpr = selectMessageExpression(methodName, argCount, args,
                firstStringExpr, firstStringArgIndex, lastStringExpr, lastStringArgIndex,
                lastSupplierExpr, lastSupplierArgIndex, source, style);

        if (messageExpr != null) {
            MessageTemplate template = messageTemplates.get(messageExpr);
            if (template == null) {
                template = extractMessageTemplate(messageExpr);
            }
            if (template != null) {
                boolean argumentNameMessage = isRequireNonNullArgumentMessage(methodName, source, args, template);
                List<String> inputValues = new ArrayList<>();
                for (Map.Entry<DetailAST, String> entry : exprToInputValue.entrySet()) {
                    if (entry.getKey() != messageExpr) {
                        inputValues.add(entry.getValue());
                    }
                }
                LoopIndexAnalyzer.LoopIndexInfo loopIndexInfo = loopAnalyzer.findLoopIndexInfo(methodCall, messageExpr, template.message());
                AssertionOperandExtractor.ComparisonInfo comparisonInfo = null;
                AssertionOperandExtractor.StringSearchInfo searchInfo = null;
                boolean constantMessage;
                if (source == MessageSource.ASSERTION
                        && (methodName.equals("assertTrue") || methodName.equals("assertFalse"))) {
                    AssertionOperandExtractor.BooleanAssertionOperands operands = operandExtractor.resolveBooleanAssertionOperands(elist, style);
                    comparisonInfo = operandExtractor.extractComparison(operands.conditionExpr());
                    searchInfo = operandExtractor.extractStringSearch(operands.conditionExpr());
                    constantMessage = isConstantStringExpression(astSupport().unwrapExpr(operands.messageExpr()));
                } else {
                    constantMessage = isConstantStringExpression(astSupport().unwrapExpr(messageExpr));
                }

                int keyValueLabelCount = countKeyValueLabels(template.message());

                MessageCandidate.Builder builder = new MessageCandidate.Builder()
                        .source(source)
                        .lineNo(lineNo)
                        .message(template.message())
                        .normalisedMessage(MessageNormaliser.normalise(template.message()))
                        .placeholderCount(template.placeholderCount())
                        .keyValueLabelCount(keyValueLabelCount)
                        .inputValues(inputValues)
                        .argumentNameMessage(argumentNameMessage)
                        .constantMessage(constantMessage)
                        .trivialSupplierDescription(cheapSupplierDesc);
                if (loopIndexInfo != null) {
                    builder.loopNames(loopIndexInfo.loopNames())
                            .missingLoopIndex(loopIndexInfo.isMissing());
                }
                if (comparisonInfo != null) {
                    builder.comparisonOperator(comparisonInfo.operator())
                            .comparisonLeftOperand(comparisonInfo.leftOperand())
                            .comparisonRightOperand(comparisonInfo.rightOperand());
                }
                if (searchInfo != null) {
                    builder.stringSearchMethod(searchInfo.methodName())
                            .stringSearchTarget(searchInfo.stringVar())
                            .stringSearchArg(searchInfo.searchArg());
                }
                sink().emitCandidate(builder.build());
            } else if (isSupplierMessageExpression(messageExpr)) {
                if (cheapSupplierDesc != null) {
                    MessageCandidate candidate = new MessageCandidate.Builder()
                            .source(source)
                            .lineNo(lineNo)
                            .trivialSupplierDescription(cheapSupplierDesc)
                            .build();
                    sink().emitCandidate(candidate);
                }
                return;
            } else {
                if (!context().hasInlineReasonComment(methodCall)) {
                    DetailAST content = astSupport().unwrapExpr(messageExpr);
                    if (content != null && isNonLiteralMessageExpression(content)) {
                        emitUnhandled(methodCall, "Non-literal message argument not handled for " + methodName);
                    } else {
                        sink().emitMissingMessage(lineNo, source);
                    }
                }
            }
        } else if (cheapSupplierDesc != null) {
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(source)
                    .lineNo(lineNo)
                    .trivialSupplierDescription(cheapSupplierDesc)
                    .build();
            sink().emitCandidate(candidate);
        } else if (firstStringExpr != null) {
            if (source == MessageSource.ASSERTION && style == AssertionOperandExtractor.AssertionStyle.UNKNOWN) {
                boolean missingMessage = false;
                if (AssertionMethodClassifier.isEqualityAssertionMethod(methodName)) {
                    missingMessage = argCount < 3;
                } else if (AssertionMethodClassifier.isAssertThatMethod(methodName)) {
                    DetailAST content = astSupport().unwrapExpr(firstStringExpr);
                    if (argCount < 3 && content != null && astSupport().containsStringLiteralDeep(content)) {
                        missingMessage = true;
                    }
                } else if (AssertionMethodClassifier.isAssertThrowsMethod(methodName)
                        || AssertionMethodClassifier.isTimeoutAssertionMethod(methodName)) {
                    missingMessage = argCount < 3;
                } else if (AssertionMethodClassifier.isDoesNotThrowMethod(methodName)
                        || AssertionMethodClassifier.isBooleanAssertionMethod(methodName)
                        || AssertionMethodClassifier.isNullnessAssertionMethod(methodName)) {
                    missingMessage = argCount < 2;
                } else if (methodName.startsWith("assert")) {
                    missingMessage = argCount < 2;
                }
                if (missingMessage) {
                    if (!context().hasInlineReasonComment(methodCall)) {
                        sink().emitMissingMessage(lineNo, source);
                    }
                    return;
                }
            }
            if (source == MessageSource.PRECONDITION && argCount < 2) {
                if (!context().hasInlineReasonComment(methodCall)) {
                    sink().emitMissingMessage(lineNo, source);
                }
                return;
            }
            // Unrecognised assert* methods are silently ignored when we cannot determine
            // message position. Many custom assertion methods (e.g., assertCustomCondition)
            // do not have message-accepting overloads, so flagging them as "missing message"
            // or "unhandled" would produce false positives. We only flag unhandled when we
            // have a string argument but cannot determine its role.
            if (source == MessageSource.ASSERTION
                    && style == AssertionOperandExtractor.AssertionStyle.UNKNOWN
                    && methodName.startsWith("assert")
                    && !AssertionMethodClassifier.isRecognisedAssertionMethod(methodName)) {
                return;
            }
            emitUnhandled(methodCall, "Unable to resolve message argument for " + methodName
                    + " (argCount=" + argCount
                    + ", firstStringIndex=" + firstStringArgIndex
                    + ", lastStringIndex=" + lastStringArgIndex
                    + ", style=" + style + ")");
        }
    }

    /**
     * Select the message expression from the method call arguments.
     *
     * <p>Returns {@code null} for several distinct reasons:
     * <ul>
     *   <li>No string expression exists in arguments (caller should emit missing message)</li>
     *   <li>Insufficient arguments for the method signature (e.g., assertTrue(boolean) has no message)</li>
     *   <li>String at unexpected position for method pattern (likely not a message)</li>
     *   <li>Source type not handled by this method</li>
     * </ul>
     *
     * <p>Callers cannot distinguish these reasons from the null return alone.
     * The caller uses {@code firstStringExpr != null} to distinguish "no string found" from
     * "string found but not in message position", which determines whether to emit
     * {@code MMMissingMessage} or {@code MMUnhandled}.
     */
    private DetailAST selectMessageExpression(String methodName, int argCount, List<DetailAST> args,
                                              DetailAST firstStringExpr, int firstStringArgIndex,
                                              DetailAST lastStringExpr, int lastStringArgIndex,
                                              DetailAST lastSupplierExpr, int lastSupplierArgIndex,
                                              MessageSource source, AssertionOperandExtractor.AssertionStyle style) {
        if (source == MessageSource.ASSERTION
                && style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
            return selectJUnit4MessageExpression(methodName, argCount, args);
        }
        if (source == MessageSource.ASSERTION
                && style == AssertionOperandExtractor.AssertionStyle.JUNIT5
                && lastSupplierExpr != null
                && lastSupplierArgIndex == argCount
                && !AssertionMethodClassifier.isAssertThatMethod(methodName)
                && methodName.startsWith("assert")) {
            return lastSupplierExpr;
        }
        if (firstStringExpr == null) {
            return null;
        }
        if (source == MessageSource.PRECONDITION) {
            if (argCount < 2) {
                return null;
            }
            if (lastStringArgIndex == argCount) {
                return lastStringExpr;
            }
            if (argCount == 2 && firstStringArgIndex == 1) {
                return firstStringExpr;
            }
            return null;
        }
        if (source != MessageSource.ASSERTION) {
            return null;
        }
        if (AssertionMethodClassifier.isFailMethod(methodName)) {
            return firstStringArgIndex == 1 ? firstStringExpr : null;
        }
        if (AssertionMethodClassifier.isAssertThatMethod(methodName)) {
            if (argCount < 3) {
                return null;
            }
            return firstStringArgIndex == 1 ? firstStringExpr : null;
        }
        if (AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            if (argCount < 3) {
                return null;
            }
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4
                    || (style == AssertionOperandExtractor.AssertionStyle.UNKNOWN
                    && firstStringArgIndex == 1)) {
                return firstStringArgIndex == 1 ? firstStringExpr : null;
            }
            return lastStringArgIndex == argCount ? lastStringExpr : null;
        }
        if (AssertionMethodClassifier.isTimeoutAssertionMethod(methodName)) {
            if (argCount < 3) {
                return null;
            }
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
                return firstStringArgIndex == 1 ? firstStringExpr : null;
            }
            return lastStringArgIndex == argCount ? lastStringExpr : null;
        }
        if (AssertionMethodClassifier.isBooleanAssertionMethod(methodName) || AssertionMethodClassifier.isNullnessAssertionMethod(methodName)) {
            if (argCount < 2) {
                return null;
            }
            return selectByStyle(style, firstStringExpr, firstStringArgIndex,
                    lastStringExpr, lastStringArgIndex, argCount);
        }
        if (AssertionMethodClassifier.isEqualityAssertionMethod(methodName)) {
            if (argCount < 3) {
                return null;
            }
            return selectByStyle(style, firstStringExpr, firstStringArgIndex,
                    lastStringExpr, lastStringArgIndex, argCount);
        }
        if (argCount < 2) {
            return null;
        }
        return selectByStyle(style, firstStringExpr, firstStringArgIndex,
                lastStringExpr, lastStringArgIndex, argCount);
    }

    private DetailAST selectJUnit4MessageExpression(String methodName, int argCount, List<DetailAST> args) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        if (AssertionMethodClassifier.isFailMethod(methodName)) {
            return argCount >= 1 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isAssertThatMethod(methodName)) {
            return argCount >= 3 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            return argCount >= 3 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isTimeoutAssertionMethod(methodName)) {
            return argCount >= 3 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isDoesNotThrowMethod(methodName)) {
            return argCount >= 2 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isBooleanAssertionMethod(methodName)
                || AssertionMethodClassifier.isNullnessAssertionMethod(methodName)) {
            return argCount >= 2 ? args.get(0) : null;
        }
        if (AssertionMethodClassifier.isEqualityAssertionMethod(methodName)) {
            return argCount >= 3 ? args.get(0) : null;
        }
        if (methodName.startsWith("assert")) {
            return argCount >= 2 ? args.get(0) : null;
        }
        return null;
    }

    private DetailAST selectByStyle(AssertionOperandExtractor.AssertionStyle style, DetailAST firstStringExpr, int firstStringArgIndex,
                                    DetailAST lastStringExpr, int lastStringArgIndex, int argCount) {
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
            return firstStringArgIndex == 1 ? firstStringExpr : null;
        }
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
            return lastStringArgIndex == argCount ? lastStringExpr : null;
        }
        if (firstStringArgIndex == lastStringArgIndex) {
            return (firstStringArgIndex == 1 || lastStringArgIndex == argCount)
                    ? firstStringExpr
                    : null;
        }
        if (firstStringArgIndex == 1 && lastStringArgIndex != argCount) {
            return firstStringExpr;
        }
        if (lastStringArgIndex == argCount && firstStringArgIndex != 1) {
            return lastStringExpr;
        }
        return null;
    }

    private boolean isLambdaMessageArgument(String methodName, AssertionOperandExtractor.AssertionStyle style,
                                            int argCount, int firstStringArgIndex, int lastLambdaArgIndex) {
        if (lastLambdaArgIndex != argCount) {
            return false;
        }
        if (!AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            return true;
        }
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
            return false;
        }
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
            return argCount >= 3;
        }
        return argCount >= 3 && firstStringArgIndex != 1;
    }

    private boolean isLocalAssertionHelper(DetailAST methodCall, String methodName) {
        if (methodCall == null || methodName == null) {
            return false;
        }
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            DetailAST qualifier = dot.getFirstChild();
            if (qualifier != null
                    && (qualifier.getType() == TokenTypes.LITERAL_THIS
                    || qualifier.getType() == TokenTypes.LITERAL_SUPER)) {
                return context().isDeclaredMethodName(methodName);
            }
            return false;
        }
        return context().isDeclaredMethodName(methodName);
    }

    private boolean isNonLiteralMessageExpression(DetailAST expr) {
        if (expr == null) {
            return false;
        }
        if (expr.getType() == TokenTypes.METHOD_CALL || expr.getType() == TokenTypes.METHOD_REF) {
            return true;
        }
        return typeAnalyzer.isStringTypedExpression(expr)
                || typeAnalyzer.isSupplierTypedExpression(expr);
    }

    private boolean isSupplierExpression(DetailAST expr) {
        if (expr == null) {
            return false;
        }
        return expr.getType() == TokenTypes.METHOD_REF
                || typeAnalyzer.isSupplierTypedExpression(expr);
    }

    private boolean isSupplierMessageExpression(DetailAST expr) {
        DetailAST content = astSupport().unwrapExpr(expr);
        if (content == null) {
            return false;
        }
        if (content.getType() == TokenTypes.LAMBDA || content.getType() == TokenTypes.METHOD_REF) {
            return true;
        }
        return typeAnalyzer.isSupplierTypedExpression(content);
    }

    private boolean isParameterNameMessage(MessageTemplate template, List<DetailAST> args,
                                           Map<DetailAST, String> exprToInputValue) {
        if (template.placeholderCount() != 0) {
            return false;
        }
        return isParameterNameMessage(template.message(), args, exprToInputValue);
    }

    private boolean isParameterNameMessage(String message, List<DetailAST> args,
                                           Map<DetailAST, String> exprToInputValue) {
        if (message == null) {
            return false;
        }
        if (args.isEmpty()) {
            return false;
        }
        String inputValue = exprToInputValue.get(args.get(0));
        if (inputValue == null) {
            return false;
        }
        return message.equals(inputValue);
    }

    private boolean isRequireNonNullArgumentMessage(String methodName, MessageSource source,
                                                    List<DetailAST> args, MessageTemplate template) {
        if (template == null || template.placeholderCount() != 0) {
            return false;
        }
        return isRequireNonNullArgumentMessage(methodName, source, args, template.message());
    }

    private boolean isRequireNonNullArgumentMessage(String methodName, MessageSource source,
                                                    List<DetailAST> args, String message) {
        if (source != MessageSource.PRECONDITION) {
            return false;
        }
        if (!AssertionMethodClassifier.isRequireNotNullMethod(methodName)) {
            return false;
        }
        if (message == null || args.isEmpty()) {
            return false;
        }
        String argText = extractArgumentText(args.get(0));
        if (argText == null) {
            return false;
        }
        return normaliseExpressionText(message).equals(normaliseExpressionText(argText));
    }

    private String extractArgumentText(DetailAST expr) {
        requireNonNull(expr);
        DetailAST content = astSupport().unwrapExpr(expr);
        return renderArgumentExpression(content);
    }

    private String renderArgumentExpression(DetailAST expr) {
        if (expr == null) {
            return null;
        }
        switch (expr.getType()) {
            case TokenTypes.IDENT:
                return expr.getText();
            case TokenTypes.LITERAL_THIS:
                return "this";
            case TokenTypes.DOT:
                return renderDotExpression(expr);
            case TokenTypes.METHOD_CALL:
                return renderMethodCallExpression(expr);
            default:
                return null;
        }
    }

    private String renderDotExpression(DetailAST dot) {
        if (dot == null || dot.getType() != TokenTypes.DOT) {
            return null;
        }
        String left = renderArgumentExpression(dot.getFirstChild());
        String right = renderArgumentExpression(dot.getLastChild());
        if (left == null || right == null) {
            return null;
        }
        return left + "." + right;
    }

    private String renderMethodCallExpression(DetailAST methodCall) {
        if (methodCall == null || methodCall.getType() != TokenTypes.METHOD_CALL) {
            return null;
        }
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist != null && elist.getFirstChild() != null) {
            return null;
        }
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            String qualified = renderDotExpression(dot);
            return qualified == null ? null : qualified + "()";
        }
        DetailAST ident = methodCall.findFirstToken(TokenTypes.IDENT);
        return ident == null ? null : ident.getText() + "()";
    }

    private String normaliseExpressionText(String text) {
        return text == null ? null : text.replaceAll("\\s+", "");
    }

    private boolean isMissingAssertionMessage(String methodName, AssertionOperandExtractor.AssertionStyle style, List<DetailAST> args) {
        if (style == AssertionOperandExtractor.AssertionStyle.UNKNOWN) {
            return false;
        }
        if (AssertionMethodClassifier.isAssertJMessageMethod(methodName)) {
            return false;
        }
        if (AssertionMethodClassifier.isAssertThatMethod(methodName)) {
            return false;
        }
        int argCount = args.size();
        if (AssertionMethodClassifier.isFailMethod(methodName)) {
            if (argCount == 0) {
                return true;
            }
            return !isMessageArgumentPresent(args.get(0));
        }
        if (!methodName.startsWith("assert")
                && !AssertionMethodClassifier.isAssumptionMethod(methodName)) {
            return false;
        }
        if (methodName.equals("assertAll")) {
            if (argCount == 0) {
                return true;
            }
            DetailAST firstArg = args.get(0);
            if (typeAnalyzer.isLambdaArgument(firstArg)) {
                return true;
            }
            return !isMessageArgumentPresent(firstArg);
        }
        if (AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
                if (argCount < 3) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(0));
            }
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
                if (argCount < 3) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(argCount - 1));
            }
        }
        if (AssertionMethodClassifier.isTimeoutAssertionMethod(methodName)) {
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
                if (argCount < 3) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(argCount - 1));
            }
        }
        if (AssertionMethodClassifier.isDoesNotThrowMethod(methodName)) {
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
                if (argCount < 2) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(argCount - 1));
            }
        }
        if (AssertionMethodClassifier.isAssertThatMethod(methodName)) {
            if (argCount < 3) {
                return true;
            }
            return !isMessageArgumentPresent(args.get(0));
        }
        if (AssertionMethodClassifier.isBooleanAssertionMethod(methodName) || AssertionMethodClassifier.isNullnessAssertionMethod(methodName)) {
            if (argCount < 2) {
                return true;
            }
            DetailAST candidate = style == AssertionOperandExtractor.AssertionStyle.JUNIT4 ? args.get(0) : args.get(argCount - 1);
            return astSupport().isNullLiteral(candidate);
        }
        if (AssertionMethodClassifier.isEqualityAssertionMethod(methodName)) {
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
                if (argCount < 3) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(0));
            }
            if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
                if (argCount < 3) {
                    return true;
                }
                return !isMessageArgumentPresent(args.get(argCount - 1));
            }
        }
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT4) {
            if (argCount < 2) {
                return true;
            }
            return !isMessageArgumentPresent(args.get(0));
        }
        if (style == AssertionOperandExtractor.AssertionStyle.JUNIT5) {
            if (argCount < 2) {
                return true;
            }
            return !isMessageArgumentPresent(args.get(argCount - 1));
        }
        return false;
    }

    private boolean isMessageArgumentPresent(DetailAST expr) {
        requireNonNull(expr);
        if (astSupport().isNullLiteral(expr)) {
            return false;
        }
        DetailAST content = requireNonNull(astSupport().unwrapExpr(expr));
        if (content.getType() == TokenTypes.LAMBDA
                || content.getType() == TokenTypes.METHOD_REF) {
            return true;
        }
        if (typeAnalyzer.isStringTypedExpression(content) || typeAnalyzer.isSupplierTypedExpression(content)) {
            return true;
        }
        return astSupport().containsStringLiteralDeep(content);
    }

    private boolean isConstantStringExpression(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.isConstantStringExpression(expr);
    }

    private MessageTemplate extractMessageTemplate(DetailAST expr) {
        return context().extractMessageTemplate(expr);
    }

    private String extractStringLiteral(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.extractStringLiteral(expr, true);
    }

    private int countKeyValueLabels(String constantParts) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.countKeyValueLabels(constantParts);
    }

    private void emitMessageCandidate(String message, int lineNo,
                                      int placeholderCount, int keyValueLabelCount) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(lineNo)
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .placeholderCount(placeholderCount)
                .keyValueLabelCount(keyValueLabelCount)
                .build();
        sink().emitCandidate(candidate);
    }

    private String extractInputValue(DetailAST expr) {
        requireNonNull(expr);

        DetailAST content = expr;
        if (content.getType() == TokenTypes.EXPR && content.getChildCount() == 1) {
            content = content.getFirstChild();
        }
        if (content.getType() == TokenTypes.STRING_LITERAL) {
            String text = content.getText();
            if (text.length() >= 2) {
                return text.substring(1, text.length() - 1);
            }
            return null;
        }
        if (content.getType() == TokenTypes.IDENT) {
            return content.getText();
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST lastChild = content.getLastChild();
            if (lastChild != null && "class".equals(lastChild.getText())) {
                DetailAST firstChild = content.getFirstChild();
                if (firstChild != null) {
                    DetailAST ident = firstChild.getType() == TokenTypes.IDENT
                            ? firstChild
                            : astSupport().findRightmostIdent(firstChild);
                    if (ident != null) {
                        return ident.getText();
                    }
                }
            }
        }
        if (content.getType() == TokenTypes.METHOD_CALL) {
            DetailAST ident = content.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                return ident.getText();
            }
        }
        return null;
    }

    private String extractExceptionClassLiteral(DetailAST elist) {
        requireNonNull(elist);
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                String className = astSupport().extractClassLiteralName(child);
                if (className != null) {
                    return className;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private void checkAssertAllHeading(DetailAST methodCall, DetailAST elist, int lineNo) {
        DetailAST child = elist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                MessageTemplate template = extractMessageTemplate(child);
                if (template == null) {
                    child = child.getNextSibling();
                    continue;
                }
                LoopIndexAnalyzer.LoopIndexInfo loopIndexInfo = loopAnalyzer.findLoopIndexInfo(methodCall, child, template.message());
                int keyValueLabelCount = countKeyValueLabels(template.message());
                MessageCandidate.Builder builder = new MessageCandidate.Builder()
                        .source(MessageSource.ASSERTION)
                        .lineNo(lineNo)
                        .message(template.message())
                        .normalisedMessage(MessageNormaliser.normalise(template.message()))
                        .placeholderCount(template.placeholderCount())
                        .keyValueLabelCount(keyValueLabelCount)
                        .assertAllHeading(true);
                if (loopIndexInfo != null) {
                    builder.loopNames(loopIndexInfo.loopNames())
                            .missingLoopIndex(loopIndexInfo.isMissing());
                }
                sink().emitCandidate(builder.build());
                return;
            }
            child = child.getNextSibling();
        }
    }
}
