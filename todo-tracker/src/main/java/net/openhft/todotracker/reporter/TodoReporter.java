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
package net.openhft.todotracker.reporter;

import net.openhft.todotracker.model.TodoTask;
import net.openhft.todotracker.parser.TodoReport;
import org.apache.maven.plugin.logging.Log;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Formats and outputs TODO task reports to the Maven build log console.
 */
public final class TodoReporter {

    private static final String SEPARATOR_LINE =
            "================================================================================";
    private static final String THIN_SEPARATOR =
            "--------------------------------------------------------------------------------";

    private final Log log;
    private final int maxTasks;
    private final boolean showContext;

    /**
     * Creates a reporter with the specified settings.
     *
     * @param log         Maven log for output
     * @param maxTasks    maximum number of tasks to display (0 for unlimited)
     * @param showContext whether to show section header context
     */
    public TodoReporter(Log log, int maxTasks, boolean showContext) {
        this.log = requireNonNull(log, "reporter log must not be null");
        this.maxTasks = maxTasks;
        this.showContext = showContext;
    }

    /**
     * Reports uncompleted tasks to the log.
     *
     * @param report the report containing tasks
     */
    public void reportUncompleted(TodoReport report) {
        reportUncompleted(report, Collections.emptyList());
    }

    /**
     * Reports uncompleted tasks to the log, excluding tasks matching the given patterns.
     *
     * @param report          the report containing tasks
     * @param excludePatterns patterns to exclude based on context heading
     */
    public void reportUncompleted(TodoReport report, Collection<Pattern> excludePatterns) {
        requireNonNull(report, "task report must not be null");

        List<TodoTask> uncompleted = report.getUncompletedTasks(excludePatterns);
        if (uncompleted.isEmpty()) {
            return;
        }

        // Print header
        log.warn(SEPARATOR_LINE);
        log.warn(" TODO TRACKER: Uncompleted tasks found");
        log.warn(SEPARATOR_LINE);
        log.warn("");

        // Group tasks by file
        Map<String, List<TodoTask>> tasksByFile = uncompleted.stream()
                .collect(Collectors.groupingBy(TodoTask::getFilePath));

        int displayCount = 0;
        int effectiveMax = maxTasks > 0 ? maxTasks : Integer.MAX_VALUE;

        for (Map.Entry<String, List<TodoTask>> entry : tasksByFile.entrySet()) {
            if (displayCount >= effectiveMax) {
                break;
            }

            String filePath = entry.getKey();
            List<TodoTask> fileTasks = entry.getValue();

            log.warn("Found " + fileTasks.size() + " uncompleted task(s) in " + filePath + ":");
            log.warn("");

            for (TodoTask task : fileTasks) {
                if (displayCount >= effectiveMax) {
                    break;
                }

                displayCount++;
                formatTask(displayCount, task);
            }
        }

        // Show "more" message if tasks were truncated
        if (uncompleted.size() > effectiveMax) {
            int remaining = uncompleted.size() - effectiveMax;
            log.warn("");
            log.warn("  ... and " + remaining + " more task(s) not shown");
            log.warn("      (increase <maxTasks> to see more)");
        }

        // Print instructions
        log.warn("");
        log.warn(THIN_SEPARATOR);
        log.warn(" TO RESOLVE: Complete the tasks above by changing '- [ ]' to '- [x]'");
        log.warn("             Then run: mvn verify");
        log.warn(THIN_SEPARATOR);
    }

    private void formatTask(int index, TodoTask task) {
        StringBuilder line = new StringBuilder(64);
        line.append("  ").append(index).append(". ")
                .append("[Line ").append(task.getLineNumber()).append("] ");

        // Add priority and effort if present
        if (task.hasPriority()) {
            line.append("[P").append(task.getPriority()).append("] ");
        }
        if (task.hasEffort()) {
            line.append("[E:").append(task.getEffort()).append("] ");
        }

        line.append(task.getText());
        log.warn(line.toString());

        if (showContext && task.getContext() != null) {
            log.warn("     Context: " + task.getContext());
        }
        log.warn("");
    }

    /**
     * Generates a summary string for the report.
     *
     * @param report the report to summarize
     * @return summary string
     */
    public String generateSummary(TodoReport report) {
        requireNonNull(report, "summary report must not be null");

        int total = report.getTotalCount();
        int completed = report.getCompletedCount();
        int uncompleted = report.getUncompletedCount();
        int files = report.getFileCount();

        return String.format("%d task(s) (%d completed, %d uncompleted) in %d file(s)",
                total, completed, uncompleted, files);
    }

    /**
     * Reports that no TODO files were found.
     *
     * @param searchedPaths the paths that were searched
     */
    public void reportNoFilesFound(List<String> searchedPaths) {
        String paths = String.join(", ", searchedPaths);
        log.info("TODO Tracker: No TODO files found (searched: " + paths + ")");
        log.info("TODO Tracker: Check passed (no TODO files to verify)");
    }

    /**
     * Reports that all tasks are completed.
     */
    public void reportAllCompleted() {
        log.info("TODO Tracker: No uncompleted tasks found");
        log.info("TODO Tracker: Check passed");
    }

    /**
     * Reports that the plugin is being skipped.
     */
    public void reportSkipped() {
        log.info("TODO Tracker: Skipping execution (skip=true)");
    }
}
