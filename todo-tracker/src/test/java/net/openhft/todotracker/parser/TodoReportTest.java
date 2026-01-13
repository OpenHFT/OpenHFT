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

import java.util.List;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Todo aggregation tracks tasks and counts totals")
class TodoReportTest {

    private TodoReport report;

    @BeforeEach
    void setUp() {
        report = new TodoReport();
    }

    @Test
    @DisplayName("New instance should start with zero task counts")
    void newReport_isEmpty() {
        assertTrue(report.isEmpty(), "new instance should start out empty because no entries are added yet");
        assertEquals(0, report.getTotalCount(), "new instance should have zero total tasks");
        assertEquals(0, report.getUncompletedCount(), "new instance should have zero uncompleted tasks");
        assertEquals(0, report.getCompletedCount(), "new instance should have zero completed tasks");
        assertFalse(report.hasFilesProcessed(), "new instance should have no files processed");
        assertFalse(report.hasUncompletedTasks(), "new instance should have no uncompleted tasks");
    }

    @Test
    @DisplayName("Task collection should increase total count")
    void addTask_increasesTotalCount() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        report.addTask(task);

        assertEquals(1, report.getTotalCount(),
                "task collection should increase total count after adding task because totals are additive");
        assertFalse(report.isEmpty(), "task collection should not be empty after adding task");
    }

    @Test
    @DisplayName("Task collection should reject null input entry")
    void addTask_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.addTask(null),
                "task collection should throw when task is null");
    }

    @Test
    @DisplayName("File tracking should record processed file")
    void addFileProcessed_tracksFile() {
        report.addFileProcessed("TODO.md");

        assertTrue(report.hasFilesProcessed(),
                "file tracking should mark files processed after adding because summary output uses the list");
        assertEquals(1, report.getFileCount(), "file tracking should count one processed file entry");
        assertTrue(report.getFilesProcessed().contains("TODO.md"),
                "file tracking should include TODO.md in processed files");
    }

    @Test
    @DisplayName("File tracking should reject null file path")
    void addFileProcessed_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.addFileProcessed(null),
                "file tracking should throw when file path is null");
    }

    @Test
    @DisplayName("Uncompleted list should filter entries correctly")
    void getUncompletedTasks_filtersCorrectly() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addTask(new TodoTask("f.md", 3, "uncompleted2", null, false));

        List<TodoTask> uncompleted = report.getUncompletedTasks();

        assertEquals(2, uncompleted.size(), "uncompleted list should return two task entries");
        assertTrue(uncompleted.stream().allMatch(TodoTask::isUncompleted),
                "uncompleted list should include only open entries because completed items are filtered out");
    }

    @Test
    @DisplayName("Completed list should filter entries correctly")
    void getCompletedTasks_filtersCorrectly() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addTask(new TodoTask("f.md", 3, "completed2", null, true));

        List<TodoTask> completed = report.getCompletedTasks();

        assertEquals(2, completed.size(), "completed list should return two task entries");
        assertTrue(completed.stream().allMatch(TodoTask::isCompleted),
                "completed list should include only completed task entries");
    }

    @Test
    @DisplayName("Uncompleted flag should show when tasks are open")
    void hasUncompletedTasks_returnsTrueWhenPresent() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        assertTrue(report.hasUncompletedTasks(),
                "uncompleted flag should show open tasks are present because enforcement needs visibility");
    }

    @Test
    @DisplayName("Uncompleted flag should be false when all tasks are done")
    void hasUncompletedTasks_returnsFalseWhenAllCompleted() {
        report.addTask(new TodoTask("f.md", 1, "completed", null, true));
        assertFalse(report.hasUncompletedTasks(),
                "uncompleted flag should be false when every entry is completed because none remain open");
    }

    @Test
    @DisplayName("Merge should combine task lists from sources")
    void merge_combinesTasks() {
        TodoReport other = new TodoReport();
        report.addTask(new TodoTask("f1.md", 1, "task1", null, false));
        other.addTask(new TodoTask("f2.md", 1, "task2", null, true));

        report.merge(other);

        assertEquals(2, report.getTotalCount(),
                "merge should combine total task count correctly because counts are additive");
        assertEquals(1, report.getUncompletedCount(), "merge should combine uncompleted task count correctly");
        assertEquals(1, report.getCompletedCount(), "merge should combine completed task count correctly");
    }

    @Test
    @DisplayName("Merge should combine processed file entries")
    void merge_combinesFilesProcessed() {
        TodoReport other = new TodoReport();
        report.addFileProcessed("file1.md");
        other.addFileProcessed("file2.md");

        report.merge(other);

        assertEquals(2, report.getFileCount(), "merge should combine processed file count correctly");
        assertTrue(report.getFilesProcessed().contains("file1.md"), "merge should include file1.md in merged list");
        assertTrue(report.getFilesProcessed().contains("file2.md"), "merge should include file2.md in merged list");
    }

    @Test
    @DisplayName("Merge should reject null summary arguments early")
    void merge_withNull_throwsException() {
        assertThrows(NullPointerException.class, () -> report.merge(null),
                "merge should throw when other summary is null because merge needs data");
    }

    @Test
    @DisplayName("All tasks view should be unmodifiable by callers")
    void getAllTasks_returnsUnmodifiableList() {
        report.addTask(new TodoTask("f.md", 1, "task", null, false));
        List<TodoTask> tasks = report.getAllTasks();

        assertThrows(UnsupportedOperationException.class, () ->
                        tasks.add(new TodoTask("f.md", 2, "new", null, false)),
                "all tasks view should reject additions to the list");
    }

    @Test
    @DisplayName("All tasks view should include added tasks")
    void getAllTasks_returnsAddedTasks() {
        TodoTask task1 = new TodoTask("f.md", 1, "task1", null, false);
        TodoTask task2 = new TodoTask("f.md", 2, "task2", null, true);
        report.addTask(task1);
        report.addTask(task2);

        List<TodoTask> all = report.getAllTasks();

        assertEquals(2, all.size(), "all tasks view should return two entries");
        assertTrue(all.contains(task1), all + " should contain " + task1);
        assertTrue(all.contains(task2), all + " should contain " + task2);
    }

    @Test
    @DisplayName("Processed files view should be unmodifiable")
    void getFilesProcessed_returnsUnmodifiableList() {
        report.addFileProcessed("file.md");
        List<String> files = report.getFilesProcessed();

        assertThrows(UnsupportedOperationException.class, () -> files.add("new.md"),
                "processed files view should reject additions to the list");
    }

    @Test
    @DisplayName("ToString output should include relevant count values")
    void toString_containsRelevantInfo() {
        report.addTask(new TodoTask("f.md", 1, "uncompleted", null, false));
        report.addTask(new TodoTask("f.md", 2, "completed", null, true));
        report.addFileProcessed("f.md");

        String str = report.toString();

        String totalCount = "2";
        String singleCount = "1";
        assertTrue(str.contains(totalCount),
                "toString output should include total count " + totalCount + " in: " + str);  // total
        assertTrue(str.contains(singleCount),
                "toString output should include count " + singleCount + " in: " + str);  // uncompleted and completed counts
    }

    @Test
    @DisplayName("Mixed tasks should yield expected counts")
    void counts_withMixedTasks() {
        report.addTask(new TodoTask("f.md", 1, "t1", null, false));
        report.addTask(new TodoTask("f.md", 2, "t2", null, true));
        report.addTask(new TodoTask("f.md", 3, "t3", null, false));
        report.addTask(new TodoTask("f.md", 4, "t4", null, true));
        report.addTask(new TodoTask("f.md", 5, "t5", null, false));

        assertEquals(5, report.getTotalCount(), "mixed set should count total tasks correctly");
        assertEquals(3, report.getUncompletedCount(), "mixed set should count open tasks correctly");
        assertEquals(2, report.getCompletedCount(), "mixed set should count completed tasks correctly");
    }
}
