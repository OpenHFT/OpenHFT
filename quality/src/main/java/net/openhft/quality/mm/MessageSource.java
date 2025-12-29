/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Source categories for message candidates.
 */
public enum MessageSource {
    /**
     * Assertion messages.
     */
    ASSERTION(4, 2),
    /**
     * Precondition messages.
     */
    PRECONDITION(4, 2),
    /**
     * Thrown exception messages.
     */
    THROW(2, 1),
    /**
     * Annotation messages.
     */
    ANNOTATION(6, 4),
    /**
     * Log messages.
     */
    LOG(4, 2),
    /**
     * First top-level class Javadoc first paragraph.
     */
    JAVADOC_CLASS(10, 8),
    /**
     * Member Javadoc first paragraphs (methods, fields, nested types).
     */
    JAVADOC_MEMBER(6, 4);

    private final int minWordCount;
    private final int minMeaningfulWordCount;

    MessageSource(int minWordCount, int minMeaningfulWordCount) {
        this.minWordCount = minWordCount;
        this.minMeaningfulWordCount = minMeaningfulWordCount;
    }

    /**
     * Return the minimum total word count required.
     *
     * @return minimum total word count.
     */
    public int minWordCount() {
        return minWordCount;
    }

    /**
     * Return the minimum meaningful word count required.
     *
     * @return minimum meaningful word count.
     */
    public int minMeaningfulWordCount() {
        return minMeaningfulWordCount;
    }
}
