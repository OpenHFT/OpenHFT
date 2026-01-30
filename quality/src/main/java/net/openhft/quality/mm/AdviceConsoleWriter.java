/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.List;
import java.util.Map;

/**
 * Writes aggregated advice to the console.
 */
public final class AdviceConsoleWriter {
    private final boolean verbose;

    public AdviceConsoleWriter(boolean verbose) {
        this.verbose = verbose;
    }

    public void write(FileReport report) {
        if (report == null || !report.hasIssues()) {
            return;
        }
        System.out.println("FILE: " + report.fileName());
        printRuleSummary(report);
        for (FileAdviceGroup group : report.fileAdvice()) {
            printFileAdvice(group);
        }
        for (AdviceGroup group : report.lineAdvice()) {
            printLineAdvice(group);
        }
        if (verbose) {
            printVerboseCandidates(report.candidatesByLine());
            printLegacySuppressions(report.legacySuppressionRuleIds());
        }
    }

    public void writeRunSummary(int fileCount, int issueCount) {
        if (issueCount == 0) {
            System.out.println(fileCount + " files read; no issues found.");
        }
    }

    private void printRuleSummary(FileReport report) {
        List<String> fileRules = new java.util.ArrayList<>();
        for (FileAdviceGroup group : report.fileAdvice()) {
            int lineNo = group.details().lineNo();
            String entry = lineNo > 0
                    ? group.adviceId().name() + "(L" + lineNo + ')'
                    : group.adviceId().name();
            fileRules.add(entry);
        }
        java.util.Map<Integer, java.util.List<String>> lineRules = new java.util.TreeMap<>();
        for (AdviceGroup group : report.lineAdvice()) {
            String adviceName = group.adviceId().name();
            for (AdviceOccurrence occurrence : group.occurrences()) {
                lineRules.computeIfAbsent(occurrence.lineNo(), key -> new java.util.ArrayList<>())
                        .add(adviceName);
            }
        }
        if (fileRules.isEmpty() && lineRules.isEmpty()) {
            return;
        }
        System.out.println("  RULES:");
        if (!fileRules.isEmpty()) {
            java.util.Collections.sort(fileRules);
            System.out.println("    FILE: " + String.join(", ", fileRules));
        }
        for (java.util.Map.Entry<Integer, java.util.List<String>> entry : lineRules.entrySet()) {
            java.util.List<String> rules = entry.getValue();
            if (rules == null || rules.isEmpty()) {
                continue;
            }
            java.util.Collections.sort(rules);
            String joined = String.join(", ", new java.util.LinkedHashSet<>(rules));
            System.out.println("    L" + entry.getKey() + ": " + joined);
        }
    }

    private void printFileAdvice(FileAdviceGroup group) {
        AdviceText text = group.adviceText();
        String lineRef = group.details().lineNo() > 0 ? " L" + group.details().lineNo() : "";
        System.out.println("  FILE-LEVEL " + group.adviceId().name() + " - " + text.title() + lineRef);
        System.out.println("    " + text.intentIntro());
        System.out.println("    " + text.intentOutro());
        if (!group.details().items().isEmpty()) {
            System.out.println("    Result: " + String.join(", ", group.details().items()));
        }
    }

    private void printLineAdvice(AdviceGroup group) {
        AdviceText text = group.adviceText();
        System.out.println("  " + group.adviceId().name() + " - " + text.title());
        System.out.println("    " + text.intentIntro());
        System.out.println("    " + text.intentOutro());
        System.out.println("    Checklist: " + text.checklist());
        System.out.println("    Anti-patterns: " + text.antiPatterns());
        for (AdviceOccurrence occurrence : group.occurrences()) {
            String messageText = occurrence.messageLiteral() != null
                    ? occurrence.messageLiteral()
                    : occurrence.messageExpr();
            StringBuilder line = new StringBuilder(128);
            line.append("    L").append(occurrence.lineNo())
                    .append(" ").append(occurrence.source());
            if (messageText != null && !messageText.isEmpty()) {
                line.append(" message_text=\"").append(messageText).append('"');
            }
            if (occurrence.snippet() != null && !occurrence.snippet().isEmpty()) {
                line.append(" snippet=\"").append(occurrence.snippet()).append('"');
            } else if (occurrence.lineText() != null && !occurrence.lineText().isEmpty()) {
                line.append(" line: \"").append(occurrence.lineText()).append('"');
            } else {
                line.append(" hint=").append(text.hintA()).append(" / ").append(text.hintB());
            }
            System.out.println(line);
        }
    }

    private void printVerboseCandidates(Map<Integer, List<CandidateAdvice>> candidatesByLine) {
        if (candidatesByLine == null || candidatesByLine.isEmpty()) {
            return;
        }
        System.out.println("  Verbose candidates:");
        for (Map.Entry<Integer, List<CandidateAdvice>> entry : candidatesByLine.entrySet()) {
            int lineNo = entry.getKey();
            for (CandidateAdvice candidate : entry.getValue()) {
                StringBuilder line = new StringBuilder(128);
                line.append("    line=").append(lineNo)
                        .append(" advice=").append(candidate.adviceId().name())
                        .append(" rule=").append(candidate.ruleId() == null ? "unknown" : candidate.ruleId().code());
                AdviceMetrics metrics = candidate.metrics();
                if (metrics != null) {
                    line.append(" words=").append(metrics.wordCount())
                            .append(" meaningful=").append(metrics.meaningfulWordCount());
                    if (metrics.totalWordCount() != 0) {
                        line.append(" totalWords=").append(metrics.totalWordCount());
                    }
                    if (metrics.effectiveMeaningfulWordCount() != 0) {
                        line.append(" effectiveMeaningful=").append(metrics.effectiveMeaningfulWordCount());
                    }
                    if (metrics.minWordCount() != null) {
                        line.append(" minWords=").append(metrics.minWordCount());
                    }
                    if (metrics.maxWordCount() != null) {
                        line.append(" maxWords=").append(metrics.maxWordCount());
                    }
                    if (metrics.minMeaningfulWordCount() != null) {
                        line.append(" minMeaningful=").append(metrics.minMeaningfulWordCount());
                    }
                    if (metrics.maxWordLength() != null) {
                        line.append(" maxWordLength=").append(metrics.maxWordLength());
                    }
                    line.append(" placeholders=").append(metrics.placeholderCount())
                            .append(" keyValueLabels=").append(metrics.keyValueLabelCount());
                    if (metrics.comparisonOperator() != null) {
                        line.append(" comparison=").append(metrics.comparisonOperator());
                    }
                    if (metrics.stringSearchMethod() != null) {
                        line.append(" search=").append(metrics.stringSearchMethod());
                    }
                }
                System.out.println(line);
            }
        }
    }

    private void printLegacySuppressions(List<RuleId> legacyRuleIds) {
        if (legacyRuleIds == null || legacyRuleIds.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder(64);
        for (RuleId ruleId : legacyRuleIds) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(ruleId.code());
        }
        System.out.println("  Legacy suppressions: " + builder);
    }
}
