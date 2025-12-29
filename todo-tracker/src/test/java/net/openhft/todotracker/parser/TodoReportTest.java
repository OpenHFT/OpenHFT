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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Report should cover todo report behaviours")
class TodoReportTest {

    private TodoReport report;

    @BeforeEach
    void setUp() {
        report = new TodoReport();
    }

    @Test
    @DisplayName("Report should start with zero tasks in new instance")
    void newReport_isEmpty() {
        assertTrue(report.isEmpty(), "new report should start out empty");
        assertEquals(0, report.getTotalCount(), "new report should have zero total tasks");
        assertEquals(0, report.getUncompletedCount(), "new report should have zero uncompleted tasks");
        assertEquals(0, report.getCompletedCount(), "new report should have zero completed tasks");
        assertFalse(report.hasFilesProcessed(), "new report should have no files processed");
        assertFalse(report.hasUncompletedTasks(), "new report should have no uncompleted tasks");
    }

    @Test
    @DisplayName("Report should add task increases total count")
    void addTask_increasesTotalCount() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        report.addTask(task);

        assertEquals(1, report.getTotalCount(), "report should increase total count after adding task");
        assertFalse(report.isEmpty(), "report should not be empty after adding task");
    }

    @Test
    @DisplayName("Report should add task with null throws exception")
    void addTask_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.addTask(null),
                "report should throw when task is null");
    }

    @Test
    @DisplayName("Report should add file processed tracks file")
    void addFileProcessed_tracksFile() {
        report.addFileProcessed("TODO.md");

        assertTrue(report.hasFilesProcessed(), "report should mark files processed after adding");
        assertEquals(1, report.getFileCount(), "report should count one processed file entry");
        assertTrue(report.getFilesProcessed().contains("TODO.md"), "report should include TODO.md in processed files");
    }

    @Test
    @DisplayName("Report should add file processed with null throws exception")
    void addFileProcessed_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.addFileProcessed(null),
                "report should throw when file path is null");
    }

    @Test
    @DisplayName("Report should get uncompleted tasks filters correctly")
    void getUncompletedTasks_filtersCorrectly() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addTask(new TodoTask("f.md", 3, "uncompleted2", null, false));

        List<TodoTask> uncompleted = report.getUncompletedTasks();

        assertEquals(2, uncompleted.size(), "report should return two uncompleted task entries");
        assertTrue(uncompleted.stream().allMatch(TodoTask::isUncompleted),
                "report should include only uncompleted task entries");
    }

    @Test
    @DisplayName("Report should get completed tasks filters correctly")
    void getCompletedTasks_filtersCorrectly() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addTask(new TodoTask("f.md", 3, "completed2", null, true));

        List<TodoTask> completed = report.getCompletedTasks();

        assertEquals(2, completed.size(), "report should return two completed task entries");
        assertTrue(completed.stream().allMatch(TodoTask::isCompleted),
                "report should include only completed task entries");
    }

    @Test
    @DisplayName("Report should report uncompleted tasks when present")
    void hasUncompletedTasks_returnsTrueWhenPresent() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        assertTrue(report.hasUncompletedTasks(), "report should report uncompleted tasks as present");
    }

    @Test
    @DisplayName("Report should has uncompleted tasks returns false when all completed")
    void hasUncompletedTasks_returnsFalseWhenAllCompleted() {
        report.addTask(new TodoTask("f.md", 1, "completed", null, true));
        assertFalse(report.hasUncompletedTasks(), "report should report no uncompleted tasks when all done");
    }

    @Test
    @DisplayName("Report should merge task lists from another report")
    void merge_combinesTasks() {
        TodoReport other = new TodoReport();
        report.addTask(new TodoTask("f1.md", 1, "task1", null, false));
        other.addTask(new TodoTask("f2.md", 1, "task2", null, true));

        report.merge(other);

        assertEquals(2, report.getTotalCount(), "report should merge total task count correctly");
        assertEquals(1, report.getUncompletedCount(), "report should merge uncompleted task count correctly");
        assertEquals(1, report.getCompletedCount(), "report should merge completed task count correctly");
    }

    @Test
    @DisplayName("Report should merge combines files processed")
    void merge_combinesFilesProcessed() {
        TodoReport other = new TodoReport();
        report.addFileProcessed("file1.md");
        other.addFileProcessed("file2.md");

        report.merge(other);

        assertEquals(2, report.getFileCount(), "report should merge processed file count correctly");
        assertTrue(report.getFilesProcessed().contains("file1.md"), "report should include file1.md in merged list");
        assertTrue(report.getFilesProcessed().contains("file2.md"), "report should include file2.md in merged list");
    }

    @Test
    @DisplayName("Report should merge with null throws exception")
    void merge_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.merge(null),
                "report should throw when other report is null");
    }

    @Test
    @DisplayName("Report should get all tasks returns unmodifiable list")
    void getAllTasks_returnsUnmodifiableList() {
        report.addTask(new TodoTask("f.md", 1, "task", null, false));
        List<TodoTask> tasks = report.getAllTasks();

        assertThrows(UnsupportedOperationException.class, () ->
                tasks.add(new TodoTask("f.md", 2, "new", null, false)),
                "report should not allow adding tasks to list");
    }

    @Test
    @DisplayName("Report should get files processed returns unmodifiable list")
    void getFilesProcessed_returnsUnmodifiableList() {
        report.addFileProcessed("file.md");
        List<String> files = report.getFilesProcessed();

        assertThrows(UnsupportedOperationException.class, () -> files.add("new.md"),
                "report should not allow adding files to list");
    }

    @Test
    @DisplayName("Report should to string contains relevant info")
    void toString_containsRelevantInfo() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addFileProcessed("f.md");

        String str = report.toString();

        String totalCount = "2";
        String singleCount = "1";
        assertTrue(str.contains(totalCount),
                "report should include total count " + totalCount + " in string: " + str);  // total
        assertTrue(str.contains(singleCount),
                "report should include count " + singleCount + " in string: " + str);  // uncompleted and completed counts
    }

    @Test
    @DisplayName("Report should counts with mixed tasks")
    void counts_withMixedTasks() {
        report.addTask(new TodoTask("f.md", 1, "t1", null, false));
        report.addTask(new TodoTask("f.md", 2, "t2", null, true));
        report.addTask(new TodoTask("f.md", 3, "t3", null, false));
        report.addTask(new TodoTask("f.md", 4, "t4", null, true));
        report.addTask(new TodoTask("f.md", 5, "t5", null, false));

        assertEquals(5, report.getTotalCount(), "report should count total tasks for mixed set");
        assertEquals(3, report.getUncompletedCount(), "report should count open tasks for mixed set");
        assertEquals(2, report.getCompletedCount(), "report should count completed tasks for mixed set");
    }
}
