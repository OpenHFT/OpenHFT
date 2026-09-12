/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("File report tests")
class FileReportTest {

    @Test
    @DisplayName("candidatesByLine defensively copies nested candidate lists")
    void candidatesByLineDefensivelyCopiesNestedCandidateLists() {
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .source(AdviceSource.ASSERTION)
                .lineNo(9)
                .fileName("Test.java")
                .build();
        List<CandidateAdvice> lineCandidates = new ArrayList<>();
        lineCandidates.add(candidate);

        Map<Integer, List<CandidateAdvice>> source = new HashMap<>();
        source.put(9, lineCandidates);

        FileReport report = new FileReport("Test.java", List.of(), List.of(), source, List.of());
        lineCandidates.clear();

        assertEquals(1, report.candidatesByLine().get(9).size(),
                "report should retain its own snapshot of candidate lists");
        assertThrows(UnsupportedOperationException.class,
                () -> report.candidatesByLine().get(9).add(candidate),
                "exposed candidate lists should be immutable");
    }

    @Test
    @DisplayName("candidatesByLine replaces null nested candidate lists with empty snapshots")
    void candidatesByLineReplacesNullNestedCandidateLists() {
        Map<Integer, List<CandidateAdvice>> source = new HashMap<>();
        source.put(12, null);

        FileReport report = new FileReport("Test.java", List.of(), List.of(), source, List.of());

        assertEquals(0, report.candidatesByLine().get(12).size(),
                "report should replace null candidate lists with an empty snapshot");
        assertThrows(UnsupportedOperationException.class,
                () -> report.candidatesByLine().put(13, List.of()),
                "exposed candidate map should stay immutable");
    }
}
