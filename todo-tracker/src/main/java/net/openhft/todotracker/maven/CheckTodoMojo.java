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

import net.openhft.todotracker.locator.TodoFileLocator;
import net.openhft.todotracker.parser.TodoParser;
import net.openhft.todotracker.parser.TodoReport;
import net.openhft.todotracker.reporter.TodoReporter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Scans TODO.md files for uncompleted tasks and reports findings.
 * By default, fails the build when uncompleted tasks are found.
 * <p>
 * Supports multiple checkbox formats:
 * <ul>
 *   <li>{@code - [ ]} / {@code - [x]} - dash bullet</li>
 *   <li>{@code * [ ]} / {@code * [x]} - asterisk bullet</li>
 *   <li>{@code 1. [ ]} / {@code 1. [x]} - numbered list</li>
 * </ul>
 * <p>
 * Also recognizes priority tags [P1-3] and effort tags [E:S/M/L].
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class CheckTodoMojo extends AbstractMojo {

    /**
     * Default patterns for excluding contexts (case-insensitive).
     */
    private static final List<String> DEFAULT_EXCLUDE_PATTERNS = Arrays.asList(
            "(?i)\\bPlanned\\b",
            "(?i)\\bFuture\\b",
            "(?i)\\bBacklog\\b",
            "(?i)\\bMaybe\\b",
            "(?i)\\bSomeday\\b"
    );
    /**
     * The Maven project used as the base for file discovery.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * Maximum number of uncompleted tasks to display.
     * Set to 0 for unlimited.
     */
    @Parameter(property = "todotracker.maxTasks", defaultValue = "10")
    private int maxTasks;
    /**
     * Whether to fail the build when uncompleted tasks are found.
     */
    @Parameter(property = "todotracker.failOnIncomplete", defaultValue = "true")
    private boolean failOnIncomplete;
    /**
     * List of TODO file paths relative to project basedir.
     * When specified, disables pattern matching and uses exact paths only.
     */
    @Parameter(property = "todotracker.todoFiles")
    private List<String> todoFiles;
    /**
     * Enable glob pattern matching to discover TODO files.
     * <p>
     * Default Markdown patterns: **&#47;*TODO.md, **&#47;*TODOS.md, **&#47;*TODO.markdown,
     * **&#47;*TODOS.markdown, todo/*.md, todo/*.markdown
     * <p>
     * Default AsciiDoc patterns: todo/*.ad, todo/*.adoc, todo/*.asciidoc,
     * src/main/docs/*plan.ad, src/main/docs/*todo.ad
     * <p>
     * Set to false to use exact paths only (legacy behavior).
     */
    @Parameter(property = "todotracker.usePatterns", defaultValue = "true")
    private boolean usePatterns;
    /**
     * Skip plugin execution entirely when this flag is true.
     */
    @Parameter(property = "todotracker.skip", defaultValue = "false")
    private boolean skip;
    /**
     * Show section heading context for each task.
     */
    @Parameter(property = "todotracker.showContext", defaultValue = "true")
    private boolean showContext;
    /**
     * Regex pattern to identify context headings in Markdown files.
     * Default matches ## headers (level 2).
     */
    @Parameter(property = "todotracker.contextPattern", defaultValue = "^##\\s+.*")
    private String contextPattern = "^##\\s+.*";
    /**
     * Regex pattern to identify context headings in AsciiDoc files.
     * Default matches == headers (level 2).
     */
    @Parameter(property = "todotracker.asciidocContextPattern", defaultValue = "^==\\s+.*")
    private String asciidocContextPattern = "^==\\s+.*";
    /**
     * Regex patterns to exclude tasks from enforcement based on their context heading.
     * Tasks under headings matching any of these patterns will be skipped.
     * <p>
     * Default patterns exclude headings containing:
     * <ul>
     *   <li>"Planned" - e.g., "## Phase 9: Future Enhancements (Planned)"</li>
     *   <li>"Future" - e.g., "## Future Work"</li>
     *   <li>"Backlog" - e.g., "## Backlog"</li>
     *   <li>"Maybe" - e.g., "## Maybe Later"</li>
     *   <li>"Someday" - e.g., "## Someday/Maybe"</li>
     * </ul>
     * Set to empty list to enforce all tasks regardless of heading.
     */
    @Parameter(property = "todotracker.excludeContextPatterns")
    private List<String> excludeContextPatterns;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        TodoReporter reporter = new TodoReporter(getLog(), maxTasks, showContext);

        if (skip) {
            reporter.reportSkipped();
            return;
        }

        // Determine locator mode
        TodoFileLocator locator;
        List<String> searchDescription;

        if (todoFiles != null && !todoFiles.isEmpty()) {
            // Custom paths specified - use exact-path mode
            locator = new TodoFileLocator(todoFiles);
            searchDescription = todoFiles;
        } else if (usePatterns) {
            // Default with pattern matching enabled
            locator = new TodoFileLocator();
            searchDescription = TodoFileLocator.getDefaultPatterns();
        } else {
            // Legacy exact-path mode
            locator = new TodoFileLocator(TodoFileLocator.getDefaultTodoFiles());
            searchDescription = TodoFileLocator.getDefaultTodoFiles();
        }

        File basedir = project.getBasedir();

        // Find existing TODO files
        List<File> foundFiles = locator.findTodoFiles(basedir);

        if (foundFiles.isEmpty()) {
            reporter.reportNoFilesFound(searchDescription);
            return;
        }

        // Parse all TODO files with format-specific parsers
        TodoParser markdownParser = new TodoParser(contextPattern);
        TodoParser asciidocParser = new TodoParser(asciidocContextPattern);
        TodoReport aggregatedReport = new TodoReport();

        for (File todoFile : foundFiles) {
            try {
                // Select parser based on file format
                TodoParser.Format format = TodoParser.detectFormat(todoFile.getPath());
                TodoParser parser = (format == TodoParser.Format.ASCIIDOC)
                        ? asciidocParser : markdownParser;
                TodoReport fileReport = parser.parse(todoFile);
                aggregatedReport.merge(fileReport);
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Failed to parse TODO file: " + todoFile.getAbsolutePath(), e);
            }
        }

        // Build exclude patterns
        List<Pattern> compiledExcludePatterns = compileExcludePatterns();

        // Get enforceable uncompleted tasks (excluding planned/future contexts)
        List<net.openhft.todotracker.model.TodoTask> enforceableTasks =
                aggregatedReport.getUncompletedTasks(compiledExcludePatterns);

        // Report findings
        if (!enforceableTasks.isEmpty()) {
            reporter.reportUncompleted(aggregatedReport, compiledExcludePatterns);

            if (failOnIncomplete) {
                throw new MojoFailureException(
                        "Build failed: " + enforceableTasks.size() +
                                " uncompleted task(s) found. " +
                                "Set <failOnIncomplete>false</failOnIncomplete> to warn only.");
            }
        } else {
            reporter.reportAllCompleted();
        }
    }

    /**
     * Compiles exclude patterns, using defaults if not configured.
     */
    private List<Pattern> compileExcludePatterns() {
        List<String> patterns = (excludeContextPatterns != null)
                ? excludeContextPatterns
                : DEFAULT_EXCLUDE_PATTERNS;

        if (patterns.isEmpty()) {
            return Collections.emptyList();
        }

        return patterns.stream()
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    // Setters for testing
    void setProject(MavenProject project) {
        this.project = project;
    }

    void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    void setFailOnIncomplete(boolean failOnIncomplete) {
        this.failOnIncomplete = failOnIncomplete;
    }

    void setTodoFiles(List<String> todoFiles) {
        this.todoFiles = todoFiles;
    }

    // TODO test skip=false
    void setSkip(boolean skip) {
        this.skip = skip;
    }

    void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    void setContextPattern(String contextPattern) {
        this.contextPattern = contextPattern;
    }

    void setAsciidocContextPattern(String asciidocContextPattern) {
        this.asciidocContextPattern = asciidocContextPattern;
    }

    void setUsePatterns(boolean usePatterns) {
        this.usePatterns = usePatterns;
    }

    void setExcludeContextPatterns(List<String> excludeContextPatterns) {
        this.excludeContextPatterns = excludeContextPatterns;
    }
}
