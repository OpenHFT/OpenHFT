/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MeaningfulMessageProcessor}.
 */
public class MeaningfulMessageProcessorTest {

    private MeaningfulMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MeaningfulMessageProcessor();
    }

    // --- Token methods ---

    @Test
    void getDefaultTokensReturnsRequiredTokens() {
        Assertions.assertArrayEquals(processor.getRequiredTokens(), processor.getDefaultTokens());
    }

    @Test
    void getAcceptableTokensReturnsRequiredTokens() {
        Assertions.assertArrayEquals(processor.getRequiredTokens(), processor.getAcceptableTokens());
    }

    @Test
    void getRequiredTokensContainsExpectedTypes() {
        int[] tokens = processor.getRequiredTokens();
        assertNotNull(tokens);
        assertTrue(tokens.length > 0, "Should have tokens");

        Set<Integer> tokenSet = new HashSet<>();
        for (int token : tokens) {
            tokenSet.add(token);
        }

        // Verify key token types are present
        assertTrue(tokenSet.contains(TokenTypes.IMPORT), "Should include IMPORT");
        assertTrue(tokenSet.contains(TokenTypes.STATIC_IMPORT), "Should include STATIC_IMPORT");
        assertTrue(tokenSet.contains(TokenTypes.CLASS_DEF), "Should include CLASS_DEF");
        assertTrue(tokenSet.contains(TokenTypes.METHOD_DEF), "Should include METHOD_DEF");
        assertTrue(tokenSet.contains(TokenTypes.LITERAL_ASSERT), "Should include LITERAL_ASSERT");
        assertTrue(tokenSet.contains(TokenTypes.LITERAL_THROW), "Should include LITERAL_THROW");
        assertTrue(tokenSet.contains(TokenTypes.ANNOTATION), "Should include ANNOTATION");
        assertTrue(tokenSet.contains(TokenTypes.METHOD_CALL), "Should include METHOD_CALL");
    }

    // --- setMessageExtractionFile tests ---

    @Test
    void setMessageExtractionFileNull() {
        processor.setMessageExtractionFile(null);
        // Should not throw
    }

    @Test
    void setMessageExtractionFileEmpty() {
        processor.setMessageExtractionFile("");
        // Should not throw
    }

    @Test
    void setMessageExtractionFileWhitespace() {
        processor.setMessageExtractionFile("   ");
        // Should not throw
    }

    @Test
    void setMessageExtractionFileWithUnresolvedProperty() {
        processor.setMessageExtractionFile("${some.property}");
        // Should not throw, and should disable extraction
    }

    @Test
    void setMessageExtractionFileWithValidPath() {
        processor.setMessageExtractionFile("/tmp/test-output.txt");
        // Should not throw
    }

    @Test
    void setMessageExtractionFileTrimsWhitespace() {
        processor.setMessageExtractionFile("  /tmp/test.txt  ");
        // Should not throw
    }

    // --- setIgnoredExceptionClassNames tests ---

    @Test
    void setIgnoredExceptionClassNamesNull() {
        processor.setIgnoredExceptionClassNames(null);
        // Should not throw
    }

    @Test
    void setIgnoredExceptionClassNamesEmpty() {
        processor.setIgnoredExceptionClassNames("");
        // Should not throw
    }

    @Test
    void setIgnoredExceptionClassNamesCommaSeparated() {
        processor.setIgnoredExceptionClassNames("IOException,RuntimeException");
        // Should not throw
    }

    @Test
    void setIgnoredExceptionClassNamesWhitespaceSeparated() {
        processor.setIgnoredExceptionClassNames("IOException RuntimeException");
        // Should not throw
    }

    @Test
    void setIgnoredExceptionClassNamesMixed() {
        processor.setIgnoredExceptionClassNames("IOException, RuntimeException  IllegalStateException");
        // Should not throw
    }

    @Test
    void setIgnoredExceptionClassNamesFullyQualified() {
        processor.setIgnoredExceptionClassNames("java.io.IOException,java.lang.RuntimeException");
        // Should not throw, normalises to simple names
    }

    @Test
    void setIgnoredExceptionClassNamesWithBlanks() {
        processor.setIgnoredExceptionClassNames("IOException,,  ,RuntimeException");
        // Should not throw, skips blanks
    }

    // --- setVerbose tests ---

    @Test
    void setVerboseTrue() {
        processor.setVerbose(true);
        // Should not throw
    }

    @Test
    void setVerboseFalse() {
        processor.setVerbose(false);
        // Should not throw
    }

    // --- normalizeClassName tests via reflection ---

    @Test
    void normalizeClassNameSimple() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertEquals("IOException", method.invoke(processor, "IOException"));
    }

    @Test
    void normalizeClassNameFullyQualified() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertEquals("IOException", method.invoke(processor, "java.io.IOException"));
    }

    @Test
    void normalizeClassNameEmpty() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(processor, ""));
    }

    @Test
    void normalizeClassNameWhitespace() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("normalizeClassName", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(processor, "   "));
    }

    // --- escapeForTsv tests via reflection ---

    @Test
    void escapeForTsvNull() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("", method.invoke(processor, (String) null));
    }

    @Test
    void escapeForTsvEmpty() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("", method.invoke(processor, ""));
    }

    @Test
    void escapeForTsvSimple() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("hello world", method.invoke(processor, "hello world"));
    }

    @Test
    void escapeForTsvWithTab() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("hello\\tworld", method.invoke(processor, "hello\tworld"));
    }

    @Test
    void escapeForTsvWithNewline() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("hello\\nworld", method.invoke(processor, "hello\nworld"));
    }

    @Test
    void escapeForTsvWithCarriageReturn() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("hello\\rworld", method.invoke(processor, "hello\rworld"));
    }

    @Test
    void escapeForTsvWithBackslash() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("hello\\\\world", method.invoke(processor, "hello\\world"));
    }

    @Test
    void escapeForTsvWithMultipleSpecialChars() throws Exception {
        Method method = MeaningfulMessageProcessor.class.getDeclaredMethod("escapeForTsv", String.class);
        method.setAccessible(true);

        assertEquals("a\\tb\\nc\\rd\\\\e", method.invoke(processor, "a\tb\nc\rd\\e"));
    }
}
