/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.*;

/**
 * Extracts message candidates from assertion and precondition calls.
 */
public final class AssertionMessageExtractor extends AbstractMessageExtractor {
    private static final String JUNIT4_ASSERTIONS = "org.junit.Assert";
    private static final String JUNIT5_ASSERTIONS = "org.junit.jupiter.api.Assertions";

    private final LoopIndexAnalyzer loopAnalyzer;
    private final LambdaMessageExtractor lambdaExtractor;
    private final ExpressionTypeAnalyzer typeAnalyzer;
    private final AssertionOperandExtractor operandExtractor;

    /**
     * Create an extractor for assertions and preconditions.
     *
     * @param context extraction context with imports and type information.
     * @param sink sink to receive message candidates.
     */
    public AssertionMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
        this.loopAnalyzer = new LoopIndexAnalyzer(astSupport());
        this.lambdaExtractor = new LambdaMessageExtractor(context);
        this.typeAnalyzer = new ExpressionTypeAnalyzer(context);
        this.operandExtractor = new AssertionOperandExtractor(astSupport(), context.templateExtractor());
    }

    /**
     * Process a Java {@code assert} statement and extract its message expression.
     *
     * @param assertAst AST node for the {@code assert} statement.
     */
    public void handleJavaAssert(DetailAST assertAst) {
        DetailAST expr = Objects.requireNonNull(assertAst.findFirstToken(TokenTypes.EXPR));
        DetailAST nextExpr = expr.getNextSibling();
        while (nextExpr != null) {
            if (nextExpr.getType() == TokenTypes.EXPR) {
                MessageTemplate template = extractMessageTemplate(nextExpr);
                if (template != null) {
                    int keyValueLabelCount = countKeyValueLabels(template.message());
                    emitMessageCandidate(template.message(), nextExpr.getLineNo(),
                            template.placeholderCount(), keyValueLabelCount);
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
        String methodName = Objects.requireNonNull(astSupport().extractMethodName(methodCall));

        if (AssertionMethodClassifier.isAssertionMethod(methodName)) {
            DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
            if (elist != null) {
                MessageSource source = AssertionMethodClassifier.isPreconditionMethod(methodName)
                        ? MessageSource.PRECONDITION
                        : MessageSource.ASSERTION;
                AssertionOperandExtractor.AssertionStyle style = source == MessageSource.ASSERTION
                        ? resolveAssertionStyle(methodCall, methodName)
                        : AssertionOperandExtractor.AssertionStyle.UNKNOWN;
                checkAssertionArguments(methodCall, elist, methodName, methodCall.getLineNo(),
                        source, style);
            }
        }
    }

    private AssertionOperandExtractor.AssertionStyle resolveAssertionStyle(DetailAST methodCall, String methodName) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            String fullCall = astSupport().flattenDot(dot);
            if (fullCall != null) {
                if (fullCall.startsWith(JUNIT4_ASSERTIONS + ".")) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT4;
                }
                if (fullCall.startsWith(JUNIT5_ASSERTIONS + ".")) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT5;
                }
            }
            String qualifier = astSupport().extractQualifierIdent(dot);
            if (qualifier != null) {
                String importName = context().importedClass(qualifier);
                if (JUNIT4_ASSERTIONS.equals(importName)) {
                    return AssertionOperandExtractor.AssertionStyle.JUNIT4;
                }
                if (JUNIT5_ASSERTIONS.equals(importName)) {
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
                                         MessageSource source, AssertionOperandExtractor.AssertionStyle style) {
        if (source == MessageSource.ASSERTION && AssertionMethodClassifier.isAssertThrowsMethod(methodName)) {
            String exceptionClassName = extractExceptionClassLiteral(elist);
            if (context().isIgnoredExceptionClass(exceptionClassName)) {
                return;
            }
        }

        int argCount = 0;
        List<DetailAST> args = new ArrayList<>();
        DetailAST firstStringExpr = null;
        DetailAST lastStringExpr = null;
        DetailAST lastLambda = null;
        int firstStringArgIndex = -1;
        int lastStringArgIndex = -1;
        int lastLambdaArgIndex = -1;
        Map<DetailAST, String> exprToInputValue = new IdentityHashMap<>();

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
                        String strLiteral = extractStringLiteral(child);
                        if (strLiteral != null) {
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

        String cheapSupplierDesc = null;
        if (lastLambda != null && lastLambdaArgIndex == argCount) {
            String trivialLambdaMessage = lambdaExtractor.extractTrivialLambdaMessageDirect(lastLambda);
            if (trivialLambdaMessage == null) {
                trivialLambdaMessage = lambdaExtractor.extractTrivialLambdaMessage(lastLambda.getParent());
            }
            if (trivialLambdaMessage != null) {
                if (source == MessageSource.PRECONDITION
                        && AssertionMethodClassifier.isRequireNotNullMethod(methodName)
                        && isParameterNameMessage(trivialLambdaMessage, args, exprToInputValue)) {
                    return;
                }
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
        }

        DetailAST messageExpr = selectMessageExpression(methodName, argCount,
                firstStringExpr, firstStringArgIndex, lastStringExpr, lastStringArgIndex, source, style);

        if (messageExpr != null) {
            MessageTemplate template = extractMessageTemplate(messageExpr);
            if (template != null) {
                if (source == MessageSource.PRECONDITION
                        && AssertionMethodClassifier.isRequireNotNullMethod(methodName)
                        && isParameterNameMessage(template, args, exprToInputValue)) {
                    return;
                }
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
                    if (operands != null) {
                        comparisonInfo = operandExtractor.extractComparison(operands.conditionExpr());
                        searchInfo = operandExtractor.extractStringSearch(operands.conditionExpr());
                        constantMessage = isConstantStringExpression(astSupport().unwrapExpr(operands.messageExpr()));
                    } else {
                        constantMessage = isConstantStringExpression(astSupport().unwrapExpr(messageExpr));
                    }
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
            }
        } else if (cheapSupplierDesc != null) {
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(source)
                    .lineNo(lineNo)
                    .trivialSupplierDescription(cheapSupplierDesc)
                    .build();
            sink().emitCandidate(candidate);
        }
    }

    private DetailAST selectMessageExpression(String methodName, int argCount,
                                              DetailAST firstStringExpr, int firstStringArgIndex,
                                              DetailAST lastStringExpr, int lastStringArgIndex,
                                              MessageSource source, AssertionOperandExtractor.AssertionStyle style) {
        if (firstStringExpr == null) {
            return null;
        }
        if (source == MessageSource.PRECONDITION) {
            if (argCount < 2) {
                return null;
            }
            return lastStringArgIndex == argCount ? lastStringExpr : null;
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
        if (!methodName.startsWith("assert")) {
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
        Objects.requireNonNull(expr);
        if (astSupport().isNullLiteral(expr)) {
            return false;
        }
        DetailAST content = Objects.requireNonNull(astSupport().unwrapExpr(expr));
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
        MessageTemplateExtractor templateExtractor = Objects.requireNonNull(context().templateExtractor());
        return templateExtractor.isConstantStringExpression(expr);
    }

    private MessageTemplate extractMessageTemplate(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = Objects.requireNonNull(context().templateExtractor());
        return templateExtractor.extractMessageTemplate(expr);
    }

    private String extractStringLiteral(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = Objects.requireNonNull(context().templateExtractor());
        return templateExtractor.extractStringLiteral(expr, true);
    }

    private int countKeyValueLabels(String constantParts) {
        MessageTemplateExtractor templateExtractor = Objects.requireNonNull(context().templateExtractor());
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
        Objects.requireNonNull(expr);

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
        Objects.requireNonNull(elist);
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
