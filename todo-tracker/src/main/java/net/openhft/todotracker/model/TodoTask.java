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
package net.openhft.todotracker.model;

import java.util.Objects;

/**
 * Immutable representation of a single TODO task from a markdown file.
 * Supports priority tags [P1-3] and effort tags [E:S/M/L].
 */
public final class TodoTask {

    private final String filePath;
    private final int lineNumber;
    private final String text;
    private final String context;
    private final boolean completed;
    private final Integer priority;
    private final Character effort;

    /**
     * Creates a new TodoTask with all fields populated.
     *
     * @param filePath   the path to the source file (required)
     * @param lineNumber the 1-based line number (must be positive)
     * @param text       the task description text (required)
     * @param context    the section header context (nullable)
     * @param completed  whether the task is completed
     * @param priority   the priority level 1-3 (nullable)
     * @param effort     the effort level S/M/L (nullable)
     */
    public TodoTask(String filePath, int lineNumber, String text,
                    String context, boolean completed,
                    Integer priority, Character effort) {
        this.filePath = Objects.requireNonNull(filePath, "task file path must not be null");
        if (lineNumber < 1) {
            throw new IllegalArgumentException("lineNumber must be positive, got: " + lineNumber);
        }
        this.lineNumber = lineNumber;
        this.text = Objects.requireNonNull(text, "task text must not be null");
        this.context = context;
        this.completed = completed;
        this.priority = priority;
        this.effort = effort;
    }

    /**
     * Creates a new TodoTask without priority or effort.
     */
    public TodoTask(String filePath, int lineNumber, String text,
                    String context, boolean completed) {
        this(filePath, lineNumber, text, context, completed, null, null);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getText() {
        return text;
    }

    public String getContext() {
        return context;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isUncompleted() {
        return !completed;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean hasPriority() {
        return priority != null;
    }

    public Character getEffort() {
        return effort;
    }

    public boolean hasEffort() {
        return effort != null;
    }

    /**
     * Returns a location string in the format "filePath:lineNumber".
     */
    public String toLocationString() {
        return filePath + ":" + lineNumber;
    }

    /**
     * Returns a formatted string with priority and effort tags if present.
     * Format: "[P1] [E:M] Task text" or just "Task text" if no tags.
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        if (hasPriority()) {
            sb.append("[P").append(priority).append("] ");
        }
        if (hasEffort()) {
            sb.append("[E:").append(effort).append("] ");
        }
        sb.append(text);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoTask)) return false;
        TodoTask task = (TodoTask) o;
        return lineNumber == task.lineNumber &&
                completed == task.completed &&
                filePath.equals(task.filePath) &&
                text.equals(task.text) &&
                Objects.equals(context, task.context) &&
                Objects.equals(priority, task.priority) &&
                Objects.equals(effort, task.effort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, lineNumber, text, context, completed, priority, effort);
    }

    @Override
    public String toString() {
        return "TodoTask{" +
                "filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                ", text='" + text + '\'' +
                ", context='" + context + '\'' +
                ", completed=" + completed +
                ", priority=" + priority +
                ", effort=" + effort +
                '}';
    }
}
