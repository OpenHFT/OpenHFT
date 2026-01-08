/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link PlaceholderPatterns}.
 */
@DisplayName("Placeholder patterns tests scenario case detail")
public class PlaceholderPatternsTest {

    @DisplayName("Log pattern scenario case detail path")
    @ParameterizedTest
    @CsvSource({
            "'{}', true",
            "'{0}', true",
            "'{1}', true",
            "'%s', true",
            "'hello', false",
            "'{ }', false",
            "'%%', false",
            "'{a}', false"
    })
    void logPattern(String input, boolean shouldMatch) {
        assertEquals(shouldMatch, PlaceholderPatterns.LOG.matcher(input).find());
    }

    @DisplayName("String format pattern scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "'%s', true",
            "'%d', true",
            "'%f', true",
            "'%2$s', true",
            "'%-10s', true",
            "'%.2f', true",
            "'%-10.2f', true",
            "'%tY', true",
            "'%n', false",
            "'%%', false",
            "'hello', false",
            "'%', false"
    })
    void stringFormatPattern(String input, boolean shouldMatch) {
        assertEquals(shouldMatch, PlaceholderPatterns.STRING_FORMAT.matcher(input).find());
    }

    @DisplayName("Message format pattern scenario case detail")
    @ParameterizedTest
    @CsvSource({
            "'{0}', true",
            "'{1}', true",
            "'{0,number}', true",
            "'{1,date}', true",
            "'{2,date,long}', true",
            "'{}', false",
            "'{a}', false",
            "'hello', false"
    })
    void messageFormatPattern(String input, boolean shouldMatch) {
        assertEquals(shouldMatch, PlaceholderPatterns.MESSAGE_FORMAT.matcher(input).find());
    }

    @DisplayName("Key value label pattern scenario case")
    @ParameterizedTest
    @CsvSource({
            "'index:', true",
            "'Index:', true",
            "'INDEX:', true",
            "'size=', true",
            "'expected:', true",
            "'actual:', true",
            "'count:', true",
            "'i:', true",
            "'j=', true",
            "'hello', false",
            "'index', false",
            "'foobar:', false"
    })
    void keyValueLabelPattern(String input, boolean shouldMatch) {
        assertEquals(shouldMatch, PlaceholderPatterns.KEY_VALUE_LABEL.matcher(input).find());
    }

    @DisplayName("Annotation pattern scenario case detail path")
    @ParameterizedTest
    @CsvSource({
            "'{value}', true",
            "'{min}', true",
            "'{max}', true",
            "'{regexp}', true",
            "'{0}', true",
            "'{}', false",
            "'hello', false",
            "'{', false"
    })
    void annotationPattern(String input, boolean shouldMatch) {
        assertEquals(shouldMatch, PlaceholderPatterns.ANNOTATION.matcher(input).find());
    }
}
