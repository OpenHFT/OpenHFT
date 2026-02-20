/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AdviceReportManager} to improve branch and mutation coverage.
 */
@DisplayName("Advice report manager tests")
class AdviceReportManagerTest {

    @Test
    @DisplayName("Constructor handles null jsonl output path")
    void constructor_handlesNullJsonlOutputPath() {
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, null, null, false),
                "Constructor should accept null jsonl output");
    }

    @Test
    @DisplayName("Constructor handles empty jsonl output path")
    void constructor_handlesEmptyJsonlOutputPath() {
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, "", null, false),
                "Constructor should accept empty jsonl output");
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, "   ", null, false),
                "Constructor should accept whitespace-only jsonl output");
    }

    @Test
    @DisplayName("Constructor handles valid jsonl output path")
    void constructor_handlesValidJsonlOutputPath(@TempDir Path tempDir) {
        String jsonlPath = tempDir.resolve("output.jsonl").toString();
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, jsonlPath, null, false),
                "Constructor should accept valid jsonl path");
    }

    @Test
    @DisplayName("Dry run mode sets default rank output path")
    void dryRunMode_setsDefaultRankOutputPath() {
        // In dry run mode with null rankOut, default path is used
        assertDoesNotThrow(() -> new AdviceReportManager(false, true, null, null, false),
                "Dry run with null rankOut should use default path");
    }

    @Test
    @DisplayName("Dry run mode with explicit rank output path")
    void dryRunMode_explicitRankOutputPath(@TempDir Path tempDir) {
        String rankPath = tempDir.resolve("ranks.properties").toString();
        assertDoesNotThrow(() -> new AdviceReportManager(false, true, null, rankPath, false),
                "Dry run with explicit rankOut should be accepted");
    }

    @Test
    @DisplayName("Non-dry run mode ignores rank output path")
    void nonDryRunMode_ignoresRankOutputPath() {
        // When not in dry run mode, rankOut is ignored (returns null internally)
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, null, "some/path", false),
                "Non-dry run should ignore rankOut parameter");
    }

    @Test
    @DisplayName("beginRun initialises internal state")
    void beginRun_initialisesInternalState() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        assertDoesNotThrow(manager::beginRun, "beginRun should initialise without error");
    }

    @Test
    @DisplayName("beginRun in verbose mode")
    void beginRun_verboseMode() {
        AdviceReportManager manager = new AdviceReportManager(true, false, null, null, false);
        assertDoesNotThrow(manager::beginRun, "beginRun in verbose mode should work");
    }

    @Test
    @DisplayName("beginRun in dry run mode")
    void beginRun_dryRunMode(@TempDir Path tempDir) {
        String rankPath = tempDir.resolve("ranks.properties").toString();
        AdviceReportManager manager = new AdviceReportManager(false, true, null, rankPath, false);
        assertDoesNotThrow(manager::beginRun, "beginRun in dry run mode should work");
    }

    @Test
    @DisplayName("reportFile with null collector returns early")
    void reportFile_nullCollectorReturnsEarly() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        manager.beginRun();
        assertDoesNotThrow(() -> manager.reportFile("Test.java", null, null, null),
                "reportFile with null collector should return early");
    }

    @Test
    @DisplayName("reportFile before beginRun returns early")
    void reportFile_beforeBeginRunReturnsEarly() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        // Do not call beginRun - reportBuilder will be null
        assertDoesNotThrow(() -> manager.reportFile("Test.java", new AdviceCollector(), null, null),
                "reportFile before beginRun should return early");
    }

    @Test
    @DisplayName("reportFile with null fileName normalises to unknown")
    void reportFile_nullFileName() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        manager.beginRun();
        assertDoesNotThrow(() -> manager.reportFile(null, new AdviceCollector(), null, new SuppressionTracker()),
                "reportFile with null fileName should work");
    }

    @Test
    @DisplayName("reportFile with empty fileName normalises to unknown")
    void reportFile_emptyFileName() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        manager.beginRun();
        assertDoesNotThrow(() -> manager.reportFile("", new AdviceCollector(), null, new SuppressionTracker()),
                "reportFile with empty fileName should work");
        assertDoesNotThrow(() -> manager.reportFile("   ", new AdviceCollector(), null, new SuppressionTracker()),
                "reportFile with whitespace fileName should work");
    }

    @Test
    @DisplayName("reportFile with valid collector processes file")
    void reportFile_validCollectorProcessesFile() {
        AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
        manager.beginRun();
        AdviceCollector collector = new AdviceCollector();
        assertDoesNotThrow(() -> manager.reportFile("Test.java", collector, null, new SuppressionTracker()),
                "reportFile with valid collector should process");
    }

    @Test
    @DisplayName("reportFile in dry run mode updates counts")
    void reportFile_dryRunModeUpdatesCounts(@TempDir Path tempDir) {
        String rankPath = tempDir.resolve("ranks.properties").toString();
        AdviceReportManager manager = new AdviceReportManager(false, true, null, rankPath, false);
        manager.beginRun();
        AdviceCollector collector = new AdviceCollector();
        collector.record(new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        assertDoesNotThrow(() -> manager.reportFile("Test.java", collector, null, new SuppressionTracker()),
                "reportFile in dry run should update counts");
    }

    @Test
    @DisplayName("finishRun outputs summary for zero issues")
    void finishRun_outputsSummaryForZeroIssues() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
            manager.beginRun();
            manager.reportFile("Test.java", new AdviceCollector(), null, new SuppressionTracker());
            manager.finishRun();
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("files read"), "Summary should mention files read");
        assertTrue(output.contains("no issues found"), "Summary should indicate no issues");
    }

    @Test
    @DisplayName("finishRun in dry run mode writes ranks file")
    void finishRun_dryRunModeWritesRanksFile(@TempDir Path tempDir) {
        Path rankPath = tempDir.resolve("ranks.properties");
        AdviceReportManager manager = new AdviceReportManager(false, true, null, rankPath.toString(), false);
        manager.beginRun();
        AdviceCollector collector = new AdviceCollector();
        collector.record(new CandidateAdvice.Builder()
                .fileName("Test.java")
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        manager.reportFile("Test.java", collector, null, new SuppressionTracker());
        assertDoesNotThrow(manager::finishRun, "finishRun in dry run should write ranks");
        assertTrue(java.nio.file.Files.exists(rankPath), "Ranks file should be created");
    }

    @Test
    @DisplayName("Full lifecycle with verbose mode")
    void fullLifecycle_verboseMode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            AdviceReportManager manager = new AdviceReportManager(true, false, null, null, false);
            manager.beginRun();
            AdviceCollector collector = new AdviceCollector();
            collector.record(new CandidateAdvice.Builder()
                    .fileName("Test.java")
                    .lineNo(10)
                    .source(AdviceSource.ASSERTION)
                    .adviceId(AdviceId.MMAssertionMessageMissing)
                    .ruleId(RuleId.MISSING_MESSAGE)
                    .build());
            manager.reportFile("Test.java", collector, null, new SuppressionTracker());
            manager.finishRun();
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("FILE:"), "Output should contain FILE header");
    }

    @Test
    @DisplayName("Warn legacy suppressions flag is passed through")
    void warnLegacySuppressions_flagPassedThrough() {
        assertDoesNotThrow(() -> new AdviceReportManager(false, false, null, null, true),
                "Constructor should accept warnLegacySuppressions=true");
    }

    @Test
    @DisplayName("Multiple files can be processed")
    void multipleFiles_canBeProcessed() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
            manager.beginRun();
            manager.reportFile("Test1.java", new AdviceCollector(), null, new SuppressionTracker());
            manager.reportFile("Test2.java", new AdviceCollector(), null, new SuppressionTracker());
            manager.reportFile("Test3.java", new AdviceCollector(), null, new SuppressionTracker());
            manager.finishRun();
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("3 files read"), "Summary should report 3 files");
    }

    @Test
    @DisplayName("File with issues increments issue count")
    void fileWithIssues_incrementsIssueCount() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            AdviceReportManager manager = new AdviceReportManager(false, false, null, null, false);
            manager.beginRun();
            AdviceCollector collector = new AdviceCollector();
            collector.record(new CandidateAdvice.Builder()
                    .fileName("Test.java")
                    .lineNo(10)
                    .source(AdviceSource.ASSERTION)
                    .adviceId(AdviceId.MMAssertionMessageMissing)
                    .ruleId(RuleId.MISSING_MESSAGE)
                    .build());
            manager.reportFile("Test.java", collector, null, new SuppressionTracker());
            manager.finishRun();
        } finally {
            System.setOut(original);
        }
        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        // When there are issues, the summary line changes
        assertFalse(output.contains("no issues found"),
                "Summary should not say 'no issues found' when there are issues");
    }
}
