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

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Aggregates TODO tasks from one or more files.
 * Provides methods for accessing and filtering tasks.
 */
public final class TodoReport {

    private final List<TodoTask> tasks;
    private final List<String> filesProcessed;

    public TodoReport() {
        this.tasks = new ArrayList<>();
        this.filesProcessed = new ArrayList<>();
    }

    /**
     * Adds a task entry to the report list.
     *
     * @param task the task to add (must not be null)
     */
    public void addTask(TodoTask task) {
        tasks.add(Objects.requireNonNull(task, "report task must not be null"));
    }

    /**
     * Records that a file has been processed.
     *
     * @param filePath the path of the processed file
     */
    public void addFileProcessed(String filePath) {
        filesProcessed.add(Objects.requireNonNull(filePath, "report file path must not be null"));
    }

    /**
     * Merges another report into this one.
     *
     * @param other the report to merge
     */
    public void merge(TodoReport other) {
        Objects.requireNonNull(other, "other report must not be null");
        tasks.addAll(other.tasks);
        filesProcessed.addAll(other.filesProcessed);
    }

    /**
     * Returns an unmodifiable view of all reported tasks.
     */
    public List<TodoTask> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Returns a list of uncompleted tasks for enforcement.
     */
    public List<TodoTask> getUncompletedTasks() {
        return tasks.stream()
                .filter(TodoTask::isUncompleted)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of uncompleted tasks, excluding those whose context
     * matches any of the provided patterns.
     * <p>
     * This is useful for excluding tasks under headings like "Planned" or "Future".
     *
     * @param excludePatterns patterns to match against task context (null-safe)
     * @return uncompleted tasks not matching any exclude pattern
     */
    public List<TodoTask> getUncompletedTasks(Collection<Pattern> excludePatterns) {
        if (excludePatterns == null || excludePatterns.isEmpty()) {
            return getUncompletedTasks();
        }
        return tasks.stream()
                .filter(TodoTask::isUncompleted)
                .filter(task -> !matchesAnyPattern(task.getContext(), excludePatterns))
                .collect(Collectors.toList());
    }

    /**
     * Checks if context matches any of the provided patterns.
     */
    private boolean matchesAnyPattern(String context, Collection<Pattern> patterns) {
        if (context == null || patterns == null) {
            return false;
        }
        return patterns.stream().anyMatch(p -> p.matcher(context).find());
    }

    /**
     * Returns a list of completed tasks for summary output.
     */
    public List<TodoTask> getCompletedTasks() {
        return tasks.stream()
                .filter(TodoTask::isCompleted)
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of tasks in the report.
     */
    public int getTotalCount() {
        return tasks.size();
    }

    /**
     * Returns the number of uncompleted tasks in the report.
     */
    public int getUncompletedCount() {
        return (int) tasks.stream().filter(TodoTask::isUncompleted).count();
    }

    /**
     * Returns the number of completed tasks in the report.
     */
    public int getCompletedCount() {
        return (int) tasks.stream().filter(TodoTask::isCompleted).count();
    }

    /**
     * Returns true when the report contains uncompleted task entries.
     */
    public boolean hasUncompletedTasks() {
        return tasks.stream().anyMatch(TodoTask::isUncompleted);
    }

    /**
     * Returns true when the report contains zero task entries.
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns true when at least one source file has been processed.
     */
    public boolean hasFilesProcessed() {
        return !filesProcessed.isEmpty();
    }

    /**
     * Returns the number of processed files tracked by the report.
     */
    public int getFileCount() {
        return filesProcessed.size();
    }

    /**
     * Returns an unmodifiable view of processed file paths.
     */
    public List<String> getFilesProcessed() {
        return Collections.unmodifiableList(filesProcessed);
    }

    @Override
    public String toString() {
        return "TodoReport{" +
                "totalTasks=" + getTotalCount() +
                ", uncompleted=" + getUncompletedCount() +
                ", completed=" + getCompletedCount() +
                ", filesProcessed=" + filesProcessed.size() +
                '}';
    }
}
