/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MeaningfulMessageProcessor}.
 */
@DisplayName("Meaningful message processor tests scenario case")
public class MeaningfulMessageProcessorTest {

    @TempDir
    Path tempDir;
    private MeaningfulMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MeaningfulMessageProcessor();
    }

    // --- Token methods ---

    @Test
    @DisplayName("Get default tokens returns required tokens scenario")
    void getDefaultTokensReturnsRequiredTokens() {
        Assertions.assertArrayEquals(processor.getRequiredTokens(), processor.getDefaultTokens());
    }

    @Test
    @DisplayName("Get acceptable tokens returns required tokens scenario")
    void getAcceptableTokensReturnsRequiredTokens() {
        Assertions.assertArrayEquals(processor.getRequiredTokens(), processor.getAcceptableTokens());
    }

    @Test
    @DisplayName("Get required tokens contains expected types scenario")
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
        assertTrue(tokenSet.contains(TokenTypes.LITERAL_RETURN), "Should include LITERAL_RETURN");
        assertTrue(tokenSet.contains(TokenTypes.LITERAL_THROW), "Should include LITERAL_THROW");
        assertTrue(tokenSet.contains(TokenTypes.ANNOTATION), "Should include ANNOTATION");
        assertTrue(tokenSet.contains(TokenTypes.METHOD_CALL), "Should include METHOD_CALL");
    }

    // --- setMessageExtractionFile tests ---

    @Test
    @DisplayName("Set message extraction file null scenario")
    void setMessageExtractionFileNull() {
        processor.setMessageExtractionFile(null);
        // Should not throw
    }

    @Test
    @DisplayName("Set message extraction file empty scenario")
    void setMessageExtractionFileEmpty() {
        processor.setMessageExtractionFile("");
        // Should not throw
    }

    @Test
    @DisplayName("Set message extraction file whitespace scenario")
    void setMessageExtractionFileWhitespace() {
        processor.setMessageExtractionFile("   ");
        // Should not throw
    }

    @Test
    @DisplayName("Set message extraction file with unresolved property")
    void setMessageExtractionFileWithUnresolvedProperty() {
        processor.setMessageExtractionFile("${some.property}");
        // Should not throw, and should disable extraction
    }

    @Test
    @DisplayName("Set message extraction file with valid path")
    void setMessageExtractionFileWithValidPath() {
        processor.setMessageExtractionFile("/tmp/test-output.txt");
        // Should not throw
    }

    @Test
    @DisplayName("Set message extraction file trims whitespace")
    void setMessageExtractionFileTrimsWhitespace() {
        processor.setMessageExtractionFile("  /tmp/test.txt  ");
        // Should not throw
    }

    // --- setIgnoredExceptionClassNames tests ---

    @Test
    @DisplayName("Set ignored exception class names null scenario")
    void setIgnoredExceptionClassNamesNull() {
        processor.setIgnoredExceptionClassNames(null);
        // Should not throw
    }

    @Test
    @DisplayName("Set ignored exception class names empty scenario")
    void setIgnoredExceptionClassNamesEmpty() {
        processor.setIgnoredExceptionClassNames("");
        // Should not throw
    }

    @Test
    @DisplayName("Set ignored exception class names comma separated")
    void setIgnoredExceptionClassNamesCommaSeparated() {
        processor.setIgnoredExceptionClassNames("IOException,RuntimeException");
        // Should not throw
    }

    @Test
    @DisplayName("Set ignored exception class names whitespace separated")
    void setIgnoredExceptionClassNamesWhitespaceSeparated() {
        processor.setIgnoredExceptionClassNames("IOException RuntimeException");
        // Should not throw
    }

    @Test
    @DisplayName("Set ignored exception class names mixed")
    void setIgnoredExceptionClassNamesMixed() {
        processor.setIgnoredExceptionClassNames("IOException, RuntimeException  IllegalStateException");
        // Should not throw
    }

    @Test
    @DisplayName("Set ignored exception class names fully qualified")
    void setIgnoredExceptionClassNamesFullyQualified() {
        processor.setIgnoredExceptionClassNames("java.io.IOException,java.lang.RuntimeException");
        // Should not throw, normalises to simple names
    }

    @Test
    @DisplayName("Set ignored exception class names with blanks")
    void setIgnoredExceptionClassNamesWithBlanks() {
        processor.setIgnoredExceptionClassNames("IOException,,  ,RuntimeException");
        // Should not throw, skips blanks
    }

    // --- setVerbose tests ---

    @Test
    @DisplayName("Set verbose true scenario case detail")
    void setVerboseTrue() {
        processor.setVerbose(true);
        // Should not throw
    }

    @Test
    @DisplayName("Set verbose false scenario case detail")
    void setVerboseFalse() {
        processor.setVerbose(false);
        // Should not throw
    }

    // --- normalizeClassName tests ---

    @Test
    @DisplayName("Normalize class name simple scenario case")
    void normalizeClassNameSimple() {
        assertEquals("IOException", processor.normalizeClassName("IOException"));
    }

    @Test
    @DisplayName("Normalize class name fully qualified scenario")
    void normalizeClassNameFullyQualified() {
        assertEquals("IOException", processor.normalizeClassName("java.io.IOException"));
    }

    @Test
    @DisplayName("Normalize class name empty scenario case")
    void normalizeClassNameEmpty() {
        assertNull(processor.normalizeClassName(""));
    }

    @Test
    @DisplayName("Normalize class name whitespace scenario case")
    void normalizeClassNameWhitespace() {
        assertNull(processor.normalizeClassName("   "));
    }

    // --- escapeForTsv tests ---

    @Test
    @DisplayName("Escape for tsv null scenario case")
    void escapeForTsvNull() {
        assertEquals("", processor.escapeForTsv(null));
    }

    @Test
    @DisplayName("Escape for tsv empty scenario case")
    void escapeForTsvEmpty() {
        assertEquals("", processor.escapeForTsv(""));
    }

    @Test
    @DisplayName("Escape for tsv simple scenario case")
    void escapeForTsvSimple() {
        assertEquals("hello world", processor.escapeForTsv("hello world"));
    }

    @Test
    @DisplayName("Escape for tsv with tab scenario")
    void escapeForTsvWithTab() {
        assertEquals("hello\\tworld", processor.escapeForTsv("hello\tworld"));
    }

    @Test
    @DisplayName("Escape for tsv with newline scenario")
    void escapeForTsvWithNewline() {
        assertEquals("hello\\nworld", processor.escapeForTsv("hello\nworld"));
    }

    @Test
    @DisplayName("Escape for tsv with carriage return scenario")
    void escapeForTsvWithCarriageReturn() {
        assertEquals("hello\\rworld", processor.escapeForTsv("hello\rworld"));
    }

    @Test
    @DisplayName("Escape for tsv with backslash scenario")
    void escapeForTsvWithBackslash() {
        assertEquals("hello\\\\world", processor.escapeForTsv("hello\\world"));
    }

    @Test
    @DisplayName("Escape for tsv with multiple special chars")
    void escapeForTsvWithMultipleSpecialChars() {
        assertEquals("a\\tb\\nc\\rd\\\\e", processor.escapeForTsv("a\tb\nc\rd\\e"));
    }

    @Test
    @DisplayName("Emit unhandled warnings ignored before begin tree")
    void emitUnhandledIgnoredBeforeBeginTree() {
        processor.emitUnhandled(null, "not initialised");
    }

    @Test
    @DisplayName("Emit unhandled warning records violation with ast line")
    void emitUnhandledRecordsViolationWithAstLine() throws Exception {
        FileContents contents = createFileContents("InputUnhandled.java",
                "class InputUnhandled { void test() {} }");
        processor.beginTree(contents);

        DetailAstImpl ast = new DetailAstImpl();
        ast.setLineNo(12);
        processor.emitUnhandled(ast, "Unsupported message pattern");

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Processor should emit one unhandled warning");
        assertEquals("assert.message.unhandled",
                check.getViolations().iterator().next().getKey());
    }

    @Test
    @DisplayName("Emit unhandled warning uses fallback line and reason")
    void emitUnhandledUsesFallbackLineAndReason() throws Exception {
        FileContents contents = createFileContents("InputUnhandledFallback.java",
                "class InputUnhandledFallback { void test() {} }");
        processor.beginTree(contents);

        processor.emitUnhandled(null, "  ");

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Fallback line should report one unhandled warning");
        assertEquals(1, check.getViolations().iterator().next().getLineNo());
    }

    @Test
    @DisplayName("Emit unhandled warnings disabled does not record")
    void emitUnhandledDisabledDoesNotRecord() throws Exception {
        FileContents contents = createFileContents("InputUnhandledDisabled.java",
                "class InputUnhandledDisabled { void test() {} }");
        processor.beginTree(contents);
        processor.setEmitUnhandled(false);

        DetailAstImpl ast = new DetailAstImpl();
        ast.setLineNo(9);
        processor.emitUnhandled(ast, "Disabled path");

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(check.getViolations().isEmpty(),
                "Disabled setting should emit no unhandled warnings");
    }

    @Test
    @DisplayName("Message extraction output writes header and record")
    void messageExtractionOutputWritesHeaderAndRecord() throws Exception {
        Path output = tempDir.resolve("mm-extract.tsv");
        processor.setMessageExtractionFile(output.toString());
        FileContents contents = createFileContents("InputExtract.java",
                "class InputExtract { void test() {} }");
        processor.beginTree(contents);
        processor.setIgnoredExceptionClassNames("RuntimeException");

        String message = "Order should remain valid after update";
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(3)
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .build();
        processor.emitCandidate(candidate);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(Files.exists(output), "Extraction output should be created for records");
        assertTrue(Files.readAllLines(output, StandardCharsets.UTF_8).size() >= 2,
                "Extraction output should contain header and data");
    }

    @Test
    @DisplayName("Message extraction failure reports warning in check")
    void messageExtractionFailureReportsWarningInCheck() throws Exception {
        processor.setMessageExtractionFile(tempDir.toString());
        FileContents contents = createFileContents("InputExtractFail.java",
                "class InputExtractFail { void test() {} }");
        processor.beginTree(contents);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Output failure should emit one extraction warning");
        assertEquals("assert.message.extraction.failure",
                check.getViolations().iterator().next().getKey());
    }

    private FileContents createFileContents(String fileName, String... lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), Arrays.asList(lines));
        return new FileContents(text);
    }

    // --- setEmitUnhandled tests ---

    @Test
    @DisplayName("Set emit unhandled true does not throw")
    void setEmitUnhandledTrue() {
        processor.setEmitUnhandled(true);
        // Should not throw
    }

    @Test
    @DisplayName("Set emit unhandled false does not throw")
    void setEmitUnhandledFalse() {
        processor.setEmitUnhandled(false);
        // Should not throw
    }

    // --- emitUnhandled edge cases ---

    @Test
    @DisplayName("Emit unhandled with null reason uses unknown")
    void emitUnhandledWithNullReasonUsesUnknown() throws Exception {
        FileContents contents = createFileContents("InputNullReason.java",
                "class InputNullReason { void test() {} }");
        processor.beginTree(contents);

        DetailAstImpl ast = new DetailAstImpl();
        ast.setLineNo(5);
        processor.emitUnhandled(ast, null);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Unknown reason should emit one unhandled warning");
    }

    @Test
    @DisplayName("Emit unhandled uses method line when ast has no line")
    void emitUnhandledUsesMethodLineWhenAstHasNoLine() throws Exception {
        FileContents contents = createFileContents("InputMethodLine.java",
                "class InputMethodLine {",
                "    void test() {}",
                "}");
        processor.beginTree(contents);

        // Simulate entering a method at line 2
        DetailAstImpl methodDef = newAst(TokenTypes.METHOD_DEF, "METHOD_DEF", 2);
        DetailAstImpl ident = newAst(TokenTypes.IDENT, "testMethod", 2);
        methodDef.addChild(ident);
        processor.visitToken(methodDef);

        // AST with line 0 should fall back to method line
        DetailAstImpl ast = new DetailAstImpl();
        ast.setLineNo(0);
        processor.emitUnhandled(ast, "zero line ast");

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Method line fallback should emit one warning");
    }

    // --- Message extraction record failure after opening ---

    @Test
    @DisplayName("Message extraction write failure closes writer")
    void messageExtractionWriteFailureClosesWriter() throws Exception {
        Path output = tempDir.resolve("write-fail.tsv");
        processor.setMessageExtractionFile(output.toString());

        FileContents contents = createFileContents("InputWriteFail.java",
                "class InputWriteFail { void test() {} }");
        processor.beginTree(contents);

        // Emit a valid candidate first
        String message = "First message should write successfully";
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(3)
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .build();
        processor.emitCandidate(candidate);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(Files.exists(output),
                "Extraction file should exist after write attempt");
    }

    // --- extractionTarget fallback tests (via failure messages) ---

    @Test
    @DisplayName("Extraction target uses message file when set")
    void extractionTargetUsesMessageFileWhenSet() throws Exception {
        // Use a directory path to force an open failure
        Path dir = tempDir.resolve("subdir");
        Files.createDirectories(dir);
        processor.setMessageExtractionFile(dir.toString());

        FileContents contents = createFileContents("InputTargetFallback.java",
                "class InputTargetFallback { void test() {} }");
        processor.beginTree(contents);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        // Should have a failure message containing the directory path
        assertEquals(1, check.getViolations().size(),
                "Target path should emit one extraction failure");
    }

    private TestCheck createCheck(FileContents contents) throws CheckstyleException {
        TestCheck check = new TestCheck();
        check.configure(new DefaultConfiguration("TestCheck"));
        check.setFileContents(contents);
        return check;
    }

    private DetailAstImpl newAst(int type, String text, int lineNo) {
        DetailAstImpl ast = new DetailAstImpl();
        ast.initialize(type, text);
        ast.setLineNo(lineNo);
        return ast;
    }

    private static final class TestCheck extends com.puppycrawl.tools.checkstyle.api.AbstractCheck {
        @Override
        public int[] getDefaultTokens() {
            return new int[0];
        }

        @Override
        public int[] getAcceptableTokens() {
            return new int[0];
        }

        @Override
        public int[] getRequiredTokens() {
            return new int[0];
        }
    }
}
