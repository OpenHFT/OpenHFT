/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes advice reports as JSON Lines.
 */
public final class AdviceJsonlWriter implements AutoCloseable {
    private static final int SCHEMA_VERSION = 1;
    private final Writer writer;
    private final boolean verbose;
    private final boolean dryRun;
    private final String rankResource;
    private final String rankOut;

    public AdviceJsonlWriter(Path output, boolean verbose, boolean dryRun,
                             String rankResource, String rankOut) {
        this.verbose = verbose;
        this.dryRun = dryRun;
        this.rankResource = rankResource;
        this.rankOut = rankOut;
        try {
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            this.writer = new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open JSONL output: " + output, e);
        }
    }

    public void writeRunRecord() {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{');
        boolean[] first = new boolean[]{true};
        field(sb, first, "type", "run");
        field(sb, first, "schema_version", SCHEMA_VERSION);
        field(sb, first, "tool", "MeaningfulMessage");
        field(sb, first, "dry_run", dryRun);
        field(sb, first, "verbose", verbose);
        field(sb, first, "rank_resource", rankResource);
        if (rankOut != null) {
            field(sb, first, "rank_out", rankOut);
        }
        sb.append('}');
        writeLine(sb);
    }

    public void writeFileRecord(FileReport report) {
        StringBuilder sb = new StringBuilder(512);
        sb.append('{');
        boolean[] first = new boolean[]{true};
        field(sb, first, "type", "file");
        field(sb, first, "file", report.fileName());
        field(sb, first, "file_level_advice");
        writeFileAdviceArray(sb, report.fileAdvice());
        field(sb, first, "line_level_advice");
        writeLineAdviceArray(sb, report.lineAdvice());
        if (verbose) {
            field(sb, first, "candidates");
            writeCandidateArray(sb, report.candidatesByLine());
            if (!report.legacySuppressionRuleIds().isEmpty()) {
                field(sb, first, "legacy_suppressions");
                writeLegacySuppressions(sb, report.legacySuppressionRuleIds());
            }
        }
        sb.append('}');
        writeLine(sb);
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close JSONL output", e);
        }
    }

    private void writeFileAdviceArray(StringBuilder sb, List<FileAdviceGroup> groups) {
        sb.append('[');
        boolean first = true;
        for (FileAdviceGroup group : groups) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{');
            boolean[] fieldFirst = new boolean[]{true};
            AdviceText text = group.adviceText();
            field(sb, fieldFirst, "advice_id", group.adviceId().name());
            field(sb, fieldFirst, "rank", group.rank());
            field(sb, fieldFirst, "title", text.title());
            field(sb, fieldFirst, "intent_intro", text.intentIntro());
            field(sb, fieldFirst, "intent_outro", text.intentOutro());
            field(sb, fieldFirst, "hint_a", text.hintA());
            field(sb, fieldFirst, "hint_b", text.hintB());
            field(sb, fieldFirst, "checklist", text.checklist());
            field(sb, fieldFirst, "anti_patterns", text.antiPatterns());
            if (group.details().lineNo() > 0) {
                field(sb, fieldFirst, "line", group.details().lineNo());
            }
            if (!group.details().items().isEmpty()) {
                field(sb, fieldFirst, "items");
                writeStringArray(sb, group.details().items());
            }
            writeFileMetrics(sb, fieldFirst, group.details());
            sb.append('}');
        }
        sb.append(']');
    }

    private void writeFileMetrics(StringBuilder sb, boolean[] first, FileAdviceDetails details) {
        boolean hasMetrics = details.messageCount() != null
                || details.purposeCueCount() != null
                || details.expectedPurposeCueCount() != null
                || details.totalMessageCount() != null
                || details.entropy() != null
                || details.minEntropy() != null;
        if (!hasMetrics) {
            return;
        }
        field(sb, first, "metrics");
        sb.append('{');
        boolean[] metricsFirst = new boolean[]{true};
        if (details.messageCount() != null) {
            field(sb, metricsFirst, "message_count", details.messageCount());
        }
        if (details.purposeCueCount() != null) {
            field(sb, metricsFirst, "purpose_cue_count", details.purposeCueCount());
        }
        if (details.expectedPurposeCueCount() != null) {
            field(sb, metricsFirst, "expected_purpose_cue_count", details.expectedPurposeCueCount());
        }
        if (details.totalMessageCount() != null) {
            field(sb, metricsFirst, "total_message_count", details.totalMessageCount());
        }
        if (details.entropy() != null) {
            field(sb, metricsFirst, "entropy", details.entropy());
        }
        if (details.minEntropy() != null) {
            field(sb, metricsFirst, "min_entropy", details.minEntropy());
        }
        sb.append('}');
    }

    private void writeLineAdviceArray(StringBuilder sb, List<AdviceGroup> groups) {
        sb.append('[');
        boolean first = true;
        for (AdviceGroup group : groups) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{');
            boolean[] fieldFirst = new boolean[]{true};
            AdviceText text = group.adviceText();
            field(sb, fieldFirst, "advice_id", group.adviceId().name());
            field(sb, fieldFirst, "rank", group.rank());
            field(sb, fieldFirst, "title", text.title());
            field(sb, fieldFirst, "intent_intro", text.intentIntro());
            field(sb, fieldFirst, "intent_outro", text.intentOutro());
            field(sb, fieldFirst, "hint_a", text.hintA());
            field(sb, fieldFirst, "hint_b", text.hintB());
            field(sb, fieldFirst, "checklist", text.checklist());
            field(sb, fieldFirst, "anti_patterns", text.antiPatterns());
            field(sb, fieldFirst, "occurrences");
            writeOccurrences(sb, group.occurrences());
            sb.append('}');
        }
        sb.append(']');
    }

    private void writeOccurrences(StringBuilder sb, List<AdviceOccurrence> occurrences) {
        sb.append('[');
        boolean first = true;
        for (AdviceOccurrence occurrence : occurrences) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('{');
            boolean[] fieldFirst = new boolean[]{true};
            field(sb, fieldFirst, "line", occurrence.lineNo());
            field(sb, fieldFirst, "source", occurrence.source().name());
            if (occurrence.messageLiteral() != null) {
                field(sb, fieldFirst, "message_literal", occurrence.messageLiteral());
            }
            if (occurrence.messageExpr() != null) {
                field(sb, fieldFirst, "message_expr", occurrence.messageExpr());
            }
            if (occurrence.snippet() != null) {
                field(sb, fieldFirst, "snippet", occurrence.snippet());
            }
            sb.append('}');
        }
        sb.append(']');
    }

    private void writeCandidateArray(StringBuilder sb, Map<Integer, List<CandidateAdvice>> candidatesByLine) {
        sb.append('[');
        boolean first = true;
        for (Map.Entry<Integer, List<CandidateAdvice>> entry : candidatesByLine.entrySet()) {
            int lineNo = entry.getKey();
            for (CandidateAdvice candidate : entry.getValue()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('{');
                boolean[] fieldFirst = new boolean[]{true};
                field(sb, fieldFirst, "line", lineNo);
                field(sb, fieldFirst, "advice_id", candidate.adviceId().name());
                if (candidate.ruleId() != null) {
                    field(sb, fieldFirst, "rule_id", candidate.ruleId().code());
                }
                field(sb, fieldFirst, "source", candidate.source().name());
                if (candidate.messageLiteral() != null) {
                    field(sb, fieldFirst, "message_literal", candidate.messageLiteral());
                }
                if (candidate.messageExpr() != null) {
                    field(sb, fieldFirst, "message_expr", candidate.messageExpr());
                }
                if (candidate.metrics() != null) {
                    field(sb, fieldFirst, "metrics");
                    writeMetrics(sb, candidate.metrics());
                }
                sb.append('}');
            }
        }
        sb.append(']');
    }

    private void writeMetrics(StringBuilder sb, AdviceMetrics metrics) {
        sb.append('{');
        boolean[] fieldFirst = new boolean[]{true};
        field(sb, fieldFirst, "word_count", metrics.wordCount());
        field(sb, fieldFirst, "meaningful_word_count", metrics.meaningfulWordCount());
        field(sb, fieldFirst, "total_word_count", metrics.totalWordCount());
        field(sb, fieldFirst, "effective_meaningful_word_count", metrics.effectiveMeaningfulWordCount());
        field(sb, fieldFirst, "placeholder_count", metrics.placeholderCount());
        field(sb, fieldFirst, "key_value_label_count", metrics.keyValueLabelCount());
        if (metrics.minWordCount() != null) {
            field(sb, fieldFirst, "min_word_count", metrics.minWordCount());
        }
        if (metrics.maxWordCount() != null) {
            field(sb, fieldFirst, "max_word_count", metrics.maxWordCount());
        }
        if (metrics.minMeaningfulWordCount() != null) {
            field(sb, fieldFirst, "min_meaningful_word_count", metrics.minMeaningfulWordCount());
        }
        if (metrics.maxWordLength() != null) {
            field(sb, fieldFirst, "max_word_length", metrics.maxWordLength());
        }
        if (metrics.comparisonOperator() != null) {
            field(sb, fieldFirst, "comparison_operator", metrics.comparisonOperator());
        }
        if (metrics.comparisonLeftOperand() != null) {
            field(sb, fieldFirst, "comparison_left_operand", metrics.comparisonLeftOperand());
        }
        if (metrics.comparisonRightOperand() != null) {
            field(sb, fieldFirst, "comparison_right_operand", metrics.comparisonRightOperand());
        }
        if (metrics.stringSearchMethod() != null) {
            field(sb, fieldFirst, "string_search_method", metrics.stringSearchMethod());
        }
        if (metrics.stringSearchTarget() != null) {
            field(sb, fieldFirst, "string_search_target", metrics.stringSearchTarget());
        }
        if (metrics.stringSearchArg() != null) {
            field(sb, fieldFirst, "string_search_arg", metrics.stringSearchArg());
        }
        sb.append('}');
    }

    private void writeLegacySuppressions(StringBuilder sb, List<RuleId> legacyRuleIds) {
        sb.append('[');
        boolean first = true;
        for (RuleId ruleId : legacyRuleIds) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            quote(sb, ruleId.code());
        }
        sb.append(']');
    }

    private void writeStringArray(StringBuilder sb, List<String> items) {
        sb.append('[');
        boolean first = true;
        for (String item : items) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            quote(sb, item);
        }
        sb.append(']');
    }

    private void field(StringBuilder sb, boolean[] first, String name) {
        if (!first[0]) {
            sb.append(',');
        }
        first[0] = false;
        quote(sb, name);
        sb.append(':');
    }

    private void field(StringBuilder sb, boolean[] first, String name, String value) {
        field(sb, first, name);
        quote(sb, value);
    }

    private void field(StringBuilder sb, boolean[] first, String name, int value) {
        field(sb, first, name);
        sb.append(value);
    }

    private void field(StringBuilder sb, boolean[] first, String name, boolean value) {
        field(sb, first, name);
        sb.append(value);
    }

    private void field(StringBuilder sb, boolean[] first, String name, double value) {
        field(sb, first, name);
        sb.append(formatDouble(value));
    }

    private void quote(StringBuilder sb, String value) {
        sb.append('"');
        if (value != null) {
            escape(sb, value);
        }
        sb.append('"');
    }

    private void escape(StringBuilder sb, String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
    }

    private String formatDouble(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private void writeLine(StringBuilder sb) {
        try {
            writer.write(sb.toString());
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write JSONL output", e);
        }
    }
}
