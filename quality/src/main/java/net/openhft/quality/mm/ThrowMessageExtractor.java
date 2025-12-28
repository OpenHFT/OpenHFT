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
     * @param sink sink to receive message candidates.
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
            return;
        }
        DetailAST literalNew = expr.findFirstToken(TokenTypes.LITERAL_NEW);
        if (literalNew == null) {
            return;
        }
        String exceptionClassName = astSupport().extractNewClassName(literalNew);
        if (context().isIgnoredExceptionClass(exceptionClassName)) {
            return;
        }
        DetailAST elist = literalNew.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return;
        }
        List<DetailAST> args = astSupport().collectArguments(elist);
        boolean hasMessageArg = false;
        for (DetailAST arg : args) {
            if (astSupport().isNullLiteral(arg)) {
                continue;
            }
            if (!isThrowableExpression(arg)) {
                hasMessageArg = true;
                break;
            }
        }
        if (!hasMessageArg) {
            if (!context().hasInlineReasonComment(literalNew)) {
                sink().emitMissingMessage(throwAst.getLineNo(), MessageSource.THROW);
            }
            return;
        }
        MessageTemplateExtractor templateExtractor = context().templateExtractor();
        DetailAST messageExpr = astSupport().findFirstStringArgument(elist, templateExtractor);
        if (messageExpr == null) {
            return;
        }
        MessageTemplate template = templateExtractor.extractMessageTemplate(messageExpr);
        if (template == null) {
            return;
        }
        int keyValueLabelCount = templateExtractor.countKeyValueLabels(template.message());
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
        return false;
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
