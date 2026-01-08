/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MessageMetrics}.
 */
@DisplayName("Message metrics tests scenario case detail")
class MessageMetricsTest {

    @Test
    @DisplayName("Char count returns constructor value scenario")
    void charCountReturnsConstructorValue() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertEquals(25, metrics.charCount(), "charCount should return constructor value");
    }

    @Test
    @DisplayName("Word count returns constructor value scenario")
    void wordCountReturnsConstructorValue() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertEquals(5, metrics.wordCount(), "wordCount should return constructor value");
    }

    @Test
    @DisplayName("Meaningful word count returns constructor value")
    void meaningfulWordCountReturnsConstructorValue() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertEquals(3, metrics.meaningfulWordCount(), "meaningfulWordCount should return value");
    }

    @Test
    @DisplayName("Meaningful words returns empty list when not provided")
    void meaningfulWordsReturnsEmptyListWhenNotProvided() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertTrue(metrics.meaningfulWords().isEmpty(), "meaningfulWords should be empty when not provided");
    }

    @Test
    @DisplayName("Meaningful words returns provided list scenario")
    void meaningfulWordsReturnsProvidedList() {
        List<String> meaningful = Arrays.asList("account", "balance");
        MessageMetrics metrics = new MessageMetrics(25, 5, 2, 6, 4, meaningful, Collections.emptyList());
        assertEquals(meaningful, metrics.meaningfulWords(), "meaningfulWords should match provided list");
    }

    @Test
    @DisplayName("Meaningful words returns unmodifiable list scenario")
    void meaningfulWordsReturnsUnmodifiableList() {
        List<String> meaningful = Collections.singletonList("account");
        MessageMetrics metrics = new MessageMetrics(25, 5, 1, 6, 4, meaningful, Collections.emptyList());
        assertThrows(UnsupportedOperationException.class,
                () -> metrics.meaningfulWords().add("balance"),
                "meaningfulWords list should be unmodifiable");
    }

    @Test
    @DisplayName("Total word count returns constructor value")
    void totalWordCountReturnsConstructorValue() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertEquals(6, metrics.totalWordCount(), "totalWordCount should return constructor value");
    }

    @Test
    @DisplayName("Effective meaningful word count returns constructor value")
    void effectiveMeaningfulWordCountReturnsConstructorValue() {
        MessageMetrics metrics = new MessageMetrics(25, 5, 3, 6, 4, Collections.emptyList());
        assertEquals(4, metrics.effectiveMeaningfulWordCount(), "effectiveMeaningfulWordCount mismatch");
    }

    @Test
    @DisplayName("Long words returns empty list when none provided")
    void longWordsReturnsEmptyListWhenNoneProvided() {
        MessageMetrics metrics = new MessageMetrics(10, 2, 1, 3, 2, Collections.emptyList());
        assertTrue(metrics.longWords().isEmpty(), "longWords should be empty when none provided");
    }

    @Test
    @DisplayName("Long words returns populated list scenario")
    void longWordsReturnsPopulatedList() {
        List<String> longWordsList = Arrays.asList("superlongidentifier", "anotherVeryLongWord");
        MessageMetrics metrics = new MessageMetrics(50, 5, 2, 7, 4, longWordsList);
        assertEquals(2, metrics.longWords().size(), "longWords should contain 2 items");
        assertEquals("superlongidentifier", metrics.longWords().get(0), "first long word mismatch");
        assertEquals("anotherVeryLongWord", metrics.longWords().get(1), "second long word mismatch");
    }

    @Test
    @DisplayName("Long words returns unmodifiable list scenario")
    void longWordsReturnsUnmodifiableList() {
        List<String> longWordsList = Collections.singletonList("identifier");
        MessageMetrics metrics = new MessageMetrics(20, 3, 1, 4, 2, longWordsList);
        assertThrows(UnsupportedOperationException.class,
                () -> metrics.longWords().add("newWord"),
                "longWords list should be unmodifiable");
    }

    @Test
    @DisplayName("Constructor rejects null long words scenario")
    void constructorRejectsNullLongWords() {
        assertThrows(NullPointerException.class,
                () -> new MessageMetrics(10, 2, 1, 3, 2, null),
                "constructor should reject null longWords");
    }

    @Test
    @DisplayName("Zero values allowed scenario case detail")
    void zeroValuesAllowed() {
        MessageMetrics metrics = new MessageMetrics(0, 0, 0, 0, 0, Collections.emptyList());
        assertEquals(0, metrics.charCount(), "zero charCount allowed");
        assertEquals(0, metrics.wordCount(), "zero wordCount allowed");
        assertEquals(0, metrics.meaningfulWordCount(), "zero meaningfulWordCount allowed");
        assertEquals(0, metrics.totalWordCount(), "zero totalWordCount allowed");
        assertEquals(0, metrics.effectiveMeaningfulWordCount(), "zero effectiveMeaningfulWordCount allowed");
    }

    @Test
    @DisplayName("Negative values allowed scenario case detail")
    void negativeValuesAllowed() {
        // The class does not validate values, so negatives should be stored
        MessageMetrics metrics = new MessageMetrics(-1, -2, -3, -4, -5, Collections.emptyList());
        assertEquals(-1, metrics.charCount(), "negative charCount stored");
        assertEquals(-2, metrics.wordCount(), "negative wordCount stored");
        assertEquals(-3, metrics.meaningfulWordCount(), "negative meaningfulWordCount stored");
        assertEquals(-4, metrics.totalWordCount(), "negative totalWordCount stored");
        assertEquals(-5, metrics.effectiveMeaningfulWordCount(), "negative effectiveMeaningfulWordCount stored");
    }
}
