/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

/**
 * Extracts message candidates from throw statements.
 */
public final class ThrowMessageExtractor extends AbstractMessageExtractor {

    /**
     * Create a throw-statement extractor.
     *
     * @param context extraction context with imports and type information.
     * @param sink    sink to receive message candidates.
     */
    public ThrowMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
    }

    /**
     * Process a {@code throw} statement and emit any message candidate.
     *
     * @param throwAst AST node for the throw statement.
     */
    public void handleThrowStatement(DetailAST throwAst) {
        DetailAST expr = throwAst.findFirstToken(TokenTypes.EXPR);
        if (expr == null) {
            emitUnhandled(throwAst, "Throw statement missing expression");
            return;
        }
        if (astSupport().isNullLiteral(expr)) {
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.THROW)
                    .lineNo(throwAst.getLineNo())
                    .throwNull(true)
                    .build();
            sink().emitCandidate(candidate);
            return;
        }
        DetailAST literalNew = expr.findFirstToken(TokenTypes.LITERAL_NEW);
        if (literalNew == null) {
            if (isThrowableRethrow(expr)) {
                return;
            }
            emitUnhandled(throwAst, "Throw statement does not construct new exception");
            return;
        }
        String exceptionClassName = astSupport().extractNewClassName(literalNew);
        if (context().isIgnoredExceptionClass(exceptionClassName)) {
            return;
        }
        DetailAST elist = literalNew.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            emitUnhandled(literalNew, "Thrown exception has no argument list");
            return;
        }
        List<DetailAST> args = astSupport().collectArguments(elist);
        DetailAST messageExpr = findMessageExpression(args);
        if (messageExpr == null) {
            if (!context().hasInlineReasonComment(literalNew)) {
                sink().emitMissingMessage(throwAst.getLineNo(), MessageSource.THROW);
            }
            return;
        }
        MessageTemplate template = extractMessageTemplate(messageExpr);
        if (template == null) {
            if (isThrowableMessageCall(messageExpr)) {
                return;
            }
            if (!context().hasInlineReasonComment(literalNew)) {
                sink().emitMissingMessage(throwAst.getLineNo(), MessageSource.THROW);
            }
            return;
        }
        int keyValueLabelCount = context().templateExtractor().countKeyValueLabels(template.message());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .lineNo(messageExpr.getLineNo())
                .message(template.message())
                .normalisedMessage(MessageNormaliser.normalise(template.message()))
                .placeholderCount(template.placeholderCount())
                .keyValueLabelCount(keyValueLabelCount)
                .build();
        sink().emitCandidate(candidate);
    }

    private boolean isThrowableExpression(DetailAST expr) {
        DetailAST content = astSupport().unwrapExpr(expr);
        if (content == null) {
            return false;
        }
        if (content.getType() == TokenTypes.LITERAL_NEW) {
            String className = astSupport().extractNewClassName(content);
            return isThrowableTypeName(className);
        }
        if (content.getType() == TokenTypes.IDENT) {
            String typeName = context().getVariableType(content.getText());
            return isThrowableTypeName(typeName);
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST ident = astSupport().findRightmostIdent(content);
            if (ident == null) {
                return false;
            }
            String typeName = context().getVariableType(ident.getText());
            if (isThrowableTypeName(typeName)) {
                return true;
            }
            String qualifier = astSupport().extractQualifierIdent(content);
            return isThrowableTypeName(qualifier);
        }
        return false;
    }

    private DetailAST findMessageExpression(List<DetailAST> args) {
        for (DetailAST arg : args) {
            if (astSupport().isNullLiteral(arg)) {
                continue;
            }
            if (isThrowableExpression(arg)) {
                continue;
            }
            if (extractMessageTemplate(arg) != null) {
                return arg;
            }
            if (isThrowableMessageCall(arg)) {
                return arg;
            }
        }
        return null;
    }

    private MessageTemplate extractMessageTemplate(DetailAST expr) {
        return context().extractMessageTemplate(expr);
    }

    private boolean isThrowableRethrow(DetailAST expr) {
        DetailAST content = astSupport().unwrapExpr(expr);
        if (content == null) {
            return false;
        }
        if (content.getType() == TokenTypes.TYPECAST) {
            DetailAST type = content.findFirstToken(TokenTypes.TYPE);
            if (type != null) {
                String typeName = astSupport().extractTypeName(type);
                if (isThrowableTypeName(typeName)) {
                    return true;
                }
            }
            DetailAST castExpr = content.getLastChild();
            return castExpr != null && isThrowableExpression(castExpr);
        }
        if (content.getType() == TokenTypes.METHOD_CALL) {
            return true;
        }
        if (content.getType() == TokenTypes.IDENT || content.getType() == TokenTypes.DOT) {
            return isThrowableExpression(content);
        }
        return false;
    }

    private boolean isThrowableMessageCall(DetailAST expr) {
        DetailAST content = astSupport().unwrapExpr(expr);
        if (content == null || content.getType() != TokenTypes.METHOD_CALL) {
            return false;
        }
        String methodName = astSupport().extractMethodName(content);
        if (!"getMessage".equals(methodName)) {
            return false;
        }
        DetailAST dot = content.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }
        String qualifier = astSupport().extractQualifierIdent(dot);
        if (qualifier == null || qualifier.isEmpty()) {
            return false;
        }
        String typeName = context().getVariableType(qualifier);
        return isThrowableTypeName(typeName);
    }

    private boolean isThrowableTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
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
}
