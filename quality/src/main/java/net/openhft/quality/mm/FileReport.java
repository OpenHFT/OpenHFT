/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Aggregated advice report for a single file.
 */
public final class FileReport {
    private final String fileName;
    private final List<FileAdviceGroup> fileAdvice;
    private final List<AdviceGroup> lineAdvice;
    private final Map<Integer, List<CandidateAdvice>> candidatesByLine;
    private final List<RuleId> legacySuppressionRuleIds;

    public FileReport(String fileName, List<FileAdviceGroup> fileAdvice,
                      List<AdviceGroup> lineAdvice,
                      Map<Integer, List<CandidateAdvice>> candidatesByLine,
                      List<RuleId> legacySuppressionRuleIds) {
        this.fileName = requireNonNull(fileName, "fileName");
        this.fileAdvice = Collections.unmodifiableList(new java.util.ArrayList<>(fileAdvice));
        this.lineAdvice = Collections.unmodifiableList(new java.util.ArrayList<>(lineAdvice));
        this.candidatesByLine = immutableCandidates(candidatesByLine);
        this.legacySuppressionRuleIds = legacySuppressionRuleIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new java.util.ArrayList<>(legacySuppressionRuleIds));
    }

    public String fileName() {
        return fileName;
    }

    public List<FileAdviceGroup> fileAdvice() {
        return fileAdvice;
    }

    public List<AdviceGroup> lineAdvice() {
        return lineAdvice;
    }

    public Map<Integer, List<CandidateAdvice>> candidatesByLine() {
        return candidatesByLine;
    }

    public List<RuleId> legacySuppressionRuleIds() {
        return legacySuppressionRuleIds;
    }

    public boolean hasIssues() {
        return !(fileAdvice.isEmpty() && lineAdvice.isEmpty());
    }

    private static Map<Integer, List<CandidateAdvice>> immutableCandidates(
            Map<Integer, List<CandidateAdvice>> candidatesByLine) {
        if (candidatesByLine == null || candidatesByLine.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<CandidateAdvice>> copy = new java.util.HashMap<>();
        for (Map.Entry<Integer, List<CandidateAdvice>> entry : candidatesByLine.entrySet()) {
            List<CandidateAdvice> candidates = entry.getValue() == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new java.util.ArrayList<>(entry.getValue()));
            copy.put(entry.getKey(), candidates);
        }
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public String toString() {
        return "FileReport{fileName='" + fileName + '\''
                + ", fileAdvice=" + fileAdvice.size()
                + ", lineAdvice=" + lineAdvice.size()
                + '}';
    }
}
