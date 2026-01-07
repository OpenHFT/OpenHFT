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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Parser should cover todo parsing scenarios")
class TodoParserTest {

    @TempDir
    Path tempDir;
    private TodoParser parser;

    @BeforeEach
    void setUp() {
        parser = new TodoParser();
    }

    // === Basic checkbox parsing ===

    @Test
    @DisplayName("Parser should parse content dash checkbox uncompleted")
    void parseContent_dashCheckboxUncompleted() {
        TodoReport report = parser.parseContent("- [ ] Do something", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for dash checkbox");
        assertEquals(1, report.getUncompletedCount(), "parser should mark dash checkbox task as uncompleted");
        assertEquals("Do something", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for dash checkbox");
    }

    @Test
    @DisplayName("Parser should parse content dash checkbox completed lowercase")
    void parseContent_dashCheckboxCompleted_lowercase() {
        TodoReport report = parser.parseContent("- [x] Done task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for lowercase done");
        assertEquals(1, report.getCompletedCount(), "parser should mark lowercase x checkbox completed");
        assertEquals(0, report.getUncompletedCount(), "parser should have zero uncompleted tasks for done");
    }

    @Test
    @DisplayName("Parser should parse content dash checkbox completed uppercase")
    void parseContent_dashCheckboxCompleted_uppercase() {
        TodoReport report = parser.parseContent("- [X] Done task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for uppercase done");
        assertEquals(1, report.getCompletedCount(), "parser should mark uppercase X checkbox completed");
    }

    @Test
    @DisplayName("Parser should parse content asterisk checkbox uncompleted")
    void parseContent_asteriskCheckboxUncompleted() {
        TodoReport report = parser.parseContent("* [ ] Asterisk task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asterisk checkbox");
        assertEquals("Asterisk task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for asterisk checkbox");
    }

    @Test
    @DisplayName("Parser should parse content asterisk checkbox completed")
    void parseContent_asteriskCheckboxCompleted() {
        TodoReport report = parser.parseContent("* [x] Asterisk done", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asterisk done");
        assertEquals(1, report.getCompletedCount(), "parser should mark asterisk checkbox completed");
    }

    @Test
    @DisplayName("Parser should parse content numbered list uncompleted")
    void parseContent_numberedListUncompleted() {
        TodoReport report = parser.parseContent("1. [ ] First item", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for numbered list");
        assertEquals("First item", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for numbered list");
    }

    @Test
    @DisplayName("Parser should parse content numbered list completed")
    void parseContent_numberedListCompleted() {
        TodoReport report = parser.parseContent("2. [x] Second item", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for numbered done");
        assertEquals(1, report.getCompletedCount(), "parser should mark numbered checkbox completed");
    }

    // === Dropped state (GFM extension) ===

    @Test
    @DisplayName("Parser should parse content dropped task dash bullet")
    void parseContent_droppedTask_dashBullet() {
        TodoReport report = parser.parseContent("- [-] Dropped task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one dropped task for dash bullet");
        assertEquals(1, report.getCompletedCount(),
                "parser should treat dropped dash task as completed"); // Dropped counts as completed (no action needed)
        assertEquals(0, report.getUncompletedCount(), "parser should have zero uncompleted for dropped dash");
    }

    @Test
    @DisplayName("Parser should parse content dropped task asterisk bullet")
    void parseContent_droppedTask_asteriskBullet() {
        TodoReport report = parser.parseContent("* [-] Cancelled task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one dropped task for asterisk bullet");
        assertEquals(1, report.getCompletedCount(), "parser should treat dropped asterisk task as completed");
    }

    @Test
    @DisplayName("Parser should parse content dropped task numbered list")
    void parseContent_droppedTask_numberedList() {
        TodoReport report = parser.parseContent("1. [-] Abandoned item", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one dropped task for numbered list");
        assertEquals(1, report.getCompletedCount(), "parser should treat dropped numbered task as completed");
    }

    @Test
    @DisplayName("Parser should parse content mixed states")
    void parseContent_mixedStates() {
        String content = "- [ ] Open task\n- [x] Done task\n- [-] Dropped task\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(3, report.getTotalCount(), "parser should count three tasks for mixed states");
        assertEquals(2, report.getCompletedCount(),
                "parser should count two completed tasks for mixed states"); // done + dropped
        assertEquals(1, report.getUncompletedCount(),
                "parser should count one open task for mixed states"); // only open
    }

    @Test
    @DisplayName("Parser should parse content multi digit numbered list")
    void parseContent_multiDigitNumberedList() {
        TodoReport report = parser.parseContent("123. [ ] Task 123", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for multi digit list");
        assertEquals("Task 123", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for multi digit list");
    }

    // === Indentation ===

    @Test
    @DisplayName("Parser should parse content indented dash checkbox")
    void parseContent_indentedDashCheckbox() {
        TodoReport report = parser.parseContent("  - [ ] Indented task", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for indented dash");
        assertEquals("Indented task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for indented dash");
    }

    @Test
    @DisplayName("Parser should parse content indented numbered list")
    void parseContent_indentedNumberedList() {
        TodoReport report = parser.parseContent("    1. [ ] Deeply indented", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count one task for indented numbered list");
    }

    // === Priority and effort extraction ===

    @Test
    @DisplayName("Parser should parse content extracts priority")
    void parseContent_extractsPriority() {
        TodoReport report = parser.parseContent("- [ ] [P1] High priority task", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertTrue(task.hasPriority(), "parser should mark task as having priority tag");
        assertEquals(1, task.getPriority(), "parser should read priority value from task tag");
        assertEquals("High priority task", task.getText(), "parser should strip priority tag from task text");
    }

    @ParameterizedTest
    @CsvSource({
            "'- [ ] [P1] Task', 1",
            "'- [ ] [P2] Task', 2",
            "'- [ ] [P3] Task', 3"
    })
    @DisplayName("Parser should parse content priority values")
    void parseContent_priorityValues(String line, int expectedPriority) {
        TodoReport report = parser.parseContent(line, "test.md");
        assertEquals(expectedPriority, report.getUncompletedTasks().get(0).getPriority(),
                "parser should parse priority value from tag");
    }

    @Test
    @DisplayName("Parser should parse content extracts effort")
    void parseContent_extractsEffort() {
        TodoReport report = parser.parseContent("- [ ] [E:M] Medium effort", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertTrue(task.hasEffort(), "parser should mark task as having effort tag");
        assertEquals('M', task.getEffort(), "parser should read effort value from task tag");
        assertEquals("Medium effort", task.getText(), "parser should strip effort tag from task text");
    }

    @ParameterizedTest
    @CsvSource({
            "'- [ ] [E:S] Task', S",
            "'- [ ] [E:M] Task', M",
            "'- [ ] [E:L] Task', L"
    })
    @DisplayName("Parser should parse content effort values")
    void parseContent_effortValues(String line, char expectedEffort) {
        TodoReport report = parser.parseContent(line, "test.md");
        assertEquals(expectedEffort, report.getUncompletedTasks().get(0).getEffort(),
                "parser should parse effort value from tag");
    }

    @Test
    @DisplayName("Parser should parse content extracts both priority and effort")
    void parseContent_extractsBothPriorityAndEffort() {
        TodoReport report = parser.parseContent("- [ ] [P1] [E:L] Complex task", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertEquals(1, task.getPriority(), "parser should read priority when effort also present");
        assertEquals('L', task.getEffort(), "parser should read effort when priority also present");
        assertEquals("Complex task", task.getText(), "parser should strip both priority and effort tags");
    }

    @Test
    @DisplayName("Parser should parse content priority and effort in different order")
    void parseContent_priorityAndEffortInDifferentOrder() {
        TodoReport report = parser.parseContent("- [ ] [E:S] [P2] Task", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertEquals(2, task.getPriority(), "parser should read priority when tags are swapped");
        assertEquals('S', task.getEffort(), "parser should read effort when tags are swapped");
    }

    @Test
    @DisplayName("Parser should parse content no priority or effort")
    void parseContent_noPriorityOrEffort() {
        TodoReport report = parser.parseContent("- [ ] Simple task", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertFalse(task.hasPriority(), "parser should report no priority when tag absent");
        assertFalse(task.hasEffort(), "parser should report no effort when tag absent");
    }

    // === Context extraction ===

    @Test
    @DisplayName("Parser should parse content captures context from header")
    void parseContent_capturesContextFromHeader() {
        String content = "## Phase 1: Setup\n\n- [ ] First task";
        TodoReport report = parser.parseContent(content, "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertEquals("Phase 1: Setup", task.getContext(), "parser should capture context from header line");
    }

    @Test
    @DisplayName("Parser should parse content context updates with new header")
    void parseContent_contextUpdatesWithNewHeader() {
        String content = "## Phase 1\n- [ ] Task in phase 1\n## Phase 2\n- [ ] Task in phase 2\n";
        TodoReport report = parser.parseContent(content, "test.md");

        List<TodoTask> tasks = report.getUncompletedTasks();
        assertEquals("Phase 1", tasks.get(0).getContext(), "parser should set context from first header");
        assertEquals("Phase 2", tasks.get(1).getContext(), "parser should update context for second header");
    }

    @Test
    @DisplayName("Parser should parse content ignores h1 headers")
    void parseContent_ignoresH1Headers() {
        String content = "# Main Title\n- [ ] Task without context\n";
        TodoReport report = parser.parseContent(content, "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertNull(task.getContext(), "parser should not set context from H1 header");
    }

    @Test
    @DisplayName("Parser should parse content custom context pattern")
    void parseContent_customContextPattern() {
        TodoParser customParser = new TodoParser("^###\\s+.*");
        String content = "## Ignored Header\n### Captured Header\n- [ ] Task\n";
        TodoReport report = customParser.parseContent(content, "test.md");

        assertEquals("Captured Header", report.getUncompletedTasks().get(0).getContext(),
                "parser should capture context using custom pattern");
    }

    // === Line numbers ===

    @Test
    @DisplayName("Parser should parse content captures correct line number")
    void parseContent_capturesCorrectLineNumber() {
        String content = "# Header\n\n- [ ] Task on line 3";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(3, report.getUncompletedTasks().get(0).getLineNumber(),
                "parser should report line number for task");
    }

    @Test
    @DisplayName("Parser should parse content multiple tasks have correct line numbers")
    void parseContent_multipleTasksHaveCorrectLineNumbers() {
        String content = "- [ ] Line 1\n- [ ] Line 2\n\n- [ ] Line 4\n";
        TodoReport report = parser.parseContent(content, "test.md");

        List<TodoTask> tasks = report.getUncompletedTasks();
        assertEquals(1, tasks.get(0).getLineNumber(), "parser should report line number for first task");
        assertEquals(2, tasks.get(1).getLineNumber(), "parser should report line number for second task");
        assertEquals(4, tasks.get(2).getLineNumber(), "parser should report line number for third task");
    }

    // === Non-matching lines ===

    @Test
    @DisplayName("Parser should parse content empty content returns empty report")
    void parseContent_emptyContent_returnsEmptyReport() {
        TodoReport report = parser.parseContent("", "test.md");

        assertEquals(0, report.getTotalCount(), "parser should return zero tasks for empty content");
        assertTrue(report.hasFilesProcessed(), "parser should mark files processed for empty content");
    }

    @Test
    @DisplayName("Parser should parse content no checkboxes returns empty report")
    void parseContent_noCheckboxes_returnsEmptyReport() {
        String content = "# Header\n\nSome regular text here.\n\n- Regular list item without checkbox\n* Another list item\n1. Numbered without checkbox\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(0, report.getTotalCount(), "parser should return zero tasks when no checkboxes exist");
    }

    @Test
    @DisplayName("Parser should parse content malformed checkbox not recognized")
    void parseContent_malformedCheckbox_notRecognized() {
        // These should NOT be recognized as checkboxes
        String content = "-[ ] No space after dash\n- [] No space in brackets\n-[] Both missing\n[ ] Missing bullet\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(0, report.getTotalCount(), "parser should ignore malformed checkbox markers");
    }

    @Test
    @DisplayName("Parser should parse content only headers no tasks")
    void parseContent_onlyHeaders_noTasks() {
        String content = "# Header 1\n## Header 2\n### Header 3\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(0, report.getTotalCount(), "parser should return zero tasks when only headers");
    }

    // === Mixed content ===

    @Test
    @DisplayName("Parser should parse content mixed bullet styles")
    void parseContent_mixedBulletStyles() {
        String content = "- [ ] Dash task\n* [ ] Asterisk task\n1. [ ] Numbered task\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(3, report.getTotalCount(), "parser should count tasks across mixed bullet styles");
    }

    @Test
    @DisplayName("Parser should parse content mixed completed and uncompleted")
    void parseContent_mixedCompletedAndUncompleted() {
        String content = "- [ ] Uncompleted\n- [x] Completed 1\n* [ ] Uncompleted 2\n* [X] Completed 2\n1. [ ] Uncompleted 3\n2. [x] Completed 3\n";
        TodoReport report = parser.parseContent(content, "test.md");

        assertEquals(6, report.getTotalCount(), "parser should count total tasks for mixed completion");
        assertEquals(3, report.getUncompletedCount(), "parser should count open tasks for mixed completion");
        assertEquals(3, report.getCompletedCount(), "parser should count completed tasks for mixed completion");
    }

    // === File parsing ===

    @Test
    @DisplayName("Parser should parse reads file correctly")
    void parse_readsFileCorrectly() throws IOException {
        File file = createTodoFile("- [ ] File task\n- [x] Done task");
        TodoReport report = parser.parse(file);

        assertEquals(2, report.getTotalCount(), "parser should read total count from file");
        assertEquals(1, report.getUncompletedCount(), "parser should read uncompleted count from file");
        assertEquals(1, report.getCompletedCount(), "parser should read completed count from file");
        assertTrue(report.getFilesProcessed().get(0).endsWith("TODO.md"),
                "parser should record processed file path");
    }

    @Test
    @DisplayName("Parser should parse empty file returns empty report")
    void parse_emptyFile_returnsEmptyReport() throws IOException {
        File file = createTodoFile("");
        TodoReport report = parser.parse(file);

        assertEquals(0, report.getTotalCount(), "parser should return zero tasks for empty file");
        assertTrue(report.hasFilesProcessed(), "parser should record empty file as processed");
    }

    @Test
    @DisplayName("Parser should parse file not found throws exception")
    void parse_fileNotFound_throwsException() {
        File nonExistent = new File(tempDir.toFile(), "nonexistent.md");
        assertThrows(IOException.class, () -> parser.parse(nonExistent),
                "parser should throw when todo file is missing");
    }

    @Test
    @DisplayName("Parser should parse null file throws exception")
    void parse_nullFile_throwsException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null),
                "parser should throw when todo file is null");
    }

    // === Edge cases ===

    @Test
    @DisplayName("Parser should parse content task with empty text")
    void parseContent_taskWithEmptyText() {
        TodoReport report = parser.parseContent("- [ ] ", "test.md");

        assertEquals(1, report.getTotalCount(), "parser should count task even with empty text");
        assertEquals("", report.getUncompletedTasks().get(0).getText(), "parser should preserve empty task text");
    }

    @Test
    @DisplayName("Parser should parse content task with only priority tag")
    void parseContent_taskWithOnlyPriorityTag() {
        TodoReport report = parser.parseContent("- [ ] [P1]", "test.md");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertEquals(1, task.getPriority(), "parser should read priority from tag only");
        assertEquals("", task.getText(), "parser should return empty text when only tags");
    }

    @Test
    @DisplayName("Parser should parse content preserves non ascii characters")
    void parseContent_preservesNonAsciiCharacters() {
        String taskText = "caf\u00E9";
        TodoReport report = parser.parseContent("- [ ] " + taskText, "test.md");

        assertEquals(taskText,
                report.getUncompletedTasks().get(0).getText(), "parser should preserve non ascii task text");
    }

    @Test
    @DisplayName("Parser should parse content collapses multiple spaces")
    void parseContent_collapsesMultipleSpaces() {
        TodoReport report = parser.parseContent("- [ ] [P1]  [E:M]   Task  with   spaces", "test.md");

        assertEquals("Task with spaces", report.getUncompletedTasks().get(0).getText(),
                "parser should collapse repeated spaces in task text");
    }

    @Test
    @DisplayName("Parser should parse content null content throws exception")
    void parseContent_nullContent_throwsException() {
        assertThrows(NullPointerException.class, () -> parser.parseContent(null, "test.md"),
                "parser should throw when content is null");
    }

    @Test
    @DisplayName("Parser should parse content null source path throws exception")
    void parseContent_nullSourcePath_throwsException() {
        assertThrows(NullPointerException.class, () -> parser.parseContent("content", null),
                "parser should throw when source path is null");
    }

    @Test
    @DisplayName("Parser should constructor null context pattern throws exception")
    void constructor_nullContextPattern_throwsException() {
        assertThrows(NullPointerException.class, () -> new TodoParser(null),
                "parser should throw when context pattern is null");
    }

    // === AsciiDoc format detection ===

    @Test
    @DisplayName("Parser should detect format markdown by default")
    void detectFormat_markdownByDefault() {
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat("test.md"),
                "parser should default to markdown for md extension");
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat("notes.markdown"),
                "parser should default to markdown for markdown extension");
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat("TODO.md"),
                "parser should default to markdown for TODO.md");
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat("TODO.MARKDOWN"),
                "parser should default to markdown for upper case markdown extension");
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat("path/to/file.txt"),
                "parser should default to markdown for unknown extension");
        assertEquals(TodoParser.Format.MARKDOWN, TodoParser.detectFormat(null),
                "parser should default to markdown when path is null");
    }

    @Test
    @DisplayName("Parser should detect format asciidoc from extension")
    void detectFormat_asciidocFromExtension() {
        assertEquals(TodoParser.Format.ASCIIDOC, TodoParser.detectFormat("test.ad"),
                "parser should detect asciidoc format from ad extension");
        assertEquals(TodoParser.Format.ASCIIDOC, TodoParser.detectFormat("test.adoc"),
                "parser should detect asciidoc format from adoc extension");
        assertEquals(TodoParser.Format.ASCIIDOC, TodoParser.detectFormat("TODO.ADOC"),
                "parser should detect asciidoc format from upper case extension");
        assertEquals(TodoParser.Format.ASCIIDOC, TodoParser.detectFormat("path/to/file.asciidoc"),
                "parser should detect asciidoc format from asciidoc extension");
        assertEquals(TodoParser.Format.ASCIIDOC, TodoParser.detectFormat("test.AsciiDoc"),
                "parser should detect asciidoc format from mixed case");
    }

    // === AsciiDoc checkbox parsing ===

    @Test
    @DisplayName("Parser should parse content asciidoc asterisk checkbox uncompleted")
    void parseContent_asciidoc_asteriskCheckboxUncompleted() {
        TodoReport report = parser.parseContent("* [ ] AsciiDoc task", "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asciidoc asterisk");
        assertEquals("AsciiDoc task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for asciidoc asterisk");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc asterisk checkbox completed x")
    void parseContent_asciidoc_asteriskCheckboxCompleted_x() {
        TodoReport report = parser.parseContent("* [x] Done task", "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asciidoc done x");
        assertEquals(1, report.getCompletedCount(), "parser should mark asciidoc x checkbox completed");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc asterisk checkbox completed star")
    void parseContent_asciidoc_asteriskCheckboxCompleted_star() {
        // AsciiDoc uses [*] as alternative completed marker
        TodoReport report = parser.parseContent("* [*] Done task", "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asciidoc done star");
        assertEquals(1, report.getCompletedCount(), "parser should mark asciidoc star checkbox completed");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc nested asterisk checkbox")
    void parseContent_asciidoc_nestedAsteriskCheckbox() {
        String content = "* [ ] Level 1\n** [ ] Level 2\n*** [ ] Level 3";
        TodoReport report = parser.parseContent(content, "test.adoc");

        assertEquals(3, report.getTotalCount(), "parser should count tasks in nested asciidoc bullets");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc dash checkbox uncompleted")
    void parseContent_asciidoc_dashCheckboxUncompleted() {
        TodoReport report = parser.parseContent("- [ ] Dash task", "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one task for asciidoc dash");
        assertEquals("Dash task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture task text for asciidoc dash");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc dropped task")
    void parseContent_asciidoc_droppedTask() {
        TodoReport report = parser.parseContent("* [-] Dropped task", "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one dropped task for asciidoc");
        assertEquals(1, report.getCompletedCount(),
                "parser should treat dropped asciidoc task as completed"); // Dropped counts as completed
    }

    // === AsciiDoc context extraction ===

    @Test
    @DisplayName("Parser should parse content asciidoc captures context from header")
    void parseContent_asciidoc_capturesContextFromHeader() {
        // AsciiDoc uses = for headers (== is level 2, like ## in Markdown)
        TodoParser adocParser = new TodoParser("^==\\s+.*");
        String content = "== Phase 1: Setup\n\n* [ ] First task";
        TodoReport report = adocParser.parseContent(content, "test.adoc");

        assertEquals(1, report.getTotalCount(), "parser should count one asciidoc task under header");
        assertEquals("Phase 1: Setup", report.getUncompletedTasks().get(0).getContext(),
                "parser should capture asciidoc context from header");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc context updates with new header")
    void parseContent_asciidoc_contextUpdatesWithNewHeader() {
        TodoParser adocParser = new TodoParser("^==\\s+.*");
        String content = "== Phase 1\n* [ ] Task 1\n== Phase 2\n* [ ] Task 2\n";
        TodoReport report = adocParser.parseContent(content, "test.adoc");

        List<TodoTask> tasks = report.getUncompletedTasks();
        assertEquals("Phase 1", tasks.get(0).getContext(), "parser should set asciidoc context from first header");
        assertEquals("Phase 2", tasks.get(1).getContext(), "parser should update asciidoc context for second header");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc ignores h1 headers")
    void parseContent_asciidoc_ignoresH1Headers() {
        TodoParser adocParser = new TodoParser("^==\\s+.*");
        String content = "= Main Title\n* [ ] Task without context\n";
        TodoReport report = adocParser.parseContent(content, "test.adoc");

        assertNull(report.getUncompletedTasks().get(0).getContext(),
                "parser should not set context from asciidoc H1");
    }

    // === AsciiDoc mixed content ===

    @Test
    @DisplayName("Parser should parse content asciidoc mixed bullet styles")
    void parseContent_asciidoc_mixedBulletStyles() {
        String content = "* [ ] Asterisk task\n** [ ] Nested task\n- [ ] Dash task\n";
        TodoReport report = parser.parseContent(content, "test.adoc");

        assertEquals(3, report.getTotalCount(), "parser should count tasks for mixed asciidoc bullets");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc mixed states")
    void parseContent_asciidoc_mixedStates() {
        String content = "* [ ] Open\n* [x] Done\n* [*] Also done\n* [-] Dropped\n";
        TodoReport report = parser.parseContent(content, "test.adoc");

        assertEquals(4, report.getTotalCount(), "parser should count four tasks for asciidoc states");
        assertEquals(3, report.getCompletedCount(),
                "parser should count completed tasks for asciidoc states"); // x, *, - are all completed
        assertEquals(1, report.getUncompletedCount(), "parser should count open tasks for asciidoc states");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc with priority and effort")
    void parseContent_asciidoc_withPriorityAndEffort() {
        TodoReport report = parser.parseContent("* [ ] [P1] [E:L] Complex task", "test.adoc");

        TodoTask task = report.getUncompletedTasks().get(0);
        assertEquals(1, task.getPriority(), "parser should read priority from asciidoc task tags");
        assertEquals('L', task.getEffort(), "parser should read effort from asciidoc task tags");
        assertEquals("Complex task", task.getText(), "parser should strip asciidoc task tags from text");
    }

    // === File parsing with format detection ===

    @Test
    @DisplayName("Parser should parse asciidoc file detects format")
    void parse_asciidocFile_detectsFormat() throws IOException {
        Path path = tempDir.resolve("TODO.adoc");
        Files.write(path, "* [ ] AsciiDoc file task\n* [*] Done task"
                .getBytes(StandardCharsets.UTF_8));

        TodoReport report = parser.parse(path.toFile());

        assertEquals(2, report.getTotalCount(), "parser should count total tasks in asciidoc file");
        assertEquals(1, report.getUncompletedCount(), "parser should count open tasks in asciidoc file");
        assertEquals(1, report.getCompletedCount(), "parser should count completed tasks in asciidoc file");
    }

    @Test
    @DisplayName("Parser should parse asciidoc file with ad extension")
    void parse_asciidocFile_adExtension() throws IOException {
        Path path = tempDir.resolve("TODO.ad");
        Files.write(path, "* [ ] Short extension task\n* [x] Done task"
                .getBytes(StandardCharsets.UTF_8));

        TodoReport report = parser.parse(path.toFile());

        assertEquals(2, report.getTotalCount(),
                "parser should count tasks in asciidoc ad extension file");
        assertEquals(1, report.getUncompletedCount(),
                "parser should count open tasks in asciidoc ad extension file");
        assertEquals(1, report.getCompletedCount(),
                "parser should count completed tasks in asciidoc ad extension file");
    }

    @Test
    @DisplayName("Parser should parse markdown file uses markdown patterns")
    void parse_markdownFile_usesMarkdownPatterns() throws IOException {
        // Verify that .md files still use Markdown patterns
        File file = createTodoFile("- [ ] Markdown task\n* [ ] Asterisk task");
        TodoReport report = parser.parse(file);

        assertEquals(2, report.getTotalCount(), "parser should count tasks using markdown patterns");
    }

    @Test
    @DisplayName("Parser should parse markdown file with markdown extension")
    void parse_markdownFile_markdownExtension() throws IOException {
        Path path = tempDir.resolve("TODO.markdown");
        Files.write(path, "- [ ] Markdown extension task\n- [ ] Second task"
                .getBytes(StandardCharsets.UTF_8));

        TodoReport report = parser.parse(path.toFile());

        assertEquals(2, report.getTotalCount(),
                "parser should count tasks in markdown extension file");
    }

    // === Helper methods ===

    @Test
    @DisplayName("Parser should extract priority finds priority")
    void extractPriority_findsPriority() {
        assertEquals(1, parser.extractPriority("[P1] Task"), "parser should parse P1 priority from tag");
        assertEquals(2, parser.extractPriority("Task [P2] here"), "parser should parse P2 priority from tag");
        assertEquals(3, parser.extractPriority("Task [P3]"), "parser should parse P3 priority from tag");
    }

    @Test
    @DisplayName("Parser should extract priority returns null when not found")
    void extractPriority_returnsNullWhenNotFound() {
        assertNull(parser.extractPriority("No priority here"), "parser should return null when priority is missing");
        assertNull(parser.extractPriority("[P4] Invalid priority"), "parser should return null for invalid priority tag");
        assertNull(parser.extractPriority("[P0] Invalid priority"), "parser should return null for zero priority tag");
    }

    @Test
    @DisplayName("Parser should extract effort finds effort")
    void extractEffort_findsEffort() {
        assertEquals('S', parser.extractEffort("[E:S] Task"), "parser should parse small effort from tag");
        assertEquals('M', parser.extractEffort("Task [E:M] here"), "parser should parse medium effort from tag");
        assertEquals('L', parser.extractEffort("Task [E:L]"), "parser should parse large effort from tag");
    }

    @Test
    @DisplayName("Parser should extract effort returns null when not found")
    void extractEffort_returnsNullWhenNotFound() {
        assertNull(parser.extractEffort("No effort here"), "parser should return null when effort is missing");
        assertNull(parser.extractEffort("[E:X] Invalid effort"), "parser should return null for invalid effort tag");
    }

    @Test
    @DisplayName("Parser should clean task text removes tags and trims")
    void cleanTaskText_removesTagsAndTrims() {
        assertEquals("Task", parser.cleanTaskText("[P1] Task"), "parser should remove priority tag from task text");
        assertEquals("Task", parser.cleanTaskText("[P1] [E:M] Task"), "parser should remove priority and effort tags");
        assertEquals("Task", parser.cleanTaskText("Task [P2]"), "parser should remove trailing priority tag");
        assertEquals("Do something", parser.cleanTaskText("[P1]  Do   something  [E:L]"),
                "parser should trim and collapse spaced task text");
    }

    // === Format-specific parsing with explicit format ===

    @Test
    @DisplayName("Parser should parse content explicit markdown format uses markdown patterns")
    void parseContent_explicitMarkdownFormat_usesMarkdownPatterns() {
        String content = "## Header\n- [ ] Task";
        TodoReport report = parser.parseContent(content, "test.txt", TodoParser.Format.MARKDOWN);

        assertEquals(1, report.getTotalCount(), "parser should count task in explicit markdown format");
        assertEquals("Header", report.getUncompletedTasks().get(0).getContext(),
                "parser should capture markdown context with explicit format");
    }

    @Test
    @DisplayName("Parser should parse content explicit asciidoc format uses asciidoc patterns")
    void parseContent_explicitAsciidocFormat_usesAsciidocPatterns() {
        TodoParser adocParser = new TodoParser("^==\\s+.*");
        String content = "== Header\n* [ ] Task";
        TodoReport report = adocParser.parseContent(content, "test.txt", TodoParser.Format.ASCIIDOC);

        assertEquals(1, report.getTotalCount(), "parser should count task in explicit asciidoc format");
        assertEquals("Header", report.getUncompletedTasks().get(0).getContext(),
                "parser should capture asciidoc context with explicit format");
    }

    @Test
    @DisplayName("Parser should parse content null format throws exception")
    void parseContent_nullFormat_throwsException() {
        assertThrows(NullPointerException.class,
                () -> parser.parseContent("- [ ] Task", "test.md", null),
                "parser should throw when format argument is null");
    }

    // === Individual checkbox marker tests ===

    @Test
    @DisplayName("Parser should parse content checkbox marker space is uncompleted")
    void parseContent_checkboxMarkerSpace_isUncompleted() {
        TodoReport report = parser.parseContent("- [ ] Task", "test.md");
        assertEquals(1, report.getUncompletedCount(), "parser should treat space checkbox marker as open");
    }

    @Test
    @DisplayName("Parser should parse content checkbox marker lowercase x is completed")
    void parseContent_checkboxMarkerLowercaseX_isCompleted() {
        TodoReport report = parser.parseContent("- [x] Task", "test.md");
        assertEquals(1, report.getCompletedCount(), "parser should treat lowercase x as completed marker");
    }

    @Test
    @DisplayName("Parser should parse content checkbox marker uppercase x is completed")
    void parseContent_checkboxMarkerUppercaseX_isCompleted() {
        TodoReport report = parser.parseContent("- [X] Task", "test.md");
        assertEquals(1, report.getCompletedCount(), "parser should treat uppercase X as completed marker");
    }

    @Test
    @DisplayName("Parser should parse content checkbox marker dash is completed")
    void parseContent_checkboxMarkerDash_isCompleted() {
        TodoReport report = parser.parseContent("- [-] Task", "test.md");
        assertEquals(1, report.getCompletedCount(), "parser should treat dash marker as dropped completed");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc checkbox marker asterisk is completed")
    void parseContent_asciidoc_checkboxMarkerAsterisk_isCompleted() {
        TodoReport report = parser.parseContent("* [*] Task", "test.adoc");
        assertEquals(1, report.getCompletedCount(), "parser should treat asterisk marker as completed");
    }

    // === AsciiDoc specific branch coverage ===

    @Test
    @DisplayName("Parser should parse content asciidoc dash bullet only")
    void parseContent_asciidoc_dashBulletOnly() {
        // Test that dash bullet in AsciiDoc is recognized separately from asterisk
        TodoReport report = parser.parseContent("- [ ] Dash only task", "test.adoc");
        assertEquals(1, report.getTotalCount(), "parser should count dash bullet task in asciidoc");
        assertEquals("Dash only task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture dash bullet task text");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc asterisk bullet only")
    void parseContent_asciidoc_asteriskBulletOnly() {
        // Test asterisk bullet separately
        TodoReport report = parser.parseContent("* [ ] Asterisk only task", "test.adoc");
        assertEquals(1, report.getTotalCount(), "parser should count asterisk bullet task in asciidoc");
        assertEquals("Asterisk only task", report.getUncompletedTasks().get(0).getText(),
                "parser should capture asterisk bullet task text");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc no matching tasks")
    void parseContent_asciidoc_noMatchingTasks() {
        // Test when no tasks match AsciiDoc patterns
        TodoReport report = parser.parseContent("Regular text\nMore text", "test.adoc");
        assertEquals(0, report.getTotalCount(), "parser should return zero tasks for non matching text");
    }

    @Test
    @DisplayName("Parser should parse content markdown header not matching context pattern")
    void parseContent_markdown_headerNotMatchingContextPattern() {
        // Header that doesn't match the context pattern (# vs ##)
        String content = "# H1 Header\n- [ ] Task";
        TodoReport report = parser.parseContent(content, "test.md");
        assertEquals(1, report.getTotalCount(), "parser should parse task when header pattern mismatches");
        assertNull(report.getUncompletedTasks().get(0).getContext(),
                "parser should leave context null for mismatched markdown header");
    }

    @Test
    @DisplayName("Parser should parse content asciidoc header not matching context pattern")
    void parseContent_asciidoc_headerNotMatchingContextPattern() {
        // AsciiDoc header that doesn't match pattern (= vs ==)
        TodoParser adocParser = new TodoParser("^==\\s+.*");
        String content = "= H1 Header\n* [ ] Task";
        TodoReport report = adocParser.parseContent(content, "test.adoc");
        assertEquals(1, report.getTotalCount(), "parser should parse task when asciidoc header mismatches");
        assertNull(report.getUncompletedTasks().get(0).getContext(),
                "parser should leave context null for mismatched asciidoc header");
    }

    // === File helper ===

    private File createTodoFile(String content) throws IOException {
        Path path = tempDir.resolve("TODO.md");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }
}
