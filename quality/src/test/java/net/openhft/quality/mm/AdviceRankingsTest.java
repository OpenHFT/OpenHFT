/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdviceRankings}.
 */
@DisplayName("Advice rankings tests")
class AdviceRankingsTest {

    @Test
    @DisplayName("Build ranks orders by count then name")
    void buildRanks_ordersByCountThenName() {
        Map<AdviceId, Integer> counts = new EnumMap<>(AdviceId.class);
        counts.put(AdviceId.MMAssertionMessageMissing, 1);
        counts.put(AdviceId.MMAssertionMessageGeneric, 5);
        counts.put(AdviceId.MMAssertionMessageTooShort, 5);

        Map<AdviceId, Integer> ranks = AdviceRankings.buildRanks(counts);

        assertEquals(1, ranks.get(AdviceId.MMAssertionMessageMissing),
                "lowest count should be rank 1");
        int genericRank = ranks.get(AdviceId.MMAssertionMessageGeneric);
        int tooShortRank = ranks.get(AdviceId.MMAssertionMessageTooShort);
        assertNotEquals(genericRank, tooShortRank,
                "ties should still result in unique ranks");
        if (AdviceId.MMAssertionMessageGeneric.name()
                .compareTo(AdviceId.MMAssertionMessageTooShort.name()) < 0) {
            assertTrue(genericRank < tooShortRank,
                    "name ordering should break ties");
        } else {
            assertTrue(tooShortRank < genericRank,
                    "name ordering should break ties");
        }
        assertEquals(-1, ranks.get(AdviceId.MMOverusedWord),
                "unseen advice should be -1");
    }

    @Test
    @DisplayName("Load from path records unknown keys")
    void loadFromPath_recordsUnknownKeys() throws Exception {
        Path temp = Files.createTempFile("mm-advice-ranks", ".properties");
        Files.write(temp,
                ("UnknownKey=5\n" + AdviceId.MMAssertionMessageMissing.name() + "=1\n")
                        .getBytes(StandardCharsets.UTF_8));
        AdviceRankings rankings = AdviceRankings.load(temp);

        assertTrue(rankings.unknownKeys().contains("UnknownKey"),
                "unknown keys should be recorded");
        assertEquals(1, rankings.rankFor(AdviceId.MMAssertionMessageMissing),
                "known rank should be loaded");
    }

    @Test
    @DisplayName("Every AdviceId except UNKNOWN has an explicit rank entry")
    void everyAdviceIdHasRankEntry() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(AdviceReportManager.RANK_RESOURCE)) {
            assertNotNull(input, "rank resource should exist");
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        }
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            assertTrue(properties.containsKey(adviceId.name()),
                    adviceId.name() + " should have an explicit rank entry");
        }
    }

    @Test
    @DisplayName("No unknown or stale keys exist in rank properties")
    void noUnknownKeysInRankProperties() {
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        assertTrue(rankings.unknownKeys().isEmpty(),
                "rank properties should not contain unknown keys: " + rankings.unknownKeys());
    }

    // --- Edge case coverage tests ---

    @Test
    @DisplayName("Build ranks skips zero counts")
    void buildRanksSkipsZeroCounts() {
        Map<AdviceId, Integer> counts = new EnumMap<>(AdviceId.class);
        counts.put(AdviceId.MMAssertionMessageMissing, 0);
        counts.put(AdviceId.MMAssertionMessageGeneric, 3);

        Map<AdviceId, Integer> ranks = AdviceRankings.buildRanks(counts);

        assertEquals(-1, ranks.get(AdviceId.MMAssertionMessageMissing),
                "zero count should result in unranked -1");
        assertEquals(1, ranks.get(AdviceId.MMAssertionMessageGeneric),
                "positive count should be ranked 1");
    }

    @Test
    @DisplayName("Build ranks skips negative counts")
    void buildRanksSkipsNegativeCounts() {
        Map<AdviceId, Integer> counts = new EnumMap<>(AdviceId.class);
        counts.put(AdviceId.MMAssertionMessageMissing, -5);
        counts.put(AdviceId.MMAssertionMessageGeneric, 2);

        Map<AdviceId, Integer> ranks = AdviceRankings.buildRanks(counts);

        assertEquals(-1, ranks.get(AdviceId.MMAssertionMessageMissing),
                "negative count should result in unranked -1");
        assertEquals(1, ranks.get(AdviceId.MMAssertionMessageGeneric),
                "positive count should be ranked 1");
    }

    @Test
    @DisplayName("Build ranks with empty counts produces all unranked")
    void buildRanksEmptyCountsProducesAllUnranked() {
        Map<AdviceId, Integer> counts = new EnumMap<>(AdviceId.class);

        Map<AdviceId, Integer> ranks = AdviceRankings.buildRanks(counts);

        assertEquals(-1, ranks.get(AdviceId.MMAssertionMessageMissing),
                "absent advice should be unranked -1");
        assertEquals(-1, ranks.get(AdviceId.MMAssertionMessageGeneric),
                "absent advice should be unranked -1");
    }

    @Test
    @DisplayName("Write returns early for null output path")
    void writeReturnsEarlyForNullOutput() {
        Map<AdviceId, Integer> ranks = new EnumMap<>(AdviceId.class);
        ranks.put(AdviceId.MMAssertionMessageMissing, 1);
        AdviceRankings.write(null, ranks);
        // Should not throw
    }

    @Test
    @DisplayName("Write returns early for null ranks map")
    void writeReturnsEarlyForNullRanks() throws Exception {
        Path temp = Files.createTempFile("mm-advice-ranks-null", ".properties");
        Files.delete(temp);
        AdviceRankings.write(temp, null);
        assertFalse(Files.exists(temp),
                "file should not be created when ranks are null");
    }

    @Test
    @DisplayName("Write creates output file with all advice ids")
    void writeCreatesOutputFile() throws Exception {
        Path temp = Files.createTempFile("mm-advice-ranks-write", ".properties");
        Map<AdviceId, Integer> ranks = new EnumMap<>(AdviceId.class);
        ranks.put(AdviceId.MMAssertionMessageMissing, 1);
        ranks.put(AdviceId.MMAssertionMessageGeneric, 2);
        AdviceRankings.write(temp, ranks);

        assertTrue(Files.exists(temp),
                "output file should be created by write");
        String content = Files.readString(temp);
        assertTrue(content.contains("MMAssertionMessageMissing=1"),
                "written file should contain ranked advice id");
    }

    @Test
    @DisplayName("From properties ignores empty key and empty value")
    void fromPropertiesIgnoresEmptyKeyAndEmptyValue() throws Exception {
        Path temp = Files.createTempFile("mm-advice-ranks-empty", ".properties");
        Files.write(temp,
                (AdviceId.MMAssertionMessageMissing.name() + "=\n"
                        + "=5\n"
                        + AdviceId.MMAssertionMessageGeneric.name() + "=3\n")
                        .getBytes(StandardCharsets.UTF_8));
        AdviceRankings rankings = AdviceRankings.load(temp);

        assertEquals(-1, rankings.rankFor(AdviceId.MMAssertionMessageMissing),
                "empty value should result in default -1 rank");
        assertEquals(3, rankings.rankFor(AdviceId.MMAssertionMessageGeneric),
                "valid entry should be loaded");
    }

    @Test
    @DisplayName("Rank for unknown advice returns minus one")
    void rankForUnknownAdviceReturnsMinusOne() {
        AdviceRankings rankings = AdviceRankings.loadFromResource(AdviceReportManager.RANK_RESOURCE);
        assertEquals(-1, rankings.rankFor(AdviceId.UNKNOWN),
                "UNKNOWN should always return -1");
    }
}
