/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

/**
 * Coordinates advice report generation across a run.
 */
public final class AdviceReportManager {
    static final String ADVICE_TEXT_RESOURCE = "net/openhft/quality/mm-advice.properties";
    static final String RANK_RESOURCE = "net/openhft/quality/mm-advice-ranks.properties";
    // Relative to user.dir (CWD); resolved at runtime when rankOut is not explicitly configured
    static final String DEFAULT_RANK_OUT = "logs/mm-advice-ranks.properties";

    private final boolean verbose;
    private final boolean dryRun;
    private final boolean warnLegacySuppressions;
    private final Path jsonlPath;
    private final Path rankOutPath;

    private AdviceTextLoader textLoader;
    private AdviceRankings rankings;
    private AdviceReportBuilder reportBuilder;
    private AdviceConsoleWriter consoleWriter;
    private AdviceJsonlWriter jsonlWriter;
    private final Map<AdviceId, Integer> counts = new EnumMap<>(AdviceId.class);
    private int fileCount;
    private int issueCount;
    private boolean started = false;

    public AdviceReportManager(boolean verbose, boolean dryRun,
                               String jsonlOutput, String rankOut,
                               boolean warnLegacySuppressions) {
        this.verbose = verbose;
        this.dryRun = dryRun;
        this.warnLegacySuppressions = warnLegacySuppressions;
        this.jsonlPath = resolvePath(jsonlOutput);
        this.rankOutPath = resolveRankOut(rankOut, dryRun);
    }

    public void beginRun() {
        textLoader = AdviceTextLoader.loadFromResource(ADVICE_TEXT_RESOURCE);
        rankings = dryRun ? null : AdviceRankings.loadFromResource(RANK_RESOURCE);
        reportBuilder = new AdviceReportBuilder(textLoader, rankings, dryRun, verbose,
                warnLegacySuppressions);
        consoleWriter = new AdviceConsoleWriter(verbose);
        if (jsonlPath != null) {
            jsonlWriter = new AdviceJsonlWriter(jsonlPath, verbose, dryRun, RANK_RESOURCE,
                    rankOutPath == null ? null : rankOutPath.toString());
            jsonlWriter.writeRunRecord();
        }
        started = true;
    }

    public void reportFile(String fileName, AdviceCollector collector,
                           MessageExtractionContext context,
                           SuppressionTracker suppressionTracker) {
        if (reportBuilder == null || collector == null) {
            return;
        }
        fileCount++;
        String resolvedName = normalisePath(fileName);
        FileReport report = reportBuilder.build(fileName, resolvedName, collector, context, suppressionTracker);
        if (report.hasIssues()) {
            issueCount++;
        }
        if (consoleWriter != null) {
            consoleWriter.write(report);
        }
        if (jsonlWriter != null) {
            jsonlWriter.writeFileRecord(report);
        }
        if (dryRun) {
            updateCounts(report);
        }
    }

    public void finishRun() {
        if (!started) return;
        if (dryRun && rankOutPath != null) {
            Map<AdviceId, Integer> ranks = AdviceRankings.buildRanks(counts);
            AdviceRankings.write(rankOutPath, ranks);
        }
        if (consoleWriter != null) {
            consoleWriter.writeRunSummary(fileCount, issueCount);
        }
        if (jsonlWriter != null) {
            jsonlWriter.close();
        }
    }

    private void updateCounts(FileReport report) {
        if (report == null) {
            return;
        }
        java.util.EnumSet<AdviceId> seen = java.util.EnumSet.noneOf(AdviceId.class);
        for (FileAdviceGroup group : report.fileAdvice()) {
            seen.add(group.adviceId());
        }
        for (Map.Entry<Integer, java.util.List<CandidateAdvice>> entry : report.candidatesByLine().entrySet()) {
            for (CandidateAdvice candidate : entry.getValue()) {
                seen.add(candidate.adviceId());
            }
        }
        for (AdviceId adviceId : seen) {
            counts.merge(adviceId, 1, Integer::sum);
        }
    }

    private Path resolvePath(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return Paths.get(trimmed);
    }

    private Path resolveRankOut(String path, boolean dryRun) {
        if (!dryRun) {
            return null;
        }
        if (path == null || path.trim().isEmpty()) {
            return Paths.get(DEFAULT_RANK_OUT);
        }
        return Paths.get(path.trim());
    }

    private String normalisePath(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unknown";
        }
        try {
            java.nio.file.Path root = Paths.get(".").toAbsolutePath().normalize();
            java.nio.file.Path path = Paths.get(fileName).toAbsolutePath().normalize();
            if (path.startsWith(root)) {
                return root.relativize(path).toString();
            }
        } catch (RuntimeException e) {
            return fileName;
        }
        return fileName;
    }
}
