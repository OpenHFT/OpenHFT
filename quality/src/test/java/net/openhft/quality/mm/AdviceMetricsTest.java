/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link AdviceMetrics} to verify accessor methods return correct values.
 */
@DisplayName("Advice metrics tests")
class AdviceMetricsTest {

    @Test
    @DisplayName("Accessors return correct int values")
    void accessors_returnCorrectIntValues() {
        AdviceMetrics metrics = new AdviceMetrics(
                5,      // wordCount
                3,      // meaningfulWordCount
                7,      // totalWordCount
                4,      // effectiveMeaningfulWordCount
                2,      // placeholderCount
                1,      // keyValueLabelCount
                null, null, null, null,
                null, null, null, null, null, null);

        assertEquals(5, metrics.wordCount(), "wordCount should match constructor value");
        assertEquals(3, metrics.meaningfulWordCount(), "meaningfulWordCount should match constructor value");
        assertEquals(7, metrics.totalWordCount(), "totalWordCount should match constructor value");
        assertEquals(4, metrics.effectiveMeaningfulWordCount(),
                "effectiveMeaningfulWordCount should match constructor value");
        assertEquals(2, metrics.placeholderCount(), "placeholderCount should match constructor value");
        assertEquals(1, metrics.keyValueLabelCount(), "keyValueLabelCount should match constructor value");
    }

    @Test
    @DisplayName("Accessors return correct Integer values when set")
    void accessors_returnCorrectIntegerValues() {
        AdviceMetrics metrics = new AdviceMetrics(
                0, 0, 0, 0, 0, 0,
                4,      // minWordCount
                42,     // maxWordCount
                2,      // minMeaningfulWordCount
                35,     // maxWordLength
                null, null, null, null, null, null);

        assertEquals(Integer.valueOf(4), metrics.minWordCount(),
                "minWordCount should match constructor value");
        assertEquals(Integer.valueOf(42), metrics.maxWordCount(),
                "maxWordCount should match constructor value");
        assertEquals(Integer.valueOf(2), metrics.minMeaningfulWordCount(),
                "minMeaningfulWordCount should match constructor value");
        assertEquals(Integer.valueOf(35), metrics.maxWordLength(),
                "maxWordLength should match constructor value");
    }

    @Test
    @DisplayName("Accessors return null Integer values when not set")
    void accessors_returnNullIntegerValuesWhenNotSet() {
        AdviceMetrics metrics = new AdviceMetrics(
                0, 0, 0, 0, 0, 0,
                null, null, null, null,
                null, null, null, null, null, null);

        assertNull(metrics.minWordCount(), "minWordCount should be null when not set");
        assertNull(metrics.maxWordCount(), "maxWordCount should be null when not set");
        assertNull(metrics.minMeaningfulWordCount(), "minMeaningfulWordCount should be null when not set");
        assertNull(metrics.maxWordLength(), "maxWordLength should be null when not set");
    }

    @Test
    @DisplayName("Accessors return correct String values when set")
    void accessors_returnCorrectStringValues() {
        AdviceMetrics metrics = new AdviceMetrics(
                0, 0, 0, 0, 0, 0,
                null, null, null, null,
                ">=",           // comparisonOperator
                "left",         // comparisonLeftOperand
                "right",        // comparisonRightOperand
                "contains",     // stringSearchMethod
                "target",       // stringSearchTarget
                "searchArg");   // stringSearchArg

        assertEquals(">=", metrics.comparisonOperator(),
                "comparisonOperator should match constructor value");
        assertEquals("left", metrics.comparisonLeftOperand(),
                "comparisonLeftOperand should match constructor value");
        assertEquals("right", metrics.comparisonRightOperand(),
                "comparisonRightOperand should match constructor value");
        assertEquals("contains", metrics.stringSearchMethod(),
                "stringSearchMethod should match constructor value");
        assertEquals("target", metrics.stringSearchTarget(),
                "stringSearchTarget should match constructor value");
        assertEquals("searchArg", metrics.stringSearchArg(),
                "stringSearchArg should match constructor value");
    }

    @Test
    @DisplayName("Accessors return null String values when not set")
    void accessors_returnNullStringValuesWhenNotSet() {
        AdviceMetrics metrics = new AdviceMetrics(
                0, 0, 0, 0, 0, 0,
                null, null, null, null,
                null, null, null, null, null, null);

        assertNull(metrics.comparisonOperator(), "comparisonOperator should be null when not set");
        assertNull(metrics.comparisonLeftOperand(), "comparisonLeftOperand should be null when not set");
        assertNull(metrics.comparisonRightOperand(), "comparisonRightOperand should be null when not set");
        assertNull(metrics.stringSearchMethod(), "stringSearchMethod should be null when not set");
        assertNull(metrics.stringSearchTarget(), "stringSearchTarget should be null when not set");
        assertNull(metrics.stringSearchArg(), "stringSearchArg should be null when not set");
    }

    @Test
    @DisplayName("All fields set with distinct values are retrievable")
    void allFields_distinctValuesRetrievable() {
        AdviceMetrics metrics = new AdviceMetrics(
                10, 8, 12, 9, 3, 2,
                4, 42, 3, 35,
                "==", "actual", "expected",
                "startsWith", "str", "prefix");

        assertEquals(10, metrics.wordCount(), "wordCount");
        assertEquals(8, metrics.meaningfulWordCount(), "meaningfulWordCount");
        assertEquals(12, metrics.totalWordCount(), "totalWordCount");
        assertEquals(9, metrics.effectiveMeaningfulWordCount(), "effectiveMeaningfulWordCount");
        assertEquals(3, metrics.placeholderCount(), "placeholderCount");
        assertEquals(2, metrics.keyValueLabelCount(), "keyValueLabelCount");
        assertEquals(Integer.valueOf(4), metrics.minWordCount(), "minWordCount");
        assertEquals(Integer.valueOf(42), metrics.maxWordCount(), "maxWordCount");
        assertEquals(Integer.valueOf(3), metrics.minMeaningfulWordCount(), "minMeaningfulWordCount");
        assertEquals(Integer.valueOf(35), metrics.maxWordLength(), "maxWordLength");
        assertEquals("==", metrics.comparisonOperator(), "comparisonOperator");
        assertEquals("actual", metrics.comparisonLeftOperand(), "comparisonLeftOperand");
        assertEquals("expected", metrics.comparisonRightOperand(), "comparisonRightOperand");
        assertEquals("startsWith", metrics.stringSearchMethod(), "stringSearchMethod");
        assertEquals("str", metrics.stringSearchTarget(), "stringSearchTarget");
        assertEquals("prefix", metrics.stringSearchArg(), "stringSearchArg");
    }

    @Test
    @DisplayName("Zero values are distinguishable from defaults")
    void zeroValues_distinguishableFromDefaults() {
        AdviceMetrics metrics = new AdviceMetrics(
                0, 0, 0, 0, 0, 0,
                Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0),
                "", "", "", "", "", "");

        assertEquals(0, metrics.wordCount(), "zero wordCount");
        assertEquals(0, metrics.meaningfulWordCount(), "zero meaningfulWordCount");
        assertEquals(Integer.valueOf(0), metrics.minWordCount(), "zero minWordCount boxed");
        assertEquals(Integer.valueOf(0), metrics.maxWordCount(), "zero maxWordCount boxed");
        assertEquals("", metrics.comparisonOperator(), "empty comparisonOperator");
        assertEquals("", metrics.stringSearchMethod(), "empty stringSearchMethod");
    }
}
