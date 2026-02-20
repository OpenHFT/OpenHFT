/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Metrics payload for verbose advice diagnostics.
 */
public final class AdviceMetrics {
    private final int wordCount;
    private final int meaningfulWordCount;
    private final int totalWordCount;
    private final int effectiveMeaningfulWordCount;
    private final int placeholderCount;
    private final int keyValueLabelCount;
    private final Integer minWordCount;
    private final Integer maxWordCount;
    private final Integer minMeaningfulWordCount;
    private final Integer maxWordLength;
    private final String comparisonOperator;
    private final String comparisonLeftOperand;
    private final String comparisonRightOperand;
    private final String stringSearchMethod;
    private final String stringSearchTarget;
    private final String stringSearchArg;

    /**
     * Create a metrics snapshot.
     *
     * @param wordCount              total word count.
     * @param meaningfulWordCount    meaningful word count.
     * @param totalWordCount         total word count including placeholders.
     * @param effectiveMeaningfulWordCount effective meaningful count including placeholders.
     * @param placeholderCount       placeholder count.
     * @param keyValueLabelCount     key-value label count.
     * @param minWordCount           minimum word count threshold, if applicable.
     * @param maxWordCount           maximum word count threshold, if applicable.
     * @param minMeaningfulWordCount minimum meaningful word threshold, if applicable.
     * @param maxWordLength          maximum word length threshold, if applicable.
     * @param comparisonOperator     comparison operator, if detected.
     * @param comparisonLeftOperand  comparison left operand, if detected.
     * @param comparisonRightOperand comparison right operand, if detected.
     * @param stringSearchMethod     string search method name, if detected.
     * @param stringSearchTarget     string search target variable, if detected.
     * @param stringSearchArg        string search argument, if detected.
     */
    public AdviceMetrics(int wordCount, int meaningfulWordCount,
                         int totalWordCount, int effectiveMeaningfulWordCount,
                         int placeholderCount, int keyValueLabelCount,
                         Integer minWordCount, Integer maxWordCount,
                         Integer minMeaningfulWordCount, Integer maxWordLength,
                         String comparisonOperator, String comparisonLeftOperand,
                         String comparisonRightOperand, String stringSearchMethod,
                         String stringSearchTarget, String stringSearchArg) {
        this.wordCount = wordCount;
        this.meaningfulWordCount = meaningfulWordCount;
        this.totalWordCount = totalWordCount;
        this.effectiveMeaningfulWordCount = effectiveMeaningfulWordCount;
        this.placeholderCount = placeholderCount;
        this.keyValueLabelCount = keyValueLabelCount;
        this.minWordCount = minWordCount;
        this.maxWordCount = maxWordCount;
        this.minMeaningfulWordCount = minMeaningfulWordCount;
        this.maxWordLength = maxWordLength;
        this.comparisonOperator = comparisonOperator;
        this.comparisonLeftOperand = comparisonLeftOperand;
        this.comparisonRightOperand = comparisonRightOperand;
        this.stringSearchMethod = stringSearchMethod;
        this.stringSearchTarget = stringSearchTarget;
        this.stringSearchArg = stringSearchArg;
    }

    public int wordCount() {
        return wordCount;
    }

    public int meaningfulWordCount() {
        return meaningfulWordCount;
    }

    public int totalWordCount() {
        return totalWordCount;
    }

    public int effectiveMeaningfulWordCount() {
        return effectiveMeaningfulWordCount;
    }

    public int placeholderCount() {
        return placeholderCount;
    }

    public int keyValueLabelCount() {
        return keyValueLabelCount;
    }

    public Integer minWordCount() {
        return minWordCount;
    }

    public Integer maxWordCount() {
        return maxWordCount;
    }

    public Integer minMeaningfulWordCount() {
        return minMeaningfulWordCount;
    }

    public Integer maxWordLength() {
        return maxWordLength;
    }

    public String comparisonOperator() {
        return comparisonOperator;
    }

    public String comparisonLeftOperand() {
        return comparisonLeftOperand;
    }

    public String comparisonRightOperand() {
        return comparisonRightOperand;
    }

    public String stringSearchMethod() {
        return stringSearchMethod;
    }

    public String stringSearchTarget() {
        return stringSearchTarget;
    }

    public String stringSearchArg() {
        return stringSearchArg;
    }
}
