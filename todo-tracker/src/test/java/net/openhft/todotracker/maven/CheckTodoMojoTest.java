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
package net.openhft.todotracker.maven;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Todo check mojo validates Maven plugin execution path")
class CheckTodoMojoTest {

    @TempDir
    Path tempDir;
    @Mock
    private MavenProject project;
    @Mock
    private Log log;
    @Captor
    private ArgumentCaptor<CharSequence> warnCaptor;
    private CheckTodoMojo mojo;

    @BeforeEach
    void setUp() {
        mojo = new CheckTodoMojo();
        mojo.setProject(project);
        mojo.setLog(log);
        mojo.setContextPattern("^##\\s+.*");  // Set default context pattern
        mojo.setAsciidocContextPattern("^==\\s+.*");  // Set default AsciiDoc context pattern
        mojo.setFailOnIncomplete(true);  // Set default fail on incomplete
        mojo.setShowContext(true);  // Set default show context
        mojo.setUsePatterns(true);  // Enable pattern matching (default in Maven)
        lenient().when(project.getBasedir()).thenReturn(tempDir.toFile());
    }

    @Test
    @DisplayName("Mojo should execute no todo files passes with message")
    void execute_noTodoFiles_passesWithMessage() {
        // No TODO files exist
        assertDoesNotThrow(mojo::execute, "mojo should not throw when no todo files exist");
    }

    @Test
    @DisplayName("Mojo should execute all tasks completed passes")
    void execute_allTasksCompleted_passes() throws Exception {
        createFile("TODO.md", "# Tasks\n- [x] Completed task 1\n- [x] Completed task 2\n");

        assertDoesNotThrow(mojo::execute, "mojo should not throw when all tasks completed");
    }

    @Test
    @DisplayName("Mojo should execute uncompleted tasks fails by default")
    void execute_uncompletedTasks_failsByDefault() throws Exception {
        createFile("TODO.md", "# Tasks\n- [ ] Uncompleted task\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when uncompleted task exists by default");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) by default");
    }

    @Test
    @DisplayName("Mojo should execute uncompleted tasks warns when fail on incomplete false")
    void execute_uncompletedTasks_warnsWhenFailOnIncompleteFalse() throws Exception {
        createFile("TODO.md", "# Tasks\n- [ ] Uncompleted task\n");
        mojo.setFailOnIncomplete(false);

        assertDoesNotThrow(mojo::execute, "mojo should not throw when failOnIncomplete is false");
    }

    @Test
    @DisplayName("Mojo should skip execution without checking tasks")
    void execute_skip_doesNotCheck() throws Exception {
        createFile("TODO.md", "# Tasks\n- [ ] Uncompleted task\n");
        mojo.setSkip(true);

        assertDoesNotThrow(mojo::execute, "mojo should not throw when mojo is skipped");
    }

    @Test
    @DisplayName("Mojo should execute todo in subdirectory is found")
    void execute_todoInSubdirectory_isFound() throws Exception {
        Files.createDirectories(tempDir.resolve("todo"));
        createFile("todo/TODO.md", "# Tasks\n- [ ] Task in todo folder\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when todo file in subdirectory");

        assertTrue(exception.getMessage().contains("uncompleted task(s)"),
                "mojo should report uncompleted task(s) for subdirectory file");
    }

    @Test
    @DisplayName("Mojo should execute custom todo files uses specified paths")
    void execute_customTodoFiles_usesSpecifiedPaths() throws Exception {
        createFile("TASKS.md", "# Custom Tasks\n- [ ] Custom task\n");
        mojo.setTodoFiles(Collections.singletonList("TASKS.md"));

        assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should fail when custom todo file has open tasks");
    }

    @Test
    @DisplayName("Mojo should execute custom todo files ignores default")
    void execute_customTodoFiles_ignoresDefault() throws Exception {
        createFile("TODO.md", "# Default Tasks\n- [ ] Should be ignored\n");
        createFile("TASKS.md", "# Custom Tasks\n- [x] All done\n");
        mojo.setTodoFiles(Collections.singletonList("TASKS.md"));

        // Should pass because we're only checking TASKS.md which has no uncompleted tasks
        assertDoesNotThrow(mojo::execute, "mojo should ignore default files when custom list set");
    }

    @Test
    @DisplayName("Mojo should execute multiple files with mixed tasks reports all")
    void execute_multipleFilesWithMixedTasks_reportsAll() throws Exception {
        createFile("TODO.md", "- [ ] Task 1\n- [x] Task 2\n");
        Files.createDirectories(tempDir.resolve("todo"));
        createFile("todo/TODO.md", "- [ ] Task 3\n- [ ] Task 4\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when multiple files have open tasks");

        assertTrue(exception.getMessage().contains("3 uncompleted task(s)"),
                "mojo should report 3 uncompleted task(s) across files");
    }

    @Test
    @DisplayName("Mojo should execute max tasks configuration is respected")
    void execute_maxTasksConfiguration_isRespected() throws Exception {
        createFile("TODO.md", "- [ ] Task 1\n- [ ] Task 2\n- [ ] Task 3\n");
        mojo.setMaxTasks(2);

        // Should still fail, maxTasks only affects display
        assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should still fail even with maxTasks set");
        List<String> warnings = warnLines();
        assertTrue(warnings.stream().anyMatch(line -> line.contains("more task(s)")),
                "mojo should report truncated task list when maxTasks is set");
    }

    @Test
    @DisplayName("Mojo should execute show context configuration is respected")
    void execute_showContextConfiguration_isRespected() throws Exception {
        createFile("TODO.md", "## Phase 1\n- [ ] Task with context\n");
        mojo.setShowContext(false);

        // Should still fail, showContext only affects display
        assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should still fail even when context hidden");
        List<String> warnings = warnLines();
        assertTrue(warnings.stream().noneMatch(line -> line.contains("Context:")),
                "mojo should omit context lines when showContext is false");
    }

    @Test
    @DisplayName("Mojo should execute custom context pattern is respected")
    void execute_customContextPattern_isRespected() throws Exception {
        createFile("TODO.md", "### Custom Header\n- [ ] Task\n");
        mojo.setContextPattern("^###\\s+.*");

        assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should fail when custom context pattern used");
        List<String> warnings = warnLines();
        assertTrue(warnings.stream().anyMatch(line -> line.contains("Context: Custom Header")),
                "mojo should include custom header in context output");
    }

    @Test
    @DisplayName("Mojo should execute all bullet styles are recognized")
    void execute_allBulletStyles_areRecognized() throws Exception {
        createFile("TODO.md", "- [ ] Dash task\n* [ ] Asterisk task\n1. [ ] Numbered task\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when tasks found for all bullet styles");

        assertTrue(exception.getMessage().contains("3 uncompleted task(s)"),
                "mojo should report 3 uncompleted task(s) from bullet styles");
    }

    @Test
    @DisplayName("Mojo should execute priority and effort are recognized")
    void execute_priorityAndEffort_areRecognized() throws Exception {
        createFile("TODO.md", "- [ ] [P1] [E:M] High priority medium effort\n- [ ] [P2] [E:L] Medium priority large effort\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when tasks include priority and effort");

        assertTrue(exception.getMessage().contains("2 uncompleted task(s)"),
                "mojo should report 2 uncompleted task(s) with tags");
    }

    @Test
    @DisplayName("Mojo should execute empty todo file passes")
    void execute_emptyTodoFile_passes() throws Exception {
        createFile("TODO.md", "");

        assertDoesNotThrow(mojo::execute, "mojo should not throw for empty todo file");
    }

    @Test
    @DisplayName("Mojo should execute todo file with only headers passes")
    void execute_todoFileWithOnlyHeaders_passes() throws Exception {
        createFile("TODO.md", "# Main Header\n## Section 1\n### Subsection\n");

        assertDoesNotThrow(mojo::execute, "mojo should not throw for headers only file");
    }

    @Test
    @DisplayName("Mojo should execute mixed completed and uncompleted reports only uncompleted")
    void execute_mixedCompletedAndUncompleted_reportsOnlyUncompleted() throws Exception {
        createFile("TODO.md", "- [x] Completed 1\n- [ ] Uncompleted 1\n- [x] Completed 2\n- [ ] Uncompleted 2\n- [x] Completed 3\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when open tasks remain");

        assertTrue(exception.getMessage().contains("2 uncompleted task(s)"),
                "mojo should report 2 uncompleted task(s) only");
    }

    // === AsciiDoc format tests ===

    @Test
    @DisplayName("Mojo should execute asciidoc file parses correctly")
    void execute_asciidocFile_parsesCorrectly() throws Exception {
        // Create .adoc file with AsciiDoc checkbox syntax
        createFile("todo/sprint.adoc", "== Sprint Tasks\n* [ ] AsciiDoc uncompleted task\n* [*] AsciiDoc completed task\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when asciidoc file has open task");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) in asciidoc");
    }

    @Test
    @DisplayName("Mojo should execute asciidoc with context uses asciidoc pattern")
    void execute_asciidocWithContext_usesAsciidocPattern() throws Exception {
        // Create .adoc file - context should use == pattern not ## pattern
        createFile("todo/tasks.adoc", "== AsciiDoc Section\n* [ ] Task under AsciiDoc heading\n");

        // By default, the mojo uses ^==\s+.* for AsciiDoc context
        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when asciidoc context uses pattern");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) with asciidoc context");
        List<String> warnings = warnLines();
        assertTrue(warnings.stream().anyMatch(line -> line.contains("Context: AsciiDoc Section")),
                "mojo should include asciidoc header in context output");
    }

    @Test
    @DisplayName("Mojo should execute mixed markdown and asciidoc parse both formats")
    void execute_mixedMarkdownAndAsciidoc_parseBothFormats() throws Exception {
        createFile("TODO.md", "- [ ] Markdown task\n");
        createFile("todo/sprint.adoc", "* [ ] AsciiDoc task\n");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when mixed formats have open tasks");

        assertTrue(exception.getMessage().contains("2 uncompleted task(s)"),
                "mojo should report 2 uncompleted task(s) across formats");
    }

    // === Context exclusion tests ===

    @Test
    @DisplayName("Mojo should execute planned context excluded by default")
    void execute_plannedContextExcludedByDefault() throws Exception {
        // Tasks under "Planned" heading should NOT fail the build
        String content = "## Phase 1: Current\n- [ ] Current task\n## Phase 2: Future Enhancements (Planned)\n- [ ] Planned task\n";
        createFile("TODO.md", content);

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail only on current tasks when future excluded");

        // Only 1 task should cause failure (the current one, not the planned one)
        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) for current work");
    }

    @Test
    @DisplayName("Mojo should execute future context excluded by default")
    void execute_futureContextExcludedByDefault() throws Exception {
        // Tasks under "Future" heading should NOT fail the build
        String content = "## Current Work\n- [ ] Current task\n## Future Work\n- [ ] Future task\n";
        createFile("TODO.md", content);

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail only on current tasks by default");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) for future exclusion");
    }

    @Test
    @DisplayName("Mojo should execute all planned tasks passes")
    void execute_allPlannedTasks_passes() throws Exception {
        // If ALL tasks are under excluded contexts, build should pass
        String content = "## Future Enhancements (Planned)\n- [ ] Future task 1\n- [ ] Future task 2\n";
        createFile("TODO.md", content);

        assertDoesNotThrow(mojo::execute, "mojo should not throw when all tasks are planned");
    }

    @Test
    @DisplayName("Mojo should execute custom exclude patterns overrides defaults")
    void execute_customExcludePatterns_overridesDefaults() throws Exception {
        // Custom patterns should override defaults
        String content = "## Phase 1: Current\n- [ ] Task 1\n## Backlog\n- [ ] Task 2\n";
        createFile("TODO.md", content);

        // Set empty exclude patterns - should enforce all tasks
        mojo.setExcludeContextPatterns(java.util.Collections.emptyList());

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when exclusions are cleared");

        // Both tasks should cause failure when no exclusions
        assertTrue(exception.getMessage().contains("2 uncompleted task(s)"),
                "mojo should report 2 uncompleted task(s) with no exclusions");
    }

    // === Default patterns mode tests ===

    @Test
    @DisplayName("Mojo should execute default patterns mode finds tasks with pattern matching")
    void execute_defaultPatternsMode_findsTasksWithPatternMatching() throws Exception {
        // Don't set todoFiles - use default pattern matching
        mojo.setTodoFiles(null);
        createFile("TODO.md", "- [ ] Task found by pattern");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when patterns find open task");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) for pattern match");
    }

    @Test
    @DisplayName("Mojo should execute default patterns mode finds prefixed todo files")
    void execute_defaultPatternsMode_findsPrefixedTodoFiles() throws Exception {
        // Don't set todoFiles - use default pattern matching
        mojo.setTodoFiles(null);
        createFile("SPRINT-TODO.md", "- [ ] Sprint task");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when prefixed todo file found");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) for prefixed file");
    }

    @Test
    @DisplayName("Mojo should execute asciidoc file uses asciidoc parser")
    void execute_asciidocFile_usesAsciidocParser() throws Exception {
        // Create .adoc file - should use AsciiDoc parser with == context pattern
        mojo.setTodoFiles(null);
        createFile("todo/sprint.adoc", "== Sprint Tasks\n* [ ] AsciiDoc task\n* [*] Completed");

        MojoFailureException exception = assertThrows(MojoFailureException.class,
                mojo::execute, "mojo should fail when asciidoc parser finds open task");

        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) from asciidoc parser");
    }

    // === Legacy mode tests ===

    @Test
    @DisplayName("Mojo should execute legacy mode uses default exact paths")
    void execute_legacyMode_usesDefaultExactPaths() throws Exception {
        // Disable pattern matching, don't set custom paths -> legacy mode
        mojo.setUsePatterns(false);
        mojo.setTodoFiles(null);

        // Create file at default location
        createFile("TODO.md", "- [ ] Legacy mode task");

        MojoFailureException exception = assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should fail when legacy mode finds open tasks");
        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) in legacy mode");
    }

    @Test
    @DisplayName("Mojo should execute legacy mode ignores pattern matched files")
    void execute_legacyMode_ignoresPatternMatchedFiles() throws Exception {
        mojo.setUsePatterns(false);
        mojo.setTodoFiles(null);

        // Create file that would match pattern but not default exact path
        createFile("SPRINT-TODO.md", "- [ ] Should be ignored in legacy mode");

        // Should pass - file not at default exact path
        assertDoesNotThrow(mojo::execute, "mojo should ignore pattern matched files in legacy mode");
    }

    @Test
    @DisplayName("Mojo should execute legacy mode finds todo in subdir")
    void execute_legacyMode_findsTodoInSubdir() throws Exception {
        mojo.setUsePatterns(false);
        mojo.setTodoFiles(null);

        // Default legacy paths include todo/TODO.md
        Files.createDirectories(tempDir.resolve("todo"));
        createFile("todo/TODO.md", "- [ ] Task in todo subfolder");

        MojoFailureException exception = assertThrows(MojoFailureException.class, mojo::execute,
                "mojo should fail when legacy mode finds subdir todo task");
        assertTrue(exception.getMessage().contains("1 uncompleted task(s)"),
                "mojo should report 1 uncompleted task(s) in legacy subdir");
    }

    private void createFile(String relativePath, String content) throws IOException {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private List<String> warnLines() {
        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        return warnCaptor.getAllValues().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
