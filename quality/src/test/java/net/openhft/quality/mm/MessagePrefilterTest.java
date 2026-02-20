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
 * Tests for {@link MessagePrefilter}.
 */
@DisplayName("Message prefilter tests scenario case")
class MessagePrefilterTest {

    private MessagePrefilter prefilter;

    @BeforeEach
    void setUp() {
        prefilter = new MessagePrefilter();
    }

    @Test
    @DisplayName("Data-like message with newline and colon should be skipped")
    void dataLikeMessageWithNewlineAndColonShouldBeSkipped() {
        assertTrue(prefilter.shouldSkip("header:\nvalue"),
                "Data-like messages should be skipped before rule evaluation");
    }

    // --- looksLikeClassName tests ---

    @Test
    @DisplayName("Qualified class name should be skipped")
    void shouldSkip_qualifiedClassName_returnsTrue() {
        assertTrue(prefilter.shouldSkip("java.lang.String"),
                "Qualified class name should be skipped");
    }

    @Test
    @DisplayName("Single part name without dot should not be skipped")
    void shouldSkip_singlePartName_returnsFalse() {
        assertFalse(prefilter.shouldSkip("String"),
                "Single part name without dot should not be skipped");
    }

    @Test
    @DisplayName("Class name with space should not be skipped")
    void shouldSkip_nameWithSpace_returnsFalse() {
        assertFalse(prefilter.shouldSkip("java.lang String"),
                "Name containing space should not be skipped");
    }

    @Test
    @DisplayName("Part starting with digit should not be class name")
    void shouldSkip_partStartsWithDigit_returnsFalse() {
        assertFalse(prefilter.shouldSkip("com.1invalid.Class"),
                "Part starting with digit is not valid Java identifier");
    }

    @Test
    @DisplayName("Part with invalid character should not be class name")
    void shouldSkip_partHasInvalidChar_returnsFalse() {
        assertFalse(prefilter.shouldSkip("com.exam-ple.Class"),
                "Part with hyphen is not valid Java identifier");
    }

    @Test
    @DisplayName("Exactly two parts is valid class name boundary")
    void shouldSkip_exactlyTwoParts_returnsTrue() {
        assertTrue(prefilter.shouldSkip("java.String"),
                "Two-part qualified name should be class name");
    }

    @Test
    @DisplayName("Empty part in qualified name should not be class name")
    void shouldSkip_emptyPart_returnsFalse() {
        assertFalse(prefilter.shouldSkip("java..String"),
                "Empty part between dots is not valid");
    }

    // --- looksLikeDataValue tests ---

    @Test
    @DisplayName("Escaped newline with equals should be data value")
    void shouldSkip_escapedNewlineWithEquals_returnsTrue() {
        assertTrue(prefilter.shouldSkip("key=\\nvalue"),
                "Escaped newline with equals should be data");
    }

    @Test
    @DisplayName("Message starting with bang should be data")
    void shouldSkip_startsWithBang_returnsTrue() {
        assertTrue(prefilter.shouldSkip("!command arg"),
                "Message starting with ! should be skipped");
    }

    @Test
    @DisplayName("Exactly two escaped newlines is data boundary")
    void shouldSkip_exactlyTwoEscapedNewlines_returnsTrue() {
        assertTrue(prefilter.shouldSkip("line1\\nline2\\nline3"),
                "Text with exactly two escaped newlines should be data");
    }

    @Test
    @DisplayName("One escaped newline without separator not data")
    void shouldSkip_oneEscapedNewlineNoSeparator_returnsFalse() {
        assertFalse(prefilter.shouldSkip("hello\\nworld"),
                "Single escaped newline without colon/equals should not be data");
    }

    @Test
    @DisplayName("One real newline without separator not data")
    void shouldSkip_oneRealNewlineNoSeparator_returnsFalse() {
        assertFalse(prefilter.shouldSkip("hello\nworld"),
                "Single real newline without colon/equals should not be data");
    }

    // --- JSON/array prefix tests ---

    @ParameterizedTest
    @DisplayName("JSON-like prefix should be skipped")
    @ValueSource(strings = {"{\"key\": \"value\"}", "[{\"item\": 1}]", "[\"a\", \"b\"]", "[[1,2],[3,4]]"})
    void shouldSkip_jsonLikePrefix_returnsTrue(String message) {
        assertTrue(prefilter.shouldSkip(message),
                "JSON-like message should be skipped: " + message);
    }

    // --- Pattern matching tests ---

    @Test
    @DisplayName("Array value pattern should be skipped")
    void shouldSkip_arrayValuePattern_returnsTrue() {
        assertTrue(prefilter.shouldSkip("[a,b,c]"),
                "Array value pattern should be skipped");
    }

    @Test
    @DisplayName("Escape only pattern should be skipped")
    void shouldSkip_escapeOnlyPattern_returnsTrue() {
        assertTrue(prefilter.shouldSkip("\\n\\t"),
                "Escape-only message should be skipped");
    }

    @Test
    @DisplayName("Generated class name pattern should be skipped")
    void shouldSkip_generatedClassNamePattern_returnsTrue() {
        assertTrue(prefilter.shouldSkip("MyVeryLongGeneratedClassName"),
                "Generated PascalCase name should be skipped");
    }

    // --- Edge cases ---

    @Test
    @DisplayName("Empty string should not be skipped")
    void shouldSkip_emptyString_returnsFalse() {
        assertFalse(prefilter.shouldSkip(""),
                "Empty string should not be skipped");
    }

    @Test
    @DisplayName("Whitespace only should not be skipped")
    void shouldSkip_whitespaceOnly_returnsFalse() {
        assertFalse(prefilter.shouldSkip("   "),
                "Whitespace-only string should not be skipped after trim");
    }

    @Test
    @DisplayName("Null message throws NPE")
    void shouldSkip_null_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> prefilter.shouldSkip(null),
                "Null message should throw NPE");
    }

    @Test
    @DisplayName("Regular message should not be skipped")
    void shouldSkip_regularMessage_returnsFalse() {
        assertFalse(prefilter.shouldSkip("This is a normal error message"),
                "Regular error message should not be skipped");
    }

    @Test
    @DisplayName("Message with colon but no newline should not be data")
    void shouldSkip_colonNoNewline_returnsFalse() {
        assertFalse(prefilter.shouldSkip("Error: something failed"),
                "Colon without newline should not be data");
    }
}
