/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.FileContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Builds per-file advice reports from collected candidates.
 */
public final class AdviceReportBuilder {
    private static final Logger LOG = Logger.getLogger(AdviceReportBuilder.class.getName());
    private static final int SNIPPET_CONTEXT_CHARS = 40;
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
        Map<Integer, List<CandidateAdvice>> deduped = new LinkedHashMap<>();
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
                    LOG.warning("Duplicate advice on line " + line + ": " + adviceId + "; keeping first");
                    continue;
                }
                byAdvice.put(adviceId, candidate);
            }
            List<CandidateAdvice> dedupedCandidates = new ArrayList<>(byAdvice.values());
            deduped.put(line, dedupedCandidates);
            List<CandidateAdvice> selected = selectCandidates(dedupedCandidates);
            for (CandidateAdvice candidate : selected) {
                LineSnippet snippet = snippetForLine(context, line,
                        candidate.messageLiteral(), candidate.messageExpr());
                AdviceOccurrence occurrence = new AdviceOccurrence(line, candidate.source(),
                        candidate.messageLiteral(), candidate.messageExpr(),
                        snippet.lineText, snippet.snippet);
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
                .comparingInt((AdviceGroup g) -> g.rank() < 0 ? Integer.MAX_VALUE : g.rank())
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
                .comparingInt((FileAdviceGroup g) -> g.rank() < 0 ? Integer.MAX_VALUE : g.rank())
                .thenComparing(group -> group.adviceId().name()));
        return groups;
    }

    private List<CandidateAdvice> selectCandidates(List<CandidateAdvice> candidates) {
        if (dryRun) {
            return candidates;
        }
        List<CandidateAdvice> selected = new ArrayList<>();
        int minRank = Integer.MAX_VALUE;
        for (CandidateAdvice c : candidates) {
            int rank = rankForSelection(c.adviceId());
            if (rank < minRank) {
                minRank = rank;
                selected.clear();
                selected.add(c);
            } else if (rank == minRank) {
                selected.add(c);
            }
        }
        return selected;
    }

    private int rankForSelection(AdviceId adviceId) {
        int rank = rankings == null ? -1 : rankings.rankFor(adviceId);
        return rank < 0 ? Integer.MAX_VALUE : rank;
    }

    private LineSnippet snippetForLine(MessageExtractionContext context, int lineNo,
                                       String messageLiteral, String messageExpr) {
        if (context == null || lineNo <= 0) {
            return LineSnippet.empty();
        }
        FileContents contents = context.fileContents();
        if (contents == null) {
            return LineSnippet.empty();
        }
        String[] lines = contents.getLines();
        if (lines == null || lineNo > lines.length) {
            return LineSnippet.empty();
        }
        String line = lines[lineNo - 1];
        String trimmed = line == null ? null : line.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return LineSnippet.empty();
        }
        String snippet = extractSnippet(trimmed, messageExpr, messageLiteral);
        if (snippet == null || snippet.equals(trimmed)) {
            return new LineSnippet(trimmed, null);
        }
        return new LineSnippet(null, snippet);
    }

    private String extractSnippet(String line, String messageExpr, String messageLiteral) {
        String target = pickTarget(messageExpr, messageLiteral);
        if (target == null || target.isEmpty()) {
            return null;
        }
        int index = line.indexOf(target);
        if (index < 0) {
            return null;
        }
        int start = Math.max(0, index - SNIPPET_CONTEXT_CHARS);
        int end = Math.min(line.length(), index + target.length() + SNIPPET_CONTEXT_CHARS);
        if (start == 0 && end == line.length()) {
            return line;
        }
        StringBuilder snippet = new StringBuilder();
        if (start > 0) {
            snippet.append("...");
        }
        snippet.append(line, start, end);
        if (end < line.length()) {
            snippet.append("...");
        }
        return snippet.toString();
    }

    private String pickTarget(String messageExpr, String messageLiteral) {
        if (messageLiteral != null && !messageLiteral.isEmpty()) {
            return messageLiteral;
        }
        if (messageExpr != null && !messageExpr.isEmpty()) {
            return messageExpr;
        }
        return null;
    }

    private static final class LineSnippet {
        private static final LineSnippet EMPTY = new LineSnippet(null, null);
        private final String lineText;
        private final String snippet;

        private LineSnippet(String lineText, String snippet) {
            this.lineText = lineText;
            this.snippet = snippet;
        }

        private static LineSnippet empty() {
            return EMPTY;
        }
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
