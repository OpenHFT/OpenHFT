/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LogMessageExtractor}.
 */
public class LogMessageExtractorTest {

    private LogMessageExtractor extractor;
    private MessageExtractionContext context;
    private TestMessageSink sink;

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        sink = new TestMessageSink();
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
