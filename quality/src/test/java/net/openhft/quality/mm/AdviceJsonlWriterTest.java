/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdviceJsonlWriter}.
 */
@DisplayName("AdviceJsonlWriter tests")
class AdviceJsonlWriterTest {

    @TempDir
    Path tempDir;

    private AdviceText sampleText() {
        return new AdviceText(
                AdviceId.MMAssertionMessageMissing,
                "Title", "IntroText", "OutroText",
                "HintA", "HintB", "Checklist",
                "AntiPatterns", "Verbose");
    }

    @Test
    @DisplayName("writeRunRecord writes correct JSON with all fields")
    void writeRunRecord_withRankOut() throws IOException {
        Path output = tempDir.resolve("run.jsonl");
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, true, false, "ranks.properties", "/tmp/ranks-out")) {
            writer.writeRunRecord();
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"type\":\"run\""), "should contain type:run");
        assertTrue(content.contains("\"schema_version\":1"), "should contain schema_version");
        assertTrue(content.contains("\"tool\":\"MeaningfulMessage\""), "should contain tool");
        assertTrue(content.contains("\"dry_run\":false"), "should contain dry_run");
        assertTrue(content.contains("\"verbose\":true"), "should contain verbose");
        assertTrue(content.contains("\"rank_resource\":\"ranks.properties\""), "should contain rank_resource");
        assertTrue(content.contains("\"rank_out\":\"/tmp/ranks-out\""), "should contain rank_out");
        assertTrue(content.endsWith("\n"), "should end with newline");
    }

    @Test
    @DisplayName("writeRunRecord omits rank_out when null")
    void writeRunRecord_withoutRankOut() throws IOException {
        Path output = tempDir.resolve("run-null.jsonl");
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, true, "ranks.properties", null)) {
            writer.writeRunRecord();
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"rank_out\""), "should not contain rank_out");
        assertTrue(content.contains("\"dry_run\":true"), "should contain dry_run:true");
        assertTrue(content.contains("\"verbose\":false"), "should contain verbose:false");
    }

    @Test
    @DisplayName("writeFileRecord with empty file-level and line-level advice")
    void writeFileRecord_emptyAdvice() throws IOException {
        Path output = tempDir.resolve("file-empty.jsonl");
        FileReport report = new FileReport("Test.java",
                Collections.emptyList(), Collections.emptyList(), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"type\":\"file\""), "should contain type:file");
        assertTrue(content.contains("\"file\":\"Test.java\""), "should contain file name");
        assertTrue(content.contains("\"file_level_advice\":[]"), "should have empty file advice");
        assertTrue(content.contains("\"line_level_advice\":[]"), "should have empty line advice");
    }

    @Test
    @DisplayName("writeFileRecord with file-level advice including details and metrics")
    void writeFileRecord_withFileAdvice() throws IOException {
        Path output = tempDir.resolve("file-advice.jsonl");
        FileAdviceDetails details = new FileAdviceDetails(
                AdviceId.MMOverusedWord, 42, List.of("word1", "word2"),
                10, 5, 8, 20, 3.14, 2.0);
        FileAdviceGroup group = new FileAdviceGroup(
                AdviceId.MMOverusedWord, sampleText(), 1, details);
        FileReport report = new FileReport("MyFile.java",
                List.of(group), Collections.emptyList(), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"advice_id\":\"MMOverusedWord\""), "should contain advice_id");
        assertTrue(content.contains("\"rank\":1"), "should contain rank");
        assertTrue(content.contains("\"title\":\"Title\""), "should contain title");
        assertTrue(content.contains("\"intent_intro\":\"IntroText\""),
                "should contain intent_intro in file advice");
        assertTrue(content.contains("\"intent_outro\":\"OutroText\""),
                "should contain intent_outro in file advice");
        assertTrue(content.contains("\"hint_a\":\"HintA\""),
                "should contain hint_a in file advice");
        assertTrue(content.contains("\"hint_b\":\"HintB\""),
                "should contain hint_b in file advice");
        assertTrue(content.contains("\"checklist\":\"Checklist\""),
                "should contain checklist in file advice");
        assertTrue(content.contains("\"anti_patterns\":\"AntiPatterns\""),
                "should contain anti_patterns in file advice");
        assertTrue(content.contains("\"line\":42"), "should contain line number");
        assertTrue(content.contains("\"items\":[\"word1\",\"word2\"]"), "should contain items");
        assertTrue(content.contains("\"metrics\":{"), "should contain metrics block");
        assertTrue(content.contains("\"message_count\":10"), "should contain message_count");
        assertTrue(content.contains("\"purpose_cue_count\":5"), "should contain purpose_cue_count");
        assertTrue(content.contains("\"expected_purpose_cue_count\":8"),
                "should contain expected_purpose_cue_count");
        assertTrue(content.contains("\"total_message_count\":20"), "should contain total_message_count");
        assertTrue(content.contains("\"entropy\":3.14"), "should contain entropy");
        assertTrue(content.contains("\"min_entropy\":2.00"), "should contain min_entropy");
    }

    @Test
    @DisplayName("writeFileRecord with file-level advice with no line and no items and no metrics")
    void writeFileRecord_withFileAdviceNoLineNoItemsNoMetrics() throws IOException {
        Path output = tempDir.resolve("file-advice-bare.jsonl");
        FileAdviceDetails details = new FileAdviceDetails(
                AdviceId.MMOverusedWord, 0, Collections.emptyList(),
                null, null, null, null, null, null);
        FileAdviceGroup group = new FileAdviceGroup(
                AdviceId.MMOverusedWord, sampleText(), 2, details);
        FileReport report = new FileReport("Bare.java",
                List.of(group), Collections.emptyList(), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"line\""), "should not contain line when 0");
        assertFalse(content.contains("\"items\""), "should not contain items when empty");
        assertFalse(content.contains("\"metrics\""), "should not contain metrics when all null");
    }

    @Test
    @DisplayName("writeFileRecord with line-level advice and occurrences")
    void writeFileRecord_withLineAdvice() throws IOException {
        Path output = tempDir.resolve("line-advice.jsonl");
        AdviceOccurrence occ1 = new AdviceOccurrence(
                10, AdviceSource.ASSERTION, "literal msg", null, null, "snippet text");
        AdviceOccurrence occ2 = new AdviceOccurrence(
                20, AdviceSource.THROW, null, "expr()", "line text", null);
        AdviceGroup group = new AdviceGroup(
                AdviceId.MMAssertionMessageMissing, sampleText(), 5,
                List.of(occ1, occ2));
        FileReport report = new FileReport("Lines.java",
                Collections.emptyList(), List.of(group), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"occurrences\":["), "should contain occurrences array");
        assertTrue(content.contains("\"line\":10"), "should contain first occurrence line");
        assertTrue(content.contains("\"source\":\"ASSERTION\""), "should contain first occurrence source");
        assertTrue(content.contains("\"message_literal\":\"literal msg\""),
                "should contain message_literal");
        assertTrue(content.contains("\"snippet\":\"snippet text\""), "should contain snippet");
        assertTrue(content.contains("\"line\":20"), "should contain second occurrence line");
        assertTrue(content.contains("\"message_expr\":\"expr()\""), "should contain message_expr");
        assertTrue(content.contains("\"line_text\":\"line text\""), "should contain line_text");
        // Verify writeLineAdviceArray fields
        assertTrue(content.contains("\"advice_id\":\"MMAssertionMessageMissing\""),
                "should contain advice_id in line advice");
        assertTrue(content.contains("\"rank\":5"), "should contain rank in line advice");
        assertTrue(content.contains("\"title\":\"Title\""), "should contain title in line advice");
        assertTrue(content.contains("\"intent_intro\":\"IntroText\""), "should contain intent_intro");
        assertTrue(content.contains("\"intent_outro\":\"OutroText\""), "should contain intent_outro");
        assertTrue(content.contains("\"hint_a\":\"HintA\""), "should contain hint_a");
        assertTrue(content.contains("\"hint_b\":\"HintB\""), "should contain hint_b");
        assertTrue(content.contains("\"checklist\":\"Checklist\""), "should contain checklist");
        assertTrue(content.contains("\"anti_patterns\":\"AntiPatterns\""), "should contain anti_patterns");
    }

    @Test
    @DisplayName("writeFileRecord with occurrence that has no literal, no expr, no snippet, no line_text")
    void writeFileRecord_withBareOccurrence() throws IOException {
        Path output = tempDir.resolve("bare-occ.jsonl");
        AdviceOccurrence occ = new AdviceOccurrence(
                5, AdviceSource.LOG, null, null, null, null);
        AdviceGroup group = new AdviceGroup(
                AdviceId.MMLogMessageMissing, sampleText(), 3,
                List.of(occ));
        FileReport report = new FileReport("Bare.java",
                Collections.emptyList(), List.of(group), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"message_literal\""), "should not contain message_literal");
        assertFalse(content.contains("\"message_expr\""), "should not contain message_expr");
        assertFalse(content.contains("\"snippet\""), "should not contain snippet");
        assertFalse(content.contains("\"line_text\""), "should not contain line_text");
    }

    @Test
    @DisplayName("writeFileRecord verbose mode with candidates and legacy suppressions")
    void writeFileRecord_verboseWithCandidatesAndLegacySuppressons() throws IOException {
        Path output = tempDir.resolve("verbose.jsonl");
        AdviceMetrics metrics = new AdviceMetrics(
                5, 3, 6, 4, 1, 2,
                2, 10, 1, 20,
                "==", "left", "right",
                "contains", "target", "arg");
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Verbose.java")
                .lineNo(15)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .messageLiteral("a msg")
                .messageExpr("getMsg()")
                .metrics(metrics)
                .build();
        Map<Integer, List<CandidateAdvice>> candidatesByLine = new TreeMap<>();
        candidatesByLine.put(15, List.of(candidate));
        FileReport report = new FileReport("Verbose.java",
                Collections.emptyList(), Collections.emptyList(),
                candidatesByLine, List.of(RuleId.TOO_SHORT, RuleId.DUPLICATE));
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, true, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"candidates\":["), "should contain candidates");
        assertTrue(content.contains("\"advice_id\":\"MMAssertionMessageTooShort\""),
                "should contain candidate advice_id");
        assertTrue(content.contains("\"rule_id\":\"MMTooShort\""), "should contain rule_id");
        assertTrue(content.contains("\"message_literal\":\"a msg\""),
                "should contain candidate message_literal");
        assertTrue(content.contains("\"message_expr\":\"getMsg()\""),
                "should contain candidate message_expr");
        assertTrue(content.contains("\"metrics\":{"), "should contain candidate metrics");
        assertTrue(content.contains("\"word_count\":5"), "should contain word_count");
        assertTrue(content.contains("\"meaningful_word_count\":3"), "should contain meaningful_word_count");
        assertTrue(content.contains("\"min_word_count\":2"), "should contain min_word_count");
        assertTrue(content.contains("\"max_word_count\":10"), "should contain max_word_count");
        assertTrue(content.contains("\"min_meaningful_word_count\":1"),
                "should contain min_meaningful_word_count");
        assertTrue(content.contains("\"max_word_length\":20"), "should contain max_word_length");
        assertTrue(content.contains("\"comparison_operator\":\"==\""),
                "should contain comparison_operator");
        assertTrue(content.contains("\"comparison_left_operand\":\"left\""),
                "should contain comparison_left_operand");
        assertTrue(content.contains("\"comparison_right_operand\":\"right\""),
                "should contain comparison_right_operand");
        assertTrue(content.contains("\"string_search_method\":\"contains\""),
                "should contain string_search_method");
        assertTrue(content.contains("\"string_search_target\":\"target\""),
                "should contain string_search_target");
        assertTrue(content.contains("\"string_search_arg\":\"arg\""),
                "should contain string_search_arg");
        assertTrue(content.contains("\"legacy_suppressions\":["), "should contain legacy_suppressions");
        assertTrue(content.contains("\"MMTooShort\""), "should contain first suppression rule");
        assertTrue(content.contains("\"MMDuplicate\""), "should contain second suppression rule");
    }

    @Test
    @DisplayName("writeFileRecord verbose mode without legacy suppressions or metrics")
    void writeFileRecord_verboseNoCandidatesNoLegacy() throws IOException {
        Path output = tempDir.resolve("verbose-empty.jsonl");
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("Empty.java")
                .lineNo(5)
                .source(AdviceSource.LOG)
                .adviceId(AdviceId.MMLogMessageMissing)
                .build();
        Map<Integer, List<CandidateAdvice>> candidatesByLine = new TreeMap<>();
        candidatesByLine.put(5, List.of(candidate));
        FileReport report = new FileReport("Empty.java",
                Collections.emptyList(), Collections.emptyList(),
                candidatesByLine, Collections.emptyList());
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, true, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"candidates\":["), "should contain candidates");
        assertFalse(content.contains("\"legacy_suppressions\""),
                "should not contain legacy_suppressions when empty");
        assertFalse(content.contains("\"rule_id\""),
                "should not contain rule_id when null");
        assertFalse(content.contains("\"message_literal\""),
                "should not contain message_literal when null");
        assertFalse(content.contains("\"message_expr\""),
                "should not contain message_expr when null");
        assertFalse(content.contains("\"metrics\""),
                "should not contain metrics when null");
    }

    @Test
    @DisplayName("writeFileRecord non-verbose mode omits candidates and legacy suppressions")
    void writeFileRecord_nonVerboseOmitsCandidates() throws IOException {
        Path output = tempDir.resolve("non-verbose.jsonl");
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("NonVerbose.java")
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageMissing)
                .build();
        Map<Integer, List<CandidateAdvice>> candidatesByLine = new TreeMap<>();
        candidatesByLine.put(1, List.of(candidate));
        FileReport report = new FileReport("NonVerbose.java",
                Collections.emptyList(), Collections.emptyList(),
                candidatesByLine, List.of(RuleId.MISSING_MESSAGE));
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"candidates\""), "non-verbose should omit candidates");
        assertFalse(content.contains("\"legacy_suppressions\""),
                "non-verbose should omit legacy_suppressions");
    }

    @Test
    @DisplayName("escape handles all special characters per RFC 8259")
    void escape_handlesAllSpecialCharacters() throws IOException {
        Path output = tempDir.resolve("escape.jsonl");
        AdviceOccurrence occ = new AdviceOccurrence(
                1, AdviceSource.ASSERTION,
                "back\\slash \"quote\" newline\n return\r tab\t bs\b ff\f ctrl\u0001",
                null, null, null);
        AdviceGroup group = new AdviceGroup(
                AdviceId.MMAssertionMessageMissing, sampleText(), 1,
                List.of(occ));
        FileReport report = new FileReport("Escape.java",
                Collections.emptyList(), List.of(group), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\\\\"), "should escape backslash");
        assertTrue(content.contains("\\\""), "should escape quote");
        assertTrue(content.contains("\\n"), "should escape newline");
        assertTrue(content.contains("\\r"), "should escape carriage return");
        assertTrue(content.contains("\\t"), "should escape tab");
        assertTrue(content.contains("\\b"), "should escape backspace");
        assertTrue(content.contains("\\f"), "should escape form feed");
        assertTrue(content.contains("\\u0001"), "should escape control char as unicode");
    }

    @Test
    @DisplayName("quote handles null value")
    void quote_handlesNullValue() throws IOException {
        Path output = tempDir.resolve("null-quote.jsonl");
        // A null messageLiteral should still produce valid JSON via the quote method
        AdviceOccurrence occ = new AdviceOccurrence(
                1, AdviceSource.ASSERTION, null, null, null, null);
        AdviceGroup group = new AdviceGroup(
                AdviceId.MMAssertionMessageMissing, sampleText(), 1,
                List.of(occ));
        FileReport report = new FileReport("NullQuote.java",
                Collections.emptyList(), List.of(group), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"type\":\"file\""), "should produce valid JSON");
    }

    @Test
    @DisplayName("creates parent directories for output path")
    void createsParentDirectories() throws IOException {
        Path output = tempDir.resolve("sub").resolve("dir").resolve("output.jsonl");
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeRunRecord();
        }
        assertTrue(Files.exists(output), "output file should exist");
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"type\":\"run\""), "should have written content");
    }

    @Test
    @DisplayName("writeFileRecord with multiple file advice groups separates with commas")
    void writeFileRecord_multipleFileAdviceGroups() throws IOException {
        Path output = tempDir.resolve("multi-file-advice.jsonl");
        FileAdviceDetails details1 = new FileAdviceDetails(
                AdviceId.MMOverusedWord, 0, Collections.emptyList(),
                null, null, null, null, null, null);
        FileAdviceDetails details2 = new FileAdviceDetails(
                AdviceId.MMLacksPurpose, 0, Collections.emptyList(),
                null, null, null, null, null, null);
        FileAdviceGroup group1 = new FileAdviceGroup(
                AdviceId.MMOverusedWord, sampleText(), 1, details1);
        AdviceText text2 = new AdviceText(
                AdviceId.MMLacksPurpose,
                "Title2", "Intro2", "Outro2",
                "HintA2", "HintB2", "Check2", "Anti2", "Verbose2");
        FileAdviceGroup group2 = new FileAdviceGroup(
                AdviceId.MMLacksPurpose, text2, 2, details2);
        FileReport report = new FileReport("Multi.java",
                List.of(group1, group2), Collections.emptyList(), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        // Verify both groups are present
        assertTrue(content.contains("MMOverusedWord"), "should contain first group");
        assertTrue(content.contains("MMLacksPurpose"), "should contain second group");
    }

    @Test
    @DisplayName("writeFileRecord with multiple line advice groups")
    void writeFileRecord_multipleLineAdviceGroups() throws IOException {
        Path output = tempDir.resolve("multi-line-advice.jsonl");
        AdviceOccurrence occ1 = new AdviceOccurrence(
                10, AdviceSource.ASSERTION, "msg1", null, null, null);
        AdviceOccurrence occ2 = new AdviceOccurrence(
                20, AdviceSource.THROW, "msg2", null, null, null);
        AdviceGroup group1 = new AdviceGroup(
                AdviceId.MMAssertionMessageMissing, sampleText(), 1,
                List.of(occ1));
        AdviceGroup group2 = new AdviceGroup(
                AdviceId.MMThrowMessageMissing, sampleText(), 2,
                List.of(occ2));
        FileReport report = new FileReport("MultiLine.java",
                Collections.emptyList(), List.of(group1, group2), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("MMAssertionMessageMissing"), "should contain first group");
        assertTrue(content.contains("MMThrowMessageMissing"), "should contain second group");
    }

    @Test
    @DisplayName("writeFileRecord verbose with multiple candidates on same line")
    void writeFileRecord_verboseMultipleCandidatesSameLine() throws IOException {
        Path output = tempDir.resolve("multi-candidates.jsonl");
        CandidateAdvice c1 = new CandidateAdvice.Builder()
                .fileName("Multi.java")
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .ruleId(RuleId.TOO_SHORT)
                .build();
        CandidateAdvice c2 = new CandidateAdvice.Builder()
                .fileName("Multi.java")
                .lineNo(10)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageGeneric)
                .ruleId(RuleId.GENERIC)
                .build();
        Map<Integer, List<CandidateAdvice>> candidatesByLine = new TreeMap<>();
        candidatesByLine.put(10, List.of(c1, c2));
        FileReport report = new FileReport("Multi.java",
                Collections.emptyList(), Collections.emptyList(),
                candidatesByLine, Collections.emptyList());
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, true, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("MMAssertionMessageTooShort"), "first candidate");
        assertTrue(content.contains("MMAssertionMessageGeneric"), "second candidate");
    }

    @Test
    @DisplayName("writeFileRecord with file advice partial metrics (only some non-null)")
    void writeFileRecord_withPartialMetrics() throws IOException {
        Path output = tempDir.resolve("partial-metrics.jsonl");
        FileAdviceDetails details = new FileAdviceDetails(
                AdviceId.MMLowEntropy, 0, Collections.emptyList(),
                null, null, null, null, 2.5, null);
        FileAdviceGroup group = new FileAdviceGroup(
                AdviceId.MMLowEntropy, sampleText(), 1, details);
        FileReport report = new FileReport("Partial.java",
                List.of(group), Collections.emptyList(), null, null);
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, false, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"metrics\":{"), "should contain metrics block");
        assertTrue(content.contains("\"entropy\":2.50"), "should contain entropy");
        assertFalse(content.contains("\"min_entropy\""), "should not contain min_entropy");
        assertFalse(content.contains("\"message_count\""), "should not contain message_count");
    }

    @Test
    @DisplayName("writeFileRecord with AdviceMetrics containing null optional fields")
    void writeFileRecord_verboseMetricsWithNullOptionals() throws IOException {
        Path output = tempDir.resolve("null-metrics.jsonl");
        AdviceMetrics metrics = new AdviceMetrics(
                3, 2, 4, 3, 0, 0,
                null, null, null, null,
                null, null, null,
                null, null, null);
        CandidateAdvice candidate = new CandidateAdvice.Builder()
                .fileName("NullMetrics.java")
                .lineNo(1)
                .source(AdviceSource.ASSERTION)
                .adviceId(AdviceId.MMAssertionMessageTooShort)
                .metrics(metrics)
                .build();
        Map<Integer, List<CandidateAdvice>> candidatesByLine = new TreeMap<>();
        candidatesByLine.put(1, List.of(candidate));
        FileReport report = new FileReport("NullMetrics.java",
                Collections.emptyList(), Collections.emptyList(),
                candidatesByLine, Collections.emptyList());
        try (AdviceJsonlWriter writer = new AdviceJsonlWriter(
                output, true, false, "ranks.properties", null)) {
            writer.writeFileRecord(report);
        }
        String content = Files.readString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"word_count\":3"), "should contain word_count");
        assertFalse(content.contains("\"min_word_count\""), "should omit null min_word_count");
        assertFalse(content.contains("\"max_word_count\""), "should omit null max_word_count");
        assertFalse(content.contains("\"min_meaningful_word_count\""),
                "should omit null min_meaningful_word_count");
        assertFalse(content.contains("\"max_word_length\""), "should omit null max_word_length");
        assertFalse(content.contains("\"comparison_operator\""),
                "should omit null comparison_operator");
        assertFalse(content.contains("\"comparison_left_operand\""),
                "should omit null comparison_left_operand");
        assertFalse(content.contains("\"comparison_right_operand\""),
                "should omit null comparison_right_operand");
        assertFalse(content.contains("\"string_search_method\""),
                "should omit null string_search_method");
        assertFalse(content.contains("\"string_search_target\""),
                "should omit null string_search_target");
        assertFalse(content.contains("\"string_search_arg\""),
                "should omit null string_search_arg");
    }
}
