/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Collects advice candidates grouped by file and line.
 */
public final class AdviceCollector {
    private final Map<String, Map<Integer, List<CandidateAdvice>>> byFile = new HashMap<>();
    private final Map<String, List<FileAdviceDetails>> fileAdvice = new HashMap<>();

    public void record(CandidateAdvice candidate) {
        requireNonNull(candidate, "candidate");
        String file = candidate.fileName();
        if (file == null || file.trim().isEmpty()) {
            file = "unknown";
        }
        byFile.computeIfAbsent(file, key -> new HashMap<>())
                .computeIfAbsent(candidate.lineNo(), key -> new ArrayList<>())
                .add(candidate);
    }

    public Map<Integer, List<CandidateAdvice>> candidatesForFile(String file) {
        Map<Integer, List<CandidateAdvice>> result = byFile.get(file);
        return result == null ? java.util.Collections.emptyMap() : result;
    }

    public Map<String, Map<Integer, List<CandidateAdvice>>> allCandidates() {
        return byFile;
    }

    public void recordFileAdvice(String file, FileAdviceDetails details) {
        requireNonNull(details, "details");
        String resolved = file;
        if (resolved == null || resolved.trim().isEmpty()) {
            resolved = "unknown";
        }
        fileAdvice.computeIfAbsent(resolved, key -> new ArrayList<>()).add(details);
    }

    public List<FileAdviceDetails> fileAdviceForFile(String file) {
        List<FileAdviceDetails> details = fileAdvice.get(file);
        return details == null ? java.util.Collections.emptyList() : details;
    }

    public void clearFile(String file) {
        byFile.remove(file);
        fileAdvice.remove(file);
    }

    public void clearAll() {
        byFile.clear();
        fileAdvice.clear();
    }
}
