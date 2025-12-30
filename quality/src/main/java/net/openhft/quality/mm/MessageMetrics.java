/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable metrics describing a message's content.
 */
public final class MessageMetrics {
    private final int charCount;
    private final int wordCount;
    private final int meaningfulWordCount;
    private final int totalWordCount;
    private final int effectiveMeaningfulWordCount;
    private final List<String> meaningfulWords;
    private final List<String> longWords;

    /**
     * Create a metrics snapshot for a message.
     *
     * @param charCount                    character count for the message.
     * @param wordCount                    word count excluding placeholders.
     * @param meaningfulWordCount          count of unique meaningful words.
     * @param totalWordCount               total word count including placeholders.
     * @param effectiveMeaningfulWordCount effective meaningful count including placeholders.
     * @param meaningfulWords              list of unique meaningful words.
     * @param longWords                    list of words exceeding the maximum length.
     */
    public MessageMetrics(int charCount, int wordCount, int meaningfulWordCount,
                          int totalWordCount, int effectiveMeaningfulWordCount,
                          List<String> meaningfulWords, List<String> longWords) {
        this.charCount = charCount;
        this.wordCount = wordCount;
        this.meaningfulWordCount = meaningfulWordCount;
        this.totalWordCount = totalWordCount;
        this.effectiveMeaningfulWordCount = effectiveMeaningfulWordCount;
        Objects.requireNonNull(meaningfulWords);
        this.meaningfulWords = Collections.unmodifiableList(meaningfulWords);
        Objects.requireNonNull(longWords);
        this.longWords = Collections.unmodifiableList(longWords);
    }

    /**
     * Create a metrics snapshot for a message without meaningful word detail.
     *
     * @param charCount                    character count for the message.
     * @param wordCount                    word count excluding placeholders.
     * @param meaningfulWordCount          count of unique meaningful words.
     * @param totalWordCount               total word count including placeholders.
     * @param effectiveMeaningfulWordCount effective meaningful count including placeholders.
     * @param longWords                    list of words exceeding the maximum length.
     */
    public MessageMetrics(int charCount, int wordCount, int meaningfulWordCount,
                          int totalWordCount, int effectiveMeaningfulWordCount,
                          List<String> longWords) {
        this(charCount, wordCount, meaningfulWordCount, totalWordCount,
                effectiveMeaningfulWordCount, Collections.emptyList(), longWords);
    }

    /**
     * Return the character count.
     *
     * @return character count.
     */
    public int charCount() {
        return charCount;
    }

    /**
     * Return the word count excluding placeholders.
     *
     * @return word count excluding placeholders.
     */
    public int wordCount() {
        return wordCount;
    }

    /**
     * Return the unique meaningful word count excluding placeholders.
     *
     * @return unique meaningful word count excluding placeholders.
     */
    public int meaningfulWordCount() {
        return meaningfulWordCount;
    }

    /**
     * Return the total word count including placeholders.
     *
     * @return total word count including placeholders.
     */
    public int totalWordCount() {
        return totalWordCount;
    }

    /**
     * Return the effective meaningful word count including placeholders.
     *
     * @return effective meaningful word count including placeholders.
     */
    public int effectiveMeaningfulWordCount() {
        return effectiveMeaningfulWordCount;
    }

    /**
     * Return the list of unique meaningful words.
     *
     * @return unmodifiable list of unique meaningful words.
     */
    public List<String> meaningfulWords() {
        return meaningfulWords;
    }

    /**
     * Return the list of words exceeding the maximum length.
     *
     * @return unmodifiable list of long words.
     */
    public java.util.List<String> longWords() {
        return longWords;
    }
}
