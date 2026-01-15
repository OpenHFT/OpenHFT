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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogMessageExtractor}.
 */
@DisplayName("Log message extractor tests scenario case")
public class LogMessageExtractorTest {

    private LogMessageExtractor extractor;
    private TestMessageSink sink;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        MessageExtractionContext context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(createFileContents("InputLog.java", 5));
        sink = new TestMessageSink();
        extractor = new LogMessageExtractor(context, sink);
    }

    // --- isBlankMessage tests ---

    @Test
    @DisplayName("Is blank message null scenario case")
    void isBlankMessageNull() {
        assertTrue(extractor.isBlankMessage(null));
    }

    @Test
    @DisplayName("Is blank message empty scenario case")
    void isBlankMessageEmpty() {
        assertTrue(extractor.isBlankMessage(""));
    }

    @Test
    @DisplayName("Is blank message whitespace scenario case")
    void isBlankMessageWhitespace() {
        assertTrue(extractor.isBlankMessage("   "));
    }

    @Test
    @DisplayName("Is blank message with content scenario")
    void isBlankMessageWithContent() {
        assertFalse(extractor.isBlankMessage("hello"));
    }

    // --- isThrowableTypeName tests ---

    @Test
    @DisplayName("Is throwable type name null scenario")
    void isThrowableTypeNameNull() {
        assertFalse(extractor.isThrowableTypeName(null));
    }

    @Test
    @DisplayName("Is throwable type name throwable scenario")
    void isThrowableTypeNameThrowable() {
        assertTrue(extractor.isThrowableTypeName("Throwable"));
    }

    @Test
    @DisplayName("Is throwable type name exception scenario")
    void isThrowableTypeNameException() {
        assertTrue(extractor.isThrowableTypeName("IOException"));
        assertTrue(extractor.isThrowableTypeName("RuntimeException"));
        assertTrue(extractor.isThrowableTypeName("IllegalStateException"));
    }

    @Test
    @DisplayName("Is throwable type name error scenario")
    void isThrowableTypeNameError() {
        assertTrue(extractor.isThrowableTypeName("OutOfMemoryError"));
        assertTrue(extractor.isThrowableTypeName("StackOverflowError"));
    }

    @Test
    @DisplayName("Is throwable type name stack trace")
    void isThrowableTypeNameStackTrace() {
        assertTrue(extractor.isThrowableTypeName("StackTrace"));
    }

    @Test
    @DisplayName("Is throwable type name fully qualified")
    void isThrowableTypeNameFullyQualified() {
        assertTrue(extractor.isThrowableTypeName("java.lang.RuntimeException"));
        assertTrue(extractor.isThrowableTypeName("java.io.IOException"));
    }

    @Test
    @DisplayName("Is throwable type name not throwable scenario")
    void isThrowableTypeNameNotThrowable() {
        assertFalse(extractor.isThrowableTypeName("String"));
        assertFalse(extractor.isThrowableTypeName("Integer"));
        assertFalse(extractor.isThrowableTypeName("List"));
    }

    // --- isSupplierTypeName tests ---

    @Test
    @DisplayName("Is supplier type name null scenario")
    void isSupplierTypeNameNull() {
        assertFalse(extractor.isSupplierTypeName(null));
    }

    @Test
    @DisplayName("Is supplier type name supplier scenario")
    void isSupplierTypeNameSupplier() {
        assertTrue(extractor.isSupplierTypeName("Supplier"));
    }

    @Test
    @DisplayName("Is supplier type name fully qualified")
    void isSupplierTypeNameFullyQualified() {
        assertTrue(extractor.isSupplierTypeName("java.util.function.Supplier"));
    }

    @Test
    @DisplayName("Is supplier type name ending with dot supplier")
    void isSupplierTypeNameEndingWithDotSupplier() {
        assertTrue(extractor.isSupplierTypeName("com.example.Supplier"));
        assertFalse(extractor.isSupplierTypeName("com.example.MySupplier"));
    }

    @Test
    @DisplayName("Is supplier type name not supplier scenario")
    void isSupplierTypeNameNotSupplier() {
        assertFalse(extractor.isSupplierTypeName("String"));
        assertFalse(extractor.isSupplierTypeName("Consumer"));
    }

    // --- resolveLoggerKindFromType tests ---

    @Test
    @DisplayName("Resolve logger kind from type slf 4 j")
    void resolveLoggerKindFromType_slf4j() {
        assertEquals(LogMessageExtractor.LoggerKind.SLF4J,
                extractor.resolveLoggerKindFromType("org.slf4j.Logger"));
    }

    @Test
    @DisplayName("Resolve logger kind from type log 4 j 2")
    void resolveLoggerKindFromType_log4j2() {
        assertEquals(LogMessageExtractor.LoggerKind.LOG4J2,
                extractor.resolveLoggerKindFromType("org.apache.logging.log4j.Logger"));
    }

    @Test
    @DisplayName("Resolve logger kind from type jul")
    void resolveLoggerKindFromType_jul() {
        assertEquals(LogMessageExtractor.LoggerKind.JUL,
                extractor.resolveLoggerKindFromType("java.util.logging.Logger"));
    }

    @Test
    @DisplayName("Resolve logger kind from type system logger")
    void resolveLoggerKindFromType_systemLogger() {
        assertEquals(LogMessageExtractor.LoggerKind.SYSTEM,
                extractor.resolveLoggerKindFromType("java.lang.System.Logger"));
    }

    @Test
    @DisplayName("Resolve logger kind from type system logger short")
    void resolveLoggerKindFromType_systemLoggerShort() {
        assertEquals(LogMessageExtractor.LoggerKind.SYSTEM,
                extractor.resolveLoggerKindFromType("System.Logger"));
    }

    @Test
    @DisplayName("Resolve logger kind from type unknown")
    void resolveLoggerKindFromType_unknown() {
        assertEquals(LogMessageExtractor.LoggerKind.UNKNOWN,
                extractor.resolveLoggerKindFromType("com.example.CustomLogger"));
    }

    // --- isLogMethod tests ---

    @Test
    @DisplayName("Is log method slf 4 j trace scenario")
    void isLogMethod_slf4jTrace() {
        LogMessageExtractor.LoggerKind slf4j = LogMessageExtractor.LoggerKind.SLF4J;
        assertTrue(extractor.isLogMethod(slf4j, "trace"));
        assertTrue(extractor.isLogMethod(slf4j, "debug"));
        assertTrue(extractor.isLogMethod(slf4j, "info"));
        assertTrue(extractor.isLogMethod(slf4j, "warn"));
        assertTrue(extractor.isLogMethod(slf4j, "error"));
        assertFalse(extractor.isLogMethod(slf4j, "fatal"));
    }

    @Test
    @DisplayName("Is log method log 4 j 2 fatal scenario case")
    void isLogMethod_log4j2Fatal() {
        LogMessageExtractor.LoggerKind log4j2 = LogMessageExtractor.LoggerKind.LOG4J2;
        assertTrue(extractor.isLogMethod(log4j2, "fatal"));
    }

    @Test
    @DisplayName("Is log method jul log scenario case")
    void isLogMethod_julLog() {
        LogMessageExtractor.LoggerKind jul = LogMessageExtractor.LoggerKind.JUL;
        assertTrue(extractor.isLogMethod(jul, "log"));
        assertTrue(extractor.isLogMethod(jul, "severe"));
        assertTrue(extractor.isLogMethod(jul, "warning"));
    }

    @Test
    @DisplayName("Is log method system log scenario case")
    void isLogMethod_systemLog() {
        LogMessageExtractor.LoggerKind system = LogMessageExtractor.LoggerKind.SYSTEM;
        assertTrue(extractor.isLogMethod(system, "log"));
        assertFalse(extractor.isLogMethod(system, "info"));
    }

    @Test
    @DisplayName("Is log method unknown returns false scenario case")
    void isLogMethod_unknownReturnsFalse() {
        LogMessageExtractor.LoggerKind unknown = LogMessageExtractor.LoggerKind.UNKNOWN;
        assertFalse(extractor.isLogMethod(unknown, "info"));
    }

    // --- isSupplierTypedExpression tests ---

    @Test
    @DisplayName("Is supplier typed expression null returns flse")
    void isSupplierTypedExpression_nullReturnsFlse() {
        assertFalse(extractor.isSupplierTypedExpression(null));
    }

    @Test
    @DisplayName("Is supplier typed expression ident with unknown type")
    void isSupplierTypedExpression_identWithUnknownType() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVar");

        assertFalse(extractor.isSupplierTypedExpression(ident));
    }

    @Test
    @DisplayName("Is supplier typed expression dot with unknown rightmost ident")
    void isSupplierTypedExpression_dotWithUnknownRightmostIdent() {
        // Create a DOT node with an IDENT as last child (which findRightmostIdent returns)
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownFieldVar");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(ident);

        // Context has no type for this variable, so returns false
        assertFalse(extractor.isSupplierTypedExpression(dot));
    }

    @Test
    @DisplayName("Is supplier typed expression typecast without type")
    void isSupplierTypedExpression_typecastWithoutType() {
        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.findFirstToken(TokenTypes.TYPE)).thenReturn(null);

        assertFalse(extractor.isSupplierTypedExpression(typecast));
    }

    @Test
    @DisplayName("Is supplier typed expression literal returns false")
    void isSupplierTypedExpression_literalReturnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertFalse(extractor.isSupplierTypedExpression(literal));
    }

    // --- containsThrowable tests ---

    @Test
    @DisplayName("Contains throwable empty list scenario case")
    void containsThrowable_emptyList() {
        assertFalse(extractor.containsThrowable(Collections.emptyList()));
    }

    // --- isThrowableExpression edge cases ---

    @Test
    @DisplayName("Is throwable expression literal new with exception")
    void isThrowableExpression_literalNewWithException() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.LITERAL_NEW);
        when(expr.getFirstChild()).thenReturn(null);

        // When extractNewClassName returns null
        assertFalse(extractor.isThrowableExpression(expr));
    }

    @Test
    @DisplayName("Is throwable expression ident with null type")
    void isThrowableExpression_identWithNullType() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.IDENT);
        when(expr.getText()).thenReturn("unknownVar");

        // Context has no type for this variable
        assertFalse(extractor.isThrowableExpression(expr));
    }

    // --- isLogMethod return true mutations tests ---

    @Test
    @DisplayName("Is log method slf 4 j non log method returns false scenario case")
    void isLogMethod_slf4jNonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind slf4j = LogMessageExtractor.LoggerKind.SLF4J;
        assertFalse(extractor.isLogMethod(slf4j, "getName"));
        assertFalse(extractor.isLogMethod(slf4j, "isDebugEnabled"));
        assertFalse(extractor.isLogMethod(slf4j, ""));
        assertFalse(extractor.isLogMethod(slf4j, "log"));
    }

    @Test
    @DisplayName("Is log method log 4 j 2 non log method returns false scenario case detail")
    void isLogMethod_log4j2NonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind log4j2 = LogMessageExtractor.LoggerKind.LOG4J2;
        assertFalse(extractor.isLogMethod(log4j2, "getName"));
        assertFalse(extractor.isLogMethod(log4j2, "isInfoEnabled"));
        assertFalse(extractor.isLogMethod(log4j2, "log"));
    }

    @Test
    @DisplayName("Is log method jul log method scenario case")
    void isLogMethod_julLogMethod() {
        LogMessageExtractor.LoggerKind jul = LogMessageExtractor.LoggerKind.JUL;
        assertTrue(extractor.isLogMethod(jul, "log"));
        assertFalse(extractor.isLogMethod(jul, "getName"));
    }

    @Test
    @DisplayName("Is log method system non log method returns false scenario case")
    void isLogMethod_systemNonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind system = LogMessageExtractor.LoggerKind.SYSTEM;
        assertFalse(extractor.isLogMethod(system, "getName"));
        assertFalse(extractor.isLogMethod(system, "trace"));
        assertFalse(extractor.isLogMethod(system, "debug"));
    }

    // --- extractQualifierNameForLog edge cases ---

    @Test
    @DisplayName("Extract qualifier name returns null when dot missing")
    void extractQualifierNameReturnsNullWhenDotMissing() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        assertNull(invokeExtractQualifierNameForLog(methodCall));
    }

    @Test
    @DisplayName("Extract qualifier name returns null for method call qualifier")
    void extractQualifierNameReturnsNullForMethodCallQualifier() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createMethodCall("LoggerFactory", "getLogger"));
        dot.addChild(createIdent("info"));

        assertNull(invokeExtractQualifierNameForLog(methodCall));
    }

    @Test
    @DisplayName("Extract qualifier name returns ident qualifier")
    void extractQualifierNameReturnsIdentQualifier() throws Exception {
        DetailAstImpl methodCall = createMethodCall("logger", "info");

        assertEquals("logger", invokeExtractQualifierNameForLog(methodCall));
    }

    @Test
    @DisplayName("Extract qualifier name returns rightmost ident for dotted qualifier")
    void extractQualifierNameReturnsRightmostIdentForDottedQualifier() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);

        DetailAstImpl qualifier = new DetailAstImpl();
        qualifier.setType(TokenTypes.DOT);
        qualifier.addChild(createIdent("System"));
        qualifier.addChild(createIdent("out"));
        dot.addChild(qualifier);
        dot.addChild(createIdent("println"));

        assertEquals("out", invokeExtractQualifierNameForLog(methodCall));
    }

    @Test
    @DisplayName("Contains throwable single throwable element scenario")
    void containsThrowable_singleThrowableElement() {
        DetailAST throwableArg = mock(DetailAST.class);
        when(throwableArg.getType()).thenReturn(TokenTypes.EXPR);

        DetailAST literalNew = mock(DetailAST.class);
        when(literalNew.getType()).thenReturn(TokenTypes.LITERAL_NEW);
        when(throwableArg.getFirstChild()).thenReturn(literalNew);
        when(throwableArg.getChildCount()).thenReturn(1);

        DetailAST className = mock(DetailAST.class);
        when(className.getType()).thenReturn(TokenTypes.IDENT);
        when(className.getText()).thenReturn("RuntimeException");
        when(literalNew.getFirstChild()).thenReturn(className);

        assertTrue(extractor.containsThrowable(Collections.singletonList(throwableArg)));
    }

    @Test
    @DisplayName("Contains throwable no throwable element scenario case")
    void containsThrowable_noThrowableElement() {
        DetailAST stringArg = mock(DetailAST.class);
        when(stringArg.getType()).thenReturn(TokenTypes.EXPR);

        DetailAST strLiteral = mock(DetailAST.class);
        when(strLiteral.getType()).thenReturn(TokenTypes.STRING_LITERAL);
        when(strLiteral.getText()).thenReturn("\"test\"");
        when(stringArg.getFirstChild()).thenReturn(strLiteral);
        when(stringArg.getChildCount()).thenReturn(1);

        assertFalse(extractor.containsThrowable(Collections.singletonList(stringArg)));
    }

    // --- isBlankMessage edge cases ---

    @Test
    @DisplayName("Is blank message tabs and newlines")
    void isBlankMessage_tabsAndNewlines() {
        assertTrue(extractor.isBlankMessage("\t\n\r"));
    }

    @Test
    @DisplayName("Is blank message single space scenario")
    void isBlankMessage_singleSpace() {
        assertTrue(extractor.isBlankMessage(" "));
    }

    @Test
    @DisplayName("Jvm log with method call message emits missing message")
    void jvmLogMethodCallMessage_emitsMissingMessage() {
        DetailAstImpl methodCall = createJvmOnCall(
                "startup",
                createExpr(createClassLiteral("RollCyclesTest")),
                createExpr(createMethodCall("stringBuilder", "toString"))
        );

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Jvm log call returns false when method is not on")
    void jvmLogCallReturnsFalseWhenMethodIsNotOn() throws Exception {
        DetailAstImpl methodCall = createMethodCall("Jvm", "warn");
        assertFalse(invokeCheckJvmLogCall(methodCall, "warn"));
    }

    @Test
    @DisplayName("Jvm log call returns false when owner is not method call")
    void jvmLogCallReturnsFalseWhenOwnerIsNotMethodCall() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createIdent("Jvm"));
        dot.addChild(createIdent("on"));

        assertFalse(invokeCheckJvmLogCall(methodCall, "on"));
    }

    @Test
    @DisplayName("Jvm log call returns false when qualifier is not Jvm")
    void jvmLogCallReturnsFalseWhenQualifierIsNotJvm() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createMethodCall("Logger", "warn"));
        dot.addChild(createIdent("on"));
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        elist.addChild(createExpr(createIdent("first")));
        elist.addChild(createExpr(createIdent("second")));

        assertFalse(invokeCheckJvmLogCall(methodCall, "on"));
    }

    @Test
    @DisplayName("Jvm log call returns false when args fewer than two")
    void jvmLogCallReturnsFalseWhenArgsFewerThanTwo() throws Exception {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createJvmLevelCall("warn"));
        dot.addChild(createIdent("on"));
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        elist.addChild(createExpr(createIdent("only")));

        assertFalse(invokeCheckJvmLogCall(methodCall, "on"));
    }

    @Test
    @DisplayName("Jvm log with null message emits missing message")
    void jvmLogWithNullMessageEmitsMissingMessage() {
        DetailAstImpl methodCall = createJvmOnCall(
                "warn",
                createExpr(createIdent("context")),
                createExpr(createNullLiteral())
        );

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Jvm log with throwable only does not emit missing message")
    void jvmLogWithThrowableOnlyDoesNotEmitMissingMessage() {
        DetailAstImpl methodCall = createJvmOnCall(
                "warn",
                createExpr(createIdent("context")),
                createExpr(createLiteralNew("RuntimeException"))
        );

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Jvm log call returns true for message template")
    void jvmLogCallReturnsTrueForMessageTemplate() throws Exception {
        DetailAstImpl methodCall = createJvmOnCall(
                "warn",
                createExpr(createIdent("context")),
                createExpr(createStringLiteral("\"value {}\""))
        );
        methodCall.setLineNo(7);

        assertTrue(invokeCheckJvmLogCall(methodCall, "on"),
                "Jvm.on should return true when message template present");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Jvm log call returns true for null message with throwable")
    void jvmLogCallReturnsTrueForNullMessageWithThrowable() throws Exception {
        DetailAstImpl methodCall = createJvmOnCall(
                "error",
                createExpr(createIdent("context")),
                createExpr(createNullLiteral()),
                createExpr(createLiteralNew("RuntimeException"))
        );
        methodCall.setLineNo(9);

        assertTrue(invokeCheckJvmLogCall(methodCall, "on"),
                "Jvm.on should return true when message is null");
        assertTrue(sink.missingMessages.isEmpty(), "Throwable should suppress missing message");
    }

    @Test
    @DisplayName("Count placeholder helpers return expected values")
    void countPlaceholderHelpersReturnExpectedValues() throws Exception {
        assertEquals(2, invokeCountLogPlaceholders("value {} for {}"));
        assertEquals(1, invokeCountFormatPlaceholders("value %s"));
        assertEquals(1, invokeCountKeyValueLabels("index= value"));
    }

    @Test
    @DisplayName("Extract constant supplier message returns literal")
    void extractConstantSupplierMessageReturnsLiteral() throws Exception {
        DetailAstImpl lambda = createLambdaWithExpr(createStringLiteral("\"hello\""), false);
        DetailAstImpl expr = createExpr(lambda);

        assertEquals("hello", invokeExtractConstantSupplierMessage(expr));
    }

    @Test
    @DisplayName("Extract constant supplier message returns format template")
    void extractConstantSupplierMessageReturnsFormatTemplate() throws Exception {
        DetailAstImpl formatCall = createMethodCall("String", "format");
        DetailAstImpl elist = (DetailAstImpl) formatCall.findFirstToken(TokenTypes.ELIST);
        elist.addChild(createExpr(createStringLiteral("\"value %s\"")));
        DetailAstImpl lambda = createLambdaWithExpr(formatCall, false);

        assertEquals("value %s", invokeExtractConstantSupplierMessage(lambda));
    }

    @Test
    @DisplayName("Extract constant supplier message returns null for parameterised lambda")
    void extractConstantSupplierMessageReturnsNullForParameterisedLambda() throws Exception {
        DetailAstImpl lambda = createLambdaWithExpr(createStringLiteral("\"ignored\""), true);

        assertNull(invokeExtractConstantSupplierMessage(lambda));
    }

    /**
     * Test sink for capturing emitted candidates.
     */
    private static class TestMessageSink implements MessageCandidateSink {
        final List<MessageCandidate> candidates = new java.util.ArrayList<>();
        final List<MessageSource> missingMessages = new java.util.ArrayList<>();
        final List<String> unhandledReasons = new java.util.ArrayList<>();

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

    private DetailAstImpl createJvmOnCall(String levelMethod, DetailAstImpl firstArg, DetailAstImpl secondArg) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);

        DetailAstImpl ownerCall = createJvmLevelCall(levelMethod);
        dot.addChild(ownerCall);
        dot.addChild(createIdent("on"));

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        elist.addChild(firstArg);
        elist.addChild(secondArg);
        return methodCall;
    }

    private DetailAstImpl createJvmOnCall(String levelMethod, DetailAstImpl firstArg,
                                          DetailAstImpl secondArg, DetailAstImpl thirdArg) {
        DetailAstImpl methodCall = createJvmOnCall(levelMethod, firstArg, secondArg);
        DetailAstImpl elist = (DetailAstImpl) methodCall.findFirstToken(TokenTypes.ELIST);
        elist.addChild(thirdArg);
        return methodCall;
    }

    private DetailAstImpl createJvmLevelCall(String methodName) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createIdent("Jvm"));
        dot.addChild(createIdent(methodName));

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createMethodCall(String qualifier, String methodName) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        methodCall.addChild(dot);
        dot.addChild(createIdent(qualifier));
        dot.addChild(createIdent(methodName));

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createClassLiteral(String className) {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(className));
        dot.addChild(createIdent("class"));
        return dot;
    }

    private DetailAstImpl createLiteralNew(String className) {
        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        literalNew.addChild(createIdent(className));
        return literalNew;
    }

    private DetailAstImpl createNullLiteral() {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.LITERAL_NULL);
        literal.setText("null");
        return literal;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private DetailAstImpl createStringLiteral(String text) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText(text);
        return literal;
    }

    private DetailAstImpl createLambdaWithExpr(DetailAstImpl body, boolean withParams) {
        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        DetailAstImpl parameters = new DetailAstImpl();
        parameters.setType(TokenTypes.PARAMETERS);
        if (withParams) {
            parameters.addChild(createIdent("value"));
        }
        lambda.addChild(parameters);
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(body);
        lambda.addChild(expr);
        return lambda;
    }

    private boolean invokeCheckJvmLogCall(DetailAST methodCall, String methodName) throws Exception {
        return extractor.checkJvmLogCall(methodCall, methodName);
    }

    private String invokeExtractQualifierNameForLog(DetailAST methodCall) throws Exception {
        return extractor.extractQualifierNameForLog(methodCall);
    }

    private String invokeExtractConstantSupplierMessage(DetailAST expr) throws Exception {
        return extractor.extractConstantSupplierMessage(expr);
    }

    private int invokeCountLogPlaceholders(String message) throws Exception {
        return extractor.countLogPlaceholders(message);
    }

    private int invokeCountFormatPlaceholders(String message) throws Exception {
        return extractor.countFormatPlaceholders(message);
    }

    private int invokeCountKeyValueLabels(String message) throws Exception {
        return extractor.countKeyValueLabels(message);
    }

    private DetailAstImpl createExpr(DetailAstImpl child) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(child);
        return expr;
    }

    private FileContents createFileContents(String fileName, int lineCount) throws java.io.IOException {
        java.util.List<String> lines = java.util.Collections.nCopies(lineCount, "");
        Path file = tempDir.resolve(fileName);
        java.nio.file.Files.write(file, lines, java.nio.charset.StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        return new FileContents(text);
    }
}
