/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Extracts message candidates and missing-message signals from annotations.
 */
public final class AnnotationMessageExtractor extends AbstractMessageExtractor {

    /**
     * Create an extractor for annotation messages.
     *
     * @param context extraction context with imports and type information.
     * @param sink    sink to receive message candidates.
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
        String annotationName = extractAnnotationName(annotationAst);
        if (annotationName == null) {
            // Annotation name could not be extracted (unusual AST structure).
            // This was previously an NPE; now explicitly flagged as unhandled.
            emitUnhandled(annotationAst, "Could not extract annotation name from AST");
            return;
        }
        context().recordMethodAnnotation(annotationName, annotationAst.getLineNo());
        String fullName = resolveAnnotationFullName(annotationAst, annotationName);
        if (context().isJUnit4TestAnnotation(annotationName, fullName)) {
            context().recordJUnit4AnnotationUsage(annotationName, annotationAst.getLineNo());
        }

        // Track JUnit 5 test annotations (not JUnit 4)
        if (context().isJUnit5TestAnnotation(annotationName)) {
            context().markCurrentMethodAsTest();
        }

        // Track @DisplayName presence
        if (annotationName.equals("DisplayName")) {
            if (context().currentMethodName() == null) {
                context().markCurrentClassHasDisplayName();
            } else {
                context().markCurrentMethodHasDisplayName();
            }
            checkAnnotationValue(annotationAst, annotationName, "value");
        } else if (annotationName.equals("Disabled") || annotationName.equals("Ignore")) {
            boolean hasValue = checkAnnotationValue(annotationAst, annotationName, "value");
            if (!hasValue) {
                sink().emitMissingMessage(annotationAst.getLineNo(), MessageSource.ANNOTATION,
                        AdviceSource.ANNOTATION_DISABLED, null);
            }
        }

        if (annotationName.equals("ParameterizedTest")
                || annotationName.equals("RepeatedTest")) {
            checkAnnotationValue(annotationAst, annotationName, "name");
        }

        checkAnnotationValue(annotationAst, annotationName, "disabledReason");
    }

    private boolean checkAnnotationValue(DetailAST annotationAst, String annotationName,
                                         String attributeName) {
        DetailAST expr = findAnnotationValue(annotationAst, attributeName);
        if (expr == null) {
            return false;
        }
        MessageTemplateExtractor templateExtractor = context().templateExtractor();
        String message = templateExtractor.extractStringLiteral(expr);
        if (message == null) {
            emitUnhandled(expr, "Annotation attribute " + attributeName + " is not a string literal");
            return true;
        }
        int placeholderCount = templateExtractor.countAnnotationPlaceholders(message);
        int keyValueLabelCount = templateExtractor.countKeyValueLabels(message);
        AdviceSource adviceSource = resolveAdviceSource(annotationName, attributeName);
        String messageExpr = MessageExpressionRenderer.render(expr, astSupport());
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ANNOTATION)
                .adviceSource(adviceSource)
                .lineNo(expr.getLineNo())
                .message(message)
                .messageExpr(messageExpr)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .placeholderCount(placeholderCount)
                .keyValueLabelCount(keyValueLabelCount)
                .constantMessage(true)
                .build();
        sink().emitCandidate(candidate);
        return true;
    }

    private AdviceSource resolveAdviceSource(String annotationName, String attributeName) {
        if ("DisplayName".equals(annotationName)) {
            return AdviceSource.ANNOTATION_DISPLAY_NAME;
        }
        if ("Disabled".equals(annotationName) || "Ignore".equals(annotationName)) {
            return AdviceSource.ANNOTATION_DISABLED;
        }
        if ("disabledReason".equals(attributeName)) {
            return AdviceSource.ANNOTATION_DISABLED;
        }
        if ("name".equals(attributeName)
                && ("ParameterizedTest".equals(annotationName)
                || "RepeatedTest".equals(annotationName))) {
            return AdviceSource.ANNOTATION_DISPLAY_NAME;
        }
        // Fallthrough: treat unrecognised annotation/attribute combinations as display names.
        // New annotation kinds with distinct semantics should add explicit cases above.
        return AdviceSource.ANNOTATION_DISPLAY_NAME;
    }

    String extractAnnotationName(DetailAST annotationAst) {
        DetailAST ident = annotationAst.findFirstToken(TokenTypes.IDENT);
        if (ident == null) {
            DetailAST dot = annotationAst.findFirstToken(TokenTypes.DOT);
            if (dot != null) {
                ident = astSupport().findRightmostIdent(dot);
            }
        }
        return ident != null ? ident.getText() : null;
    }

    private String resolveAnnotationFullName(DetailAST annotationAst, String annotationName) {
        String imported = annotationName == null ? null : context().importedClass(annotationName);
        if (imported != null && !imported.isEmpty()) {
            return imported;
        }
        DetailAST dot = annotationAst.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return astSupport().flattenDot(dot);
        }
        return null;
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
