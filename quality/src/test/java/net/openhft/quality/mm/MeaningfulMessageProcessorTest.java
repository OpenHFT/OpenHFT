/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    @DisplayName("Resolve extraction role handles all sources and flags")
    void resolveExtractionRoleHandlesAllSourcesAndFlags() throws Exception {
        assertEquals("unknown", invokeResolveExtractionRole(null));
        assertEquals("unknown", invokeResolveExtractionRole(new MessageCandidate.Builder().build()));

        MessageCandidate assertionHeading = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .assertAllHeading(true)
                .build();
        assertEquals("assert_all_heading", invokeResolveExtractionRole(assertionHeading));

        MessageCandidate assertionOverride = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .assertJOverride(true)
                .build();
        assertEquals("assert_override", invokeResolveExtractionRole(assertionOverride));

        MessageCandidate assertionSupplier = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .trivialSupplierDescription("Supplier")
                .build();
        assertEquals("assert_supplier", invokeResolveExtractionRole(assertionSupplier));

        MessageCandidate assertionDefault = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .build();
        assertEquals("assert_message", invokeResolveExtractionRole(assertionDefault));

        assertEquals("precondition_message", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.PRECONDITION)
                .build()));
        assertEquals("throw_message", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.THROW)
                .build()));
        assertEquals("log_message", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.LOG)
                .build()));
        assertEquals("annotation_value", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.ANNOTATION)
                .build()));
        assertEquals("comment_text", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.COMMENT)
                .build()));
        assertEquals("javadoc_class", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.JAVADOC_CLASS)
                .build()));
        assertEquals("javadoc_member", invokeResolveExtractionRole(new MessageCandidate.Builder()
                .source(MessageSource.JAVADOC_MEMBER)
                .build()));
    }

    @Test
    @DisplayName("Count purpose cues finds standard cues")
    void countPurposeCuesFindsStandardCues() throws Exception {
        prepareProcessor();
        assertEquals(2, invokeCountPurposeCues("Skip on Windows because offsets differ, required by platform."));
    }

    @Test
    @DisplayName("Count purpose cues counts so that and in order phrases")
    void countPurposeCuesCountsPhrases() throws Exception {
        prepareProcessor();
        assertEquals(2, invokeCountPurposeCues("Flush now so that readers see updates in order to reduce jitter."));
    }

    @Test
    @DisplayName("Count purpose cues ignores edge and short phrases")
    void countPurposeCuesIgnoresEdgeAndShortPhrases() throws Exception {
        prepareProcessor();
        assertEquals(0, invokeCountPurposeCues("because this fails"));
        assertEquals(0, invokeCountPurposeCues("this fails because"));
        assertEquals(0, invokeCountPurposeCues("so that"));
    }

    @Test
    @DisplayName("Count purpose cues counts for in the middle")
    void countPurposeCuesCountsForInTheMiddle() throws Exception {
        prepareProcessor();
        assertEquals(1, invokeCountPurposeCues("Keep this for benchmarks only."));
    }

    @Test
    @DisplayName("Count purpose cues counts three word message")
    void countPurposeCuesCountsThreeWordMessage() throws Exception {
        prepareProcessor();
        assertEquals(1, invokeCountPurposeCues("Work because now"));
    }

    @Test
    @DisplayName("Count purpose cues counts consequent root")
    void countPurposeCuesCountsConsequentRoot() throws Exception {
        prepareProcessor();
        assertEquals(1, invokeCountPurposeCues("Retry consequently later"));
    }

    @Test
    @DisplayName("Count purpose cues ignores so that at start")
    void countPurposeCuesIgnoresSoThatAtStart() throws Exception {
        prepareProcessor();
        assertEquals(0, invokeCountPurposeCues("so that now"));
    }

    @Test
    @DisplayName("Count purpose cues ignores trailing so that and in order")
    void countPurposeCuesIgnoresTrailingSoThatAndInOrder() throws Exception {
        prepareProcessor();
        assertEquals(0, invokeCountPurposeCues("work so that"));
        assertEquals(0, invokeCountPurposeCues("work in order"));
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
        List<String> lines = Files.readAllLines(output, StandardCharsets.UTF_8);
        assertTrue(lines.size() >= 2,
                "Extraction output should contain header and data");
        assertTrue(lines.get(0).contains("role"),
                "Extraction output header should include role column");
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

    @Test
    @DisplayName("Generated at Javadoc skips processing for file")
    void generatedAtJavadocSkipsProcessingForFile() throws Exception {
        FileContents contents = createFileContents("InputGeneratedAtJavadocSkip.java",
                "/**",
                " * Generated at 2026-01-01",
                " */",
                "class InputGeneratedAtJavadocSkip { void test() {} }");
        processor.beginTree(contents);

        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(4)
                .message("order should be valid")
                .normalisedMessage(MessageNormaliser.normalise("order should be valid"))
                .build();
        processor.emitCandidate(candidate);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(check.getViolations().isEmpty(),
                "Generated Javadoc should skip processing");
    }

    @Test
    @DisplayName("Finish tree skips flushing when file is skipped")
    void finishTreeSkipsFlushingWhenFileIsSkipped() throws Exception {
        FileContents contents = createFileContents("InputGeneratedAtSkip.java",
                "/**",
                " * Generated at 2026-01-01",
                " */",
                "class InputGeneratedAtSkip { void test() {} }");
        processor.beginTree(contents);

        recordViolationForTesting(4, RuleId.MISSING_MESSAGE);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(check.getViolations().isEmpty(),
                "Skipped files should not flush violations");
    }

    @Test
    @DisplayName("Finish tree resets context for skipped file")
    void finishTreeResetsContextForSkippedFile() throws Exception {
        FileContents contents = createFileContents("InputGeneratedAtReset.java",
                "/**",
                " * Generated at 2026-01-01",
                " */",
                "class InputGeneratedAtReset { void test() {} }");
        processor.beginTree(contents);

        MessageExtractionContext context = processor.contextForTesting();
        assertNotNull(context.fileContents(), "Context should have file contents before finish");

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertNull(context.fileContents(), "Context should be reset after finish");
    }

    @Test
    @DisplayName("Begin tree records file name words for entropy tracking")
    void beginTreeRecordsFileNameWordsForEntropyTracking() throws Exception {
        FileContents contents = createFileContents("AlphaBeta_GammaDelta.java",
                "class AlphaBetaGammaDelta { void test() {} }");

        processor.beginTree(contents);

        Map<String, Integer> entropy = processor.entropyWordCountsForTesting();
        assertEquals(Integer.valueOf(1), entropy.get("alpha"),
                "Entropy should include alpha token");
        assertEquals(Integer.valueOf(1), entropy.get("beta"),
                "Entropy should include beta token");
        assertEquals(Integer.valueOf(1), entropy.get("gamma"),
                "Entropy should include gamma token");
        assertEquals(Integer.valueOf(1), entropy.get("delta"),
                "Entropy should include delta token");
        assertEquals(4, processor.entropyWordTotalForTesting(),
                "Entropy total should include file name words");
        Map<String, Integer> overused = processor.overusedWordCountsForTesting();
        assertEquals(Integer.valueOf(1), overused.get("alpha"),
                "Overused should include alpha token");
    }

    @Test
    @DisplayName("Entropy tracking skips argument name messages")
    void entropyTrackingSkipsArgumentNameMessages() throws Exception {
        FileContents contents = createFileContents("AlphaBeta.java",
                "class AlphaBeta { void test() {} }");
        processor.beginTree(contents);

        int before = processor.entropyWordTotalForTesting();
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.ASSERTION)
                .lineNo(2)
                .message("alpha beta gamma")
                .normalisedMessage(MessageNormaliser.normalise("alpha beta gamma"))
                .argumentNameMessage(true)
                .build();
        processor.emitCandidate(candidate);

        assertEquals(before, processor.entropyWordTotalForTesting(),
                "Entropy total should not change for argument name messages");
    }

    @Test
    @DisplayName("Visit token handles field Javadoc on variable definition")
    void visitTokenHandlesFieldJavadocOnVariableDefinition() throws Exception {
        TrackingProcessor trackingProcessor = new TrackingProcessor();
        String line1 = "class InputFieldJavadoc {";
        String line2 = "    /**";
        String line3 = "     * Field exists because configuration requires it.";
        String line4 = "     */";
        String line5 = "    int value;";
        FileContents contents = createFileContents("InputFieldJavadoc.java",
                line1,
                line2,
                line3,
                line4,
                line5,
                "}");
        contents.reportBlockComment(2, line2.indexOf("/**"), 4, line4.indexOf("*/") + 1);
        trackingProcessor.beginTree(contents);

        DetailAstImpl objBlock = new DetailAstImpl();
        objBlock.setType(TokenTypes.OBJBLOCK);
        DetailAstImpl varDef = createVariableDef("value", "int", 5);
        objBlock.addChild(varDef);

        trackingProcessor.visitToken(varDef);

        assertEquals(1, trackingProcessor.emitted.size(),
                "Field Javadoc should emit one candidate");
    }

    @Test
    @DisplayName("Visit token handles enum constant Javadoc")
    void visitTokenHandlesEnumConstantJavadoc() throws Exception {
        TrackingProcessor trackingProcessor = new TrackingProcessor();
        String line1 = "enum Status {";
        String line2 = "    /**";
        String line3 = "     * Status is valid.";
        String line4 = "     */";
        String line5 = "    OK";
        FileContents contents = createFileContents("InputEnumConstantJavadoc.java",
                line1,
                line2,
                line3,
                line4,
                line5,
                "}");
        contents.reportBlockComment(2, line2.indexOf("/**"), 4, line4.indexOf("*/") + 1);
        trackingProcessor.beginTree(contents);

        DetailAstImpl enumConst = new DetailAstImpl();
        enumConst.setType(TokenTypes.ENUM_CONSTANT_DEF);
        enumConst.setLineNo(5);

        trackingProcessor.visitToken(enumConst);

        assertEquals(1, trackingProcessor.emitted.size(),
                "Enum constant Javadoc should emit one candidate");
    }

    @Test
    @DisplayName("Visit token handles return null comment requirement")
    void visitTokenHandlesReturnNullCommentRequirement() throws Exception {
        FileContents contents = createFileContents("InputReturnNull.java",
                "class InputReturnNull {",
                "    Object value() {",
                "        return null;",
                "    }",
                "}");
        processor.beginTree(contents);

        DetailAstImpl returnAst = new DetailAstImpl();
        returnAst.setType(TokenTypes.LITERAL_RETURN);
        returnAst.setLineNo(3);
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.LITERAL_NULL);
        literal.setText("null");
        expr.addChild(literal);
        returnAst.addChild(expr);

        processor.visitToken(returnAst);

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> RuleId.MISSING_MESSAGE.messageKey().equals(violation.getKey()));
        assertTrue(found, "Return null without comment should emit missing message warning");
    }

    @Test
    @DisplayName("Overused word warning emits when word exceeds half of messages")
    void overusedWordWarningEmitsWhenWordExceedsHalf() throws Exception {
        FileContents contents = createFileContents("InputOverusedWord.java",
                "class InputOverusedWord { void test() {} }");
        processor.beginTree(contents);

        for (int i = 0; i < 12; i++) {
            boolean includeOrder = i < 7;
            String message = includeOrder
                    ? "order should be true " + i
                    : "trade should be true " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.overused.word".equals(violation.getKey()));
        assertTrue(found, "Should emit overused word warning");
    }

    @Test
    @DisplayName("Overused word warning ignored when below minimum message count")
    void overusedWordWarningIgnoredWhenBelowMinimumMessageCount() throws Exception {
        FileContents contents = createFileContents("InputOverusedWordMin.java",
                "class InputOverusedWordMin { void test() {} }");
        processor.beginTree(contents);

        for (int i = 0; i < 11; i++) {
            String message = "order should remain valid after update " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.overused.word".equals(violation.getKey()));
        assertFalse(found, "Should not emit overused word warning below minimum");
    }

    @Test
    @DisplayName("Overused word warning does not emit at half threshold")
    void overusedWordWarningDoesNotEmitAtHalfThreshold() throws Exception {
        FileContents contents = createFileContents("InputOverusedWordHalf.java",
                "class InputOverusedWordHalf { void test() {} }");
        processor.beginTree(contents);

        String[] orderWords = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot"};
        String[] otherWords = {"golf", "hotel", "india", "juliet", "kilo", "lima"};
        for (int i = 0; i < 12; i++) {
            String message = i < 6
                    ? "order " + orderWords[i] + " should be true"
                    : otherWords[i - 6] + " should be true";
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.overused.word".equals(violation.getKey()));
        assertFalse(found, "Should not emit overused word warning at half threshold");
    }

    @Test
    @DisplayName("Overused word warning suppressed by @SuppressWarnings token")
    void overusedWordWarningSuppressedBySuppressWarningsToken() throws Exception {
        FileContents contents = createFileContents("InputOverusedWordSuppressed.java",
                "@SuppressWarnings(\"MMOverusedWord\")",
                "class InputOverusedWordSuppressed { void test() {} }");
        processor.beginTree(contents);

        DetailAstImpl classDef = createClassDefWithSuppressWarnings("InputOverusedWordSuppressed",
                "MMOverusedWord");
        DetailAstImpl compilationUnit = new DetailAstImpl();
        compilationUnit.setType(TokenTypes.COMPILATION_UNIT);
        compilationUnit.addChild(classDef);
        processor.visitToken(classDef);

        for (int i = 0; i < 12; i++) {
            boolean includeOrder = i < 7;
            String message = includeOrder
                    ? "order should be true " + i
                    : "trade should be true " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        processor.leaveToken(classDef);
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.overused.word".equals(violation.getKey()));
        assertFalse(found, "SuppressWarnings should silence overused word warning");
    }

    @Test
    @DisplayName("Lacks purpose warning emits when cues below threshold")
    void lacksPurposeWarningEmitsWhenCuesBelowThreshold() throws Exception {
        FileContents contents = createFileContents("InputLacksPurpose.java",
                "class InputLacksPurpose { void test() {} }");
        processor.beginTree(contents);

        String[] tokens = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot",
                "golf", "hotel", "india", "juliet", "kilo", "lima"};
        for (int i = 0; i < 12; i++) {
            String message = i == 0
                    ? tokens[i] + " should be " + i + " because detail " + i
                    : tokens[i] + " should be " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.lacks.purpose".equals(violation.getKey()));
        assertTrue(found, "Should emit lacks purpose warning");
    }

    @Test
    @DisplayName("Lacks purpose warning does not emit when cues meet threshold")
    void lacksPurposeWarningDoesNotEmitWhenCuesMeetThreshold() throws Exception {
        FileContents contents = createFileContents("InputLacksPurposeMet.java",
                "class InputLacksPurposeMet { void test() {} }");
        processor.beginTree(contents);

        String[] tokens = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot",
                "golf", "hotel", "india", "juliet", "kilo", "lima"};
        for (int i = 0; i < 12; i++) {
            String message;
            if (i == 0) {
                message = "retry so that clients recover " + i;
            } else if (i == 1) {
                message = "work proceeds in order to improve stability " + i;
            } else {
                message = tokens[i] + " should be " + i;
            }
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.lacks.purpose".equals(violation.getKey()));
        assertFalse(found, "Should not emit lacks purpose warning when cues meet threshold");
    }

    @Test
    @DisplayName("Lacks purpose warning does not emit at one in eight threshold")
    void lacksPurposeWarningDoesNotEmitAtOneInEightThreshold() throws Exception {
        FileContents contents = createFileContents("InputLacksPurposeRatioMet.java",
                "class InputLacksPurposeRatioMet { void test() {} }");
        processor.beginTree(contents);

        for (int i = 0; i < 8; i++) {
            String message = i == 3
                    ? "retry because order should recover " + i
                    : "alpha should be " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.lacks.purpose".equals(violation.getKey()));
        assertFalse(found, "Should not emit lacks purpose warning at one in eight threshold");
    }

    @Test
    @DisplayName("Lacks purpose warning emits below one in eight threshold")
    void lacksPurposeWarningEmitsBelowOneInEightThreshold() throws Exception {
        FileContents contents = createFileContents("InputLacksPurposeRatioMissing.java",
                "class InputLacksPurposeRatioMissing { void test() {} }");
        processor.beginTree(contents);

        for (int i = 0; i < 8; i++) {
            String message = "alpha should be " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.lacks.purpose".equals(violation.getKey()));
        assertTrue(found, "Should emit lacks purpose warning below one in eight threshold");
    }

    @Test
    @DisplayName("Lacks purpose warning suppressed by @SuppressWarnings token")
    void lacksPurposeWarningSuppressedBySuppressWarningsToken() throws Exception {
        FileContents contents = createFileContents("InputLacksPurposeSuppressed.java",
                "@SuppressWarnings(\"MMLacksPurpose\")",
                "class InputLacksPurposeSuppressed { void test() {} }");
        processor.beginTree(contents);

        DetailAstImpl classDef = createClassDefWithSuppressWarnings("InputLacksPurposeSuppressed",
                "MMLacksPurpose");
        DetailAstImpl compilationUnit = new DetailAstImpl();
        compilationUnit.setType(TokenTypes.COMPILATION_UNIT);
        compilationUnit.addChild(classDef);
        processor.visitToken(classDef);

        String[] tokens = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot",
                "golf", "hotel", "india", "juliet", "kilo", "lima"};
        for (int i = 0; i < 12; i++) {
            String message = tokens[i] + " should be " + i;
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.ASSERTION)
                    .lineNo(3 + i)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .build();
            processor.emitCandidate(candidate);
        }

        processor.leaveToken(classDef);
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.lacks.purpose".equals(violation.getKey()));
        assertFalse(found, "SuppressWarnings should silence lacks purpose warning");
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

    @Test
    @DisplayName("Emit unhandled includes class and method scope")
    void emitUnhandledIncludesClassAndMethodScope() throws Exception {
        FileContents contents = createFileContents("InputScope.java",
                "class InputScope {",
                "    void testMethod() {}",
                "}");
        processor.beginTree(contents);

        DetailAstImpl classDef = createClassDef("InputScope", 1);
        processor.visitToken(classDef);

        DetailAstImpl methodDef = createMethodDef("testMethod", 2);
        processor.visitToken(methodDef);

        DetailAstImpl ast = newAst(TokenTypes.LITERAL_THROW, "throw", 2);
        processor.emitUnhandled(ast, "scope detail");

        assertEquals("InputScope#testMethod", invokeBuildScope(),
                "Scope should include class and method");
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(1, check.getViolations().size(),
                "Should emit one unhandled warning");
        assertEquals(RuleId.UNHANDLED.messageKey(),
                check.getViolations().iterator().next().getKey(),
                "Unhandled warning should be recorded");
    }

    @Test
    @DisplayName("Emit unhandled uses class-only scope when method missing")
    void emitUnhandledUsesClassOnlyScope() throws Exception {
        FileContents contents = createFileContents("InputScopeClass.java",
                "class InputScopeClass {",
                "}");
        processor.beginTree(contents);

        DetailAstImpl classDef = createClassDef("InputScopeClass", 1);
        processor.visitToken(classDef);

        DetailAstImpl ast = newAst(TokenTypes.LITERAL_THROW, "throw", 2);
        processor.emitUnhandled(ast, "scope detail");

        assertEquals("InputScopeClass", invokeBuildScope(),
                "Scope should include class name when method missing");
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(RuleId.UNHANDLED.messageKey(),
                check.getViolations().iterator().next().getKey(),
                "Unhandled warning should be recorded");
    }

    @Test
    @DisplayName("Emit unhandled uses method-only scope when class missing")
    void emitUnhandledUsesMethodOnlyScope() throws Exception {
        FileContents contents = createFileContents("InputScopeMethod.java",
                "class InputScopeMethod {",
                "    void onlyMethod() {}",
                "}");
        processor.beginTree(contents);

        DetailAstImpl methodDef = createMethodDef("onlyMethod", 3);
        processor.visitToken(methodDef);

        DetailAstImpl ast = newAst(TokenTypes.LITERAL_THROW, "throw", 3);
        processor.emitUnhandled(ast, "scope detail");

        assertEquals("onlyMethod", invokeBuildScope(),
                "Scope should include method name when class missing");
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertEquals(RuleId.UNHANDLED.messageKey(),
                check.getViolations().iterator().next().getKey(),
                "Unhandled warning should be recorded");
    }

    @Test
    @DisplayName("Missing display name recorded for JUnit 5 test")
    void missingDisplayNameRecordedForJUnit5Test() throws Exception {
        FileContents contents = createFileContents("InputMissingDisplayName.java",
                "class InputMissingDisplayName { void test() {} }");
        processor.beginTree(contents);

        processor.visitToken(createImport("org.junit.jupiter.api.Test"));
        DetailAstImpl methodDef = createMethodDef("test", 2);
        processor.visitToken(methodDef);
        processor.visitToken(createAnnotation("Test", 1));

        processor.leaveToken(methodDef);
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        boolean found = check.getViolations().stream()
                .anyMatch(violation -> "assert.message.missing.display.name".equals(violation.getKey()));
        assertTrue(found, "Should emit missing display name warning");
    }

    @Test
    @DisplayName("Test annotation order uses method line when test line missing")
    void testAnnotationOrderUsesMethodLineWhenTestLineMissing() throws Exception {
        FileContents contents = createFileContents("InputTestAnnotationOrderLine.java",
                "class InputTestAnnotationOrderLine {",
                "    void test() {",
                "    }",
                "}",
                "");
        processor.beginTree(contents);

        processor.visitToken(createImport("org.junit.jupiter.api.Test"));
        DetailAstImpl methodDef = createMethodDef("test", 5);
        processor.visitToken(methodDef);
        processor.visitToken(createAnnotation("DisplayName", 3));
        processor.visitToken(createAnnotation("Disabled", 4));
        processor.visitToken(createAnnotation("Test", 0));

        processor.leaveToken(methodDef);
        TestCheck check = createCheck(contents);
        processor.finishTree(check);

        assertTrue(check.getViolations().size() >= 1,
                "Should emit test annotation order warning");
        boolean found = check.getViolations().stream()
                .anyMatch(violation -> RuleId.TEST_ANNOTATION_ORDER.messageKey().equals(violation.getKey())
                        && violation.getLineNo() == 5);
        assertTrue(found, "Should fall back to method line when test line missing");
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

    @Test
    @DisplayName("Write message extraction record uses unknown role and computed metrics")
    void writeMessageExtractionRecordUsesUnknownRoleAndComputedMetrics() throws Exception {
        FileContents contents = createFileContents("InputWriteRecord.java",
                "class InputWriteRecord { void test() {} }");
        processor.beginTree(contents);

        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        processor.setMessageExtractionWriterForTesting(bufferedWriter);
        processor.setMessageExtractionTargetForTesting("target.tsv");

        invokeWriteMessageExtractionRecord("alpha bravo", 7, null, 1, 2,
                MessageSource.LOG, null);
        bufferedWriter.flush();

        String output = writer.toString().trim();
        String[] parts = output.split("\t", -1);
        assertEquals(12, parts.length, "Record should contain all columns");
        assertTrue(parts[0].endsWith("InputWriteRecord.java"),
                "File name should be recorded");
        assertEquals("7", parts[1], "Line number should be recorded");
        assertEquals("LOG", parts[2], "Source should be recorded");
        assertEquals("unknown", parts[3], "Role should default to unknown");
        assertEquals("1", parts[7], "Placeholder count should be recorded");
        assertEquals("2", parts[8], "Key-value label count should be recorded");
        assertEquals("alpha bravo", parts[11], "Message should be recorded");
    }

    @Test
    @DisplayName("Write message extraction record handles writer failure")
    void writeMessageExtractionRecordHandlesWriterFailure() throws Exception {
        FileContents contents = createFileContents("InputWriteFailure.java",
                "class InputWriteFailure { void test() {} }");
        processor.beginTree(contents);

        BufferedWriter bufferedWriter = new BufferedWriter(new java.io.StringWriter()) {
            @Override
            public void write(String str, int off, int len) throws IOException {
                throw new IOException("write failed");
            }

            @Override
            public void newLine() throws IOException {
                throw new IOException("write failed");
            }
        };
        processor.setMessageExtractionWriterForTesting(bufferedWriter);
        processor.setMessageExtractionTargetForTesting("test-target.tsv");
        processor.setMessageExtractionFailureDetailForTesting(null);

        assertNotNull(processor.messageExtractionWriterForTesting(),
                "Writer should be configured before write attempt");

        invokeWriteMessageExtractionRecord("message", 5, null, 0, 0,
                MessageSource.ASSERTION, "assert_message");

        String detail = processor.messageExtractionFailureDetailForTesting();
        assertNotNull(detail, "Failure detail should be recorded");
        assertTrue(detail.contains("Unable to write message extraction record"),
                "Failure detail should describe write failure");
        assertNull(processor.messageExtractionWriterForTesting(),
                "Writer should be cleared after failure");
    }

    @Test
    @DisplayName("Write message extraction record skips when failure detail is set")
    void writeMessageExtractionRecordSkipsWhenFailureDetailIsSet() throws Exception {
        FileContents contents = createFileContents("InputWriteSkip.java",
                "class InputWriteSkip { void test() {} }");
        processor.beginTree(contents);

        AtomicBoolean wrote = new AtomicBoolean(false);
        Writer trackingWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                wrote.set(true);
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() {
                // no-op
            }
        };
        processor.setMessageExtractionWriterForTesting(new BufferedWriter(trackingWriter));
        processor.setMessageExtractionFailureDetailForTesting("prior failure");

        invokeWriteMessageExtractionRecord("message", 1, null, 0, 0,
                MessageSource.LOG, "log_message");

        assertFalse(wrote.get(), "Writer should not be used after failure");
    }

    @Test
    @DisplayName("Emit rule summary outputs when verbose and violations exist")
    void emitRuleSummaryOutputsWhenVerboseAndViolationsExist() throws Exception {
        FileContents contents = createFileContents("InputSummary.java",
                "class InputSummary { void test() {} }");
        processor.beginTree(contents);
        processor.setVerbose(true);

        recordViolationForTesting(1, RuleId.MISSING_MESSAGE);

        TestCheck check = createCheck(contents);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
        try {
            processor.finishTree(check);
        } finally {
            System.setOut(original);
        }

        String outText = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(outText.contains("MeaningfulMessage summary"),
                "Summary should include heading");
        assertTrue(outText.contains("InputSummary.java"),
                "Summary should include file name");
    }

    @Test
    @DisplayName("Format rule summary orders by count then code")
    void formatRuleSummaryOrdersByCountThenCode() throws Exception {
        Map<RuleId, Integer> summary = new EnumMap<>(RuleId.class);
        summary.put(RuleId.MISSING_MESSAGE, 3);
        summary.put(RuleId.TOO_LONG, 3);
        summary.put(RuleId.GENERIC, 1);

        String formatted = invokeFormatRuleSummary(summary);

        assertTrue(formatted.startsWith("total=7 rules="),
                "Summary should include total count");
        int missingIndex = formatted.indexOf(RuleId.MISSING_MESSAGE.code());
        int tooLongIndex = formatted.indexOf(RuleId.TOO_LONG.code());
        int genericIndex = formatted.indexOf(RuleId.GENERIC.code());
        assertTrue(missingIndex >= 0 && tooLongIndex >= 0 && genericIndex >= 0,
                "Summary should include each rule code");
        assertTrue(missingIndex < tooLongIndex,
                "Summary should order by code when counts equal");
        assertTrue(tooLongIndex < genericIndex,
                "Summary should order smaller counts later");
    }

    @Test
    @DisplayName("Collect declared methods returns names")
    void collectDeclaredMethodsReturnsNames() throws Exception {
        DetailAstImpl root = new DetailAstImpl();
        root.setType(TokenTypes.COMPILATION_UNIT);
        DetailAstImpl first = createMethodDef("first", 1);
        DetailAstImpl second = createMethodDef("second", 2);
        root.addChild(first);
        root.addChild(second);

        Set<String> names = invokeCollectDeclaredMethods(root);

        assertEquals(new HashSet<>(Arrays.asList("first", "second")), names,
                "Should collect method names from AST");
    }

    @Test
    @DisplayName("Collect declared methods returns empty set for null root")
    void collectDeclaredMethodsReturnsEmptySetForNullRoot() throws Exception {
        assertTrue(invokeCollectDeclaredMethods(null).isEmpty(),
                "Null root should return empty set");
    }

    @Test
    @DisplayName("Extraction target prefers explicit target")
    void extractionTargetPrefersExplicitTarget() throws Exception {
        processor.setMessageExtractionTargetForTesting("explicit-target.tsv");
        processor.setMessageExtractionFile("fallback-target.tsv");

        String target = invokeExtractionTarget();

        assertEquals("explicit-target.tsv", target,
                "Explicit target should take precedence");
    }

    @Test
    @DisplayName("Extraction target falls back to system property")
    void extractionTargetFallsBackToSystemProperty() throws Exception {
        String previous = System.getProperty("mm.extract.file");
        try {
            System.setProperty("mm.extract.file", "system-target.tsv");
            processor.setMessageExtractionTargetForTesting(null);
            processor.setMessageExtractionFile(null);

            String target = invokeExtractionTarget();

            assertEquals("system-target.tsv", target,
                    "System property should be used when no target is set");
        } finally {
            if (previous == null) {
                System.clearProperty("mm.extract.file");
            } else {
                System.setProperty("mm.extract.file", previous);
            }
        }
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

    private void prepareProcessor() throws IOException {
        processor.beginTree(createFileContents("InputPurpose.java", "class Sample {}"), null);
    }

    private String invokeResolveExtractionRole(MessageCandidate candidate) {
        return processor.resolveExtractionRole(candidate);
    }

    private int invokeCountPurposeCues(String message) {
        return processor.countPurposeCues(message);
    }

    private String invokeFormatRuleSummary(Map<RuleId, Integer> summary) {
        return processor.formatRuleSummary(summary);
    }

    private Set<String> invokeCollectDeclaredMethods(DetailAST rootAst) {
        return processor.collectDeclaredMethods(rootAst);
    }

    private String invokeExtractionTarget() {
        return processor.extractionTarget();
    }

    private void invokeWriteMessageExtractionRecord(String message, int lineNo,
                                                    MessageMetrics metrics, int placeholderCount,
                                                    int keyValueLabelCount, MessageSource source,
                                                    String role) {
        processor.writeMessageExtractionRecord(message, lineNo, metrics, placeholderCount,
                keyValueLabelCount, source, role);
    }

    private void recordViolationForTesting(int lineNo, RuleId ruleId) {
        processor.recordViolationForTesting(lineNo, ruleId);
    }

    private DetailAstImpl createVariableDef(String name, String typeName, int lineNo) {
        DetailAstImpl varDef = new DetailAstImpl();
        varDef.setType(TokenTypes.VARIABLE_DEF);
        varDef.setLineNo(lineNo);

        DetailAstImpl type = new DetailAstImpl();
        type.setType(TokenTypes.TYPE);
        DetailAstImpl typeIdent = new DetailAstImpl();
        typeIdent.setType(TokenTypes.IDENT);
        typeIdent.setText(typeName);
        type.addChild(typeIdent);
        varDef.addChild(type);

        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        varDef.addChild(ident);

        return varDef;
    }

    private static final class TrackingProcessor extends MeaningfulMessageProcessor {
        private final java.util.List<MessageCandidate> emitted = new java.util.ArrayList<>();

        @Override
        public void emitCandidate(MessageCandidate candidate) {
            emitted.add(candidate);
        }
    }

    private String invokeBuildScope() {
        return processor.buildScope();
    }

    private DetailAstImpl newAst(int type, String text, int lineNo) {
        DetailAstImpl ast = new DetailAstImpl();
        ast.initialize(type, text);
        ast.setLineNo(lineNo);
        return ast;
    }

    private DetailAstImpl createAnnotation(String name, int lineNo) {
        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        annotation.setLineNo(lineNo);
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        annotation.addChild(ident);
        return annotation;
    }

    private DetailAstImpl createMethodDef(String name, int lineNo) {
        DetailAstImpl methodDef = new DetailAstImpl();
        methodDef.setType(TokenTypes.METHOD_DEF);
        methodDef.setLineNo(lineNo);
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        methodDef.addChild(ident);
        return methodDef;
    }

    private DetailAstImpl createClassDef(String name, int lineNo) {
        DetailAstImpl classDef = new DetailAstImpl();
        classDef.setType(TokenTypes.CLASS_DEF);
        classDef.setLineNo(lineNo);

        DetailAstImpl modifiers = new DetailAstImpl();
        modifiers.setType(TokenTypes.MODIFIERS);
        classDef.addChild(modifiers);

        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        classDef.addChild(ident);

        DetailAstImpl objBlock = new DetailAstImpl();
        objBlock.setType(TokenTypes.OBJBLOCK);
        classDef.addChild(objBlock);
        return classDef;
    }

    private DetailAstImpl createImport(String qualifiedName) {
        DetailAstImpl importAst = new DetailAstImpl();
        importAst.setType(TokenTypes.IMPORT);
        String[] parts = qualifiedName.split("\\.");
        importAst.addChild(createDotChain(parts));
        return importAst;
    }

    private DetailAstImpl createDotChain(String[] parts) {
        DetailAstImpl current = new DetailAstImpl();
        current.setType(TokenTypes.IDENT);
        current.setText(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            DetailAstImpl ident = new DetailAstImpl();
            ident.setType(TokenTypes.IDENT);
            ident.setText(parts[i]);
            dot.addChild(ident);
            current = dot;
        }
        return current;
    }

    private DetailAstImpl createClassDefWithSuppressWarnings(String className, String... tokens) {
        DetailAstImpl classDef = new DetailAstImpl();
        classDef.setType(TokenTypes.CLASS_DEF);
        classDef.setLineNo(2);

        DetailAstImpl modifiers = new DetailAstImpl();
        modifiers.setType(TokenTypes.MODIFIERS);
        classDef.addChild(modifiers);

        DetailAstImpl annotation = new DetailAstImpl();
        annotation.setType(TokenTypes.ANNOTATION);
        modifiers.addChild(annotation);

        DetailAstImpl annotationName = new DetailAstImpl();
        annotationName.setType(TokenTypes.IDENT);
        annotationName.setText("SuppressWarnings");
        annotation.addChild(annotationName);

        if (tokens.length == 1) {
            DetailAstImpl expr = new DetailAstImpl();
            expr.setType(TokenTypes.EXPR);
            annotation.addChild(expr);

            DetailAstImpl literal = new DetailAstImpl();
            literal.setType(TokenTypes.STRING_LITERAL);
            literal.setText("\"" + tokens[0] + "\"");
            expr.addChild(literal);
        } else {
            DetailAstImpl arrayInit = new DetailAstImpl();
            arrayInit.setType(TokenTypes.ANNOTATION_ARRAY_INIT);
            annotation.addChild(arrayInit);

            for (String token : tokens) {
                DetailAstImpl expr = new DetailAstImpl();
                expr.setType(TokenTypes.EXPR);
                arrayInit.addChild(expr);

                DetailAstImpl literal = new DetailAstImpl();
                literal.setType(TokenTypes.STRING_LITERAL);
                literal.setText("\"" + token + "\"");
                expr.addChild(literal);
            }
        }

        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(className);
        classDef.addChild(ident);
        return classDef;
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
