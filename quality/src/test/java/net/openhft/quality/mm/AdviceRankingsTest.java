/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

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
}
