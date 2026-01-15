/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MessageMetricsCalculator}.
 */
@DisplayName("Message metrics calculator tests scenario case")
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
    @DisplayName("Max word count returns 42 scenario")
    void maxWordCountReturns42() {
        assertEquals(42, calculator.maxWordCount(), "maxWordCount should be 42");
    }

    @Test
    @DisplayName("Max word length returns 42 scenario")
    void maxWordLengthReturns42() {
        assertEquals(42, calculator.maxWordLength(), "maxWordLength should be 42");
    }

    @Test
    @DisplayName("Word at max length not flagged as long")
    void wordAtMaxLengthNotFlaggedAsLong() {
        // Create a 42-character word - exactly at limit, should NOT be flagged
        String word42 = repeat('a', 42);
        MessageMetrics metrics = calculator.calculate(word42, 0, 0);
        assertTrue(metrics.longWords().isEmpty(), "42-char word should not be flagged as long");
    }

    @Test
    @DisplayName("Word exceeding max length flagged as long")
    void wordExceedingMaxLengthFlaggedAsLong() {
        // Create a 43-character word - exceeds limit, SHOULD be flagged
        String word43 = repeat('a', 43);
        MessageMetrics metrics = calculator.calculate(word43, 0, 0);
        assertEquals(1, metrics.longWords().size(), "43-char word should be flagged as long");
        assertEquals(word43, metrics.longWords().get(0), "long word content mismatch");
    }

    @DisplayName("Is filler word case insensitive scenario")
    @ParameterizedTest
    @ValueSource(strings = {"THE", "The", "the", "tHe"})
    void isFillerWordCaseInsensitive(String word) {
        assertTrue(calculator.isFillerWord(word), "'" + word + "' should be filler word");
    }

    @Test
    @DisplayName("Is filler word returns false for non filler scenario case")
    void isFillerWordReturnsFalseForNonFiller() {
        assertFalse(calculator.isFillerWord("configuration"), "configuration is not a filler word");
        assertFalse(calculator.isFillerWord("database"), "database is not a filler word");
        assertFalse(calculator.isFillerWord("account"), "account is not a filler word");
    }

    @Test
    @DisplayName("Is filler word treats domain tokens as meaningful")
    void isFillerWordTreatsDomainTokensAsMeaningful() {
        assertFalse(calculator.isFillerWord("Wire"), "Wire should be a meaningful word");
        assertFalse(calculator.isFillerWord("YAML"), "YAML should be a meaningful word");
        assertFalse(calculator.isFillerWord("JSON"), "JSON should be a meaningful word");
        assertFalse(calculator.isFillerWord("UUID"), "UUID should be a meaningful word");
        assertFalse(calculator.isFillerWord("DTO"), "DTO should be a meaningful word");
    }

    @Test
    @DisplayName("Calculate counts domain tokens as meaningful")
    void calculateCountsDomainTokensAsMeaningful() {
        String message = "wire yaml json uuid dto";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(), "wordCount should include all tokens");
        assertEquals(5, metrics.meaningfulWordCount(), "domain tokens should count as meaningful words");
    }

    @Test
    @DisplayName("Is filler word rejects null scenario")
    void isFillerWordRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.isFillerWord(null),
                "isFillerWord should reject null");
    }

    @Test
    @DisplayName("Calculate rejects null message scenario case")
    void calculateRejectsNullMessage() {
        assertThrows(NullPointerException.class,
                () -> calculator.calculate(null, 0, 0),
                "calculate should reject null message");
    }

    @Test
    @DisplayName("Split words rejects null scenario case")
    void splitWordsRejectsNull() {
        assertThrows(NullPointerException.class,
                () -> calculator.splitWords(null),
                "splitWords should reject null");
    }

    @Test
    @DisplayName("Calculate char count matches message length")
    void calculateCharCountMatchesMessageLength() {
        String message = "user account balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(message.length(), metrics.charCount(), "charCount should match message length");
    }

    @Test
    @DisplayName("Calculate word count excludes empty strings")
    void calculateWordCountExcludesEmptyStrings() {
        // Multiple spaces create empty strings when split
        String message = "user  account   balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(3, metrics.wordCount(), "wordCount should be 3 (empty strings excluded)");
    }

    @Test
    @DisplayName("Calculate meaningful word count excludes fillers")
    void calculateMeaningfulWordCountExcludesFillers() {
        // "the" and "is" are filler words, "account" and "balance" are meaningful
        String message = "the account balance is updated";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(), "wordCount should be 5");
        // "the", "is" are filler; "account", "balance", "updated" are meaningful
        assertEquals(3, metrics.meaningfulWordCount(), "meaningfulWordCount should be 3");
    }

    @Test
    @DisplayName("Colon counts as filler word for word count")
    void colonCountsAsFillerWord() {
        String message = "status: active";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(3, metrics.wordCount(), "wordCount should include colon token");
        assertEquals(2, metrics.meaningfulWordCount(), "colon should not increase meaningful count");
    }

    @Test
    @DisplayName("All filler symbols count as filler words for word count")
    void allFillerSymbolsCountAsFillerWords() {
        String[] fillerSymbols = {":", "&", "%", "$", ";", "|", "\\", "*", "+", "/", "-"};
        String message = "alpha " + String.join(" ", fillerSymbols) + " beta";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        int expectedWordCount = 2 + fillerSymbols.length;
        assertEquals(expectedWordCount, metrics.wordCount(),
                "wordCount should include filler symbol tokens");
        assertEquals(2, metrics.meaningfulWordCount(),
                "filler symbols should not add meaningful words");
    }

    @Test
    @DisplayName("Symbol runs are evaluated as whole tokens")
    void symbolRunsAreEvaluatedAsWholeTokens() {
        String message = "alpha <> beta => gamma >> delta << epsilon === zeta // eta ** theta #|# iota";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(9, metrics.wordCount(),
                "symbol runs should not be split into known tokens");
        assertEquals(9, metrics.meaningfulWordCount(),
                "symbol runs should not contribute meaningful words");
    }

    @Test
    @DisplayName("All comparison operators count as meaningful words")
    void allComparisonOperatorsCountAsMeaningfulWords() {
        String message = "alpha >= beta <= gamma != delta == epsilon > zeta < eta -> theta";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(15, metrics.wordCount(),
                "wordCount should include operator tokens");
        assertEquals(15, metrics.meaningfulWordCount(),
                "operators should count as meaningful words");
        assertTrue(metrics.meaningfulWords().containsAll(
                java.util.Arrays.asList(">=", "<=", "!=", "==", ">", "<", "->")),
                "meaningfulWords should include all operators");
    }

    @Test
    @DisplayName("Duplicate operator tokens only appear once in meaningful words")
    void duplicateOperatorTokensOnlyAppearOnceInMeaningfulWords() {
        java.util.LinkedHashSet<String> unique = new java.util.LinkedHashSet<>();
        java.util.List<String> meaningful = new java.util.ArrayList<>();
        java.util.List<String> operators = java.util.Arrays.asList(">=", ">=", "<=");

        calculator.appendOperatorTokens(unique, meaningful, operators);

        assertEquals(java.util.Arrays.asList(">=", "<="), meaningful,
                "operator tokens should only be added once");
        assertEquals(2, unique.size(), "unique operator count should match de-duplicated list");
    }

    @Test
    @DisplayName("Format placeholders imply meaningful words")
    void formatPlaceholdersImplyMeaningfulWords() {
        String message = "alpha %s beta %d gamma";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(),
                "wordCount should include format placeholders");
        assertEquals(5, metrics.meaningfulWordCount(),
                "format placeholders should count as meaningful words");
    }

    @Test
    @DisplayName("Whitespace escapes and %n are treated as whitespace")
    void whitespaceEscapesAndPercentNAreWhitespace() {
        String message = "alpha %n beta \\n gamma \\t delta \\r epsilon";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(5, metrics.wordCount(),
                "wordCount should ignore whitespace escape tokens");
        assertEquals(5, metrics.meaningfulWordCount(),
                "whitespace escape tokens should not add meaning");
    }

    @Test
    @DisplayName("Multiple %n tokens are treated as whitespace")
    void multiplePercentNIsWhitespace() {
        String message = "alpha %n beta %n gamma";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(3, metrics.wordCount(),
                "wordCount should ignore repeated %n tokens");
        assertEquals(3, metrics.meaningfulWordCount(),
                "meaningfulWordCount should ignore repeated %n tokens");
    }

    @Test
    @DisplayName("Non-whitespace escapes remain as tokens")
    void nonWhitespaceEscapesRemainAsTokens() {
        String message = "alpha \\x beta";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(4, metrics.wordCount(),
                "wordCount should include non-whitespace escape token and filler symbol");
        assertEquals(3, metrics.meaningfulWordCount(),
                "meaningfulWordCount should include non-whitespace escape token");
    }

    @Test
    @DisplayName("Calculate meaningful word count uses unique words")
    void calculateMeaningfulWordCountUsesUniqueWords() {
        String message = "account account balance balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(4, metrics.wordCount(), "wordCount should include duplicates");
        assertEquals(2, metrics.meaningfulWordCount(), "duplicates should not increase unique count");
        assertEquals(java.util.Arrays.asList("account", "balance"), metrics.meaningfulWords(),
                "meaningfulWords should preserve first-seen unique words");
    }

    @Test
    @DisplayName("Calculate total word count includes placeholders")
    void calculateTotalWordCountIncludesPlaceholders() {
        String message = "balance updated";
        MessageMetrics metrics = calculator.calculate(message, 2, 0);
        assertEquals(2, metrics.wordCount(), "wordCount should be 2");
        assertEquals(4, metrics.totalWordCount(), "totalWordCount should include placeholders");
    }

    @Test
    @DisplayName("Calculate effective meaningful includes placeholders scenario")
    void calculateEffectiveMeaningfulIncludesPlaceholders() {
        String message = "the value"; // "the" is filler, "value" is filler too
        MessageMetrics metrics = calculator.calculate(message, 1, 0);
        assertEquals(0, metrics.meaningfulWordCount(), "both words are fillers");
        assertEquals(1, metrics.effectiveMeaningfulWordCount(), "placeholder adds meaning");
    }

    @Test
    @DisplayName("Key value labels affect effective meaningful count")
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
    @DisplayName("Key value labels ignored when placeholders less than labels")
    void keyValueLabelsIgnoredWhenPlaceholdersLessThanLabels() {
        String message = "status";
        MessageMetrics metrics = calculator.calculate(message, 1, 3);
        // placeholderCount (1) < keyValueLabelCount (3), so keyValue adjustment skipped
        // effectiveMeaningful = meaningfulWordCount + placeholderCount = 1 + 1 = 2
        assertEquals(2, metrics.effectiveMeaningfulWordCount(), "keyValueLabels ignored when < placeholders");
    }

    @Test
    @DisplayName("Key value labels zero has no effect")
    void keyValueLabelsZeroHasNoEffect() {
        String message = "status";
        MessageMetrics metrics = calculator.calculate(message, 2, 0);
        // keyValueLabelCount = 0, so no adjustment
        // effectiveMeaningful = meaningfulWordCount + placeholderCount = 1 + 2 = 3
        assertEquals(3, metrics.effectiveMeaningfulWordCount(), "zero keyValueLabels has no effect");
    }

    @Test
    @DisplayName("Split words uses non alphanumeric delimiters")
    void splitWordsUsesNonAlphanumericDelimiters() {
        String[] words = calculator.splitWords("hello-world_test.example:value");
        // Split on non-alphanumeric: "-", "_", ".", ":"
        assertEquals(5, words.length, "should split on non-alphanumeric chars");
        assertEquals("hello", words[0], "first word mismatch");
        assertEquals("world", words[1], "second word mismatch");
    }

    @Test
    @DisplayName("Empty message produces zero metrics scenario")
    void emptyMessageProducesZeroMetrics() {
        MessageMetrics metrics = calculator.calculate("", 0, 0);
        assertEquals(0, metrics.charCount(), "empty message has 0 chars");
        assertEquals(0, metrics.wordCount(), "empty message has 0 words");
        assertEquals(0, metrics.meaningfulWordCount(), "empty message has 0 meaningful words");
    }

    @Test
    @DisplayName("Multiple filler words counted correctly scenario")
    void multipleFillerWordsCountedCorrectly() {
        // All these are filler words
        String message = "the value is null and empty error failed";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(8, metrics.wordCount(), "8 words total");
        assertEquals(0, metrics.meaningfulWordCount(), "all words are fillers");
    }

    @Test
    @DisplayName("Mixed filler and meaningful words scenario")
    void mixedFillerAndMeaningfulWords() {
        // "account" and "balance" are meaningful, "the" and "is" are fillers
        String message = "account balance";
        MessageMetrics metrics = calculator.calculate(message, 0, 0);
        assertEquals(2, metrics.wordCount(), "2 words total");
        assertEquals(2, metrics.meaningfulWordCount(), "2 meaningful words");
    }

    // --- Boundary mutation killing tests ---

    @Test
    @DisplayName("Key value labels equal to placeholders applies adjustment boundary")
    void keyValueLabelsEqualToPlaceholders_appliesAdjustment() {
        // Boundary test: placeholderCount == keyValueLabelCount should STILL apply adjustment
        // This kills the mutation that changes >= to >
        String message = "status";  // "status" is meaningful
        // placeholderCount = keyValueLabelCount = 2
        MessageMetrics metrics = calculator.calculate(message, 2, 2);
        // meaningfulWordCount = 1, placeholderCount = 2, keyValueLabelCount = 2
        // effectiveMeaningful = max(1 + 2, 2 + 2) = max(3, 4) = 4
        assertEquals(4, metrics.effectiveMeaningfulWordCount(),
                "keyValueLabels should apply when placeholders == labels");
    }

    @Test
    @DisplayName("Key value labels one and placeholders one boundary case")
    void keyValueLabelsOne_placeholdersOne_boundaryCase() {
        // Boundary test: keyValueLabelCount > 0 (exactly 1)
        // This kills the mutation that changes > to >=
        String message = "status";  // "status" is meaningful
        // placeholderCount = 1, keyValueLabelCount = 1
        MessageMetrics metrics = calculator.calculate(message, 1, 1);
        // meaningfulWordCount = 1, placeholderCount = 1, keyValueLabelCount = 1
        // effectiveMeaningful = max(1 + 1, 1 + 1) = max(2, 2) = 2
        assertEquals(2, metrics.effectiveMeaningfulWordCount(),
                "boundary case with both counts at 1");
    }

    @Test
    @DisplayName("Negative key value label count not expected")
    void negativeKeyValueLabelCount_behavesAsZero() {
        // Verify negative values behave as if keyValueLabelCount <= 0
        String message = "status";
        MessageMetrics metrics = calculator.calculate(message, 2, -1);
        // keyValueLabelCount = -1 < 0, so adjustment skipped
        // effectiveMeaningful = 1 + 2 = 3
        assertEquals(3, metrics.effectiveMeaningfulWordCount(),
                "negative keyValueLabelCount should skip adjustment");
    }
}
