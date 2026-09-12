/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link MessageNormaliser}.
 */
@DisplayName("Message normaliser tests scenario case")
class MessageNormaliserTest {

    @Test
    @DisplayName("Collapses consecutive whitespace to single space")
    void collapsesConsecutiveWhitespaceToSingleSpace() {
        String input = "alpha  bravo   charlie    delta";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo charlie delta", result,
                "multiple consecutive spaces should collapse to single space");
    }

    @Test
    @DisplayName("Collapses mixed whitespace types")
    void collapsesMixedWhitespaceTypes() {
        String input = "alpha\t\tbravo \t charlie";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo charlie", result,
                "tabs and spaces should collapse to single space");
    }

    @Test
    @DisplayName("Collapses whitespace with newlines")
    void collapsesWhitespaceWithNewlines() {
        String input = "alpha\n\nbravo\r\ncharlie";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo charlie", result,
                "newlines should collapse to single space");
    }

    @Test
    @DisplayName("Preserves single spaces between words")
    void preservesSingleSpacesBetweenWords() {
        String input = "alpha bravo charlie";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo charlie", result,
                "single spaces should remain unchanged");
    }

    @Test
    @DisplayName("Converts to lower case")
    void convertsToLowerCase() {
        String input = "Alpha BRAVO Charlie";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo charlie", result,
                "all characters should be lower cased");
    }

    @Test
    @DisplayName("Trims leading and trailing whitespace")
    void trimsLeadingAndTrailingWhitespace() {
        String input = "   alpha bravo   ";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha bravo", result,
                "leading and trailing whitespace should be trimmed");
    }

    @Test
    @DisplayName("Returns empty string for empty input")
    void returnsEmptyStringForEmptyInput() {
        String result = MessageNormaliser.normalise("");
        assertEquals("", result, "empty input should return empty string");
    }

    @Test
    @DisplayName("Returns empty string for whitespace only input")
    void returnsEmptyStringForWhitespaceOnlyInput() {
        String result = MessageNormaliser.normalise("   ");
        assertEquals("", result, "whitespace-only input should return empty string");
    }

    @Test
    @DisplayName("Throws NullPointerException for null input")
    void throwsNullPointerExceptionForNullInput() {
        assertThrows(NullPointerException.class, () -> MessageNormaliser.normalise(null),
                "null input should throw NullPointerException");
    }

    @Test
    @DisplayName("Handles leading whitespace before first non-whitespace")
    void handlesLeadingWhitespaceBeforeFirstNonWhitespace() {
        String input = "  alpha";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha", result,
                "leading whitespace before content should be trimmed");
    }

    @Test
    @DisplayName("Does not add space at start when first char is whitespace in trimmed")
    void doesNotAddSpaceAtStartWhenFirstCharIsWhitespaceInTrimmed() {
        String input = "alpha  ";
        String result = MessageNormaliser.normalise(input);
        assertEquals("alpha", result,
                "trailing whitespace should be handled correctly");
    }
}
