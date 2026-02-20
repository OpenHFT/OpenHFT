/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Advice collector tests for recording and querying candidates")
class AdviceCollectorTest {

    @Test
    @DisplayName("Record candidate with null file name uses unknown placeholder")
    void record_nullFileName_usesUnknownPlaceholder() {
        AdviceCollector collector = new AdviceCollector();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(10)
                .fileName(null)
                .build();
        collector.record(candidate);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertFalse(candidates.isEmpty(), "Null file name should map to 'unknown' key");
        assertEquals(1, candidates.get(10).size(), "Should contain one candidate on line 10");
    }

    @Test
    @DisplayName("Record candidate with empty file name uses unknown placeholder")
    void record_emptyFileName_usesUnknownPlaceholder() {
        AdviceCollector collector = new AdviceCollector();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(5)
                .fileName("  ")
                .build();
        collector.record(candidate);

        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("unknown");
        assertFalse(candidates.isEmpty(), "Blank file name should map to 'unknown' key");
    }

    @Test
    @DisplayName("Record file advice with null file uses unknown placeholder")
    void recordFileAdvice_nullFile_usesUnknownPlaceholder() {
        AdviceCollector collector = new AdviceCollector();
        FileAdviceDetails details = new FileAdviceDetails(
                AdviceId.MMCommentMapStringObject, 10,
                java.util.Collections.singletonList("config"), null, null, null, null, null, null);
        collector.recordFileAdvice(null, details);

        List<FileAdviceDetails> result = collector.fileAdviceForFile("unknown");
        assertEquals(1, result.size(), "Null file should map file advice to 'unknown' key");
    }

    @Test
    @DisplayName("candidatesForFile returns empty map for unknown file")
    void candidatesForFile_returnsEmptyMapForUnknownFile() {
        AdviceCollector collector = new AdviceCollector();
        Map<Integer, List<CandidateAdvice>> candidates = collector.candidatesForFile("nonexistent");
        assertTrue(candidates.isEmpty(), "Should return empty map for unrecorded file");
    }

    @Test
    @DisplayName("fileAdviceForFile returns empty list for unknown file")
    void fileAdviceForFile_returnsEmptyListForUnknownFile() {
        AdviceCollector collector = new AdviceCollector();
        List<FileAdviceDetails> result = collector.fileAdviceForFile("nonexistent");
        assertTrue(result.isEmpty(), "Should return empty list for unrecorded file");
    }

    @Test
    @DisplayName("clearFile removes candidates and file advice for specified file")
    void clearFile_removesCandidatesAndFileAdvice() {
        AdviceCollector collector = new AdviceCollector();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(1)
                .fileName("Test.java")
                .build();
        collector.record(candidate);
        collector.recordFileAdvice("Test.java",
                new FileAdviceDetails(AdviceId.MMCommentMapStringObject, 1,
                java.util.Collections.singletonList("item"), null, null, null, null, null, null));

        collector.clearFile("Test.java");

        assertTrue(collector.candidatesForFile("Test.java").isEmpty(),
                "Candidates should be cleared for removed file");
        assertTrue(collector.fileAdviceForFile("Test.java").isEmpty(),
                "File advice should be cleared for removed file");
    }

    @Test
    @DisplayName("clearAll removes all candidates and file advice")
    void clearAll_removesEverything() {
        AdviceCollector collector = new AdviceCollector();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(1)
                .fileName("A.java")
                .build();
        collector.record(candidate);
        collector.recordFileAdvice("B.java",
                new FileAdviceDetails(AdviceId.MMCommentMapStringObject, 1,
                java.util.Collections.singletonList("item"), null, null, null, null, null, null));

        collector.clearAll();

        assertTrue(collector.allCandidates().isEmpty(), "All candidates should be cleared");
        assertTrue(collector.candidatesForFile("A.java").isEmpty(),
                "Candidates for A.java should be cleared after clearAll");
    }

    @Test
    @DisplayName("allCandidates returns unmodifiable view of all file candidates")
    void allCandidates_returnsUnmodifiableView() {
        AdviceCollector collector = new AdviceCollector();
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(1)
                .fileName("Test.java")
                .build();
        collector.record(candidate);

        Map<String, Map<Integer, List<CandidateAdvice>>> all = collector.allCandidates();
        assertNotNull(all, "allCandidates should not return null");
        assertFalse(all.isEmpty(), "allCandidates should contain the recorded file");
    }
}
