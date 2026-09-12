/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that {@code mm-advice-ranks.properties} stays in sync with {@link AdviceId}.
 * <p>
 * Every non-UNKNOWN AdviceId must have a rank entry, and every key in the
 * properties file must correspond to a valid AdviceId. This catches silent
 * "unranked" advice caused by adding a new AdviceId without updating the
 * ranks file, and stale entries for removed AdviceIds.
 */
@DisplayName("Advice ranks sync tests because ranks must cover all advice IDs")
class AdviceRanksSyncTest {

    private static final String RANK_RESOURCE = "net/openhft/quality/mm-advice-ranks.properties";

    @Test
    @DisplayName("Every non-UNKNOWN AdviceId has a rank entry")
    void everyAdviceIdHasRankEntry() throws IOException {
        Properties ranks = loadRanks();
        Set<String> missing = Stream.of(AdviceId.values())
                .filter(id -> id != AdviceId.UNKNOWN)
                .map(Enum::name)
                .filter(name -> !ranks.containsKey(name))
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(missing.isEmpty(),
                "AdviceId(s) missing from " + RANK_RESOURCE + ": " + missing);
    }

    @Test
    @DisplayName("Every rank entry maps to a valid AdviceId")
    void everyRankEntryMapsToAdviceId() throws IOException {
        Properties ranks = loadRanks();
        Set<String> validNames = Stream.of(AdviceId.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        Set<String> stale = ranks.stringPropertyNames().stream()
                .filter(key -> !validNames.contains(key))
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(stale.isEmpty(),
                "Stale keys in " + RANK_RESOURCE + " (no matching AdviceId): " + stale);
    }

    @Test
    @DisplayName("Rank values are integers")
    void rankValuesAreIntegers() throws IOException {
        Properties ranks = loadRanks();
        for (String key : ranks.stringPropertyNames()) {
            String value = ranks.getProperty(key);
            assertDoesNotThrow(() -> Integer.parseInt(value),
                    "Non-integer rank value for " + key + ": '" + value + "'");
        }
    }

    private Properties loadRanks() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RANK_RESOURCE)) {
            assertNotNull(in, "Cannot find resource: " + RANK_RESOURCE);
            props.load(in);
        }
        return props;
    }
}
