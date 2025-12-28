#!/usr/bin/env python3
"""
Summarise MeaningfulMessage extraction output (TSV) into a Markdown report.
"""
from __future__ import print_function

import argparse
import collections
import datetime
import re
import statistics

WORD_SPLITTER = re.compile(r'[^A-Za-z0-9]+')

FILLER_WORDS = {
    "a", "an", "the", "is", "are", "was", "were", "be", "been",
    "to", "of", "in", "for", "on", "at", "by", "with", "from",
    "as", "if", "or", "and", "not", "no", "when", "after", "before",
    "should", "must", "expected", "actual", "value", "result",
    "test", "check", "assert", "equals", "return", "returns",
    "read", "todo", "this",
    "error", "fail", "failed", "failure",
    "null", "true", "false", "non", "empty",
    "have", "has", "does", "contain", "contains", "exist", "exists",
    "created", "create", "creating", "create", "init", "initialise", "initialised",
    "initialized", "initialization", "setup", "should", "must",
}

SOURCE_MINIMA = {
    "ASSERTION": (4, 2),
    "PRECONDITION": (4, 2),
    "THROW": (2, 1),
    "ANNOTATION": (6, 4),
    "LOG": (4, 2),
}

DEFAULT_MINIMA = (4, 2)


def unescape_tsv(value):
    if value is None:
        return ""
    value = value.replace("\\\\t", "\t").replace("\\\\r", "\r").replace("\\\\n", "\n")
    value = value.replace("\\\\\\\\", "\\")
    return value


def bucket_counts(values, buckets):
    counts = collections.Counter()
    for v in values:
        for label, lo, hi in buckets:
            if hi is None and v >= lo:
                counts[label] += 1
                break
            if hi is not None and lo <= v <= hi:
                counts[label] += 1
                break
    return counts


def median(values):
    if not values:
        return 0
    return int(statistics.median(values))


def mean(values):
    if not values:
        return 0
    return int(round(statistics.mean(values)))


def parse_args():
    parser = argparse.ArgumentParser(description="Summarise MeaningfulMessage extraction output (TSV).")
    parser.add_argument("--input", required=True, help="Path to message extraction TSV file.")
    parser.add_argument("--output", default="message-extraction-summary.md",
                        help="Output Markdown report path.")
    parser.add_argument("--top-words", type=int, default=40,
                        help="Number of top words to report.")
    parser.add_argument("--top-messages", type=int, default=20,
                        help="Number of top duplicate messages to report.")
    return parser.parse_args()


def main():
    args = parse_args()
    total = 0
    by_source = collections.Counter()
    char_lengths = []
    total_word_counts = []
    effective_meaningful_counts = []
    placeholder_counts = []
    empty_messages = 0
    word_counts = collections.Counter()
    non_filler_counts = collections.Counter()
    messages = collections.Counter()
    below_min_words = 0
    below_min_meaningful = 0
    below_min_by_source = collections.Counter()
    below_meaningful_by_source = collections.Counter()

    with open(args.input, "r", encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line:
                continue
            if line.startswith("file\tline\tsource\t"):
                continue
            parts = line.split("\t")
            if len(parts) < 11:
                continue
            file_name, line_no, source = parts[0], parts[1], parts[2]
            char_len = int(parts[3] or 0)
            word_count = int(parts[4] or 0)
            meaningful_count = int(parts[5] or 0)
            placeholder_count = int(parts[6] or 0)
            key_value_labels = int(parts[7] or 0)
            total_words = int(parts[8] or 0)
            effective_meaningful = int(parts[9] or 0)
            message = unescape_tsv("\t".join(parts[10:]))

            total += 1
            by_source[source] += 1
            char_lengths.append(char_len)
            total_word_counts.append(total_words)
            effective_meaningful_counts.append(effective_meaningful)
            placeholder_counts.append(placeholder_count)
            messages[message] += 1

            if not message.strip():
                empty_messages += 1
            min_words, min_meaningful = SOURCE_MINIMA.get(source, DEFAULT_MINIMA)
            if total_words < min_words:
                below_min_words += 1
                below_min_by_source[source] += 1
            if effective_meaningful < min_meaningful:
                below_min_meaningful += 1
                below_meaningful_by_source[source] += 1

            for word in WORD_SPLITTER.split(message):
                if not word:
                    continue
                word_lower = word.lower()
                word_counts[word_lower] += 1
                if word_lower not in FILLER_WORDS and not word_lower.isdigit():
                    non_filler_counts[word_lower] += 1

    char_buckets = [
        ("0-9", 0, 9),
        ("10-19", 10, 19),
        ("20-29", 20, 29),
        ("30-39", 30, 39),
        ("40-49", 40, 49),
        ("50-59", 50, 59),
        ("60-79", 60, 79),
        ("80-99", 80, 99),
        ("100-149", 100, 149),
        ("150-199", 150, 199),
        ("200+", 200, None),
    ]
    word_buckets = [
        ("0", 0, 0),
        ("1", 1, 1),
        ("2", 2, 2),
        ("3", 3, 3),
        ("4", 4, 4),
        ("5-7", 5, 7),
        ("8-10", 8, 10),
        ("11-15", 11, 15),
        ("16-20", 16, 20),
        ("21-30", 21, 30),
        ("31+", 31, None),
    ]
    placeholder_buckets = [
        ("0", 0, 0),
        ("1", 1, 1),
        ("2", 2, 2),
        ("3", 3, 3),
        ("4", 4, 4),
        ("5+", 5, None),
    ]

    report = []
    report.append("# Message extraction report")
    report.append("")
    report.append("Generated: {0}Z".format(
        datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%S")
    ))
    report.append("")
    report.append("## Summary")
    report.append("- Messages: {0}".format(total))
    report.append("- Unique messages: {0}".format(len(messages)))
    report.append("- Empty or whitespace only: {0}".format(empty_messages))
    report.append("- Below minimum total words (source-specific): {0}".format(below_min_words))
    report.append("- Below minimum meaningful words (source-specific): {0}".format(below_min_meaningful))
    report.append("- Mean chars: {0}".format(mean(char_lengths)))
    report.append("- Median chars: {0}".format(median(char_lengths)))
    report.append("- Mean total words: {0}".format(mean(total_word_counts)))
    report.append("- Median total words: {0}".format(median(total_word_counts)))
    report.append("")

    report.append("## Sources")
    for source, count in by_source.most_common():
        report.append("- {0}: {1}".format(source, count))
    report.append("")

    report.append("## Below minimum by source")
    for source, count in by_source.most_common():
        report.append("- {0}: total={1} meaningful={2}".format(
            source,
            below_min_by_source.get(source, 0),
            below_meaningful_by_source.get(source, 0),
        ))
    report.append("")

    report.append("## Character length distribution")
    for label, count in bucket_counts(char_lengths, char_buckets).items():
        report.append("- {0}: {1}".format(label, count))
    report.append("")

    report.append("## Word count distribution (total words incl placeholders)")
    for label, count in bucket_counts(total_word_counts, word_buckets).items():
        report.append("- {0}: {1}".format(label, count))
    report.append("")

    report.append("## Placeholder count distribution")
    for label, count in bucket_counts(placeholder_counts, placeholder_buckets).items():
        report.append("- {0}: {1}".format(label, count))
    report.append("")

    report.append("## Most common words (all)")
    for word, count in word_counts.most_common(args.top_words):
        report.append("- {0}: {1}".format(word, count))
    report.append("")

    report.append("## Candidate filler words (not in current filler list)")
    for word, count in non_filler_counts.most_common(args.top_words):
        report.append("- {0}: {1}".format(word, count))
    report.append("")

    report.append("## Most common duplicate messages")
    for message, count in messages.most_common(args.top_messages):
        if count < 2:
            break
        report.append("- {0} (x{1})".format(message.replace("\n", "\\n"), count))
    report.append("")

    with open(args.output, "w", encoding="utf-8") as out:
        out.write("\n".join(report))


if __name__ == "__main__":
    main()
