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
package net.openhft.todotracker.locator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Locator should cover todo file discovery scenarios")
class TodoFileLocatorTest {

    @TempDir
    Path tempDir;
    private TodoFileLocator locator;

    @BeforeEach
    void setUp() {
        locator = new TodoFileLocator();
    }

    @Test
    @DisplayName("Locator should find todo files finds root todo md")
    void findTodoFiles_findsRootTodoMd() throws IOException {
        createFile("TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(), "check should locate one root TODO file");
        assertEquals("TODO.md", found.get(0).getName(), "check should return TODO.md filename for root file");
    }

    @Test
    @DisplayName("Locator should find todo files finds todo in subdirectory")
    void findTodoFiles_findsTodoInSubdirectory() throws IOException {
        createFile("todo/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(), "check should locate one todo file in subdirectory");
        assertTrue(found.get(0).getPath().contains("todo"), "check should include todo path segment for subdirectory match");
    }

    @Test
    @DisplayName("Locator should find todo files finds both locations")
    void findTodoFiles_findsBothLocations() throws IOException {
        createFile("TODO.md");
        createFile("todo/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(2, found.size(), "check should locate todo files in root and subdirectory");
    }

    @Test
    @DisplayName("Locator should find todo files empty directory returns empty list")
    void findTodoFiles_emptyDirectory_returnsEmptyList() {
        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.isEmpty(),
                "check should return empty list for empty directory because no matches exist");
    }

    @Test
    @DisplayName("Locator should find todo files no matching files returns empty list")
    void findTodoFiles_noMatchingFiles_returnsEmptyList() throws IOException {
        createFile("README.md");
        createFile("other.txt");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.isEmpty(),
                "check should return empty list when no todo files exist because there is nothing to scan");
    }

    @Test
    @DisplayName("Locator should find todo files null basedir throws exception")
    void findTodoFiles_nullBasedir_throwsException() {
        assertThrows(NullPointerException.class, () -> locator.findTodoFiles(null),
                "check should reject null base directory for search because traversal needs a root");
    }

    @Test
    @DisplayName("Locator should find todo files directory named todo md is not included")
    void findTodoFiles_directoryNamedTodoMd_isNotIncluded() throws IOException {
        // Create a directory named TODO.md (edge case)
        Files.createDirectories(tempDir.resolve("TODO.md"));

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.isEmpty(),
                "check should ignore directories named like TODO.md because directories are not files");
    }

    @Test
    @DisplayName("Locator should constructor with custom paths")
    void constructor_withCustomPaths() throws IOException {
        createFile("custom/TASKS.md");
        TodoFileLocator customLocator = new TodoFileLocator(
                Collections.singletonList("custom/TASKS.md"));

        List<File> found = customLocator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(), "check should locate one file from custom paths");
        assertEquals("TASKS.md", found.get(0).getName(), "check should return TASKS.md filename for custom path");
    }

    @Test
    @DisplayName("Locator should allow constructor with empty custom path list")
    void constructor_withEmptyList() {
        TodoFileLocator emptyLocator = new TodoFileLocator(Collections.emptyList());

        List<File> found = emptyLocator.findTodoFiles(tempDir.toFile());

        assertTrue(found.isEmpty(), "check should return empty list for empty path config");
    }

    @Test
    @DisplayName("Locator should constructor with null list throws exception")
    void constructor_withNullList_throwsException() {
        assertThrows(NullPointerException.class, () -> new TodoFileLocator(null),
                "check should throw when custom paths list is null");
    }

    @Test
    @DisplayName("Locator should get todo file paths returns configured paths")
    void getTodoFilePaths_returnsConfiguredPaths() {
        List<String> paths = locator.getTodoFilePaths();

        assertEquals(4, paths.size(), "check should expose four configured todo paths");
        String rootTodo = "TODO.md";
        assertTrue(paths.contains(rootTodo),
                "check should include root path " + rootTodo + " in " + paths);
        String subTodo = "todo/TODO.md";
        assertTrue(paths.contains(subTodo),
                "check should include todo subdirectory path " + subTodo + " in " + paths);
        String rootMarkdown = "TODO.markdown";
        assertTrue(paths.contains(rootMarkdown),
                "check should include root markdown path " + rootMarkdown + " in " + paths);
        String subMarkdown = "todo/TODO.markdown";
        assertTrue(paths.contains(subMarkdown),
                "check should include todo markdown path " + subMarkdown + " in " + paths);
    }

    @Test
    @DisplayName("Locator should get todo file paths returns copy")
    void getTodoFilePaths_returnsCopy() {
        List<String> paths = locator.getTodoFilePaths();
        paths.clear();

        // Original should not be affected
        assertEquals(4, locator.getTodoFilePaths().size(),
                "check should return a defensive copy of paths because callers may mutate the list");
    }

    @Test
    @DisplayName("Locator should is todo file matches todo md")
    void isTodoFile_matchesTodoMd() throws IOException {
        File file = createFile("TODO.md");
        assertTrue(locator.isTodoFile(file), "check should treat TODO.md as todo file");
    }

    @Test
    @DisplayName("Locator should treat TODO markdown file as todo")
    void isTodoFile_matchesTodoMarkdown() throws IOException {
        File file = createFile("TODO.markdown");
        assertTrue(locator.isTodoFile(file), "check should treat TODO.markdown as todo file");
    }

    @Test
    @DisplayName("Locator should is todo file matches todos md")
    void isTodoFile_matchesTodosMd() throws IOException {
        File file = createFile("TODOS.md");
        assertTrue(locator.isTodoFile(file), "check should treat TODOS.md as todo file");
    }

    @Test
    @DisplayName("Locator should is todo file case insensitive")
    void isTodoFile_caseInsensitive() throws IOException {
        File lower = createFile("todo.md");
        File upper = createFile("sub/TODO.MD");

        assertTrue(locator.isTodoFile(lower), "check should accept lower case todo.md filename");
        assertTrue(locator.isTodoFile(upper), "check should accept upper case TODO.MD filename");
    }

    @Test
    @DisplayName("Locator should is todo file rejects non todo files")
    void isTodoFile_rejectsNonTodoFiles() throws IOException {
        File readme = createFile("README.md");
        File other = createFile("other.txt");

        assertFalse(locator.isTodoFile(readme), "check should reject README.md as todo file");
        assertFalse(locator.isTodoFile(other), "check should reject other.txt as todo file");
    }

    @Test
    @DisplayName("Locator should treat todo directory markdown file as todo")
    void isTodoFile_matchesTodoDirectoryMarkdownFile() throws IOException {
        File backlog = createFile("todo/backlog.markdown");

        assertTrue(locator.isTodoFile(backlog),
                "check should treat markdown files under todo directory as todo files");
    }

    @Test
    @DisplayName("Locator should reject non markdown or asciidoc file under todo directory")
    void isTodoFile_rejectsNonMarkdownUnderTodoDirectory() throws IOException {
        File notes = createFile("todo/notes.txt");

        assertFalse(locator.isTodoFile(notes),
                "check should reject non markdown/asciidoc files under todo directory");
    }

    @Test
    @DisplayName("Locator should reject null file in todo detection")
    void isTodoFile_rejectsNull() {
        assertFalse(locator.isTodoFile(null),
                "check should return false for null file reference because no path is available");
    }

    @Test
    @DisplayName("Locator should is todo file rejects directory")
    void isTodoFile_rejectsDirectory() throws IOException {
        Path dir = tempDir.resolve("todo.md");
        Files.createDirectories(dir);

        assertFalse(locator.isTodoFile(dir.toFile()), "check should return false for todo named directory");
    }

    @Test
    @DisplayName("Locator should get default todo files returns defaults")
    void getDefaultTodoFiles_returnsDefaults() {
        List<String> defaults = TodoFileLocator.getDefaultTodoFiles();

        String rootTodo = "TODO.md";
        assertTrue(defaults.contains(rootTodo),
                "default list should include root path " + rootTodo + " in " + defaults);
        String subTodo = "todo/TODO.md";
        assertTrue(defaults.contains(subTodo),
                "default list should include subdirectory path " + subTodo + " in " + defaults);
        String rootMarkdown = "TODO.markdown";
        assertTrue(defaults.contains(rootMarkdown),
                "default list should include root markdown path " + rootMarkdown + " in " + defaults);
        String subMarkdown = "todo/TODO.markdown";
        assertTrue(defaults.contains(subMarkdown),
                "default list should include todo markdown path " + subMarkdown + " in " + defaults);
    }

    @Test
    @DisplayName("Locator should get default todo files returns copy")
    void getDefaultTodoFiles_returnsCopy() {
        List<String> defaults = TodoFileLocator.getDefaultTodoFiles();
        defaults.clear();

        // Original should not be affected
        assertFalse(TodoFileLocator.getDefaultTodoFiles().isEmpty(), "default list should be a defensive copy");
    }

    @Test
    @DisplayName("Locator should find todo files multiple custom paths")
    void findTodoFiles_multipleCustomPaths() throws IOException {
        createFile("docs/TODO.md");
        createFile("plans/TASKS.md");
        createFile("other/README.md");

        TodoFileLocator customLocator = new TodoFileLocator(
                Arrays.asList("docs/TODO.md", "plans/TASKS.md", "other/MISSING.md"));

        List<File> found = customLocator.findTodoFiles(tempDir.toFile());

        assertEquals(2, found.size(), "check should resolve two matching custom paths");
    }

    // === Pattern matching tests ===

    @Test
    @DisplayName("Locator should find todo files pattern matching finds prefixed todo files")
    void findTodoFiles_patternMatching_findsPrefixedTodoFiles() throws IOException {
        createFile("SPRINT-TODO.md");
        createFile("BACKLOG-TODO.md");
        createFile("README.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(2, found.size(), "check should locate prefixed TODO files with patterns");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("SPRINT-TODO.md")),
                "check should include SPRINT-TODO.md entry in results");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("BACKLOG-TODO.md")),
                "check should include BACKLOG-TODO.md entry in results");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching finds todo in subdirectories")
    void findTodoFiles_patternMatching_findsTodoInSubdirectories() throws IOException {
        createFile("module1/TODO.md");
        createFile("module2/SPRINT-TODO.md");
        createFile("docs/todo/backlog.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 3, "check should locate todo files across subdirectories");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching case insensitive")
    void findTodoFiles_patternMatching_caseInsensitive() throws IOException {
        // Create files in different directories to avoid case-insensitive filesystem collisions
        createFile("lower/todo.md");
        createFile("upper/TODO.md");
        createFile("mixed/Todo.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        // All three should be found (in separate directories)
        assertTrue(found.size() >= 3, "check should find todo files regardless of case");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching finds todos with suffix")
    void findTodoFiles_patternMatching_findsTodosWithSuffix() throws IOException {
        // Create in different directories to avoid case-insensitive filesystem collisions
        createFile("upper/TODOS.md");
        createFile("lower/todos.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 2, "check should find TODOS.md files with suffix");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching finds markdown extension")
    void findTodoFiles_patternMatching_findsMarkdownExtension() throws IOException {
        createFile("TODO.markdown");
        createFile("upper/TODO.MARKDOWN");
        createFile("todo/backlog.markdown");
        createFile("todo/UPPER.MARKDOWN");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 4, "check should find todo files with markdown extension");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("TODO.markdown")),
                "check should include lower case TODO.markdown in pattern results");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("TODO.MARKDOWN")),
                "check should include upper case TODO.MARKDOWN in pattern results");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("backlog.markdown")),
                "check should include backlog.markdown from todo directory");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("UPPER.MARKDOWN")),
                "check should include UPPER.MARKDOWN from todo directory");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching finds files in todo directory")
    void findTodoFiles_patternMatching_findsFilesInTodoDirectory() throws IOException {
        createFile("todo/backlog.md");
        createFile("todo/sprint-1.md");
        createFile("todo/current.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 3, "check should find markdown files under todo directory");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("backlog.md")),
                "check should include backlog.md from todo directory");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching skips hidden directories")
    void findTodoFiles_patternMatching_skipsHiddenDirectories() throws IOException {
        createFile(".git/TODO.md");
        createFile(".hidden/TODO.md");
        createFile("visible/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(), "check should ignore hidden directories during search");
        assertTrue(found.get(0).getPath().contains("visible"), "check should keep visible directory matches");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching skips target directory")
    void findTodoFiles_patternMatching_skipsTargetDirectory() throws IOException {
        createFile("target/TODO.md");
        createFile("src/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(),
                "check should skip target directory when scanning because build output is excluded");
        assertTrue(found.get(0).getPath().contains("src"), "check should keep src directory matches");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching skips node modules")
    void findTodoFiles_patternMatching_skipsNodeModules() throws IOException {
        createFile("node_modules/package/TODO.md");
        createFile("app/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(),
                "check should skip node_modules directory matches because dependencies are out of scope");
        assertTrue(found.get(0).getPath().contains("app"), "check should keep app directory matches");
    }

    @Test
    @DisplayName("Locator should is using patterns default constructor returns true")
    void isUsingPatterns_defaultConstructor_returnsTrue() {
        assertTrue(locator.isUsingPatterns(), "default constructor should enable pattern mode");
    }

    @Test
    @DisplayName("Locator should is using patterns custom paths constructor returns false")
    void isUsingPatterns_customPathsConstructor_returnsFalse() {
        TodoFileLocator customLocator = new TodoFileLocator(
                Collections.singletonList("TODO.md"));
        assertFalse(customLocator.isUsingPatterns(), "custom path constructor should disable pattern mode");
    }

    @Test
    @DisplayName("Locator should get patterns default constructor returns patterns")
    void getPatterns_defaultConstructor_returnsPatterns() {
        List<String> patterns = locator.getPatterns();
        assertFalse(patterns.isEmpty(), "default patterns list should not be empty");
        assertTrue(patterns.stream().anyMatch(p -> p.contains("glob:")),
                "default patterns should include glob entries");
    }

    @Test
    @DisplayName("Default locator patterns include glob entries")
    void getDefaultPatterns_returnsPatterns() {
        List<String> patterns = TodoFileLocator.getDefaultPatterns();
        assertFalse(patterns.isEmpty(), "static default patterns should not be empty");
        assertTrue(patterns.stream().anyMatch(p -> p.contains("glob:")),
                "static default patterns should include glob entries");
    }

    @Test
    @DisplayName("Locator should is todo file matches prefixed todo")
    void isTodoFile_matchesPrefixedTodo() throws IOException {
        File file = createFile("SPRINT-TODO.md");
        assertTrue(locator.isTodoFile(file), "check should accept prefixed TODO file name");
    }

    @Test
    @DisplayName("Locator should is todo file matches mixed case")
    void isTodoFile_matchesMixedCase() throws IOException {
        File file = createFile("SpRiNt-ToDo.md");
        assertTrue(locator.isTodoFile(file), "check should accept mixed case TODO file name");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching deduplicates results")
    void findTodoFiles_patternMatching_deduplicatesResults() throws IOException {
        // Create a file that would match multiple patterns
        createFile("TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        // Should only appear once even if multiple patterns match
        long todoCount = found.stream()
                .filter(f -> f.getName().equals("TODO.md"))
                .count();
        assertEquals(1, todoCount,
                "check should deduplicate TODO.md matches because duplicate paths add noise");
    }

    // === AsciiDoc discovery tests ===

    @Test
    @DisplayName("Locator should find todo files finds adoc in todo directory")
    void findTodoFiles_findsAdocInTodoDirectory() throws IOException {
        createFile("todo/backlog.adoc");
        createFile("todo/sprint.ad");
        createFile("todo/notes.asciidoc");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 3, "check should find ad, adoc, and asciidoc files in todo directory");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("backlog.adoc")),
                "check should include backlog.adoc from todo directory");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("sprint.ad")),
                "check should include sprint.ad from todo directory");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("notes.asciidoc")),
                "check should include notes.asciidoc from todo directory");
    }

    @Test
    @DisplayName("Locator should find todo files finds adoc in nested todo directory")
    void findTodoFiles_findsAdocInNestedTodoDirectory() throws IOException {
        createFile("module1/todo/tasks.adoc");
        createFile("module2/todo/backlog.adoc");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 2, "check should find adoc files in nested todo directories");
    }

    @Test
    @DisplayName("Locator should find todo files finds plan adoc in docs")
    void findTodoFiles_findsPlanAdocInDocs() throws IOException {
        createFile("src/main/docs/release-plan.ad");
        createFile("src/main/docs/sprint-plan.adoc");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 2, "check should find plan adoc files under docs");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("release-plan.ad")),
                "check should include release plan ad file in docs");
    }

    @Test
    @DisplayName("Locator should find todo files finds todo adoc in docs")
    void findTodoFiles_findsTodoAdocInDocs() throws IOException {
        createFile("src/main/docs/project-todo.ad");
        createFile("src/main/docs/sprint-TODO.adoc");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 2, "check should find todo adoc files under docs");
    }

    @Test
    @DisplayName("Locator should find todo files finds adoc case insensitive")
    void findTodoFiles_findsAdocCaseInsensitive() throws IOException {
        // Create in different directories to avoid case collisions
        createFile("todo/lower.ad");
        createFile("nestedtodo/todo/upper.ASCIIDOC");
        createFile("src/main/docs/plan.AD");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertTrue(found.size() >= 3,
                "check should match ad and asciidoc extensions case insensitively");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("lower.ad")),
                "check should include lower.ad when matching case insensitive");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("upper.ASCIIDOC")),
                "check should include upper.ASCIIDOC when matching case insensitive");
        assertTrue(found.stream().anyMatch(f -> f.getName().equals("plan.AD")),
                "check should include plan.AD in docs when matching case insensitive");
    }

    @Test
    @DisplayName("Locator should find todo files mixed markdown and asciidoc")
    void findTodoFiles_mixedMarkdownAndAsciidoc() throws IOException {
        createFile("TODO.md");
        createFile("todo/backlog.md");
        createFile("todo/sprint.adoc");
        createFile("src/main/docs/plan.adoc");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        // Should find all 4 files
        assertTrue(found.size() >= 4, "check should find markdown and adoc files together");
        assertTrue(found.stream().anyMatch(f -> f.getName().endsWith(".md")),
                "check should include markdown files in results");
        assertTrue(found.stream().anyMatch(f -> f.getName().endsWith(".adoc")),
                "check should include adoc files in results");
    }

    @Test
    @DisplayName("Locator should is todo file matches adoc files")
    void isTodoFile_matchesAdocFiles() throws IOException {
        File shortAd = createFile("docs/plan.ad");
        assertTrue(locator.isTodoFile(shortAd), "check should accept plan.ad file in docs");
        File planAdoc = createFile("docs/plan.adoc");
        assertTrue(locator.isTodoFile(planAdoc), "check should accept plan.adoc file in docs");
        File todoAdoc = createFile("docs2/todo.adoc");
        assertTrue(locator.isTodoFile(todoAdoc), "check should accept todo.adoc file in docs");
        File todoAsciidoc = createFile("docs3/todo.asciidoc");
        assertTrue(locator.isTodoFile(todoAsciidoc), "check should accept todo.asciidoc file in docs");
    }

    @Test
    @DisplayName("Locator should is todo file matches adoc extensions case insensitive")
    void isTodoFile_matchesAdocCaseInsensitive() throws IOException {
        File lower = createFile("docs/plan.adoc");
        File upper = createFile("docs2/TODO.ASCIIDOC");

        assertTrue(locator.isTodoFile(lower), "check should accept lower case adoc file");
        assertTrue(locator.isTodoFile(upper), "check should accept upper case asciidoc file");
    }

    @Test
    @DisplayName("Locator should is todo file rejects non todo adoc")
    void isTodoFile_rejectsNonTodoAdoc() throws IOException {
        File other = createFile("docs/readme.adoc");

        assertFalse(locator.isTodoFile(other), "check should reject non todo adoc file");
    }

    @Test
    @DisplayName("Locator should get default patterns includes adoc patterns")
    void getDefaultPatterns_includesAdocPatterns() {
        List<String> patterns = TodoFileLocator.getDefaultPatterns();

        assertTrue(matchesAnyPattern(patterns, Paths.get("todo", "backlog.ad")),
                "default patterns should match todo/backlog.ad");
        assertTrue(matchesAnyPattern(patterns, Paths.get("todo", "backlog.adoc")),
                "default patterns should match todo/backlog.adoc");
        assertTrue(matchesAnyPattern(patterns, Paths.get("todo", "backlog.asciidoc")),
                "default patterns should match todo/backlog.asciidoc");
        assertTrue(matchesAnyPattern(patterns, Paths.get("src", "main", "docs", "release-plan.ad")),
                "default patterns should match src/main/docs/release-plan.ad");
        assertTrue(matchesAnyPattern(patterns, Paths.get("src", "main", "docs", "project-todo.adoc")),
                "default patterns should match src/main/docs/project-todo.adoc");
    }

    @Test
    @DisplayName("Locator should get default patterns includes markdown patterns")
    void getDefaultPatterns_includesMarkdownPatterns() {
        List<String> patterns = TodoFileLocator.getDefaultPatterns();

        assertTrue(matchesAnyPattern(patterns, Paths.get("docs", "TODO.markdown")),
                "default patterns should match docs/TODO.markdown");
        assertTrue(matchesAnyPattern(patterns, Paths.get("todo", "backlog.markdown")),
                "default patterns should match todo/backlog.markdown");
        assertTrue(matchesAnyPattern(patterns, Paths.get("todo", "backlog.md")),
                "default patterns should match todo/backlog.md");
        assertTrue(matchesAnyPattern(patterns, Paths.get("docs", "SPRINT-TODO.md")),
                "default patterns should match docs/SPRINT-TODO.md");
    }

    // === Directory skipping tests for remaining directories ===

    @Test
    @DisplayName("Locator should find todo files pattern matching skips build directory")
    void findTodoFiles_patternMatching_skipsBuildDirectory() throws IOException {
        createFile("build/TODO.md");
        createFile("src/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(),
                "check should skip build directory when scanning because build output is ignored");
        assertTrue(found.get(0).getPath().contains("src"), "check should keep src match when build skipped");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching skips out directory")
    void findTodoFiles_patternMatching_skipsOutDirectory() throws IOException {
        createFile("out/TODO.md");
        createFile("src/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(),
                "check should skip out directory when scanning because generated output is ignored");
        assertTrue(found.get(0).getPath().contains("src"), "check should keep src match when out skipped");
    }

    @Test
    @DisplayName("Locator should find todo files pattern matching skips dist directory")
    void findTodoFiles_patternMatching_skipsDistDirectory() throws IOException {
        createFile("dist/TODO.md");
        createFile("src/TODO.md");

        List<File> found = locator.findTodoFiles(tempDir.toFile());

        assertEquals(1, found.size(),
                "check should skip dist directory when scanning because distribution artefacts are ignored");
        assertTrue(found.get(0).getPath().contains("src"), "check should keep src match when dist skipped");
    }

    // === Exact-path mode tests ===

    @Test
    @DisplayName("Locator should get todo file paths exact path mode returns configured paths")
    void getTodoFilePaths_exactPathMode_returnsConfiguredPaths() {
        String docsTasks = "docs/TASKS.md";
        String plansTodo = "plans/TODO.md";
        List<String> customPaths = Arrays.asList(docsTasks, plansTodo);
        TodoFileLocator exactPathLocator = new TodoFileLocator(customPaths);

        List<String> paths = exactPathLocator.getTodoFilePaths();

        assertEquals(2, paths.size(), "check should return configured exact path list");
        assertTrue(paths.contains(docsTasks),
                "check should include docs tasks path " + docsTasks + " in " + paths);
        assertTrue(paths.contains(plansTodo),
                "check should include plans todo path " + plansTodo + " in " + paths);
    }

    @Test
    @DisplayName("Locator should find by exact paths directory exists with todo name is not included")
    void findByExactPaths_directoryExistsWithTodoName_isNotIncluded() throws IOException {
        // Create a directory with a name that looks like a TODO file
        Files.createDirectories(tempDir.resolve("custom/TASKS.md"));
        // Also create a real file
        createFile("custom/real-TODO.md");

        TodoFileLocator exactPathLocator = new TodoFileLocator(
                Arrays.asList("custom/TASKS.md", "custom/real-TODO.md"));

        List<File> found = exactPathLocator.findTodoFiles(tempDir.toFile());

        // Only the real file should be found, not the directory
        assertEquals(1, found.size(), "check should ignore directory that matches todo path");
        assertEquals("real-TODO.md", found.get(0).getName(), "check should return only real todo file");
    }

    // === File visitor tests ===

    @Test
    @DisplayName("Locator should pattern file visitor visit file failed returns continue")
    void patternFileVisitor_visitFileFailed_returnsContinue() {
        List<File> foundFiles = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*TODO.md");
        TodoFileLocator.PatternFileVisitor visitor =
                new TodoFileLocator.PatternFileVisitor(tempDir, matcher, foundFiles);

        FileVisitResult result = visitor.visitFileFailed(tempDir.resolve("missing"), new IOException("failure"));

        assertEquals(FileVisitResult.CONTINUE, result, "file visitor should continue after failure");
    }

    @Test
    @DisplayName("Locator should pattern file visitor handle root directory without name")
    void patternFileVisitor_rootDirectoryWithoutName_continues() {
        List<File> foundFiles = new ArrayList<>();
        Path root = FileSystems.getDefault().getRootDirectories().iterator().next();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*TODO.md");
        TodoFileLocator.PatternFileVisitor visitor =
                new TodoFileLocator.PatternFileVisitor(root, matcher, foundFiles);

        FileVisitResult result = visitor.preVisitDirectory(root, null);

        assertEquals(FileVisitResult.CONTINUE, result,
                "file visitor should continue when directory has no filename");
    }

    private File createFile(String relativePath) throws IOException {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.write(path, "# TODO\n- [ ] Task".getBytes(StandardCharsets.UTF_8));
        return path.toFile();
    }

    private boolean matchesAnyPattern(List<String> patterns, Path path) {
        return patterns.stream()
                .map(pattern -> FileSystems.getDefault().getPathMatcher(pattern))
                .anyMatch(matcher -> matcher.matches(path));
    }
}
