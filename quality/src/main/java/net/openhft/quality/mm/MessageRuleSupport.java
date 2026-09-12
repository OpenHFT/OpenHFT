/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Provides helper routines for analysing message content.
 */
public final class MessageRuleSupport {
    private static final int MIN_NAME_LENGTH_FOR_CHECK = 5;
    private static final int MIN_WORDS_WITHOUT_NAME = 2;
    private static final Pattern NUMERIC_WORD = Pattern.compile("^\\d+$");
    private static final Pattern GENERIC_MESSAGE_PATTERN = Pattern.compile(
            "(?i)^(actual|expected|value|result|data|object|condition|test|check|message|msg|"
                    + "err|error|fail|ok|true|false)$"
    );
    private static final Pattern RESTATES_ASSERTION_PATTERN = Pattern.compile(
            "(?i)("
                    + "^assert(equals|true|false|null|notnull|same|notequals?|that)?$|"
                    + "^should (be )?(equal|true|false|null|not null|same|the same)$|"
                    + "^should not be null$|"
                    + "^must (be )?(equal|true|false|null|not null|same|the same)$|"
                    + "^must not be null$|"
                    + "^(should|must|expected to) match$|"
                    + "^values? (should|must) match$|"
                    + "^equals?$|"
                    + "^not null$|"
                    + "^is null$|"
                    + "^is true$|"
                    + "^is false$"
                    + ")"
    );
    private static final Pattern CONTEXTLESS_PATTERN = Pattern.compile(
            "(?i)^("
                    + "comparison|check|validation|equality|verify|test|"
                    + "values? (should |must )?(match|equal|be equal)|"
                    + "(should|must) (be )?equal|"
                    + "not equal|"
                    + "mismatch|"
                    + "failed|failure|error|"
                    + "operation result should equal expected value|"
                    + "result should (match|equal) expected|"
                    + "counter meets (minimum|maximum)|"
                    + "meets (minimum|maximum|threshold)|"
                    + "within (valid |expected )?range|"
                    + "indices should be valid|"
                    + "config error rethrown"
                    + ")$"
    );

    private static final int SUBSTANCE_CACHE_MAX = 256;
    private final Map<String, Pattern> substancePatternCache = new java.util.LinkedHashMap<String, Pattern>(SUBSTANCE_CACHE_MAX, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
            return size() > SUBSTANCE_CACHE_MAX;
        }
    };
    private final MessageMetricsCalculator metricsCalculator;

    /**
     * Create rule support using the supplied metrics calculator.
     *
     * @param metricsCalculator calculator for message metrics.
     */
    public MessageRuleSupport(MessageMetricsCalculator metricsCalculator) {
        this.metricsCalculator = metricsCalculator;
    }

    /**
     * Return the metrics calculator.
     *
     * @return metrics calculator.
     */
    public MessageMetricsCalculator metricsCalculator() {
        return metricsCalculator;
    }

    /**
     * Determine whether a message is generic and low signal.
     *
     * @param message message text.
     * @return {@code true} if the message is generic.
     */
    public boolean isGenericMessage(String message) {
        return message != null && GENERIC_MESSAGE_PATTERN.matcher(message).matches();
    }

    /**
     * Determine whether a message restates the assertion itself.
     *
     * @param message message text.
     * @return {@code true} if the message restates the assertion.
     */
    public boolean isRestatesAssertion(String message) {
        return message != null && RESTATES_ASSERTION_PATTERN.matcher(message).find();
    }

    /**
     * Determine whether a message is contextless.
     *
     * @param message message text.
     * @return {@code true} if the message is contextless.
     */
    public boolean isContextless(String message) {
        return message != null && CONTEXTLESS_PATTERN.matcher(message).matches();
    }

    /**
     * Find a variant of a name that appears in the provided text.
     *
     * @param text message text to inspect.
     * @param name name to search for.
     * @return matched variant, or {@code null} if none is found.
     */
    public String findNameVariant(String text, String name) {
        requireNonNull(text);
        requireNonNull(name);
        if (name.length() < MIN_NAME_LENGTH_FOR_CHECK) {
            return null;
        }
        if (containsNameExact(text, name)) {
            return name;
        }
        String swappedCase = swapFirstLetterCase(name);
        if (containsNameExact(text, swappedCase)) {
            return swappedCase;
        }
        String lowerCase = name.toLowerCase(Locale.ROOT);
        if (!lowerCase.equals(name) && !lowerCase.equals(swappedCase)
                && containsNameExact(text, lowerCase)) {
            return lowerCase;
        }
        String upperCase = name.toUpperCase(Locale.ROOT);
        if (!upperCase.equals(name) && containsNameExact(text, upperCase)) {
            return upperCase;
        }
        return null;
    }

    /**
     * Analyse the remaining substance of a message after removing a name.
     *
     * @param message message text.
     * @param name    name to remove before analysing.
     * @return substance analysis for the message.
     */
    public SubstanceAnalysis analyseSubstance(String message, String name) {
        Pattern namePattern = substancePatternCache.computeIfAbsent(
                name, k -> Pattern.compile("(?i)" + Pattern.quote(k)));
        String withoutName = namePattern.matcher(message).replaceAll(" ");
        String[] words = metricsCalculator.splitWords(withoutName);
        List<String> filler = new ArrayList<>();
        List<String> meaningful = new ArrayList<>();
        Set<String> meaningfulSeen = new HashSet<>();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (metricsCalculator.isFillerWord(word)) {
                filler.add(word);
                continue;
            }
            if (NUMERIC_WORD.matcher(word).matches()) {
                filler.add(word);
                continue;
            }
            if (word.length() < 2) {
                filler.add(word);
                continue;
            }
            String normalised = word.toLowerCase(Locale.ROOT);
            if (meaningfulSeen.add(normalised)) {
                meaningful.add(word);
            }
        }

        boolean hasSubstance = meaningful.size() >= MIN_WORDS_WITHOUT_NAME;
        List<String> allRemaining = new ArrayList<>();
        allRemaining.addAll(meaningful);
        allRemaining.addAll(filler);
        String remainingWords = allRemaining.isEmpty() ? "(empty)"
                : String.join(", ", allRemaining);

        String diagnosis;
        if (meaningful.isEmpty() && filler.isEmpty()) {
            diagnosis = "nothing remains after removing the name";
        } else if (meaningful.isEmpty()) {
            diagnosis = "only filler words remain: " + String.join(", ", filler);
        } else if (filler.isEmpty()) {
            diagnosis = "too short - only " + meaningful.size()
                    + " unique word(s): " + String.join(", ", meaningful);
        } else {
            diagnosis = "only " + meaningful.size() + " unique meaningful word(s): "
                    + String.join(", ", meaningful)
                    + " (filler: " + String.join(", ", filler) + ")";
        }

        return new SubstanceAnalysis(hasSubstance,
                diagnosis, meaningful, filler, name);
    }

    private boolean containsNameExact(String text, String name) {
        requireNonNull(text);
        requireNonNull(name);
        if (name.isEmpty()) {
            return false;
        }
        int index = text.indexOf(name);
        while (index >= 0) {
            boolean startBoundary = (index == 0)
                    || !isNameChar(text.charAt(index - 1));
            boolean endBoundary = (index + name.length() >= text.length())
                    || !isNameChar(text.charAt(index + name.length()));
            if (startBoundary && endBoundary) {
                return true;
            }
            index = text.indexOf(name, index + 1);
        }
        return false;
    }

    private boolean isNameChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    private String swapFirstLetterCase(String name) {
        if (name.isEmpty()) {
            return name;
        }
        char first = name.charAt(0);
        if (Character.isUpperCase(first)) {
            return Character.toLowerCase(first) + name.substring(1);
        }
        return Character.toUpperCase(first) + name.substring(1);
    }

    /**
     * Result of analysing how much substance remains after removing a name.
     */
    public static final class SubstanceAnalysis {
        private final boolean hasSubstance;
        private final String diagnosis;
        private final List<String> meaningfulWords;
        private final List<String> fillerWords;
        private final String removedName;

        SubstanceAnalysis(boolean hasSubstance,
                          String diagnosis,
                          List<String> meaningfulWords,
                          List<String> fillerWords, String removedName) {
            this.hasSubstance = hasSubstance;
            this.diagnosis = diagnosis;
            this.meaningfulWords = meaningfulWords;
            this.fillerWords = fillerWords;
            this.removedName = removedName;
        }

        /**
         * Return whether the message has enough substance.
         *
         * @return {@code true} if the message has substance.
         */
        public boolean hasSubstance() {
            return hasSubstance;
        }

        /**
         * Return a human-readable diagnosis string.
         *
         * @return diagnosis string.
         */
        public String diagnosis() {
            return diagnosis;
        }

        /**
         * Return verbose details about the analysis.
         *
         * @return verbose details string.
         */
        public String verboseDetails() {
            return String.format("Details: meaningful=[%s] filler=[%s] removed=[%s]",
                    String.join(",", meaningfulWords),
                    String.join(",", fillerWords),
                    removedName);
        }
    }
}
