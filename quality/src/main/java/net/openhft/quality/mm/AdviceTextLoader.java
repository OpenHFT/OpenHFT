/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads advice text definitions from properties.
 */
public final class AdviceTextLoader {
    private static final String PREFIX = "mm.";
    private static final String TITLE = "title";
    private static final String INTRO = "intent_intro";
    private static final String OUTRO = "intent_outro";
    private static final String HINT_A = "hint_a";
    private static final String HINT_B = "hint_b";
    private static final String CHECKLIST = "checklist";
    private static final String ANTI_PATTERNS = "anti_patterns";
    private static final String VERBOSE = "verbose";
    private static final String ALLOW_INTRO_MATCH = "allow_identical_intro_outro";
    private static final String ALLOW_HINT_MATCH = "allow_identical_hints";

    private final Map<AdviceId, AdviceText> byId;

    private AdviceTextLoader(Map<AdviceId, AdviceText> byId) {
        this.byId = byId;
    }

    public static AdviceTextLoader loadFromResource(String resourcePath) {
        Properties properties = new Properties();
        ClassLoader loader = AdviceTextLoader.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Advice text resource not found: " + resourcePath);
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read advice text: " + resourcePath, e);
        }
        Map<AdviceId, AdviceText> byId = new EnumMap<>(AdviceId.class);
        for (AdviceId adviceId : AdviceId.values()) {
            if (adviceId == AdviceId.UNKNOWN) {
                continue;
            }
            String base = PREFIX + adviceId.name() + ".";
            String title = require(properties, base + TITLE);
            String intro = require(properties, base + INTRO);
            String outro = require(properties, base + OUTRO);
            String hintA = require(properties, base + HINT_A);
            String hintB = require(properties, base + HINT_B);
            String checklist = require(properties, base + CHECKLIST);
            String antiPatterns = require(properties, base + ANTI_PATTERNS);
            String verbose = require(properties, base + VERBOSE);
            boolean allowIntroMatch = Boolean.parseBoolean(properties.getProperty(base + ALLOW_INTRO_MATCH, "false"));
            boolean allowHintMatch = Boolean.parseBoolean(properties.getProperty(base + ALLOW_HINT_MATCH, "false"));
            if (!allowIntroMatch && intro.trim().equals(outro.trim())) {
                throw new IllegalStateException("Intro/outro must differ for " + adviceId.name());
            }
            if (!allowHintMatch && hintA.trim().equals(hintB.trim())) {
                throw new IllegalStateException("Hint A/B must differ for " + adviceId.name());
            }
            byId.put(adviceId, new AdviceText(adviceId, title, intro, outro, hintA, hintB, checklist,
                    antiPatterns, verbose));
        }
        return new AdviceTextLoader(byId);
    }

    public AdviceText textFor(AdviceId adviceId) {
        AdviceText text = byId.get(adviceId);
        if (text == null) {
            throw new IllegalStateException("Missing advice text for " + adviceId.name());
        }
        return text;
    }

    private static String require(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing advice text key: " + key);
        }
        return value.trim();
    }
}
