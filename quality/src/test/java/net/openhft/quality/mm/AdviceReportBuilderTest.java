/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AdviceReportBuilder}.
 */
@DisplayName("Advice report builder tests")
class AdviceReportBuilderTest {

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
    @DisplayName("Duplicate advice on the same line fails fast")
    void duplicateAdvice_failsFast() {
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
        assertThrows(IllegalStateException.class,
                () -> builder.build(file, file, collector, null, new SuppressionTracker()),
                "duplicate advice on a line should fail fast");
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
        tracker.pushScopeForTesting(scope);

        AdviceReportBuilder builder = new AdviceReportBuilder(loader, rankings, false, true, true);
        FileReport report = builder.build(file, file, collector, null, tracker);

        assertEquals(1, report.legacySuppressionRuleIds().size(),
                "legacy suppressions should be captured");
        assertEquals(RuleId.TOO_SHORT, report.legacySuppressionRuleIds().get(0),
                "expected legacy RuleId should be reported");
    }
}
