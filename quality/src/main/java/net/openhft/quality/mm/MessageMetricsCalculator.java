/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Calculates word and placeholder metrics for message strings.
 */
public final class MessageMetricsCalculator {
    /**
     * Maximum word count for a message before triggering MMTooLong.
     * Chosen to allow detailed messages while discouraging essays.
     * Beyond ~40 words, messages become harder to scan at a glance.
     */
    private static final int MAX_WORD_COUNT = 42;

    /**
     * Maximum word length before flagging as a long word (likely an identifier).
     * Most English words are under 20 characters; technical identifiers like
     * class names or variable names often exceed this threshold.
     */
    private static final int MAX_WORD_LENGTH = 42;

    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-zA-Z0-9]+");

    private static final Set<String> FILLER_WORDS = new HashSet<>(
            java.util.Arrays.asList(
                    "a", "an", "the", "is", "are", "was", "were", "be", "been",
                    "to", "of", "in", "for", "on", "at", "by", "with", "from",
                    "as", "if", "or", "and", "not", "no", "when", "after", "before",
                    "should", "must", "expected", "actual", "value", "result",
                    "test", "check", "assert", "equals", "return", "returns",
                    "read", "todo", "this",
                    "error", "fail", "failed", "failure",
                    "null", "true", "false", "non", "empty",
                    "have", "has", "does", "contain", "contains", "exist", "exists",
                    "present", "set", "get", "first", "second", "one", "two",
                    "more", "only", "all", "without", "within",
                    "line", "method", "class", "assertion", "occurred", "here",
                    "call", "new", "instance", "created", "successfully"
            )
    );

    /**
     * Create a metrics calculator.
     */
    public MessageMetricsCalculator() {
    }

    /**
     * Calculate metrics for the provided message.
     *
     * @param message            message text.
     * @param placeholderCount   number of placeholders.
     * @param keyValueLabelCount number of key-value labels.
     * @return computed metrics.
     */
    public MessageMetrics calculate(String message, int placeholderCount, int keyValueLabelCount) {
        Objects.requireNonNull(message);
        String[] words = splitWords(message);
        int wordCount = 0;
        int meaningfulWordCount = 0;
        List<String> longWords = new ArrayList<>();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            wordCount++;
            if (word.length() > MAX_WORD_LENGTH) {
                longWords.add(word);
            }
            if (!isFillerWord(word)) {
                meaningfulWordCount++;
            }
        }
        int totalWordCount = wordCount + placeholderCount;
        int effectiveMeaningfulWordCount = meaningfulWordCount + placeholderCount;
        if (keyValueLabelCount > 0 && placeholderCount >= keyValueLabelCount) {
            effectiveMeaningfulWordCount = Math.max(effectiveMeaningfulWordCount,
                    placeholderCount + keyValueLabelCount);
        }
        return new MessageMetrics(message.length(), wordCount, meaningfulWordCount,
                totalWordCount, effectiveMeaningfulWordCount, longWords);
    }

    /**
     * Return the maximum allowed word count.
     *
     * @return maximum word count.
     */
    public int maxWordCount() {
        return MAX_WORD_COUNT;
    }

    /**
     * Return the maximum allowed word length.
     *
     * @return maximum word length.
     */
    public int maxWordLength() {
        return MAX_WORD_LENGTH;
    }

    /**
     * Check whether a word is treated as filler.
     *
     * @param word word to check.
     * @return {@code true} if the word is treated as filler.
     */
    public boolean isFillerWord(String word) {
        Objects.requireNonNull(word);
        return FILLER_WORDS.contains(word.toLowerCase());
    }

    /**
     * Split a message into words using the calculator's tokenisation rules.
     *
     * @param message message text.
     * @return array of words.
     */
    public String[] splitWords(String message) {
        Objects.requireNonNull(message);
        return WORD_SPLITTER.split(message);
    }
}
