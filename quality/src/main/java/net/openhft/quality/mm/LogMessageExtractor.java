/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Extracts message candidates from logging calls (SLF4J, Log4j2, JUL, System.Logger, Jvm).
 */
public final class LogMessageExtractor extends AbstractMessageExtractor {
    private static final Set<String> SLF4J_LEVEL_METHODS = new HashSet<>(
            Arrays.asList("trace", "debug", "info", "warn", "error")
    );
    private static final Set<String> LOG4J2_LEVEL_METHODS = new HashSet<>(
            Arrays.asList("trace", "debug", "info", "warn", "error", "fatal")
    );
    private static final Set<String> JUL_LEVEL_METHODS = new HashSet<>(
            Arrays.asList("severe", "warning", "info", "fine", "finer", "finest")
    );
    private static final Set<String> JVM_LEVEL_METHODS = new HashSet<>(
            Arrays.asList("debug", "warn", "error", "startup", "perf")
    );

    /**
     * Create an extractor for logging messages.
     *
     * @param context extraction context with imports and type information.
     * @param sink    sink to receive message candidates.
     */
    public LogMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
    }

    /**
     * Inspect a logger method call and emit a candidate or missing-message signal.
     *
     * @param methodCall AST node for the method call.
     */
    public void handleMethodCall(DetailAST methodCall) {
        String methodName = requireNonNull(astSupport().extractMethodName(methodCall));
        if (checkJvmLogCall(methodCall, methodName)) {
            return;
        }
        LoggerKind loggerKind = resolveLoggerKind(methodCall);
        if (loggerKind == null || !isLogMethod(loggerKind, methodName)) {
            return;
        }
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            emitUnhandled(methodCall, "Log call without argument list: " + methodName);
            return;
        }
        List<DetailAST> args = astSupport().collectArguments(elist);
        if (args.isEmpty()) {
            emitUnhandled(methodCall, "Log call without arguments: " + methodName);
            return;
        }
        int messageIndex = resolveMessageIndex(loggerKind, methodName, args);
        if (messageIndex < 0 || messageIndex >= args.size()) {
            emitUnhandled(methodCall, "Log message index " + messageIndex
                    + " out of range for " + methodName
                    + " (args=" + args.size()
                    + ", logger=" + loggerKind + ")");
            return;
        }
        DetailAST messageExpr = args.get(messageIndex);
        List<DetailAST> extraArgs = args.subList(messageIndex + 1, args.size());
        boolean hasThrowable = containsThrowable(extraArgs);

        if (astSupport().isNullLiteral(messageExpr) && !hasThrowable && context().hasInlineReasonComment(methodCall)) {
            return;
        }

        if (loggerKind == LoggerKind.SYSTEM && "log".equals(methodName)) {
            checkSystemLoggerCall(methodCall, methodCall.getLineNo(), messageExpr, extraArgs, hasThrowable);
            return;
        }

        checkStandardLogCall(methodCall, methodCall.getLineNo(), messageExpr, extraArgs, hasThrowable);
    }

    private void checkStandardLogCall(DetailAST methodCall, int lineNo, DetailAST messageExpr,
                                      List<DetailAST> extraArgs,
                                      boolean hasThrowable) {
        if (astSupport().isNullLiteral(messageExpr)) {
            if (hasThrowable) {
                return;
            }
            sink().emitMissingMessage(lineNo, MessageSource.LOG);
            return;
        }

        MessageTemplate template = extractMessageTemplate(messageExpr);
        if (template == null) {
            if (!context().hasInlineReasonComment(methodCall)) {
                sink().emitMissingMessage(lineNo, MessageSource.LOG);
            }
            return;
        }
        if (hasThrowable && isBlankMessage(template.message())) {
            return;
        }
        int logPlaceholderCount = template.fromFormatCall() ? 0 : countLogPlaceholders(template.message());
        int placeholderCount = template.placeholderCount() + Math.max(extraArgs.size(), logPlaceholderCount);
        int keyValueLabelCount = countKeyValueLabels(template.message());
        emitMessageCandidate(template.message(), lineNo, placeholderCount, keyValueLabelCount);
    }

    private void checkSystemLoggerCall(DetailAST methodCall, int lineNo, DetailAST messageExpr,
                                       List<DetailAST> extraArgs,
                                       boolean hasThrowable) {
        if (astSupport().isNullLiteral(messageExpr)) {
            if (hasThrowable) {
                return;
            }
            sink().emitMissingMessage(lineNo, MessageSource.LOG);
            return;
        }

        DetailAST content = astSupport().unwrapExpr(messageExpr);
        boolean hasSupplier = content != null && content.getType() == TokenTypes.METHOD_REF;
        if (!hasSupplier) {
            hasSupplier = astSupport().findLambda(messageExpr) != null;
        }
        if (!hasSupplier && content != null) {
            hasSupplier = isSupplierTypedExpression(content);
        }
        if (!hasSupplier) {
            checkStandardLogCall(methodCall, lineNo, messageExpr, extraArgs, hasThrowable);
            return;
        }

        String supplierMessage = extractConstantSupplierMessage(messageExpr);
        if (supplierMessage == null) {
            int placeholderCount = extraArgs.size() + 1;
            emitMessageCandidate("", lineNo, placeholderCount, 0);
            return;
        }
        int formatPlaceholderCount = countFormatPlaceholders(supplierMessage);
        int logPlaceholderCount = formatPlaceholderCount > 0 ? 0 : countLogPlaceholders(supplierMessage);
        int placeholderCount = formatPlaceholderCount + Math.max(extraArgs.size(), logPlaceholderCount);
        int keyValueLabelCount = countKeyValueLabels(supplierMessage);
        emitMessageCandidate(supplierMessage, lineNo, placeholderCount, keyValueLabelCount
        );
    }

    private boolean checkJvmLogCall(DetailAST methodCall, String methodName) {
        if (!"on".equals(methodName)) {
            return false;
        }
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }
        DetailAST owner = dot.getFirstChild();
        if (owner == null || owner.getType() != TokenTypes.METHOD_CALL) {
            return false;
        }
        String ownerMethod = astSupport().extractMethodName(owner);
        if (ownerMethod == null || !JVM_LEVEL_METHODS.contains(ownerMethod)) {
            return false;
        }
        DetailAST ownerDot = owner.findFirstToken(TokenTypes.DOT);
        if (ownerDot == null) {
            return false;
        }
        String qualifier = astSupport().extractQualifierIdent(ownerDot);
        if (!"Jvm".equals(qualifier)) {
            return false;
        }

        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return false;
        }
        List<DetailAST> args = astSupport().collectArguments(elist);
        if (args.size() < 2) {
            return false;
        }
        DetailAST messageExpr;
        boolean hasThrowable;
        if (args.size() >= 3) {
            messageExpr = args.get(1);
            hasThrowable = isThrowableExpression(args.get(2));
        } else {
            messageExpr = args.get(1);
            hasThrowable = isThrowableExpression(messageExpr);
            if (hasThrowable) {
                messageExpr = null;
            }
        }

        if (messageExpr == null || astSupport().isNullLiteral(messageExpr)) {
            if (!hasThrowable) {
                if (!context().hasInlineReasonComment(methodCall)) {
                    sink().emitMissingMessage(methodCall.getLineNo(), MessageSource.LOG);
                }
            }
            return true;
        }

        MessageTemplate template = extractMessageTemplate(messageExpr);
        if (template == null) {
            if (!context().hasInlineReasonComment(methodCall)) {
                sink().emitMissingMessage(methodCall.getLineNo(), MessageSource.LOG);
            }
            return true;
        }
        if (hasThrowable && isBlankMessage(template.message())) {
            return true;
        }
        int keyValueLabelCount = countKeyValueLabels(template.message());
        emitMessageCandidate(template.message(), methodCall.getLineNo(),
                template.placeholderCount(), keyValueLabelCount);
        return true;
    }

    private int resolveMessageIndex(LoggerKind loggerKind, String methodName,
                                    List<DetailAST> args) {
        if (loggerKind == LoggerKind.JUL) {
            if ("log".equals(methodName)) {
                return args.size() >= 2 ? 1 : -1;
            }
            return 0;
        }
        if (loggerKind == LoggerKind.SYSTEM) {
            return "log".equals(methodName) && args.size() >= 2 ? 1 : -1;
        }
        return 0;
    }

    private LoggerKind resolveLoggerKind(DetailAST methodCall) {
        String qualifier = extractQualifierNameForLog(methodCall);
        if (qualifier == null) {
            return null;
        }
        String typeName = context().getVariableType(qualifier);
        if (typeName == null) {
            return null;
        }
        return resolveLoggerKindFromType(typeName);
    }

    LoggerKind resolveLoggerKindFromType(String typeName) {
        String resolved = context().resolveTypeName(typeName);
        requireNonNull(resolved);
        switch (resolved) {
            case "org.slf4j.Logger":
                return LoggerKind.SLF4J;
            case "org.apache.logging.log4j.Logger":
                return LoggerKind.LOG4J2;
            case "java.util.logging.Logger":
                return LoggerKind.JUL;
            case "java.lang.System.Logger":
            case "System.Logger":
                return LoggerKind.SYSTEM;
            default:
                return LoggerKind.UNKNOWN;
        }
    }

    private String extractQualifierNameForLog(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return null;
        }
        DetailAST left = dot.getFirstChild();
        requireNonNull(left);
        if (left.getType() == TokenTypes.METHOD_CALL) {
            return null;
        }
        if (left.getType() == TokenTypes.IDENT) {
            return left.getText();
        }
        if (left.getType() == TokenTypes.DOT) {
            return astSupport().extractQualifierIdent(dot);
        }
        return null;
    }

    boolean isLogMethod(LoggerKind loggerKind, String methodName) {
        switch (loggerKind) {
            case SLF4J:
                return SLF4J_LEVEL_METHODS.contains(methodName);
            case LOG4J2:
                return LOG4J2_LEVEL_METHODS.contains(methodName);
            case JUL:
                return JUL_LEVEL_METHODS.contains(methodName) || "log".equals(methodName);
            case SYSTEM:
                return "log".equals(methodName);
            default:
                return false;
        }
    }

    boolean containsThrowable(List<DetailAST> args) {
        for (DetailAST arg : args) {
            if (isThrowableExpression(arg)) {
                return true;
            }
        }
        return false;
    }

    boolean isThrowableExpression(DetailAST expr) {
        DetailAST content = astSupport().unwrapExpr(expr);
        requireNonNull(content);
        if (content.getType() == TokenTypes.LITERAL_NEW) {
            String className = astSupport().extractNewClassName(content);
            return isThrowableTypeName(className);
        }
        if (content.getType() == TokenTypes.IDENT) {
            String typeName = context().getVariableType(content.getText());
            return isThrowableTypeName(typeName);
        }
        return false;
    }

    boolean isThrowableTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
        if (resolved == null) {
            return false;
        }
        String simple = resolved;
        int lastDot = resolved.lastIndexOf('.');
        if (lastDot >= 0) {
            simple = resolved.substring(lastDot + 1);
        }
        return simple.equals("Throwable")
                || simple.endsWith("Exception")
                || simple.endsWith("Error")
                || simple.equals("StackTrace");
    }

    private int countLogPlaceholders(String message) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.countLogPlaceholders(message);
    }

    private int countFormatPlaceholders(String message) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.countFormatPlaceholders(message);
    }

    private String extractConstantSupplierMessage(DetailAST expr) {
        DetailAST lambda = astSupport().findLambda(expr);
        if (lambda == null) {
            return null;
        }
        DetailAST params = lambda.findFirstToken(TokenTypes.PARAMETERS);
        if (params != null && params.getChildCount() > 0) {
            return null;
        }
        DetailAST body = lambda.findFirstToken(TokenTypes.EXPR);
        if (body != null) {
            body = body.getFirstChild();
        }
        if (body == null) {
            body = lambda.getLastChild();
            if (body != null && body.getType() == TokenTypes.EXPR) {
                body = body.getFirstChild();
            }
        }
        requireNonNull(body);
        MessageTemplateExtractor templateExtractor = context().templateExtractor();
        if (body.getType() == TokenTypes.METHOD_CALL) {
            MessageTemplate template = templateExtractor.extractMessageTemplate(body);
            if (template != null && template.fromFormatCall()) {
                return template.message();
            }
        }
        if (isConstantStringExpression(body)) {
            return extractConstantString(body);
        }
        return null;
    }

    private boolean isConstantStringExpression(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.isConstantStringExpression(expr);
    }

    private String extractConstantString(DetailAST expr) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.extractConstantString(expr);
    }

    private MessageTemplate extractMessageTemplate(DetailAST expr) {
        return context().extractMessageTemplate(expr);
    }

    private int countKeyValueLabels(String constantParts) {
        MessageTemplateExtractor templateExtractor = requireNonNull(context().templateExtractor());
        return templateExtractor.countKeyValueLabels(constantParts);
    }

    boolean isBlankMessage(String message) {
        return message == null || message.trim().isEmpty();
    }

    private void emitMessageCandidate(String message, int lineNo,
                                      int placeholderCount, int keyValueLabelCount) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.LOG)
                .lineNo(lineNo)
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .placeholderCount(placeholderCount)
                .keyValueLabelCount(keyValueLabelCount)
                .build();
        sink().emitCandidate(candidate);
    }

    boolean isSupplierTypedExpression(DetailAST expr) {
        if (expr == null) {
            return false;
        }
        if (expr.getType() == TokenTypes.IDENT) {
            return isSupplierTypeName(context().getVariableType(expr.getText()));
        }
        if (expr.getType() == TokenTypes.DOT) {
            DetailAST rightmost = astSupport().findRightmostIdent(expr);
            if (rightmost != null) {
                return isSupplierTypeName(context().getVariableType(rightmost.getText()));
            }
        }
        if (expr.getType() == TokenTypes.TYPECAST) {
            DetailAST type = expr.findFirstToken(TokenTypes.TYPE);
            return type != null && isSupplierTypeName(astSupport().extractTypeName(type));
        }
        return false;
    }

    boolean isSupplierTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
        if (resolved == null) {
            return false;
        }
        return "java.util.function.Supplier".equals(resolved)
                || "Supplier".equals(resolved)
                || resolved.endsWith(".Supplier");
    }

    enum LoggerKind {
        SLF4J,
        LOG4J2,
        JUL,
        SYSTEM,
        UNKNOWN
    }
}
