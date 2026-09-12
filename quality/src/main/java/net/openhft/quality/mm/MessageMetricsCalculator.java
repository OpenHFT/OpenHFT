/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Calculates word and placeholder metrics for message strings.
 */
public final class MessageMetricsCalculator {
    /**
     * Maximum word count for a message before triggering MMTooLong.
     * Chosen to allow detailed messages while discouraging essays.
     * Beyond ~40 words, messages become harder to scan at a glance.
     * 42 is a practical upper bound — real assertion messages almost never
     * need more words than this threshold.
     */
    private static final int MAX_WORD_COUNT = 42;

    /**
     * Maximum word length before flagging as a long word (likely an identifier).
     * Most English words are under 20 characters; technical identifiers like
     * class names or variable names often exceed this threshold.
     * 42 is a practical upper bound — real assertion messages almost never
     * contain longer identifiers than this threshold.
     */
    private static final int MAX_WORD_LENGTH = 42;

    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-zA-Z0-9]+");
    private static final Pattern INLINE_TAG_PLACEHOLDER = Pattern.compile("\\{@[^}]*\\}");
    private static final Set<String> MEANINGFUL_OPERATOR_TOKENS = new HashSet<>(
            java.util.Arrays.asList(">", ">=", "<=", "<", "!=", "==", "->"));
    private static final Set<String> FILLER_SYMBOL_TOKENS = new HashSet<>(
            java.util.Arrays.asList(":", "&", "%", "$", ";", "|", "\\", "*", "+", "/", "-"));

    private static final Set<String> FILLER_WORDS = new HashSet<>(
            java.util.Arrays.asList(
                    "a", "an", "the", "is", "are", "was", "were", "be", "been",
                    "to", "of", "in", "on", "at", "by", "with", "from",
                    "as", "if", "or", "and", "not", "no", "none", "that", "when", "after", "before",
                    "around", "yet",
                    "should", "must", "expected", "actual", "value", "result", "data",
                    "test", "check", "assert", "equals", "match", "return", "returns", "throw",
                    "read", "todo", "this",
                    "error", "fail", "failed", "failure",
                    "null", "true", "false", "non", "empty", "npe",
                    "have", "has", "do", "does", "contain", "contains", "exist", "exists",
                    "present", "first", "second", "one", "once", "two",
                    "more", "only", "all", "nothing", "without", "within", "under",
                    "line", "method", "tostring", "class", "assertion", "occurred", "remains", "here",
                    "call", "called", "new", "instance", "created", "successfully", "op",
                    "input", "output", "conditions", "behaviour", "behavior",
                    "time", "unit", "configured", "system",
                    "behaviours"
            )
    );
    private static final Set<String> NON_FILLER_WORDS = new HashSet<>(
            java.util.Arrays.asList(
                    "wire", "yaml", "json", "uuid", "dto"
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
        requireNonNull(message);
        SymbolCounts symbolCounts = countSymbolTokens(message);
        String[] words = splitWords(message);
        int wordCount = 0;
        LinkedHashSet<String> uniqueMeaningfulWords = new LinkedHashSet<>();
        List<String> meaningfulWords = new ArrayList<>();
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
                String normalised = word.toLowerCase(Locale.ROOT);
                if (uniqueMeaningfulWords.add(normalised)) {
                    meaningfulWords.add(word);
                }
            }
        }
        appendOperatorTokens(uniqueMeaningfulWords, meaningfulWords, symbolCounts.operatorTokens);
        if (symbolCounts.fillerCount > 0 || symbolCounts.operatorCount > 0) {
            wordCount += symbolCounts.fillerCount + symbolCounts.operatorCount;
        }
        int meaningfulWordCount = meaningfulWords.size();
        int totalWordCount = wordCount + placeholderCount;
        int effectiveMeaningfulWordCount = meaningfulWordCount + placeholderCount;
        if (keyValueLabelCount > 0 && placeholderCount >= keyValueLabelCount) {
            effectiveMeaningfulWordCount = Math.max(effectiveMeaningfulWordCount,
                    placeholderCount + keyValueLabelCount);
        }
        return new MessageMetrics(message.length(), wordCount, meaningfulWordCount,
                totalWordCount, effectiveMeaningfulWordCount, meaningfulWords, longWords);
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
        requireNonNull(word);
        String normalised = word.toLowerCase(Locale.ROOT);
        if (NON_FILLER_WORDS.contains(normalised)) {
            return false;
        }
        return FILLER_WORDS.contains(normalised);
    }

    /**
     * Split a message into words using the calculator's tokenisation rules.
     *
     * @param message message text.
     * @return array of words.
     */
    public String[] splitWords(String message) {
        requireNonNull(message);
        String cleaned = normaliseForTokenisation(message);
        return WORD_SPLITTER.split(cleaned);
    }

    void appendOperatorTokens(Set<String> uniqueMeaningfulWords,
                              List<String> meaningfulWords,
                              List<String> operatorTokens) {
        requireNonNull(uniqueMeaningfulWords);
        requireNonNull(meaningfulWords);
        if (operatorTokens == null || operatorTokens.isEmpty()) {
            return;
        }
        for (String operator : operatorTokens) {
            if (uniqueMeaningfulWords.add(operator)) {
                meaningfulWords.add(operator);
            }
        }
    }

    private SymbolCounts countSymbolTokens(String message) {
        String cleaned = normaliseForTokenisation(message);
        int fillerCount = 0;
        int operatorCount = 0;
        LinkedHashSet<String> operators = new LinkedHashSet<>();
        int index = 0;
        int length = cleaned.length();
        while (index < length) {
            char ch = cleaned.charAt(index);
            if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)) {
                index++;
                continue;
            }
            // Treat contiguous symbol runs as single tokens; only exact matches count.
            int start = index;
            index++;
            while (index < length) {
                char next = cleaned.charAt(index);
                if (Character.isLetterOrDigit(next) || Character.isWhitespace(next)) {
                    break;
                }
                index++;
            }
            String token = cleaned.substring(start, index);
            if (MEANINGFUL_OPERATOR_TOKENS.contains(token)) {
                operatorCount++;
                operators.add(token);
            } else if (FILLER_SYMBOL_TOKENS.contains(token)) {
                fillerCount++;
            }
        }
        return new SymbolCounts(fillerCount, operatorCount, new ArrayList<>(operators));
    }

    private String normaliseForTokenisation(String message) {
        String cleaned = INLINE_TAG_PLACEHOLDER.matcher(message).replaceAll(" ");
        cleaned = normaliseEscapedWhitespace(cleaned);
        Matcher matcher = PlaceholderPatterns.STRING_FORMAT.matcher(cleaned);
        if (!matcher.find()) {
            return cleaned;
        }
        StringBuilder builder = new StringBuilder(cleaned.length() + 8);
        int last = 0;
        do {
            builder.append(cleaned, last, matcher.start());
            String token = matcher.group();
            char conversion = token.charAt(token.length() - 1);
            builder.append(' ').append(conversion).append(' ');
            last = matcher.end();
        } while (matcher.find());
        builder.append(cleaned, last, cleaned.length());
        return builder.toString();
    }

    private String normaliseEscapedWhitespace(String message) {
        StringBuilder builder = null;
        int length = message.length();
        int index = 0;
        while (index < length) {
            char ch = message.charAt(index);
            if (ch == '%' && index + 1 < length && message.charAt(index + 1) == 'n') {
                if (builder == null) {
                    builder = new StringBuilder(length);
                    builder.append(message, 0, index);
                }
                builder.append(' ');
                index += 2;
                continue;
            }
            if (ch == '\\' && index + 1 < length) {
                char next = message.charAt(index + 1);
                if (next == 'n' || next == 't' || next == 'r') {
                    if (builder == null) {
                        builder = new StringBuilder(length);
                        builder.append(message, 0, index);
                    }
                    builder.append(' ');
                    index += 2;
                    continue;
                }
            }
            if (builder != null) {
                builder.append(ch);
            }
            index++;
        }
        return builder == null ? message : builder.toString();
    }

    private static final class SymbolCounts {
        private final int fillerCount;
        private final int operatorCount;
        private final List<String> operatorTokens;

        private SymbolCounts(int fillerCount, int operatorCount, List<String> operatorTokens) {
            this.fillerCount = fillerCount;
            this.operatorCount = operatorCount;
            this.operatorTokens = operatorTokens;
        }
    }
}
