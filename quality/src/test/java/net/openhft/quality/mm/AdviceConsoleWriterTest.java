/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AdviceConsoleWriter} to improve branch and mutation coverage.
 */
@DisplayName("Advice console writer tests")
class AdviceConsoleWriterTest {

    // Constructor param order: lineNo, source, messageLiteral, messageExpr, lineText, snippet

    @Test
    @DisplayName("Console writer prints file and advice headers")
    void consoleWriter_printsHeaders() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "missing message", null, "assertTrue(condition)", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileAdviceDetails fileDetails = new FileAdviceDetails(AdviceId.MMOverusedWord, 3,
                Collections.singletonList("value"), 5, null, null, null, null, null);
        FileAdviceGroup fileGroup = new FileAdviceGroup(AdviceId.MMOverusedWord,
                loader.textFor(AdviceId.MMOverusedWord), 1, fileDetails);
        FileReport report = new FileReport("Test.java",
                Collections.singletonList(fileGroup),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }

        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("FILE: Test.java"),
                "output should include file header");
        assertTrue(output.contains("FILE-LEVEL MMOverusedWord"),
                "output should include file-level advice header");
        assertTrue(output.contains("MMAssertionMessageMissing"),
                "output should include line-level advice header");
    }

    @Test
    @DisplayName("write returns early for null report")
    void write_returnsEarlyForNullReport() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(null);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.isEmpty(), "null report should produce no output");
    }

    @Test
    @DisplayName("write returns early for report without issues")
    void write_returnsEarlyForReportWithoutIssues() {
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.isEmpty(), "report without issues should produce no output");
    }

    @Test
    @DisplayName("writeRunSummary prints summary for zero issues")
    void writeRunSummary_printsSummaryForZeroIssues() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).writeRunSummary(5, 0);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("5 files read"), "summary should include file count");
        assertTrue(output.contains("no issues found"), "summary should indicate no issues");
    }

    @Test
    @DisplayName("writeRunSummary is silent when issues exist")
    void writeRunSummary_silentWhenIssuesExist() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).writeRunSummary(5, 3);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.isEmpty(), "summary should be silent when issues exist");
    }

    @Test
    @DisplayName("Occurrence with lineText prints line content")
    void occurrence_withLineTextPrintsLineContent() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        // Constructor: lineNo, source, messageLiteral, messageExpr, lineText, snippet
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                null, null, "assertTrue(condition);", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("line: \"assertTrue(condition);\""),
                "output should include line text");
    }

    @Test
    @DisplayName("Occurrence with snippet prints snippet")
    void occurrence_withSnippetPrintsSnippet() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        // Constructor: lineNo, source, messageLiteral, messageExpr, lineText, snippet
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                null, null, null, "assertTrue(cond)");
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("snippet=\"assertTrue(cond)\""),
                "output should include snippet");
    }

    @Test
    @DisplayName("Occurrence with neither snippet nor lineText prints hints")
    void occurrence_withNeitherSnippetNorLineTextPrintsHints() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                null, null, null, null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("hint="), "output should include hints when no snippet or line text");
    }

    @Test
    @DisplayName("File advice with line number prints line reference")
    void fileAdvice_withLineNumberPrintsLineReference() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        FileAdviceDetails fileDetails = new FileAdviceDetails(AdviceId.MMOverusedWord, 42,
                Collections.singletonList("value"), 5, null, null, null, null, null);
        FileAdviceGroup fileGroup = new FileAdviceGroup(AdviceId.MMOverusedWord,
                loader.textFor(AdviceId.MMOverusedWord), 1, fileDetails);
        FileReport report = new FileReport("Test.java",
                Collections.singletonList(fileGroup),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("L42"), "output should include line number reference");
    }

    @Test
    @DisplayName("File advice with empty items does not print result")
    void fileAdvice_emptyItemsDoesNotPrintResult() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        FileAdviceDetails fileDetails = new FileAdviceDetails(AdviceId.MMOverusedWord, 0,
                Collections.emptyList(), 5, null, null, null, null, null);
        FileAdviceGroup fileGroup = new FileAdviceGroup(AdviceId.MMOverusedWord,
                loader.textFor(AdviceId.MMOverusedWord), 1, fileDetails);
        FileReport report = new FileReport("Test.java",
                Collections.singletonList(fileGroup),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("Result:"), "output should not include Result: when items are empty");
    }

    @Test
    @DisplayName("Verbose mode prints candidates")
    void verboseMode_printsCandidates() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        Map<Integer, List<CandidateAdvice>> candidatesByLine = new HashMap<>();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(12)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .metrics(new AdviceMetrics(5, 3, 7, 4, 2, 1,
                        4, 42, 2, 35, ">=", "left", "right", "contains", "str", "arg"))
                .build();
        candidatesByLine.put(12, Collections.singletonList(candidate));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                candidatesByLine,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("Verbose candidates:"), "verbose mode should print candidates header");
        assertTrue(output.contains("words=5"), "verbose mode should print word count");
        assertTrue(output.contains("meaningful=3"), "verbose mode should print meaningful count");
        assertTrue(output.contains("minWords=4"), "verbose mode should print minWords");
        assertTrue(output.contains("maxWords=42"), "verbose mode should print maxWords");
        assertTrue(output.contains("comparison=>="), "verbose mode should print comparison operator");
        assertTrue(output.contains("search=contains"), "verbose mode should print search method");
    }

    @Test
    @DisplayName("Verbose mode prints metrics with non-zero totalWordCount")
    void verboseMode_printsNonZeroTotalWordCount() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        Map<Integer, List<CandidateAdvice>> candidatesByLine = new HashMap<>();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(12)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .metrics(new AdviceMetrics(5, 3, 8, 5, 2, 1,
                        null, null, null, null, null, null, null, null, null, null))
                .build();
        candidatesByLine.put(12, Collections.singletonList(candidate));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                candidatesByLine,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("totalWords=8"), "verbose mode should print non-zero totalWordCount");
        assertTrue(output.contains("effectiveMeaningful=5"),
                "verbose mode should print non-zero effectiveMeaningfulWordCount");
    }

    @Test
    @DisplayName("Verbose mode omits zero totalWordCount and effectiveMeaningfulWordCount")
    void verboseMode_omitsZeroTotalWordCountAndEffective() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        Map<Integer, List<CandidateAdvice>> candidatesByLine = new HashMap<>();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(12)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .metrics(new AdviceMetrics(5, 3, 0, 0, 2, 1,
                        null, null, null, null, null, null, null, null, null, null))
                .build();
        candidatesByLine.put(12, Collections.singletonList(candidate));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                candidatesByLine,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("totalWords="), "zero totalWordCount should be omitted");
        assertFalse(output.contains("effectiveMeaningful="), "zero effectiveMeaningfulWordCount should be omitted");
    }

    @Test
    @DisplayName("Verbose mode with null metrics handles gracefully")
    void verboseMode_nullMetricsHandlesGracefully() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        Map<Integer, List<CandidateAdvice>> candidatesByLine = new HashMap<>();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(12)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .build();
        candidatesByLine.put(12, Collections.singletonList(candidate));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                candidatesByLine,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("line=12"), "verbose mode should print line even with null metrics");
        assertFalse(output.contains("words="), "null metrics should not print word counts");
    }

    @Test
    @DisplayName("Verbose mode with null ruleId prints unknown")
    void verboseMode_nullRuleIdPrintsUnknown() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        Map<Integer, List<CandidateAdvice>> candidatesByLine = new HashMap<>();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(12)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .build();
        candidatesByLine.put(12, Collections.singletonList(candidate));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                candidatesByLine,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("rule=unknown"), "null ruleId should print as 'unknown'");
    }

    @Test
    @DisplayName("Verbose mode prints legacy suppressions")
    void verboseMode_printsLegacySuppressions() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        List<RuleId> legacySuppressions = Arrays.asList(RuleId.TOO_SHORT, RuleId.TOO_LONG);
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                legacySuppressions);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("Legacy suppressions:"), "verbose mode should print legacy suppressions header");
        assertTrue(output.contains("MMTooShort"), "verbose mode should list suppressed rule codes");
        assertTrue(output.contains("MMTooLong"), "verbose mode should list suppressed rule codes");
    }

    @Test
    @DisplayName("Non-verbose mode does not print legacy suppressions")
    void nonVerboseMode_doesNotPrintLegacySuppressions() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        List<RuleId> legacySuppressions = Arrays.asList(RuleId.TOO_SHORT, RuleId.TOO_LONG);
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                legacySuppressions);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("Legacy suppressions:"),
                "non-verbose mode should not print legacy suppressions");
    }

    @Test
    @DisplayName("Verbose mode with empty candidatesByLine skips candidates section")
    void verboseMode_emptyCandidatesByLineSkipsCandidates() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("Verbose candidates:"),
                "verbose mode with empty candidates should not print candidates header");
    }

    @Test
    @DisplayName("Verbose mode with null candidatesByLine skips candidates section")
    void verboseMode_nullCandidatesByLineSkipsCandidates() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                null,
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("Verbose candidates:"),
                "verbose mode with null candidates should not print candidates header");
    }

    @Test
    @DisplayName("Verbose mode with empty legacySuppressions skips section")
    void verboseMode_emptyLegacySuppressionsSkipsSection() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "test", null, "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));

        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(true).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(output.contains("Legacy suppressions:"),
                "verbose mode with empty legacy suppressions should skip section");
    }

    @Test
    @DisplayName("Occurrence with messageExpr but no messageLiteral uses expr")
    void occurrence_usesMessageExprWhenLiteralNull() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        // Constructor: lineNo, source, messageLiteral, messageExpr, lineText, snippet
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                null, "expr + message", "assertTrue()", null);
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("message_text=\"expr + message\""),
                "output should use messageExpr when messageLiteral is null");
    }

    @Test
    @DisplayName("Multiple occurrences are all printed")
    void multipleOccurrences_allPrinted() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        List<AdviceOccurrence> occurrences = new ArrayList<>();
        occurrences.add(new AdviceOccurrence(10, AdviceSource.ASSERTION,
                "first", null, "assert1()", null));
        occurrences.add(new AdviceOccurrence(20, AdviceSource.ASSERTION,
                "second", null, "assert2()", null));
        occurrences.add(new AdviceOccurrence(30, AdviceSource.ASSERTION,
                "third", null, "assert3()", null));
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 3,
                occurrences);
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("L10"), "should print first occurrence line");
        assertTrue(output.contains("L20"), "should print second occurrence line");
        assertTrue(output.contains("L30"), "should print third occurrence line");
    }
}
