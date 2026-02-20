/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Loads and writes advice ranking properties.
 * Lower ranks are rarer and selected first; -1 represents unknown or unranked advice.
 */
public final class AdviceRankings {
    private final Map<AdviceId, Integer> ranks;
    private final List<String> unknownKeys;

    private AdviceRankings(Map<AdviceId, Integer> ranks, List<String> unknownKeys) {
        this.ranks = ranks;
        this.unknownKeys = unknownKeys;
    }

    public static AdviceRankings loadFromResource(String resourcePath) {
        Properties properties = new Properties();
        ClassLoader loader = AdviceRankings.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Advice rank resource not found: " + resourcePath);
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read advice ranks: " + resourcePath, e);
        }
        return fromProperties(properties);
    }

    public static AdviceRankings load(Path path) {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read advice ranks: " + path, e);
        }
        return fromProperties(properties);
    }

    public int rankFor(AdviceId adviceId) {
        Integer rank = ranks.get(adviceId);
        return rank == null ? -1 : rank;
    }

    public Map<AdviceId, Integer> ranks() {
        return ranks;
    }

    public List<String> unknownKeys() {
        return unknownKeys;
    }

    public static Map<AdviceId, Integer> buildRanks(Map<AdviceId, Integer> counts) {
        Map<AdviceId, Integer> result = new EnumMap<>(AdviceId.class);
        List<Map.Entry<AdviceId, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Comparator
                .comparingInt((Map.Entry<AdviceId, Integer> entry) -> entry.getValue())
                .thenComparing(entry -> entry.getKey().name()));
        int rank = 1;
        for (Map.Entry<AdviceId, Integer> entry : entries) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            result.put(entry.getKey(), rank++);
        }
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            result.putIfAbsent(adviceId, -1);
        }
        return result;
    }

    public static void write(Path output, Map<AdviceId, Integer> ranks) {
        if (output == null || ranks == null) {
            return;
        }
        try {
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create rank output directory: " + output, e);
        }
        List<AdviceId> ids = new ArrayList<>();
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            ids.add(adviceId);
        }
        ids.sort(Comparator.comparing(Enum::name));
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8)) {
            for (AdviceId adviceId : ids) {
                int rank = ranks.getOrDefault(adviceId, -1);
                writer.write(adviceId.name());
                writer.write('=');
                writer.write(Integer.toString(rank));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write advice ranks to " + output, e);
        }
    }

    private static AdviceRankings fromProperties(Properties properties) {
        Map<AdviceId, Integer> ranks = new EnumMap<>(AdviceId.class);
        List<String> unknownKeys = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = String.valueOf(entry.getKey()).trim();
            if (key.isEmpty()) {
                continue;
            }
            AdviceId adviceId = AdviceId.forName(key);
            if (adviceId == AdviceId.UNKNOWN) {
                unknownKeys.add(key);
                continue;
            }
            String value = entry.getValue() == null ? null : entry.getValue().toString().trim();
            if (value == null || value.isEmpty()) {
                continue;
            }
            try {
                int rank = Integer.parseInt(value);
                ranks.put(adviceId, rank);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid rank for " + key + ": " + value, e);
            }
        }
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            ranks.putIfAbsent(adviceId, -1);
        }
        return new AdviceRankings(Collections.unmodifiableMap(ranks),
                Collections.unmodifiableList(unknownKeys));
    }
}
