/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MessageMetricsCalculator}.
 */
class MessageMetricsCalculatorTest {

    private MessageMetricsCalculator calculator;

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    @BeforeEach
    void setUp() {
        calculator = new MessageMetricsCalculator();
    }

    @Test
    void maxWordCountReturns42() {
        assertEquals(42, calculator.maxWordCount(), "maxWordCount should be 42");
    }

    @Test
    void maxWordLengthReturns42() {
        assertEquals(42, calculator.maxWordLength(), "maxWordLength should be 42");
    }

    @Test
    void wordAtMaxLengthNotFlaggedAsLong() {
        // Create a 42-character word - exactly at limit, should NOT be flagged
        String word42 = repeat('a', 42);
        MessageMetrics metrics = calculator.calculate(word42, 0, 0);
        assertTrue(metrics.longWords().isEmpty(), "42-char word should not be flagged as long");
    }

    @Test
    void wordExceedingMaxLengthFlaggedAsLong() {
        // Create a 43-character word - exceeds limit, SHOULD be flagged
        String word43 = repeat('a', 43);
        MessageMetrics metrics = calculator.calculate(word43, 0, 0);
        assertEquals(1, metrics.longWords().size(), "43-char word should be flagged as long");
        assertEquals(word43, metrics.longWords().get(0), "long word content mismatch");
    }

    @ParameterizedTest
    @ValueSource(strings = {"THE", "The", "the", "tHe"})
    void isFillerWordCaseInsensitive(String word) {
        assertTrue(calculator.isFillerWord(word), "'" + word + "' should be filler word");
    }

    @Test
    void isFillerWordReturnsFalseForNonFiller() {
        assertFalse(calculator.isFillerWord("configuration"), "configuration is not a filler word");
        assertFalse(calculator.isFillerWord("database"), "database is not a filler word");
        assertFalse(calculator.isFillerWord("account"), "account is not a filler word");
    }

    @Test
    void isFillerWordTreatsDomainTokensAsMeaningful() {
        assertFalse(calculator.isFillerWord("Wire"), "Wire should be a meaningful word");
        assertFalse(calculator.isFillerWord("YAML"), "YAML should be a meaningful word");
        assertFalse(calculator.isFillerWord("JSON"), "JSON should be a meaningful word");
        assertFalse(calculator.isFillerWord("UUID"), "UUID should be a meaningful word");
        assertFalse(calculator.isFillerWord("DTO"), "DTO should be a meaningful word");
    }

    @Test
    void calculateCountsDomainTokensAsMeaningful() {
        String message = "wire yaml json uuid dto";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(), "wordCount should include all tokens");
        assertEquals(5, metrics.meaningfulWordCount(), "domain tokens should count as meaningful words");
    }

    @Test
    void isFillerWordRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.isFillerWord(null),
                "isFillerWord should reject null");
    }

    @Test
    void calculateRejectsNullMessage() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculate(null, 0, 0),
                "calculate should reject null message");
    }

    @Test
    void splitWordsRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.splitWords(null),
                "splitWords should reject null");
    }

    @Test
    void calculateCharCountMatchesMessageLength() {
        String message = "user account balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(message.length(), metrics.charCount(), "charCount should match message length");
    }

    @Test
    void calculateWordCountExcludesEmptyStrings() {
        // Multiple spaces create empty strings when split
        String message = "user  account   balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(3, metrics.wordCount(), "wordCount should be 3 (empty strings excluded)");
    }

    @Test
    void calculateMeaningfulWordCountExcludesFillers() {
        // "the" and "is" are filler words, "account" and "balance" are meaningful
        String message = "the account balance is updated";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(), "wordCount should be 5");
        // "the", "is" are filler; "account", "balance", "updated" are meaningful
        assertEquals(3, metrics.meaningfulWordCount(), "meaningfulWordCount should be 3");
    }

    @Test
    void calculateMeaningfulWordCountUsesUniqueWords() {
        String message = "account account balance balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(4, metrics.wordCount(), "wordCount should include duplicates");
        assertEquals(2, metrics.meaningfulWordCount(), "duplicates should not increase unique count");
        assertEquals(java.util.Arrays.asList("account", "balance"), metrics.meaningfulWords(),
                "meaningfulWords should preserve first-seen unique words");
    }

    @Test
    void calculateTotalWordCountIncludesPlaceholders() {
        String message = "balance updated";
        MessageMetrics metrics = calculator.calculate(message, 2, 0);
        assertEquals(2, metrics.wordCount(), "wordCount should be 2");
        assertEquals(4, metrics.totalWordCount(), "totalWordCount should include placeholders");
    }

    @Test
    void calculateEffectiveMeaningfulIncludesPlaceholders() {
        String message = "the value"; // "the" is filler, "value" is filler too
        MessageMetrics metrics = calculator.calculate(message, 1, 0);
        assertEquals(0, metrics.meaningfulWordCount(), "both words are fillers");
        assertEquals(1, metrics.effectiveMeaningfulWordCount(), "placeholder adds meaning");
    }

    @Test
    void keyValueLabelsAffectEffectiveMeaningfulCount() {
        // When keyValueLabelCount > 0 and placeholders >= keyValueLabelCount,
        // effectiveMeaningful = max(meaningfulWordCount + placeholderCount, placeholderCount + keyValueLabelCount)
        String message = "status"; // "status" is NOT a filler word
        MessageMetrics metrics = calculator.calculate(message, 3, 2);
        // meaningfulWordCount = 1 (status)
        // effectiveMeaningful = max(1 + 3, 3 + 2) = max(4, 5) = 5
        assertEquals(5, metrics.effectiveMeaningfulWordCount(), "keyValueLabels should increase effective count");
    }

    @Test
    void keyValueLabelsIgnoredWhenPlaceholdersLessThanLabels() {
        String message = "status";
        MessageMetrics metrics = calculator.calculate(message, 1, 3);
        // placeholderCount (1) < keyValueLabelCount (3), so keyValue adjustment skipped
        // effectiveMeaningful = meaningfulWordCount + placeholderCount = 1 + 1 = 2
        assertEquals(2, metrics.effectiveMeaningfulWordCount(), "keyValueLabels ignored when < placeholders");
    }

    @Test
    void keyValueLabelsZeroHasNoEffect() {
        String message = "status";
        MessageMetrics metrics = calculator.calculate(message, 2, 0);
        // keyValueLabelCount = 0, so no adjustment
        // effectiveMeaningful = meaningfulWordCount + placeholderCount = 1 + 2 = 3
        assertEquals(3, metrics.effectiveMeaningfulWordCount(), "zero keyValueLabels has no effect");
    }

    @Test
    void splitWordsUsesNonAlphanumericDelimiters() {
        String[] words = calculator.splitWords("hello-world_test.example:value");
        // Split on non-alphanumeric: "-", "_", ".", ":"
        assertEquals(5, words.length, "should split on non-alphanumeric chars");
        assertEquals("hello", words[0], "first word mismatch");
        assertEquals("world", words[1], "second word mismatch");
    }

    @Test
    void emptyMessageProducesZeroMetrics() {
        MessageMetrics metrics = calculator.calculate("", 0, 0);
        assertEquals(0, metrics.charCount(), "empty message has 0 chars");
        assertEquals(0, metrics.wordCount(), "empty message has 0 words");
        assertEquals(0, metrics.meaningfulWordCount(), "empty message has 0 meaningful words");
    }

    @Test
    void multipleFillerWordsCountedCorrectly() {
        // All these are filler words
        String message = "the value is null and empty error failed";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(8, metrics.wordCount(), "8 words total");
        assertEquals(0, metrics.meaningfulWordCount(), "all words are fillers");
    }

    @Test
    void mixedFillerAndMeaningfulWords() {
        // "account" and "balance" are meaningful, "the" and "is" are fillers
        String message = "account balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(2, metrics.wordCount(), "2 words total");
        assertEquals(2, metrics.meaningfulWordCount(), "2 meaningful words");
    }
}
