/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssertionMessageExtractor}.
 */
@DisplayName("Assertion message extractor tests scenario case")
class AssertionMessageExtractorTest {

    private AssertionMessageExtractor extractor;
    private TestMessageSink sink;
    private MessageExtractionContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(createFileContents("InputAssert.java", 5));
        sink = new TestMessageSink();
        extractor = new AssertionMessageExtractor(context, sink);
    }

    @Test
    @DisplayName("AssertThrows with JUnit4 signature should extract first message")
    void assertThrowsWithJUnit4SignatureExtractsMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should reject overflow")));
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambda());

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Should reject overflow", sink.candidates.get(0).message(),
                "Should use JUnit4 assertThrows message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with two arguments should emit missing message instead of unhandled")
    void assertEqualsWithTwoArgsEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("expected")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with message variable extracts template")
    void assertEqualsWithMessageVariableExtractsTemplate() {
        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("dump: "),
                createMethodCall("build", "dump")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("message")));
        elist.addChild(createExpr(createStringLiteral("text")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("dump: {}", sink.candidates.get(0).message(), "Should use template from message variable");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with JUnit4 static import uses message variable template")
    void assertEqualsWithJUnit4StaticImportUsesMessageVariableTemplate() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.assertEquals", "org.junit.Assert", true);

        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("dump: "),
                createIdent("actual")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("message")));
        elist.addChild(createExpr(createStringLiteral("text")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("dump: {}", sink.candidates.get(0).message(),
                "Should use JUnit4 message variable template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertNotNull with JUnit4 message position ignores trailing string argument")
    void assertNotNullWithJUnit4MessagePositionIgnoresTrailingStringArg() {
        context.recordStaticJUnitImportForTesting("org.junit.Assert.assertNotNull", "org.junit.Assert", true);
        context.recordVariableType(createVariableDef("actual", "String"));

        DetailAstImpl methodCall = createMethodCall("assertNotNull");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createMethodCall("message")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for non-constant message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with JUnit5 static import uses message variable template")
    void assertEqualsWithJUnit5StaticImportUsesMessageVariableTemplate() {
        context.recordStaticJUnitImportForTesting("org.junit.jupiter.api.Assertions.assertEquals",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("snapshot: "),
                createIdent("actual")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("text")));
        elist.addChild(createExpr(createIdent("actual")));
        elist.addChild(createExpr(createIdent("message")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("snapshot: {}", sink.candidates.get(0).message(),
                "Should use JUnit5 message variable template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThat with two arguments emits missing message")
    void assertThatWithTwoArgsEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThat");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("actual")));
        elist.addChild(createExpr(createIdent("matcher")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Custom assert method with data strings does not emit unhandled warning")
    void assertCustomMethodWithDataStringsDoesNotEmitUnhandled() {
        DetailAstImpl methodCall = createMethodCall("assertOtherFieldsUnchanged");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("original")));
        elist.addChild(createExpr(createStringLiteral("resend")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for custom assert");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for custom assert");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for custom assert");
    }

    @Test
    @DisplayName("Custom assert with single argument emits missing message")
    void assertBalancedBracketsWithOneArgEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertBalancedBrackets");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("payload")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Precondition message-first extracts template")
    void preconditionMessageFirstExtractsTemplate() {
        DetailAstImpl methodCall = createMethodCall("required");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Required value must be set")));
        elist.addChild(createExpr(createIdent("value")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Required value must be set", sink.candidates.get(0).message(),
                "Should use message-first precondition template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    private DetailAstImpl createMethodCall(String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl ident = createIdent(name);
        methodCall.addChild(ident);
        return methodCall;
    }

    private DetailAstImpl createMethodCall(String qualifier, String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(qualifier));
        dot.addChild(createIdent(name));
        methodCall.addChild(dot);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createElist(DetailAstImpl methodCall) {
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return elist;
    }

    private DetailAstImpl createExpr(DetailAstImpl child) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(child);
        return expr;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private DetailAstImpl createStringLiteral(String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + value + "\"");
        return literal;
    }

    private DetailAstImpl createClassLiteral(String className) {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(className));
        dot.addChild(createIdent("class"));
        return dot;
    }

    private DetailAstImpl createLambda() {
        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        return lambda;
    }

    private DetailAstImpl createVariableDefWithInitializer(String name, String typeName, DetailAstImpl initializer) {
        DetailAstImpl varDef = createVariableDef(name, typeName);
        DetailAstImpl assign = new DetailAstImpl();
        assign.setType(TokenTypes.ASSIGN);
        assign.addChild(createIdent(name));
        assign.addChild(initializer);
        varDef.addChild(assign);
        return varDef;
    }

    private DetailAstImpl createVariableDef(String name, String typeName) {
        DetailAstImpl varDef = new DetailAstImpl();
        varDef.initialize(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF");

        DetailAstImpl type = new DetailAstImpl();
        type.initialize(TokenTypes.TYPE, "TYPE");
        DetailAstImpl typeIdent = new DetailAstImpl();
        typeIdent.initialize(TokenTypes.IDENT, typeName);
        type.addChild(typeIdent);

        DetailAstImpl ident = new DetailAstImpl();
        ident.initialize(TokenTypes.IDENT, name);

        varDef.addChild(type);
        varDef.addChild(ident);
        return varDef;
    }

    private DetailAstImpl createPlusExpression(DetailAstImpl left, DetailAstImpl right) {
        DetailAstImpl plus = new DetailAstImpl();
        plus.setType(TokenTypes.PLUS);
        plus.addChild(left);
        plus.addChild(right);
        return plus;
    }

    private FileContents createFileContents(String fileName, int lineCount) throws IOException {
        List<String> lines = Collections.nCopies(lineCount, "");
        Path file = tempDir.resolve(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        return new FileContents(text);
    }

    private static final class TestMessageSink implements MessageCandidateSink {
        private final List<MessageCandidate> candidates = new ArrayList<>();
        private final List<MessageSource> missingMessages = new ArrayList<>();
        private final List<String> unhandledReasons = new ArrayList<>();

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
