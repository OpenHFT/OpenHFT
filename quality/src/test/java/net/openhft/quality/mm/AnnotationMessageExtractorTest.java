/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Annotation message extractor tests scenario case")
class AnnotationMessageExtractorTest {

    private MessageExtractionContext context;
    private TestSink sink;
    private AnnotationMessageExtractor extractor;

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        sink = new TestSink();
        extractor = new AnnotationMessageExtractor(context, sink);
    }

    @Test
    @DisplayName("Handle annotation emits unhandled when name missing")
    void handleAnnotationEmitsUnhandledWhenNameMissing() {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);

        extractor.handleAnnotation(annotation);

        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled reason");
        assertTrue(sink.unhandledReasons.get(0).contains("Could not extract annotation name"),
                "Unhandled reason should explain missing name");
    }

    @Test
    @DisplayName("Handle display name annotation emits candidate")
    void handleDisplayNameAnnotationEmitsCandidate() {
        DetailAstImpl annotation = createAnnotation("DisplayName", createStringExpr("Unit test name"));

        extractor.handleAnnotation(annotation);

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("Unit test name", sink.candidates.get(0).message());
        assertEquals(MessageSource.ANNOTATION, sink.candidates.get(0).source());
    }

    @Test
    @DisplayName("Handle disabled annotation without value emits missing message")
    void handleDisabledAnnotationWithoutValueEmitsMissingMessage() {
        DetailAstImpl annotation = createAnnotation("Disabled", null);

        extractor.handleAnnotation(annotation);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.ANNOTATION, sink.missingMessages.get(0));
    }

    @Test
    @DisplayName("Handle annotation value non string emits unhandled")
    void handleAnnotationValueNonStringEmitsUnhandled() {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.NUM_INT);
        literal.setText("123");
        expr.addChild(literal);
        DetailAstImpl annotation = createAnnotation("DisplayName", expr);

        extractor.handleAnnotation(annotation);

        assertTrue(sink.unhandledReasons.stream()
                        .anyMatch(reason -> reason.contains("Annotation attribute value")),
                "Non-string annotation should emit unhandled reason");
    }

    @Test
    @DisplayName("Extract annotation name uses rightmost ident for dot")
    void extractAnnotationNameUsesRightmostIdentForDot() throws Exception {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        annotation.addChild(createDotChain("org", "junit", "DisplayName"));

        String name = invokeExtractAnnotationName(annotation);

        assertEquals("DisplayName", name);
    }

    private DetailAstImpl createAnnotation(String name, DetailAstImpl valueExpr) {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        annotation.addChild(createIdent(name));
        if (valueExpr != null) {
            annotation.addChild(valueExpr);
        }
        return annotation;
    }

    private DetailAstImpl createStringExpr(String value) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + value + "\"");
        expr.addChild(literal);
        return expr;
    }

    private DetailAstImpl createDotChain(String... parts) {
        DetailAstImpl current = createIdent(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            dot.addChild(createIdent(parts[i]));
            current = dot;
        }
        return current;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private String invokeExtractAnnotationName(DetailAST annotation) throws Exception {
        return extractor.extractAnnotationName(annotation);
    }

    private static final class TestSink implements MessageCandidateSink {
        final List<MessageCandidate> candidates = new ArrayList<>();
        final List<MessageSource> missingMessages = new ArrayList<>();
        final List<String> unhandledReasons = new ArrayList<>();

        @Override
        public void emitCandidate(MessageCandidate candidate) {
            candidates.add(candidate);
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source) {
            missingMessages.add(source);
        }

        @Override
        public void emitUnhandled(DetailAST ast, String reason) {
            unhandledReasons.add(reason);
        }
    }
}
