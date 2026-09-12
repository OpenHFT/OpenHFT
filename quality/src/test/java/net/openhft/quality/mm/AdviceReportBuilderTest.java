/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AdviceReportBuilder}.
 */
@DisplayName("Advice report builder tests")
class AdviceReportBuilderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Normal mode selects lowest rank per line")
    void normalMode_selectsLowestRank() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        CandidateAdvice first = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build();
        CandidateAdvice second = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build();
        collector.record(first);
        collector.record(second);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, true);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        int firstRank = rankings.rankFor(first.adviceId());
        int secondRank = rankings.rankFor(second.adviceId());
        AdviceId expected = firstRank < secondRank ? first.adviceId() : second.adviceId();
        assertEquals(1, report.lineAdvice().size(),
                "normal mode should choose one advice per line");
        assertEquals(expected, report.lineAdvice().get(0).adviceId(),
                "selected advice should be the lowest rank");
    }

    @Test
    @DisplayName("Dry run includes all candidates")
    void dryRun_includesAllCandidates() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, true, false, true);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(2, report.lineAdvice().size(),
                "dry-run should include all candidate advice");
    }

    @Test
    @DisplayName("Normal mode includes all candidates when ranks tie")
    void normalMode_includesAllWhenRanksTie() throws Exception {
        Path ranksPath = Files.createTempFile("mm-ranks", ".properties");
        Files.write(ranksPath, ("MMAssertionMessageMissing=1\n").getBytes(StandardCharsets.UTF_8));
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.load(ranksPath);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build());
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooFewMeaningfulWords)
                .ruleId(RuleId.TOO_FEW_MEANINGFUL)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, true);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(2, report.lineAdvice().size(),
                "rank ties should include all advice for the line");
    }

    @Test
    @DisplayName("Normal mode prefers ranked advice over unranked advice")
    void normalMode_prefersRankedOverUnranked() throws Exception {
        Path ranksPath = Files.createTempFile("mm-ranks-ranked-vs-unranked", ".properties");
        Files.write(ranksPath, ("MMAssertionMessageMissing=1\n").getBytes(StandardCharsets.UTF_8));
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.load(ranksPath);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        CandidateAdvice ranked = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build();
        CandidateAdvice unranked = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build();
        collector.record(unranked);
        collector.record(ranked);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, true);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(),
                "normal mode should choose one advice per line when a lowest rank exists");
        assertEquals(AdviceId.MMAssertionMessageMissing, report.lineAdvice().get(0).adviceId(),
                "ranked advice should be preferred over unranked advice");
    }

    @Test
    @DisplayName("Duplicate advice on the same line merges gracefully")
    void duplicateAdvice_mergesGracefully() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        CandidateAdvice first = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build();
        CandidateAdvice second = new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build();
        collector.record(first);
        collector.record(second);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, true);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());
        assertNotNull(report, "duplicate advice on a line should merge gracefully");
        assertEquals(1, report.lineAdvice().size(),
                "duplicate advice should be deduplicated to one");
    }

    @Test
    @DisplayName("Legacy suppressions are reported when enabled")
    void legacySuppressions_reportedWhenEnabled() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());

        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTest(scope);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, true, true);
        FileReport report = builder.build(file, file, collector, null, tracker);

        assertEquals(1, report.legacySuppressionRuleIds().size(),
                "legacy suppressions should be captured");
        assertEquals(RuleId.TOO_SHORT, report.legacySuppressionRuleIds().get(0),
                "expected legacy RuleId should be reported");
    }

    @Test
    @DisplayName("Legacy suppressions not reported when disabled")
    void legacySuppressions_notReportedWhenDisabled() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());

        SuppressionTracker tracker = new SuppressionTracker();
        SuppressionTracker.SuppressionScope scope = tracker.new SuppressionScope();
        scope.addToken("MMTooShort");
        tracker.pushScopeForTest(scope);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, true, false);
        FileReport report = builder.build(file, file, collector, null, tracker);

        assertEquals(0, report.legacySuppressionRuleIds().size(),
                "legacy suppressions should not be captured when disabled");
    }

    @Test
    @DisplayName("Null rankings uses default rank of -1")
    void nullRankings_usesDefaultRank() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, null, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(2, report.lineAdvice().size(),
                "null rankings should result in tied ranks, including all candidates");
    }

    @Test
    @DisplayName("Empty candidates list produces empty report")
    void emptyCandidates_producesEmptyReport() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertNotNull(report, "report should not be null");
        assertEquals(0, report.lineAdvice().size(), "should have no line advice");
        assertEquals(0, report.fileAdvice().size(), "should have no file advice");
    }

    @Test
    @DisplayName("Report with context extracts snippet for short line")
    void reportWithContext_extractsSnippetForShortLine() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        String messageLiteral = "short message";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral(messageLiteral)
                .build());

        List<String> lines = Collections.singletonList("assertTrue(condition, \"short message\");");
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(), "should have one advice");
        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        // Short line is returned as lineText, not snippet
        assertNotNull(occurrence.lineText(), "lineText should be set for short line");
        assertNull(occurrence.snippet(), "snippet should be null when line is short");
    }

    @Test
    @DisplayName("Report with context extracts snippet for long line")
    void reportWithContext_extractsSnippetForLongLine() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        String messageLiteral = "target";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral(messageLiteral)
                .build());

        // Line longer than 80 chars with message in the middle
        String longLine = "    very_long_prefix_that_makes_the_line_exceed_limit_"
                + "and_more_padding_here_\"target\"_and_then_more_trailing_content_"
                + "that_ensures_the_line_is_very_long_indeed";
        List<String> lines = Collections.singletonList(longLine);
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(), "should have one advice");
        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNotNull(occurrence.snippet(), "snippet should be set for long line");
        assertTrue(occurrence.snippet().contains("target"), "snippet should contain target");
        assertTrue(occurrence.snippet().contains("..."), "snippet should have ellipsis");
    }

    @Test
    @DisplayName("Report with null context produces null snippet")
    void reportWithNullContext_producesNullSnippet() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("test")
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(), "should have one advice");
        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNull(occurrence.snippet(), "snippet should be null without context");
        assertNull(occurrence.lineText(), "lineText should be null without context");
    }

    @Test
    @DisplayName("Report with line number zero produces null snippet")
    void reportWithLineNumberZero_producesNullSnippet() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(0)  // Invalid line number
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("test")
                .build());

        List<String> lines = Collections.singletonList("line content");
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(), "should have one advice");
        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNull(occurrence.snippet(), "snippet should be null for line 0");
    }

    @Test
    @DisplayName("Report with line beyond file produces null snippet")
    void reportWithLineBeyondFile_producesNullSnippet() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(100)  // Beyond file length
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("test")
                .build());

        List<String> lines = Collections.singletonList("only one line");
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        assertEquals(1, report.lineAdvice().size(), "should have one advice");
        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNull(occurrence.snippet(), "snippet should be null when line exceeds file");
    }

    @Test
    @DisplayName("Report uses messageExpr when messageLiteral is null")
    void report_usesMessageExprWhenLiteralNull() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        String messageExpr = "getErrorMsg()";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral(null)
                .messageExpr(messageExpr)
                .build());

        List<String> lines = Collections.singletonList("assertTrue(condition, getErrorMsg());");
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        // Line is short so lineText is set
        assertTrue(occurrence.lineText().contains("getErrorMsg"),
                "should find target using messageExpr");
    }

    @Test
    @DisplayName("Report with empty line produces null snippet")
    void reportWithEmptyLine_producesNullSnippet() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("test")
                .build());

        List<String> lines = Collections.singletonList("");  // Empty line
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNull(occurrence.snippet(), "snippet should be null for empty line");
        assertNull(occurrence.lineText(), "lineText should be null for empty line");
    }

    @Test
    @DisplayName("Report with whitespace-only line produces null snippet")
    void reportWithWhitespaceLine_producesNullSnippet() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("test")
                .build());

        List<String> lines = Collections.singletonList("   ");  // Whitespace only
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNull(occurrence.snippet(), "snippet should be null for whitespace line");
        assertNull(occurrence.lineText(), "lineText should be null for whitespace line");
    }

    @Test
    @DisplayName("Report with target not in line produces full lineText")
    void reportWithTargetNotInLine_producesFullLineText() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral("not_in_line")  // Target not in line
                .build());

        List<String> lines = Collections.singletonList("assertTrue(condition, \"different\");");
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNotNull(occurrence.lineText(), "lineText should be set");
        assertNull(occurrence.snippet(), "snippet should be null when target not found");
    }

    @Test
    @DisplayName("Verbose mode includes all candidates in verbose map")
    void verboseMode_includesVerboseCandidates() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, true, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertNotNull(report.candidatesByLine(), "verbose candidates should not be null");
        assertFalse(report.candidatesByLine().isEmpty(), "verbose candidates should not be empty");
    }

    @Test
    @DisplayName("Non-verbose mode has empty candidates map")
    void nonVerboseMode_hasEmptyCandidatesMap() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertTrue(report.candidatesByLine().isEmpty(), "non-verbose should have empty candidates map");
    }

    @Test
    @DisplayName("File advice is included in report")
    void fileAdvice_includedInReport() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        FileAdviceDetails details = new FileAdviceDetails(AdviceId.MMOverusedWord, 5,
                Arrays.asList("value", "data"), 3, null, null, null, null, null);
        collector.recordFileAdvice(file, details);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(1, report.fileAdvice().size(), "should have one file advice");
        assertEquals(AdviceId.MMOverusedWord, report.fileAdvice().get(0).adviceId(),
                "file advice ID should match");
    }

    @Test
    @DisplayName("Multiple lines with candidates are processed in order")
    void multipleLines_processedInOrder() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        // Add in reverse order
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(30)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.THROW)
                .adviceId(AdviceId.MMThrowMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(20)
                .source(AdviceSource.LOG)
                .adviceId(AdviceId.MMLogMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, null, new SuppressionTracker());

        assertEquals(3, report.lineAdvice().size(), "should have three advice groups");
    }

    @Test
    @DisplayName("Null suppression tracker does not cause errors")
    void nullSuppressionTracker_doesNotCauseErrors() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .build());

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, true);
        FileReport report = builder.build(file, file, collector, null, null);

        assertNotNull(report, "report should not be null with null tracker");
        assertEquals(0, report.legacySuppressionRuleIds().size(),
                "legacy suppressions should be empty with null tracker");
    }

    @Test
    @DisplayName("Snippet at start of long line has trailing ellipsis only")
    void snippetAtStartOfLongLine_hasTrailingEllipsisOnly() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        String messageLiteral = "target";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral(messageLiteral)
                .build());

        // Target at the very start, line much longer than snippet context
        String longLine = "target_very_long_suffix_padding_that_extends_well_beyond_"
                + "the_snippet_context_chars_limit_to_ensure_trailing_ellipsis";
        List<String> lines = Collections.singletonList(longLine);
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNotNull(occurrence.snippet(), "snippet should be set for long line");
        assertFalse(occurrence.snippet().startsWith("..."),
                "snippet should not start with ellipsis when target is at start");
        assertTrue(occurrence.snippet().endsWith("..."),
                "snippet should end with ellipsis when line continues");
    }

    @Test
    @DisplayName("Snippet at end of long line has leading ellipsis only")
    void snippetAtEndOfLongLine_hasLeadingEllipsisOnly() throws IOException {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        AdviceCollector collector = new AdviceCollector();
        String file = "Test.java";
        String messageLiteral = "target";
        collector.record(new CandidateAdvice.Builder()
                .fileName(file)
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .ruleId(RuleId.MISSING_MESSAGE)
                .messageLiteral(messageLiteral)
                .build());

        // Target at the very end, line much longer
        String longLine = "very_long_prefix_padding_that_pushes_the_snippet_start_forward_"
                + "well_beyond_zero_to_ensure_leading_ellipsis_and_then_target";
        List<String> lines = Collections.singletonList(longLine);
        MessageExtractionContext context = createContextWithLines(file, lines);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, false, false);
        FileReport report = builder.build(file, file, collector, context, new SuppressionTracker());

        AdviceOccurrence occurrence = report.lineAdvice().get(0).occurrences().get(0);
        assertNotNull(occurrence.snippet(), "snippet should be set for long line");
        assertTrue(occurrence.snippet().startsWith("..."),
                "snippet should start with ellipsis when line starts before snippet");
        assertFalse(occurrence.snippet().endsWith("..."),
                "snippet should not end with ellipsis when target is at end");
    }

    private MessageExtractionContext createContextWithLines(String fileName, List<String> lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        FileContents contents = new FileContents(text);

        MessageExtractionContext context = new MessageExtractionContext(new MessageAstSupport());
        context.reset(contents);
        return context;
    }
}
