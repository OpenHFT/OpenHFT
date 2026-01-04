/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogMessageExtractor}.
 */
public class LogMessageExtractorTest {

    private LogMessageExtractor extractor;

    @BeforeEach
    void setUp() {
        MessageExtractionContext context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        TestMessageSink sink = new TestMessageSink();
        extractor = new LogMessageExtractor(context, sink);
    }

    // --- isBlankMessage tests ---

    @Test
    void isBlankMessageNull() {
        assertTrue(extractor.isBlankMessage(null));
    }

    @Test
    void isBlankMessageEmpty() {
        assertTrue(extractor.isBlankMessage(""));
    }

    @Test
    void isBlankMessageWhitespace() {
        assertTrue(extractor.isBlankMessage("   "));
    }

    @Test
    void isBlankMessageWithContent() {
        assertFalse(extractor.isBlankMessage("hello"));
    }

    // --- isThrowableTypeName tests ---

    @Test
    void isThrowableTypeNameNull() {
        assertFalse(extractor.isThrowableTypeName(null));
    }

    @Test
    void isThrowableTypeNameThrowable() {
        assertTrue(extractor.isThrowableTypeName("Throwable"));
    }

    @Test
    void isThrowableTypeNameException() {
        assertTrue(extractor.isThrowableTypeName("IOException"));
        assertTrue(extractor.isThrowableTypeName("RuntimeException"));
        assertTrue(extractor.isThrowableTypeName("IllegalStateException"));
    }

    @Test
    void isThrowableTypeNameError() {
        assertTrue(extractor.isThrowableTypeName("OutOfMemoryError"));
        assertTrue(extractor.isThrowableTypeName("StackOverflowError"));
    }

    @Test
    void isThrowableTypeNameStackTrace() {
        assertTrue(extractor.isThrowableTypeName("StackTrace"));
    }

    @Test
    void isThrowableTypeNameFullyQualified() {
        assertTrue(extractor.isThrowableTypeName("java.lang.RuntimeException"));
        assertTrue(extractor.isThrowableTypeName("java.io.IOException"));
    }

    @Test
    void isThrowableTypeNameNotThrowable() {
        assertFalse(extractor.isThrowableTypeName("String"));
        assertFalse(extractor.isThrowableTypeName("Integer"));
        assertFalse(extractor.isThrowableTypeName("List"));
    }

    // --- isSupplierTypeName tests ---

    @Test
    void isSupplierTypeNameNull() {
        assertFalse(extractor.isSupplierTypeName(null));
    }

    @Test
    void isSupplierTypeNameSupplier() {
        assertTrue(extractor.isSupplierTypeName("Supplier"));
    }

    @Test
    void isSupplierTypeNameFullyQualified() {
        assertTrue(extractor.isSupplierTypeName("java.util.function.Supplier"));
    }

    @Test
    void isSupplierTypeNameEndingWithDotSupplier() {
        assertTrue(extractor.isSupplierTypeName("com.example.Supplier"));
        assertFalse(extractor.isSupplierTypeName("com.example.MySupplier"));
    }

    @Test
    void isSupplierTypeNameNotSupplier() {
        assertFalse(extractor.isSupplierTypeName("String"));
        assertFalse(extractor.isSupplierTypeName("Consumer"));
    }

    // --- resolveLoggerKindFromType tests ---

    @Test
    void resolveLoggerKindFromType_slf4j() {
        assertEquals(LogMessageExtractor.LoggerKind.SLF4J,
                extractor.resolveLoggerKindFromType("org.slf4j.Logger"));
    }

    @Test
    void resolveLoggerKindFromType_log4j2() {
        assertEquals(LogMessageExtractor.LoggerKind.LOG4J2,
                extractor.resolveLoggerKindFromType("org.apache.logging.log4j.Logger"));
    }

    @Test
    void resolveLoggerKindFromType_jul() {
        assertEquals(LogMessageExtractor.LoggerKind.JUL,
                extractor.resolveLoggerKindFromType("java.util.logging.Logger"));
    }

    @Test
    void resolveLoggerKindFromType_systemLogger() {
        assertEquals(LogMessageExtractor.LoggerKind.SYSTEM,
                extractor.resolveLoggerKindFromType("java.lang.System.Logger"));
    }

    @Test
    void resolveLoggerKindFromType_systemLoggerShort() {
        assertEquals(LogMessageExtractor.LoggerKind.SYSTEM,
                extractor.resolveLoggerKindFromType("System.Logger"));
    }

    @Test
    void resolveLoggerKindFromType_unknown() {
        assertEquals(LogMessageExtractor.LoggerKind.UNKNOWN,
                extractor.resolveLoggerKindFromType("com.example.CustomLogger"));
    }

    // --- isLogMethod tests ---

    @Test
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
    void isLogMethod_log4j2Fatal() {
        LogMessageExtractor.LoggerKind log4j2 = LogMessageExtractor.LoggerKind.LOG4J2;
        assertTrue(extractor.isLogMethod(log4j2, "fatal"));
    }

    @Test
    void isLogMethod_julLog() {
        LogMessageExtractor.LoggerKind jul = LogMessageExtractor.LoggerKind.JUL;
        assertTrue(extractor.isLogMethod(jul, "log"));
        assertTrue(extractor.isLogMethod(jul, "severe"));
        assertTrue(extractor.isLogMethod(jul, "warning"));
    }

    @Test
    void isLogMethod_systemLog() {
        LogMessageExtractor.LoggerKind system = LogMessageExtractor.LoggerKind.SYSTEM;
        assertTrue(extractor.isLogMethod(system, "log"));
        assertFalse(extractor.isLogMethod(system, "info"));
    }

    @Test
    void isLogMethod_unknownReturnsFalse() {
        LogMessageExtractor.LoggerKind unknown = LogMessageExtractor.LoggerKind.UNKNOWN;
        assertFalse(extractor.isLogMethod(unknown, "info"));
    }

    // --- isSupplierTypedExpression tests ---

    @Test
    void isSupplierTypedExpression_nullReturnsFlse() {
        assertFalse(extractor.isSupplierTypedExpression(null));
    }

    @Test
    void isSupplierTypedExpression_identWithUnknownType() {
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVar");

        assertFalse(extractor.isSupplierTypedExpression(ident));
    }

    @Test
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
    void isSupplierTypedExpression_typecastWithoutType() {
        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.findFirstToken(TokenTypes.TYPE)).thenReturn(null);

        assertFalse(extractor.isSupplierTypedExpression(typecast));
    }

    @Test
    void isSupplierTypedExpression_literalReturnsFalse() {
        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertFalse(extractor.isSupplierTypedExpression(literal));
    }

    // --- containsThrowable tests ---

    @Test
    void containsThrowable_emptyList() {
        assertFalse(extractor.containsThrowable(Collections.emptyList()));
    }

    // --- isThrowableExpression edge cases ---

    @Test
    void isThrowableExpression_literalNewWithException() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.LITERAL_NEW);
        when(expr.getFirstChild()).thenReturn(null);

        // When extractNewClassName returns null
        assertFalse(extractor.isThrowableExpression(expr));
    }

    @Test
    void isThrowableExpression_identWithNullType() {
        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.IDENT);
        when(expr.getText()).thenReturn("unknownVar");

        // Context has no type for this variable
        assertFalse(extractor.isThrowableExpression(expr));
    }

    // --- isLogMethod return true mutations tests ---

    @Test
    void isLogMethod_slf4jNonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind slf4j = LogMessageExtractor.LoggerKind.SLF4J;
        assertFalse(extractor.isLogMethod(slf4j, "getName"));
        assertFalse(extractor.isLogMethod(slf4j, "isDebugEnabled"));
        assertFalse(extractor.isLogMethod(slf4j, ""));
        assertFalse(extractor.isLogMethod(slf4j, "log"));
    }

    @Test
    void isLogMethod_log4j2NonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind log4j2 = LogMessageExtractor.LoggerKind.LOG4J2;
        assertFalse(extractor.isLogMethod(log4j2, "getName"));
        assertFalse(extractor.isLogMethod(log4j2, "isInfoEnabled"));
        assertFalse(extractor.isLogMethod(log4j2, "log"));
    }

    @Test
    void isLogMethod_julLogMethod() {
        LogMessageExtractor.LoggerKind jul = LogMessageExtractor.LoggerKind.JUL;
        assertTrue(extractor.isLogMethod(jul, "log"));
        assertFalse(extractor.isLogMethod(jul, "getName"));
    }

    @Test
    void isLogMethod_systemNonLogMethodReturnsFalse() {
        LogMessageExtractor.LoggerKind system = LogMessageExtractor.LoggerKind.SYSTEM;
        assertFalse(extractor.isLogMethod(system, "getName"));
        assertFalse(extractor.isLogMethod(system, "trace"));
        assertFalse(extractor.isLogMethod(system, "debug"));
    }

    // --- extractQualifierNameForLog edge cases ---

    @Test
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
    void isBlankMessage_tabsAndNewlines() {
        assertTrue(extractor.isBlankMessage("\t\n\r"));
    }

    @Test
    void isBlankMessage_singleSpace() {
        assertTrue(extractor.isBlankMessage(" "));
    }

    /**
     * Test sink for capturing emitted candidates.
     */
    private static class TestMessageSink implements MessageCandidateSink {
        @Override
        public void emitCandidate(MessageCandidate candidate) {
            // No-op for testing
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source) {
            // No-op for testing
        }
    }
}
