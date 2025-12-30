/*
 * Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.openhft.todotracker.parser;

import net.openhft.todotracker.model.TodoTask;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses TODO files in Markdown or AsciiDoc to extract checkbox tasks and context headings.
 * <p>
 * Markdown checkbox formats:
 * <ul>
 *   <li>{@code - [ ]} / {@code - [x]} - dash bullet checkbox (open/done)</li>
 *   <li>{@code * [ ]} / {@code * [x]} - asterisk bullet checkbox (open/done)</li>
 *   <li>{@code 1. [ ]} / {@code 1. [x]} - numbered list checkbox (open/done)</li>
 *   <li>{@code - [-]} - dropped task (no further action needed)</li>
 * </ul>
 * <p>
 * AsciiDoc checkbox formats:
 * <ul>
 *   <li>{@code * [ ]} / {@code * [x]} / {@code * [*]} - single-level bullet (open/done)</li>
 *   <li>{@code ** [ ]} / {@code ** [x]} - nested bullet checkbox</li>
 *   <li>{@code - [ ]} / {@code - [x]} - single-level dash (open/done)</li>
 *   <li>{@code * [-]} - dropped task</li>
 * </ul>
 * <p>
 * Also extracts priority tags [P1-3] and effort tags [E:S/M/L] from task text.
 * <p>
 * Note: Format is detected from file extension (.ad/.adoc/.asciidoc = AsciiDoc, .md/.markdown = Markdown).
 */
public final class TodoParser {

    // === Markdown patterns ===

    // Pattern: - [ ] or * [ ] with optional leading whitespace, requires space after bullet
    // Accepts: space (open), x/X (done), - (dropped)
    private static final Pattern MD_BULLET_TASK_PATTERN =
            Pattern.compile("^\\s*[-*]\\s+\\[([ xX-])\\]\\s*(.*)$");

    // Pattern: 1. [ ] with optional leading whitespace, requires space after period
    // Accepts: space (open), x/X (done), - (dropped)
    private static final Pattern MD_NUMBERED_TASK_PATTERN =
            Pattern.compile("^\\s*\\d+\\.\\s+\\[([ xX-])\\]\\s*(.*)$");

    // Pattern: ## Header (markdown section header)
    private static final Pattern MD_HEADER_PATTERN =
            Pattern.compile("^(#{1,6})\\s+(.*)$");

    // === AsciiDoc patterns ===

    // Pattern: * [ ] or ** [ ] etc. with optional leading whitespace
    // Accepts: space (open), x/X/* (done), - (dropped)
    // Note: AsciiDoc uses repeated * for nesting, - only for single level
    private static final Pattern ADOC_BULLET_TASK_PATTERN =
            Pattern.compile("^\\s*[*]+\\s+\\[([ xX*-])\\]\\s*(.*)$");

    // Pattern: - [ ] for single-level AsciiDoc lists
    private static final Pattern ADOC_DASH_TASK_PATTERN =
            Pattern.compile("^\\s*-\\s+\\[([ xX*-])\\]\\s*(.*)$");

    // Pattern: == Header (AsciiDoc section header)
    private static final Pattern ADOC_HEADER_PATTERN =
            Pattern.compile("^(={1,6})\\s+(.*)$");

    // Pattern: [P1], [P2], [P3] for priority
    private static final Pattern PRIORITY_PATTERN =
            Pattern.compile("\\[P([1-3])\\]");

    // Pattern: [E:S], [E:M], [E:L] for effort
    private static final Pattern EFFORT_PATTERN =
            Pattern.compile("\\[E:([SML])\\]");
    private final Pattern contextPattern;

    /**
     * Creates a parser with the default context pattern (## headers).
     */
    public TodoParser() {
        this("^##\\s+.*");
    }

    /**
     * Creates a parser with a custom context pattern.
     *
     * @param contextPatternRegex regex pattern to identify context headings
     */
    public TodoParser(String contextPatternRegex) {
        this.contextPattern = Pattern.compile(
                Objects.requireNonNull(contextPatternRegex, "context pattern regex must not be null"));
    }

    /**
     * Detects the format based on file extension.
     *
     * @param filePath the file path to check
     * @return ASCIIDOC for .ad/.adoc/.asciidoc files, MARKDOWN otherwise
     */
    public static Format detectFormat(String filePath) {
        if (filePath == null) {
            return Format.MARKDOWN;
        }
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".ad") || lower.endsWith(".adoc") || lower.endsWith(".asciidoc")) {
            return Format.ASCIIDOC;
        }
        return Format.MARKDOWN;
    }

    /**
     * Parses a TODO file and extracts all tasks.
     * Format is auto-detected from file extension.
     *
     * @param file the TODO file to parse (.md or .adoc)
     * @return a TodoReport containing all found tasks
     * @throws IOException if the file cannot be read
     */
    public TodoReport parse(File file) throws IOException {
        Objects.requireNonNull(file, "todo source file path must not be null");

        Format format = detectFormat(file.getPath());
        TodoReport report = new TodoReport();
        report.addFileProcessed(file.getPath());

        String currentContext = null;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                ParseResult result = parseLine(line, format, currentContext, file.getPath(), lineNumber);
                if (result.contextUpdated) {
                    currentContext = result.newContext;
                }
                if (result.task != null) {
                    report.addTask(result.task);
                }
            }
        }

        return report;
    }

    /**
     * Parses a single line based on format.
     */
    private ParseResult parseLine(String line, Format format, String currentContext,
                                  String filePath, int lineNumber) {
        // Check for header (context) based on format
        Pattern headerPattern = (format == Format.ASCIIDOC) ? ADOC_HEADER_PATTERN : MD_HEADER_PATTERN;
        Matcher headerMatcher = headerPattern.matcher(line);
        if (headerMatcher.matches()) {
            if (contextPattern.matcher(line).matches()) {
                return ParseResult.contextChange(headerMatcher.group(2).trim());
            }
            return ParseResult.noMatch();
        }

        // Try format-specific task patterns
        if (format == Format.ASCIIDOC) {
            return parseAsciidocTask(line, currentContext, filePath, lineNumber);
        } else {
            return parseMarkdownTask(line, currentContext, filePath, lineNumber);
        }
    }

    /**
     * Parses a Markdown task line and returns the matching result.
     */
    private ParseResult parseMarkdownTask(String line, String currentContext,
                                          String filePath, int lineNumber) {
        // Try bullet checkbox (- [ ] or * [ ])
        Matcher bulletMatcher = MD_BULLET_TASK_PATTERN.matcher(line);
        if (bulletMatcher.matches()) {
            TodoTask task = createTask(filePath, lineNumber,
                    bulletMatcher.group(1), bulletMatcher.group(2), currentContext);
            return ParseResult.taskFound(task);
        }

        // Try numbered checkbox (1. [ ])
        Matcher numberedMatcher = MD_NUMBERED_TASK_PATTERN.matcher(line);
        if (numberedMatcher.matches()) {
            TodoTask task = createTask(filePath, lineNumber,
                    numberedMatcher.group(1), numberedMatcher.group(2), currentContext);
            return ParseResult.taskFound(task);
        }

        return ParseResult.noMatch();
    }

    /**
     * Parses an AsciiDoc task line and returns the matching result.
     */
    private ParseResult parseAsciidocTask(String line, String currentContext,
                                          String filePath, int lineNumber) {
        // Try asterisk bullet (* [ ] or ** [ ])
        Matcher bulletMatcher = ADOC_BULLET_TASK_PATTERN.matcher(line);
        if (bulletMatcher.matches()) {
            TodoTask task = createTask(filePath, lineNumber,
                    bulletMatcher.group(1), bulletMatcher.group(2), currentContext);
            return ParseResult.taskFound(task);
        }

        // Try dash bullet (- [ ])
        Matcher dashMatcher = ADOC_DASH_TASK_PATTERN.matcher(line);
        if (dashMatcher.matches()) {
            TodoTask task = createTask(filePath, lineNumber,
                    dashMatcher.group(1), dashMatcher.group(2), currentContext);
            return ParseResult.taskFound(task);
        }

        return ParseResult.noMatch();
    }

    /**
     * Parses content from a string with auto-detected format.
     * Format is detected from the sourcePath extension.
     *
     * @param content    the content (Markdown or AsciiDoc)
     * @param sourcePath virtual path for reporting and format detection
     * @return a TodoReport containing all found tasks
     */
    public TodoReport parseContent(String content, String sourcePath) {
        return parseContent(content, sourcePath, detectFormat(sourcePath));
    }

    /**
     * Parses content from a string with explicit format.
     *
     * @param content    the content (Markdown or AsciiDoc)
     * @param sourcePath virtual path for reporting
     * @param format     the format to use for parsing
     * @return a TodoReport containing all found tasks
     */
    public TodoReport parseContent(String content, String sourcePath, Format format) {
        Objects.requireNonNull(content, "content text must not be null");
        Objects.requireNonNull(sourcePath, "source path must not be null");
        Objects.requireNonNull(format, "parse format must not be null");

        TodoReport report = new TodoReport();
        report.addFileProcessed(sourcePath);

        String currentContext = null;
        String[] lines = content.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            ParseResult result = parseLine(line, format, currentContext, sourcePath, lineNumber);
            if (result.contextUpdated) {
                currentContext = result.newContext;
            }
            if (result.task != null) {
                report.addTask(result.task);
            }
        }

        return report;
    }

    private TodoTask createTask(String filePath, int lineNumber,
                                String checkMark, String rawText, String context) {
        boolean completed = !checkMark.equals(" ");
        String text = rawText.trim();

        // Extract priority
        Integer priority = extractPriority(text);

        // Extract effort
        Character effort = extractEffort(text);

        // Clean text by removing priority and effort tags
        String cleanText = cleanTaskText(text);

        return new TodoTask(filePath, lineNumber, cleanText, context, completed, priority, effort);
    }

    /**
     * Extracts priority from task text when a [P1-3] tag is present.
     *
     * @param text the task text
     * @return priority 1-3, or null if not found
     */
    Integer extractPriority(String text) {
        Matcher matcher = PRIORITY_PATTERN.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    /**
     * Extracts effort from task text when an [E:S/M/L] tag is present.
     *
     * @param text the task text
     * @return effort character S/M/L, or null if not found
     */
    Character extractEffort(String text) {
        Matcher matcher = EFFORT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).charAt(0);
        }
        return null;
    }

    /**
     * Removes priority and effort tags from task text.
     *
     * @param text the task text with tags
     * @return cleaned text without tags
     */
    String cleanTaskText(String text) {
        String cleaned = text;
        cleaned = PRIORITY_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = EFFORT_PATTERN.matcher(cleaned).replaceAll("");
        return cleaned.trim().replaceAll("\\s+", " ");
    }

    /**
     * Detected file format used when parsing content.
     */
    public enum Format {
        MARKDOWN,
        ASCIIDOC
    }

    /**
     * Result of parsing a single task or context line.
     */
    private static class ParseResult {
        final TodoTask task;
        final boolean contextUpdated;
        final String newContext;

        ParseResult(TodoTask task, boolean contextUpdated, String newContext) {
            this.task = task;
            this.contextUpdated = contextUpdated;
            this.newContext = newContext;
        }

        static ParseResult noMatch() {
            return new ParseResult(null, false, null);
        }

        static ParseResult contextChange(String newContext) {
            return new ParseResult(null, true, newContext);
        }

        static ParseResult taskFound(TodoTask task) {
            return new ParseResult(task, false, null);
        }
    }
}
