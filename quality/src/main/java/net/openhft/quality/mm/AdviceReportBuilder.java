/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.FileContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builds per-file advice reports from collected candidates.
 */
public final class AdviceReportBuilder {
    private final AdviceTextLoader textLoader;
    private final AdviceRankings rankings;
    private final boolean dryRun;
    private final boolean verbose;
    private final boolean warnLegacySuppressions;

    public AdviceReportBuilder(AdviceTextLoader textLoader, AdviceRankings rankings,
                               boolean dryRun, boolean verbose,
                               boolean warnLegacySuppressions) {
        this.textLoader = textLoader;
        this.rankings = rankings;
        this.dryRun = dryRun;
        this.verbose = verbose;
        this.warnLegacySuppressions = warnLegacySuppressions;
    }

    public FileReport build(String fileName, String displayName, AdviceCollector collector,
                            MessageExtractionContext context,
                            SuppressionTracker suppressionTracker) {
        Map<Integer, List<CandidateAdvice>> rawCandidates = collector.candidatesForFile(fileName);
        Map<Integer, List<CandidateAdvice>> deduped = new java.util.HashMap<>();
        List<Integer> lines = new ArrayList<>(rawCandidates.keySet());
        Collections.sort(lines);
        Map<AdviceId, List<AdviceOccurrence>> grouped = new EnumMap<>(AdviceId.class);
        for (Integer line : lines) {
            List<CandidateAdvice> candidates = rawCandidates.get(line);
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }
            Map<AdviceId, CandidateAdvice> byAdvice = new EnumMap<>(AdviceId.class);
            for (CandidateAdvice candidate : candidates) {
                AdviceId adviceId = candidate.adviceId();
                if (byAdvice.containsKey(adviceId)) {
                    throw new IllegalStateException("Duplicate advice on line " + line + ": " + adviceId);
                }
                byAdvice.put(adviceId, candidate);
            }
            List<CandidateAdvice> dedupedCandidates = new ArrayList<>(byAdvice.values());
            deduped.put(line, dedupedCandidates);
            List<CandidateAdvice> selected = selectCandidates(dedupedCandidates);
            for (CandidateAdvice candidate : selected) {
                AdviceOccurrence occurrence = new AdviceOccurrence(line, candidate.source(),
                        candidate.messageLiteral(), candidate.messageExpr(),
                        snippetForLine(context, line));
                grouped.computeIfAbsent(candidate.adviceId(), key -> new ArrayList<>())
                        .add(occurrence);
            }
        }
        List<AdviceGroup> adviceGroups = buildAdviceGroups(grouped);
        List<FileAdviceGroup> fileAdvice = buildFileAdviceGroups(fileName, collector);
        List<RuleId> legacy = legacySuppressions(suppressionTracker);
        Map<Integer, List<CandidateAdvice>> verboseCandidates = (verbose || dryRun)
                ? deduped
                : Collections.emptyMap();
        return new FileReport(displayName, fileAdvice, adviceGroups, verboseCandidates, legacy);
    }

    private List<AdviceGroup> buildAdviceGroups(Map<AdviceId, List<AdviceOccurrence>> grouped) {
        List<AdviceGroup> groups = new ArrayList<>();
        for (Map.Entry<AdviceId, List<AdviceOccurrence>> entry : grouped.entrySet()) {
            AdviceId adviceId = entry.getKey();
            AdviceText text = textLoader.textFor(adviceId);
            int rank = rankings == null ? -1 : rankings.rankFor(adviceId);
            groups.add(new AdviceGroup(adviceId, text, rank, entry.getValue()));
        }
        groups.sort(Comparator
                .comparingInt(AdviceGroup::rank)
                .thenComparing(group -> group.adviceId().name()));
        return groups;
    }

    private List<FileAdviceGroup> buildFileAdviceGroups(String fileName, AdviceCollector collector) {
        List<FileAdviceGroup> groups = new ArrayList<>();
        for (FileAdviceDetails details : collector.fileAdviceForFile(fileName)) {
            AdviceId adviceId = details.adviceId();
            AdviceText text = textLoader.textFor(adviceId);
            int rank = rankings == null ? -1 : rankings.rankFor(adviceId);
            groups.add(new FileAdviceGroup(adviceId, text, rank, details));
        }
        groups.sort(Comparator
                .comparingInt(FileAdviceGroup::rank)
                .thenComparing(group -> group.adviceId().name()));
        return groups;
    }

    private List<CandidateAdvice> selectCandidates(List<CandidateAdvice> candidates) {
        if (dryRun) {
            return candidates;
        }
        int minRank = Integer.MAX_VALUE;
        for (CandidateAdvice candidate : candidates) {
            int rank = rankings == null ? -1 : rankings.rankFor(candidate.adviceId());
            if (rank < minRank) {
                minRank = rank;
            }
        }
        List<CandidateAdvice> selected = new ArrayList<>();
        for (CandidateAdvice candidate : candidates) {
            int rank = rankings == null ? -1 : rankings.rankFor(candidate.adviceId());
            if (rank == minRank) {
                selected.add(candidate);
            }
        }
        return selected;
    }

    private String snippetForLine(MessageExtractionContext context, int lineNo) {
        if (context == null || lineNo <= 0) {
            return null;
        }
        FileContents contents = context.fileContents();
        if (contents == null) {
            return null;
        }
        String[] lines = contents.getLines();
        if (lines == null || lineNo > lines.length) {
            return null;
        }
        String line = lines[lineNo - 1];
        return line == null ? null : line.trim();
    }

    private List<RuleId> legacySuppressions(SuppressionTracker suppressionTracker) {
        if (!warnLegacySuppressions || suppressionTracker == null) {
            return Collections.emptyList();
        }
        List<RuleId> legacy = new ArrayList<>(suppressionTracker.legacySuppressedRuleIds());
        legacy.sort(Comparator.comparing(RuleId::code));
        return legacy;
    }
}
