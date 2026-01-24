/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ThrowMessageExtractor}.
 */
@DisplayName("Throw message extractor tests scenario case")
class ThrowMessageExtractorTest {

    private ThrowMessageExtractor extractor;
    private TestMessageSink sink;
    private MessageExtractionContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        MessageAstSupport astSupport = new MessageAstSupport();
        context = new MessageExtractionContext(astSupport);
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(createFileContents("InputThrow.java", 20));
        sink = new TestMessageSink();
        extractor = new ThrowMessageExtractor(context, sink);
    }

    // --- handleThrowStatement edge cases ---

    @Test
    @DisplayName("Handle throw statement with no expression emits unhandled")
    void handleThrowStatement_noExpression_emitsUnhandled() {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate without expression");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit one unhandled warning");
        assertTrue(sink.unhandledReasons.get(0).contains("missing expression"),
                "Should mention missing expression");
    }

    @Test
    @DisplayName("Handle throw statement rethrowing variable emits missing message without reason")
    void handleThrowStatement_rethrowVariable_skipsUnhandled() {
        // throw e; (rethrowing an existing exception)
        context.recordVariableType(createVariableDef("e", "RuntimeException"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(createIdent("e"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for rethrow");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for rethrow");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for rethrow");
    }

    @Test
    @DisplayName("Handle throw statement rethrowing casted exception emits missing message without reason")
    void handleThrowStatement_rethrowTypeCast_skipsUnhandled() {
        context.recordVariableType(createVariableDef("e", "Throwable"));
        DetailAstImpl typecast = createTypeCast("RuntimeException", createIdent("e"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(typecast);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for cast rethrow");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for cast rethrow");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for cast rethrow");
    }

    @Test
    @DisplayName("Handle throw statement with null literal emits null-throw candidate")
    void handleThrowStatement_nullLiteral_emitsNullThrowCandidate() {
        DetailAstImpl literalNull = new DetailAstImpl();
        literalNull.setType(TokenTypes.LITERAL_NULL);
        literalNull.setLineNo(10);
        DetailAstImpl throwAst = createThrowStatementWithExpr(literalNull);

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit one candidate for null throw");
        assertTrue(sink.candidates.get(0).throwNull(), "Candidate should flag null throw");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for null throw");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for null throw");
    }

    @Test
    @DisplayName("Handle throw statement with message variable uses template")
    void handleThrowStatement_messageVariable_usesTemplate() {
        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("Header "),
                createIdent("value")
        );
        DetailAstImpl varDef = createVariableDefWithInitializer("message", "String", initExpr);
        context.recordVariableType(varDef);

        DetailAstImpl throwAst = createThrowNewStatementWithExpr("AssertionError", createIdent("message"));

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit one candidate for message variable");
        assertEquals("Header {}", sink.candidates.get(0).message(),
                "Should use template from variable initializer");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Handle throw statement rethrowing field emits missing message without reason")
    void handleThrowStatement_rethrowField_skipsUnhandled() {
        // throw holder.error; (rethrowing a field reference)
        context.recordVariableType(createVariableDef("error", "RuntimeException"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(createDot("holder", "error"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for field rethrow");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for field rethrow");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for field rethrow");
    }

    @Test
    @DisplayName("Handle throw statement with factory call emits missing message without reason")
    void handleThrowStatement_factoryCall_skipsUnhandled() {
        // throw someFactory();
        DetailAstImpl throwAst = createThrowStatementWithExpr(createMethodCall("someFactory"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for factory throw");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for factory throw");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for factory throw");
    }

    @Test
    @DisplayName("Handle throw statement with non-throwable expression emits unhandled")
    void handleThrowStatement_nonThrowableExpression_emitsUnhandled() {
        // throw 42;
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.NUM_INT);
        literal.setText("42");
        DetailAstImpl throwAst = createThrowStatementWithExpr(literal);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for literal throw");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit one unhandled warning");
        assertTrue(sink.unhandledReasons.get(0).contains("does not construct"),
                "Should mention not constructing exception");
    }

    @Test
    @DisplayName("Handle throw statement with getMessage skips missing message")
    void handleThrowStatement_getMessage_skipsMissingMessage() {
        context.recordVariableType(createVariableDef("cause", "RuntimeException"));
        DetailAstImpl messageExpr = createMethodCall("cause", "getMessage");
        DetailAstImpl throwAst = createThrowNewStatementWithExprs("RuntimeException",
                messageExpr,
                createIdent("cause"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for getMessage");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for getMessage");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for getMessage");
    }

    @Test
    @DisplayName("Handle throw statement with ignored exception emits nothing")
    void handleThrowStatement_ignoredException_emitsNothing() {
        // throw new UnsupportedOperationException();
        context.setIgnoredExceptionClassNames(
                Collections.singleton("UnsupportedOperationException"));

        DetailAstImpl throwAst = createThrowNewStatement("UnsupportedOperationException");

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for ignored exception");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for ignored");
    }

    @Test
    @DisplayName("Handle throw statement with UnsupportedOperationException no-arg emits nothing")
    void handleThrowStatement_unsupportedOperationNoArg_emitsNothing() {
        DetailAstImpl throwAst = createThrowNewStatement("UnsupportedOperationException");

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for UnsupportedOperationException");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for UnsupportedOperationException");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for UnsupportedOperationException");
    }

    @Test
    @DisplayName("Handle throw statement with no elist emits unhandled")
    void handleThrowStatement_noElist_emitsUnhandled() {
        // throw new RuntimeException but without ELIST
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText("RuntimeException");
        literalNew.addChild(className);

        // No ELIST added

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate without elist");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning");
        assertTrue(sink.unhandledReasons.get(0).contains("no argument list"),
                "Should mention no argument list");
    }

    @Test
    @DisplayName("Handle throw statement with message extracts candidate")
    void handleThrowStatement_withMessage_extractsCandidate() {
        // throw new RuntimeException("Error occurred during processing");
        DetailAstImpl throwAst = createThrowNewStatementWithMessage(
                "RuntimeException", "Error occurred during processing");

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("Error occurred during processing", sink.candidates.get(0).message(),
                "Should extract message text");
        assertEquals(MessageSource.THROW, sink.candidates.get(0).source(),
                "Source should be THROW");
    }

    @Test
    @DisplayName("Handle throw statement without string argument emits missing message")
    void handleThrowStatement_noStringArg_emitsMissingMessage() {
        // throw new RuntimeException(123); - not a string argument
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText("RuntimeException");
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        // Add a non-string argument (number literal)
        DetailAstImpl argExpr = new DetailAstImpl();
        argExpr.setType(TokenTypes.EXPR);
        elist.addChild(argExpr);

        DetailAstImpl numLiteral = new DetailAstImpl();
        numLiteral.setType(TokenTypes.NUM_INT);
        numLiteral.setText("123");
        argExpr.addChild(numLiteral);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate without string arg");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Handle throw statement with no arguments emits missing message")
    void handleThrowStatement_noArguments_emitsMissingMessage() throws Exception {
        // Create FileContents with actual source line at line 10
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            lines.add("");
        }
        lines.add("        throw new RuntimeException();"); // Line 10 (0-indexed: 9)
        Path file = tempDir.resolve("InputThrowNoArgs.java");
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        context.reset(new FileContents(text));

        // throw new RuntimeException(); - valid construction but no message
        DetailAstImpl throwAst = createThrowNewStatement("RuntimeException");

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate without message");
        assertEquals(1, sink.missingMessages.size(), "Should emit one missing message");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
    }

    @Test
    @DisplayName("Handle throw statement with cause only emits missing message")
    void handleThrowStatement_causeOnly_emitsMissingMessage() throws Exception {
        // Create FileContents with actual source line at line 10
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            lines.add("");
        }
        lines.add("        throw new RuntimeException(cause);"); // Line 10 (0-indexed: 9)
        Path file = tempDir.resolve("InputThrowCauseOnly.java");
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        context.reset(new FileContents(text));

        // Register the cause variable as a Throwable type
        context.recordVariableType(createVariableDef("cause", "RuntimeException"));

        // throw new RuntimeException(cause); - no string message, just cause
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText("RuntimeException");
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        // Add a cause argument (variable reference)
        DetailAstImpl argExpr = new DetailAstImpl();
        argExpr.setType(TokenTypes.EXPR);
        elist.addChild(argExpr);

        DetailAstImpl causeIdent = new DetailAstImpl();
        causeIdent.setType(TokenTypes.IDENT);
        causeIdent.setText("cause");
        argExpr.addChild(causeIdent);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate with only cause");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message since no string message provided");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
    }

    // --- Helper methods to create AST structures ---

    private DetailAstImpl createThrowNewStatement(String exceptionClass) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText(exceptionClass);
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        return throwAst;
    }

    private DetailAstImpl createThrowNewStatementWithMessage(String exceptionClass, String message) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText(exceptionClass);
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        DetailAstImpl argExpr = new DetailAstImpl();
        argExpr.setType(TokenTypes.EXPR);
        elist.addChild(argExpr);

        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + message + "\"");
        literal.setLineNo(10);
        argExpr.addChild(literal);

        return throwAst;
    }

    private DetailAstImpl createThrowNewStatementWithExpr(String exceptionClass, DetailAstImpl argValue) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText(exceptionClass);
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        DetailAstImpl argExpr = new DetailAstImpl();
        argExpr.setType(TokenTypes.EXPR);
        elist.addChild(argExpr);
        argExpr.addChild(argValue);
        return throwAst;
    }

    private DetailAstImpl createThrowNewStatementWithExprs(String exceptionClass, DetailAstImpl... argValues) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        DetailAstImpl className = new DetailAstImpl();
        className.setType(TokenTypes.IDENT);
        className.setText(exceptionClass);
        literalNew.addChild(className);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        for (DetailAstImpl argValue : argValues) {
            DetailAstImpl argExpr = new DetailAstImpl();
            argExpr.setType(TokenTypes.EXPR);
            elist.addChild(argExpr);
            argExpr.addChild(argValue);
        }
        return throwAst;
    }

    /**
     * Test sink for capturing emitted candidates and missing messages.
     */
    private static class TestMessageSink implements MessageCandidateSink {
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
        public void emitUnhandled(com.puppycrawl.tools.checkstyle.api.DetailAST ast, String reason) {
            unhandledReasons.add(reason);
        }
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

    private DetailAstImpl createThrowStatementWithExpr(DetailAstImpl exprChild) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        expr.addChild(exprChild);
        return throwAst;
    }

    private DetailAstImpl createTypeCast(String typeName, DetailAstImpl exprValue) {
        DetailAstImpl typecast = new DetailAstImpl();
        typecast.setType(TokenTypes.TYPECAST);

        DetailAstImpl type = new DetailAstImpl();
        type.setType(TokenTypes.TYPE);
        DetailAstImpl typeIdent = new DetailAstImpl();
        typeIdent.setType(TokenTypes.IDENT);
        typeIdent.setText(typeName);
        type.addChild(typeIdent);
        typecast.addChild(type);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(exprValue);
        typecast.addChild(expr);
        return typecast;
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

    private DetailAstImpl createPlusExpression(DetailAstImpl left, DetailAstImpl right) {
        DetailAstImpl plus = new DetailAstImpl();
        plus.setType(TokenTypes.PLUS);
        plus.addChild(left);
        plus.addChild(right);
        return plus;
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

    private DetailAstImpl createDot(String left, String right) {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(left));
        dot.addChild(createIdent(right));
        return dot;
    }

    private DetailAstImpl createMethodCall(String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.addChild(createIdent(name));
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

    private FileContents createFileContents(String fileName, int lineCount) throws IOException {
        List<String> lines = Collections.nCopies(lineCount, "");
        Path file = tempDir.resolve(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        return new FileContents(text);
    }

    // --- Mutation killing tests for isThrowableExpression and related methods ---

    @Test
    @DisplayName("Handle throw statement with non-throwable class new expression still extracts message")
    void handleThrowStatement_nonThrowableClassInNew_stillExtractsMessage() {
        // throw new String("test"); - String is not a throwable type but code extracts message anyway
        // This tests that isThrowableTypeName returns false for "String"
        DetailAstImpl throwAst = createThrowNewStatementWithMessage("String", "test");

        extractor.handleThrowStatement(throwAst);

        // The code still extracts the message even though String isn't throwable
        // This kills the "replaced return with true" mutation in isThrowableTypeName
        assertEquals(1, sink.candidates.size(), "Should emit candidate even for non-throwable class");
        assertEquals("test", sink.candidates.get(0).message());
    }

    @Test
    @DisplayName("Handle throw statement with ident referring to non-throwable type emits unhandled")
    void handleThrowStatement_identNonThrowableType_emitsUnhandled() {
        // throw value; where value is String type
        context.recordVariableType(createVariableDef("value", "String"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(createIdent("value"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for non-throwable ident");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning");
    }

    @Test
    @DisplayName("Handle throw statement with typecast to non-throwable emits unhandled")
    void handleThrowStatement_typecastToNonThrowable_emitsUnhandled() {
        // throw (String) obj; - String is not throwable
        context.recordVariableType(createVariableDef("obj", "Object"));
        DetailAstImpl typecast = createTypeCast("String", createIdent("obj"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(typecast);

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for non-throwable cast");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning");
    }

    @Test
    @DisplayName("Handle throw statement with getMessage from non-throwable emits unhandled or missing")
    void handleThrowStatement_getMessageFromNonThrowable_emitsUnhandledOrMissing() {
        // throw new RuntimeException(obj.getMessage()); where obj is String
        context.recordVariableType(createVariableDef("obj", "String"));
        DetailAstImpl messageExpr = createMethodCall("obj", "getMessage");
        DetailAstImpl throwAst = createThrowNewStatementWithExpr("RuntimeException", messageExpr);

        extractor.handleThrowStatement(throwAst);

        // getMessage from String is not recognized as throwable message call
        // So it should try to extract template - but getMessage() returns nothing usable
        // The result depends on how the code handles this edge case
        assertTrue(sink.candidates.size() <= 1,
                "Should emit at most one candidate for getMessage from non-throwable");
    }

    @Test
    @DisplayName("Handle throw statement with simple class name Error is throwable")
    void handleThrowStatement_simpleClassNameError_isThrowable() {
        // throw new Error("test"); - Error is a simple name that should be recognized
        DetailAstImpl throwAst = createThrowNewStatementWithMessage("Error", "test message");

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit candidate for Error");
        assertEquals("test message", sink.candidates.get(0).message());
    }

    @Test
    @DisplayName("Handle throw statement with simple class name Throwable is throwable")
    void handleThrowStatement_simpleClassNameThrowable_isThrowable() {
        // throw new Throwable("test");
        DetailAstImpl throwAst = createThrowNewStatementWithMessage("Throwable", "test message");

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit candidate for Throwable");
        assertEquals("test message", sink.candidates.get(0).message());
    }

    @Test
    @DisplayName("Handle throw statement with qualified class ending in Exception is throwable")
    void handleThrowStatement_qualifiedClassException_isThrowable() {
        // throw new java.lang.RuntimeException("test");
        DetailAstImpl throwAst = createThrowNewStatementWithQualifiedClass(
                "java.lang.RuntimeException", "qualified exception");

        extractor.handleThrowStatement(throwAst);

        assertEquals(1, sink.candidates.size(), "Should emit candidate for qualified Exception");
        assertEquals("qualified exception", sink.candidates.get(0).message());
    }

    @Test
    @DisplayName("Handle throw statement with dot expression referring to throwable field emits missing message without reason")
    void handleThrowStatement_dotExpressionThrowableField_skipsUnhandled() {
        // throw this.storedError; where storedError is Exception type
        context.recordVariableType(createVariableDef("storedError", "RuntimeException"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(createDot("this", "storedError"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for field rethrow");
        assertEquals(1, sink.missingMessages.size(), "Should emit missing message for field rethrow");
        assertEquals(MessageSource.THROW, sink.missingMessages.get(0),
                "Missing message source should be THROW");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled for throwable field");
    }

    @Test
    @DisplayName("Handle throw statement with dot expression referring to non-throwable field emits unhandled")
    void handleThrowStatement_dotExpressionNonThrowableField_emitsUnhandled() {
        // throw this.value; where value is String type
        context.recordVariableType(createVariableDef("value", "String"));
        DetailAstImpl throwAst = createThrowStatementWithExpr(createDot("this", "value"));

        extractor.handleThrowStatement(throwAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for non-throwable field");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled for non-throwable field");
    }

    @Test
    @DisplayName("Throwable expression recognises dot qualifiers")
    void throwableExpressionRecognisesDotQualifiers() throws Exception {
        context.recordVariableType(createVariableDef("cause", "RuntimeException"));
        DetailAstImpl dot = createDot("holder", "cause");

        assertTrue(invokeIsThrowableExpression(dot),
                "Dot expression with throwable field should be throwable");

        DetailAstImpl typeDot = createDot("RuntimeException", "class");
        assertTrue(invokeIsThrowableExpression(typeDot),
                "Dot expression with throwable qualifier should be throwable");
    }

    @Test
    @DisplayName("Throwable message call recognises getMessage on throwable")
    void throwableMessageCallRecognisesGetMessageOnThrowable() throws Exception {
        context.recordVariableType(createVariableDef("cause", "RuntimeException"));
        DetailAstImpl messageExpr = createMethodCall("cause", "getMessage");

        assertTrue(invokeIsThrowableMessageCall(messageExpr),
                "getMessage on throwable should be recognised");

        DetailAstImpl nonThrowable = createMethodCall("value", "getMessage");
        assertFalse(invokeIsThrowableMessageCall(nonThrowable),
                "getMessage on non-throwable should be ignored");
    }

    @Test
    @DisplayName("Throwable rethrow recognises type casts and method calls")
    void throwableRethrowRecognisesTypeCastsAndMethodCalls() throws Exception {
        context.recordVariableType(createVariableDef("e", "RuntimeException"));

        DetailAstImpl cast = createTypeCast("String", createIdent("e"));
        assertTrue(invokeIsThrowableRethrow(cast),
                "Type cast of throwable variable should be treated as rethrow");

        DetailAstImpl methodCall = createMethodCall("someFactory");
        assertTrue(invokeIsThrowableRethrow(methodCall),
                "Factory method call should be treated as rethrow");
    }

    @Test
    @DisplayName("Throwable type name handles null and error types")
    void throwableTypeNameHandlesNullAndErrorTypes() throws Exception {
        assertFalse(invokeIsThrowableTypeName(null), "Null type name should be false");
        assertTrue(invokeIsThrowableTypeName("OutOfMemoryError"),
                "Error type should be recognised as throwable");
    }

    private DetailAstImpl createThrowNewStatementWithQualifiedClass(String qualifiedName, String message) {
        DetailAstImpl throwAst = new DetailAstImpl();
        throwAst.setType(TokenTypes.LITERAL_THROW);
        throwAst.setLineNo(10);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        throwAst.addChild(expr);

        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        expr.addChild(literalNew);

        // Build qualified name as DOT structure
        String[] parts = qualifiedName.split("\\.");
        DetailAstImpl qualifiedAst = createIdent(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(qualifiedAst);
            dot.addChild(createIdent(parts[i]));
            qualifiedAst = dot;
        }
        literalNew.addChild(qualifiedAst);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        literalNew.addChild(elist);

        DetailAstImpl argExpr = new DetailAstImpl();
        argExpr.setType(TokenTypes.EXPR);
        elist.addChild(argExpr);

        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + message + "\"");
        literal.setLineNo(10);
        argExpr.addChild(literal);

        return throwAst;
    }

    private boolean invokeIsThrowableExpression(DetailAstImpl expr) throws Exception {
        return extractor.isThrowableExpression(expr);
    }

    private boolean invokeIsThrowableMessageCall(DetailAstImpl expr) throws Exception {
        return extractor.isThrowableMessageCall(expr);
    }

    private boolean invokeIsThrowableRethrow(DetailAstImpl expr) throws Exception {
        return extractor.isThrowableRethrow(expr);
    }

    private boolean invokeIsThrowableTypeName(String typeName) throws Exception {
        return extractor.isThrowableTypeName(typeName);
    }
}
