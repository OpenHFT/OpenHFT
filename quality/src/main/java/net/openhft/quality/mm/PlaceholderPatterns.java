/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.regex.Pattern;

/**
 * Centralized patterns for detecting placeholders in message strings.
 *
 * <p>These patterns are used throughout the message analysis framework to identify
 * various types of placeholders in log messages, format strings, and annotations.
 */
public final class PlaceholderPatterns {

    /**
     * Matches SLF4J/Log4j2 logging placeholders.
     *
     * <p>Recognizes:
     * <ul>
     *   <li>{@code {}} - SLF4J-style placeholder</li>
     *   <li>{@code {0}, {1}, ...} - Indexed placeholders</li>
     *   <li>{@code %s} - Printf-style string placeholder in log messages</li>
     * </ul>
     *
     * <p>Examples that match: {@code "{}"}, {@code "{0}"}, {@code "%s"}
     */
    public static final Pattern LOG = Pattern.compile(
            "\\{\\}|\\{\\d+\\}|%s"
    );

    /**
     * Matches String.format placeholder specifiers.
     *
     * <p>Recognizes the full Java format specifier syntax:
     * <ul>
     *   <li>{@code %s, %d, %f} - Simple specifiers</li>
     *   <li>{@code %2$s} - Argument index specifiers</li>
     *   <li>{@code %-10s} - Width and alignment flags</li>
     *   <li>{@code %.2f} - Precision specifiers</li>
     *   <li>{@code %tY} - Date/time specifiers</li>
     * </ul>
     *
     * <p>Does not match escaped percent ({@code %%}) or newline ({@code %n}).
     *
     * <p>Examples that match: {@code "%s"}, {@code "%d"}, {@code "%2$s"}, {@code "%-10.2f"}
     */
    public static final Pattern STRING_FORMAT = Pattern.compile(
            "%(?!%|n)(?:\\d+\\$)?[-#+ 0,(<]*\\d*(?:\\.\\d+)?(?:[tT])?[a-zA-Z]"
    );

    /**
     * Matches MessageFormat placeholder patterns.
     *
     * <p>Recognizes numbered placeholders with optional format types:
     * <ul>
     *   <li>{@code {0}} - Simple placeholder</li>
     *   <li>{@code {0,number}} - Typed placeholder</li>
     *   <li>{@code {1,date,short}} - Styled placeholder</li>
     * </ul>
     *
     * <p>Examples that match: {@code "{0}"}, {@code "{1,number}"}, {@code "{2,date,long}"}
     */
    public static final Pattern MESSAGE_FORMAT = Pattern.compile(
            "\\{\\d+[^}]*\\}"
    );

    /**
     * Matches key-value label patterns in diagnostic messages.
     *
     * <p>Recognizes common variable names followed by colon or equals:
     * <ul>
     *   <li>Position: start, end, offset, index, pos, position, line, row, col</li>
     *   <li>Size: length, size, count, width, height, num, number</li>
     *   <li>Identity: key, value, id, name, type, state, status, code</li>
     *   <li>Comparison: min, max, expected, actual, result, input, output</li>
     *   <li>Navigation: left, right, first, last, prev, next, current, ref</li>
     *   <li>Loop: i, j, k, n, x, y</li>
     * </ul>
     *
     * <p>Examples that match: {@code "index:"}, {@code "size="}, {@code "expected:"}
     */
    public static final Pattern KEY_VALUE_LABEL = Pattern.compile(
            "(?i)\\b(start|end|offset|length|size|count|index|i|j|k|n|x|y|"
                    + "key|value|id|name|type|state|status|code|pos|position|"
                    + "min|max|expected|actual|result|input|output|"
                    + "left|right|first|last|prev|next|current|ref|refCount|"
                    + "width|height|row|col|column|line|num|number)\\s*[:=]"
    );

    /**
     * Matches annotation placeholders for constraint messages.
     *
     * <p>Recognizes placeholders in Bean Validation and similar annotation messages:
     * <ul>
     *   <li>{@code {value}} - Constraint attribute value</li>
     *   <li>{@code {min}, {max}} - Range bounds</li>
     *   <li>{@code {regexp}} - Pattern constraint</li>
     * </ul>
     *
     * <p>Examples that match: {@code "{value}"}, {@code "{min}"}, {@code "{max}"}
     */
    public static final Pattern ANNOTATION = Pattern.compile(
            "\\{[^}]+\\}"
    );

    private PlaceholderPatterns() {
        // Utility class - prevent instantiation
    }
}
