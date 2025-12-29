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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Locates TODO files in Markdown or AsciiDoc across a project directory tree.
 * <p>
 * Supports multiple patterns:
 * <ul>
 *   <li>Exact paths: {@code TODO.md}, {@code docs/TODO.md}</li>
 *   <li>Glob patterns: {@code *TODO.md}, {@code todo/*.md}, {@code todo/*.adoc}</li>
 *   <li>Recursive patterns: {@code **&#47;TODO.md} (searches all subdirectories)</li>
 * </ul>
 * <p>
 * Default Markdown patterns:
 * <ul>
 *   <li>{@code **&#47;*TODO.md} - any file ending with TODO.md (case-insensitive)</li>
 *   <li>{@code **&#47;*TODOS.md} - any file ending with TODOS.md (case-insensitive)</li>
 *   <li>{@code todo/*.md} - any .md file in a todo/ directory</li>
 * </ul>
 * <p>
 * Default AsciiDoc patterns (both .adoc and .asciidoc extensions):
 * <ul>
 *   <li>{@code todo/*.adoc}, {@code todo/*.asciidoc} - any AsciiDoc file in todo/</li>
 *   <li>{@code src/main/docs/*plan.adoc} - plan files in docs (case-insensitive)</li>
 *   <li>{@code src/main/docs/*todo.adoc} - todo files in docs (case-insensitive)</li>
 * </ul>
 */
public final class TodoFileLocator {

    /**
     * Default glob patterns for locating TODO files.
     * <p>
     * Markdown patterns:
     * <ul>
     *   <li>{@code *TODO.md}, {@code *TODOS.md} anywhere (case-insensitive)</li>
     *   <li>{@code todo/*.md} - all .md files in todo/ directory</li>
     * </ul>
     * <p>
     * AsciiDoc patterns:
     * <ul>
     *   <li>{@code todo/*.adoc} - all .adoc files in todo/ directory</li>
     *   <li>{@code src/main/docs/*plan.adoc} - plan files in docs (case-insensitive)</li>
     *   <li>{@code src/main/docs/*todo.adoc} - todo files in docs (case-insensitive)</li>
     * </ul>
     */
    private static final List<String> DEFAULT_PATTERNS = Arrays.asList(
            // Markdown patterns
            "glob:**/*[Tt][Oo][Dd][Oo].md",     // *TODO.md or *todo.md anywhere
            "glob:**/*[Tt][Oo][Dd][Oo][Ss].md", // *TODOS.md or *todos.md anywhere
            "glob:todo/*.md",                    // todo/*.md in root
            "glob:**/todo/*.md",                 // todo/*.md anywhere
            // AsciiDoc patterns (.adoc)
            "glob:todo/*.[Aa][Dd][Oo][Cc]",                              // todo/*.adoc in root
            "glob:**/todo/*.[Aa][Dd][Oo][Cc]",                           // todo/*.adoc anywhere
            "glob:src/main/docs/*[Pp][Ll][Aa][Nn].[Aa][Dd][Oo][Cc]",     // *plan.adoc in docs
            "glob:src/main/docs/*[Tt][Oo][Dd][Oo].[Aa][Dd][Oo][Cc]",     // *todo.adoc in docs
            // AsciiDoc patterns (.asciidoc - long extension)
            "glob:todo/*.asciidoc",                                       // todo/*.asciidoc in root
            "glob:**/todo/*.asciidoc",                                    // todo/*.asciidoc anywhere
            "glob:src/main/docs/*[Pp][Ll][Aa][Nn].asciidoc",              // *plan.asciidoc in docs
            "glob:src/main/docs/*[Tt][Oo][Dd][Oo].asciidoc"               // *todo.asciidoc in docs
    );

    /**
     * Legacy exact paths for backwards compatibility.
     */
    private static final List<String> DEFAULT_TODO_FILES = Arrays.asList(
            "TODO.md",
            "todo/TODO.md"
    );

    /**
     * Pattern to match TODO-like filenames (case-insensitive).
     * Matches files ending in TODO.md, TODOS.md, or .adoc/.asciidoc files with TODO/PLAN in name.
     */
    private static final Pattern TODO_FILENAME_PATTERN = Pattern.compile(
            ".*(?:todo[s]?\\.md|(?:todo|plan)\\.(?:adoc|asciidoc))$",
            Pattern.CASE_INSENSITIVE
    );

    private final List<String> todoFiles;
    private final List<String> patterns;
    private final boolean usePatterns;

    /**
     * Creates a locator with default patterns.
     * Searches for *TODO.md, *todo.md, and todo/*.md files.
     */
    public TodoFileLocator() {
        this.todoFiles = new ArrayList<>(DEFAULT_TODO_FILES);
        this.patterns = new ArrayList<>(DEFAULT_PATTERNS);
        this.usePatterns = true;
    }

    /**
     * Creates a locator with custom TODO file paths (exact paths, no patterns).
     * For backwards compatibility with existing configurations.
     *
     * @param todoFiles relative paths to TODO files
     */
    public TodoFileLocator(List<String> todoFiles) {
        this.todoFiles = new ArrayList<>(
                Objects.requireNonNull(todoFiles, "todo file path list must not be null"));
        this.patterns = new ArrayList<>();
        this.usePatterns = false;
    }

    /**
     * Returns the default TODO file paths (for backwards compatibility).
     */
    public static List<String> getDefaultTodoFiles() {
        return new ArrayList<>(DEFAULT_TODO_FILES);
    }

    /**
     * Returns the default glob patterns used for TODO file discovery.
     */
    public static List<String> getDefaultPatterns() {
        return new ArrayList<>(DEFAULT_PATTERNS);
    }

    /**
     * Finds all existing TODO files relative to the given base directory.
     *
     * @param basedir the base directory to search from
     * @return list of existing TODO files (deduplicated, discovery order)
     */
    public List<File> findTodoFiles(File basedir) {
        Objects.requireNonNull(basedir, "base directory must not be null");

        Set<File> foundFiles = new LinkedHashSet<>();

        if (usePatterns) {
            // Use pattern matching
            foundFiles.addAll(findByPatterns(basedir));
        } else {
            // Use exact paths (legacy behavior)
            foundFiles.addAll(findByExactPaths(basedir));
        }

        return new ArrayList<>(foundFiles);
    }

    /**
     * Finds files matching the configured glob patterns.
     */
    private List<File> findByPatterns(File basedir) {
        List<File> foundFiles = new ArrayList<>();
        Path basePath = basedir.toPath();

        for (String pattern : patterns) {
            try {
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
                Files.walkFileTree(basePath, new PatternFileVisitor(basePath, matcher, foundFiles));
            } catch (IOException e) {
                // Log and continue with other patterns
            }
        }

        return foundFiles;
    }

    /**
     * Finds files by exact relative paths (legacy behavior).
     */
    private List<File> findByExactPaths(File basedir) {
        List<File> foundFiles = new ArrayList<>();

        for (String relativePath : todoFiles) {
            File todoFile = new File(basedir, relativePath);
            if (todoFile.exists() && todoFile.isFile()) {
                foundFiles.add(todoFile);
            }
        }

        return foundFiles;
    }

    /**
     * Returns the configured TODO file paths (for exact path mode).
     */
    public List<String> getTodoFilePaths() {
        if (usePatterns) {
            return new ArrayList<>(DEFAULT_TODO_FILES);
        }
        return new ArrayList<>(todoFiles);
    }

    /**
     * Returns the configured patterns (for pattern mode).
     */
    public List<String> getPatterns() {
        return new ArrayList<>(patterns);
    }

    /**
     * Returns true when pattern matching is enabled for discovery.
     */
    public boolean isUsingPatterns() {
        return usePatterns;
    }

    /**
     * Checks if a file matches the TODO file criteria.
     * Matches filenames ending with TODO.md or TODOS.md (case-insensitive).
     *
     * @param file the file to check
     * @return true if the file name suggests it's a TODO file
     */
    public boolean isTodoFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName();
        return TODO_FILENAME_PATTERN.matcher(name).matches();
    }

    static final class PatternFileVisitor extends SimpleFileVisitor<Path> {
        private final Path basePath;
        private final PathMatcher matcher;
        private final List<File> foundFiles;

        PatternFileVisitor(Path basePath, PathMatcher matcher, List<File> foundFiles) {
            this.basePath = basePath;
            this.matcher = matcher;
            this.foundFiles = foundFiles;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path relativePath = basePath.relativize(file);
            if (matcher.matches(relativePath) || matcher.matches(file)) {
                foundFiles.add(file.toFile());
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // Skip hidden directories and common non-source directories
            String name = Objects.requireNonNull(dir.getFileName(),
                    "directory path has no filename").toString();
            if (name.startsWith(".") || name.equals("node_modules") ||
                    name.equals("target") || name.equals("build") ||
                    name.equals("out") || name.equals("dist")) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            // Ignore files we can't access
            return FileVisitResult.CONTINUE;
        }
    }
}
