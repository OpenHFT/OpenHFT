/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Objects;

/**
 * Extracts message candidates and missing-message signals from annotations.
 */
public final class AnnotationMessageExtractor extends AbstractMessageExtractor {

    /**
     * Create an extractor for annotation messages.
     *
     * @param context extraction context with imports and type information.
     * @param sink sink to receive message candidates.
     */
    public AnnotationMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
    }

    /**
     * Inspect an annotation and emit candidates or missing-message markers.
     *
     * @param annotationAst annotation AST node.
     */
    public void handleAnnotation(DetailAST annotationAst) {
        String annotationName = Objects.requireNonNull(extractAnnotationName(annotationAst));

        if (annotationName.equals("DisplayName")
                || annotationName.equals("Disabled")
                || annotationName.equals("Ignore")) {
            boolean hasValue = checkAnnotationValue(annotationAst, "value");
            if (!hasValue
                    && (annotationName.equals("Disabled") || annotationName.equals("Ignore"))) {
                if (!context().hasInlineReasonComment(annotationAst)) {
                    sink().emitMissingMessage(annotationAst.getLineNo(), MessageSource.ANNOTATION);
                }
            }
        }

        if (annotationName.equals("ParameterizedTest")
                || annotationName.equals("RepeatedTest")) {
            checkAnnotationValue(annotationAst, "name");
        }

        checkAnnotationValue(annotationAst, "disabledReason");
    }

    private boolean checkAnnotationValue(DetailAST annotationAst, String attributeName) {
        DetailAST expr = findAnnotationValue(annotationAst, attributeName);
        if (expr == null) {
            return false;
        }
        MessageTemplateExtractor templateExtractor = context().templateExtractor();
        String message = templateExtractor.extractStringLiteral(expr);
        if (message == null) {
            return true;
        }
        int placeholderCount = templateExtractor.countAnnotationPlaceholders(message);
        int keyValueLabelCount = templateExtractor.countKeyValueLabels(message);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ANNOTATION)
                .lineNo(expr.getLineNo())
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .placeholderCount(placeholderCount)
                .keyValueLabelCount(keyValueLabelCount)
                .build();
        sink().emitCandidate(candidate);
        return true;
    }

    private String extractAnnotationName(DetailAST annotationAst) {
        DetailAST ident = annotationAst.findFirstToken(TokenTypes.IDENT);
        if (ident == null) {
            DetailAST dot = annotationAst.findFirstToken(TokenTypes.DOT);
            if (dot != null) {
                ident = astSupport().findRightmostIdent(dot);
            }
        }
        return ident != null ? ident.getText() : null;
    }

    private DetailAST findAnnotationValue(DetailAST annotationAst, String attributeName) {
        DetailAST child = annotationAst.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                DetailAST ident = child.findFirstToken(TokenTypes.IDENT);
                if (ident != null && attributeName.equals(ident.getText())) {
                    DetailAST expr = child.findFirstToken(TokenTypes.EXPR);
                    if (expr != null) {
                        return expr;
                    }
                }
            } else if (child.getType() == TokenTypes.EXPR && "value".equals(attributeName)) {
                return child;
            }
            child = child.getNextSibling();
        }
        return null;
    }
}
