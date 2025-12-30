/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

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

    // --- isBlankMessage tests via reflection ---

    @Test
    void isBlankMessageNull() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isBlankMessage", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, (String) null));
    }

    @Test
    void isBlankMessageEmpty() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isBlankMessage", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, ""));
    }

    @Test
    void isBlankMessageWhitespace() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isBlankMessage", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "   "));
    }

    @Test
    void isBlankMessageWithContent() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isBlankMessage", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, "hello"));
    }

    // --- isThrowableTypeName tests via reflection ---

    @Test
    void isThrowableTypeNameNull() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, (String) null));
    }

    @Test
    void isThrowableTypeNameThrowable() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "Throwable"));
    }

    @Test
    void isThrowableTypeNameException() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "IOException"));
        assertTrue((Boolean) method.invoke(extractor, "RuntimeException"));
        assertTrue((Boolean) method.invoke(extractor, "IllegalStateException"));
    }

    @Test
    void isThrowableTypeNameError() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "OutOfMemoryError"));
        assertTrue((Boolean) method.invoke(extractor, "StackOverflowError"));
    }

    @Test
    void isThrowableTypeNameStackTrace() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "StackTrace"));
    }

    @Test
    void isThrowableTypeNameFullyQualified() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "java.lang.RuntimeException"));
        assertTrue((Boolean) method.invoke(extractor, "java.io.IOException"));
    }

    @Test
    void isThrowableTypeNameNotThrowable() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isThrowableTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, "String"));
        assertFalse((Boolean) method.invoke(extractor, "Integer"));
        assertFalse((Boolean) method.invoke(extractor, "List"));
    }

    // --- isSupplierTypeName tests via reflection ---

    @Test
    void isSupplierTypeNameNull() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isSupplierTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, (String) null));
    }

    @Test
    void isSupplierTypeNameSupplier() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isSupplierTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "Supplier"));
    }

    @Test
    void isSupplierTypeNameFullyQualified() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isSupplierTypeName", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(extractor, "java.util.function.Supplier"));
    }

    @Test
    void isSupplierTypeNameEndingWithDotSupplier() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isSupplierTypeName", String.class);
        method.setAccessible(true);

        // Must end with ".Supplier" not just "Supplier"
        assertTrue((Boolean) method.invoke(extractor, "com.example.Supplier"));
        assertFalse((Boolean) method.invoke(extractor, "com.example.MySupplier"));
    }

    @Test
    void isSupplierTypeNameNotSupplier() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod("isSupplierTypeName", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, "String"));
        assertFalse((Boolean) method.invoke(extractor, "Consumer"));
    }

    // --- resolveLoggerKindFromType tests via reflection ---

    @Test
    void resolveLoggerKindFromType_slf4j() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "org.slf4j.Logger");
        assertEquals("SLF4J", result.toString());
    }

    @Test
    void resolveLoggerKindFromType_log4j2() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "org.apache.logging.log4j.Logger");
        assertEquals("LOG4J2", result.toString());
    }

    @Test
    void resolveLoggerKindFromType_jul() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "java.util.logging.Logger");
        assertEquals("JUL", result.toString());
    }

    @Test
    void resolveLoggerKindFromType_systemLogger() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "java.lang.System.Logger");
        assertEquals("SYSTEM", result.toString());
    }

    @Test
    void resolveLoggerKindFromType_systemLoggerShort() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "System.Logger");
        assertEquals("SYSTEM", result.toString());
    }

    @Test
    void resolveLoggerKindFromType_unknown() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "resolveLoggerKindFromType", String.class);
        method.setAccessible(true);

        Object result = method.invoke(extractor, "com.example.CustomLogger");
        assertEquals("UNKNOWN", result.toString());
    }

    // --- isLogMethod tests via reflection ---

    @Test
    void isLogMethod_slf4jTrace() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isLogMethod", getLoggerKindClass(), String.class);
        method.setAccessible(true);

        Object slf4j = getLoggerKindValue("SLF4J");
        assertTrue((Boolean) method.invoke(extractor, slf4j, "trace"));
        assertTrue((Boolean) method.invoke(extractor, slf4j, "debug"));
        assertTrue((Boolean) method.invoke(extractor, slf4j, "info"));
        assertTrue((Boolean) method.invoke(extractor, slf4j, "warn"));
        assertTrue((Boolean) method.invoke(extractor, slf4j, "error"));
        assertFalse((Boolean) method.invoke(extractor, slf4j, "fatal"));
    }

    @Test
    void isLogMethod_log4j2Fatal() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isLogMethod", getLoggerKindClass(), String.class);
        method.setAccessible(true);

        Object log4j2 = getLoggerKindValue("LOG4J2");
        assertTrue((Boolean) method.invoke(extractor, log4j2, "fatal"));
    }

    @Test
    void isLogMethod_julLog() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isLogMethod", getLoggerKindClass(), String.class);
        method.setAccessible(true);

        Object jul = getLoggerKindValue("JUL");
        assertTrue((Boolean) method.invoke(extractor, jul, "log"));
        assertTrue((Boolean) method.invoke(extractor, jul, "severe"));
        assertTrue((Boolean) method.invoke(extractor, jul, "warning"));
    }

    @Test
    void isLogMethod_systemLog() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isLogMethod", getLoggerKindClass(), String.class);
        method.setAccessible(true);

        Object system = getLoggerKindValue("SYSTEM");
        assertTrue((Boolean) method.invoke(extractor, system, "log"));
        assertFalse((Boolean) method.invoke(extractor, system, "info"));
    }

    @Test
    void isLogMethod_unknownReturnsFalse() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isLogMethod", getLoggerKindClass(), String.class);
        method.setAccessible(true);

        Object unknown = getLoggerKindValue("UNKNOWN");
        assertFalse((Boolean) method.invoke(extractor, unknown, "info"));
    }

    // --- isSupplierTypedExpression tests via reflection ---

    @Test
    void isSupplierTypedExpression_nullReturnsFlse() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isSupplierTypedExpression", DetailAST.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, (DetailAST) null));
    }

    @Test
    void isSupplierTypedExpression_identWithUnknownType() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isSupplierTypedExpression", DetailAST.class);
        method.setAccessible(true);

        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownVar");

        assertFalse((Boolean) method.invoke(extractor, ident));
    }

    @Test
    void isSupplierTypedExpression_dotWithUnknownRightmostIdent() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isSupplierTypedExpression", DetailAST.class);
        method.setAccessible(true);

        // Create a DOT node with an IDENT as last child (which findRightmostIdent returns)
        DetailAST ident = mock(DetailAST.class);
        when(ident.getType()).thenReturn(TokenTypes.IDENT);
        when(ident.getText()).thenReturn("unknownFieldVar");

        DetailAST dot = mock(DetailAST.class);
        when(dot.getType()).thenReturn(TokenTypes.DOT);
        when(dot.getLastChild()).thenReturn(ident);

        // Context has no type for this variable, so returns false
        assertFalse((Boolean) method.invoke(extractor, dot));
    }

    @Test
    void isSupplierTypedExpression_typecastWithoutType() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isSupplierTypedExpression", DetailAST.class);
        method.setAccessible(true);

        DetailAST typecast = mock(DetailAST.class);
        when(typecast.getType()).thenReturn(TokenTypes.TYPECAST);
        when(typecast.findFirstToken(TokenTypes.TYPE)).thenReturn(null);

        assertFalse((Boolean) method.invoke(extractor, typecast));
    }

    @Test
    void isSupplierTypedExpression_literalReturnsFalse() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isSupplierTypedExpression", DetailAST.class);
        method.setAccessible(true);

        DetailAST literal = mock(DetailAST.class);
        when(literal.getType()).thenReturn(TokenTypes.STRING_LITERAL);

        assertFalse((Boolean) method.invoke(extractor, literal));
    }

    // --- containsThrowable tests via reflection ---

    @Test
    void containsThrowable_emptyList() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "containsThrowable", List.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(extractor, Collections.emptyList()));
    }

    // --- isThrowableExpression edge cases ---

    @Test
    void isThrowableExpression_literalNewWithException() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isThrowableExpression", DetailAST.class);
        method.setAccessible(true);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.LITERAL_NEW);
        when(expr.getFirstChild()).thenReturn(null);

        // When extractNewClassName returns null
        assertFalse((Boolean) method.invoke(extractor, expr));
    }

    @Test
    void isThrowableExpression_identWithNullType() throws Exception {
        Method method = LogMessageExtractor.class.getDeclaredMethod(
                "isThrowableExpression", DetailAST.class);
        method.setAccessible(true);

        DetailAST expr = mock(DetailAST.class);
        when(expr.getType()).thenReturn(TokenTypes.IDENT);
        when(expr.getText()).thenReturn("unknownVar");

        // Context has no type for this variable
        assertFalse((Boolean) method.invoke(extractor, expr));
    }

    // --- Helper methods for enum access ---

    private Class<?> getLoggerKindClass() {
        for (Class<?> innerClass : LogMessageExtractor.class.getDeclaredClasses()) {
            if (innerClass.getSimpleName().equals("LoggerKind")) {
                return innerClass;
            }
        }
        throw new IllegalStateException("LoggerKind enum not found");
    }

    @SuppressWarnings("unchecked")
    private Object getLoggerKindValue(String name) {
        Class<?> enumClass = getLoggerKindClass();
        for (Object constant : enumClass.getEnumConstants()) {
            if (constant.toString().equals(name)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown LoggerKind: " + name);
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
