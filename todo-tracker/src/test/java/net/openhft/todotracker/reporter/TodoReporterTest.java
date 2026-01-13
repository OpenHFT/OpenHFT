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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reporter should cover todo reporting scenarios")
class TodoReporterTest {

    @Mock
    private Log log;

    @Captor
    private ArgumentCaptor<String> warnCaptor;

    @Captor
    private ArgumentCaptor<String> infoCaptor;

    private TodoReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new TodoReporter(log, 10, true);
    }

    @Test
    @DisplayName("Reporter should constructor null log throws exception")
    void constructor_nullLog_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new TodoReporter(null, 10, true),
                "output should throw when log is null because logging requires a sink");
    }

    @Test
    @DisplayName("Reporter should report uncompleted null report throws exception")
    void reportUncompleted_nullReport_throwsException() {
        assertThrows(NullPointerException.class, () ->
                reporter.reportUncompleted(null),
                "output should throw when report is null because no data is available");
    }

    @Test
    @DisplayName("Reporter should skip logging when the task list is empty")
    void reportUncompleted_emptyReport_doesNothing() {
        TodoReport report = new TodoReport();

        reporter.reportUncompleted(report);

        verify(log, never()).warn(any(CharSequence.class));
    }

    @Test
    @DisplayName("Reporter should report uncompleted single task outputs correct format")
    void reportUncompleted_singleTask_outputsCorrectFormat() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 10, "Fix the bug", "Phase 1", false, 1, 'M'));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertTrue(warnings.stream().anyMatch(s -> s.contains("TODO TRACKER")),
                "output should include TODO TRACKER header in output because it flags the failure context");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("TODO.md")),
                "output should include TODO.md file name in output because file location aids fixes");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("[Line 10]")),
                "output should include a line marker in output list");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("[P1]")),
                "output should include [P1] priority marker in output");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("[E:M]")),
                "output should include [E:M] effort marker in output");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("Fix the bug")),
                "output should include task text Fix the bug");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("Phase 1")),
                "output should include context Phase 1 line");
    }

    @Test
    @DisplayName("Reporter should report uncompleted task without context skips context line")
    void reportUncompleted_taskWithoutContext_skipsContextLine() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 5, "Task text", null, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertFalse(warnings.stream().anyMatch(s -> s.contains("Context:")),
                "output should not include Context: when context is null because no header exists");
    }

    @Test
    @DisplayName("Reporter should render context line without heading markup")
    void reportUncompleted_contextLine_withoutHeadingMarkup() {
        TodoReport report = new TodoReport();
        String contextValue = "Phase 1";
        report.addTask(new TodoTask("TODO.md", 5, "Task text", contextValue, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        String contextLine = warnCaptor.getAllValues().stream()
                .filter(line -> line.contains("Context:"))
                .findFirst().orElse("");

        assertFalse(contextLine.isEmpty(), "output should emit context line for " + contextValue);
        assertEquals("     Context: " + contextValue, contextLine,
                "output should render exact context line without heading markup");
    }

    @Test
    @DisplayName("Reporter should report uncompleted show context false skips context")
    void reportUncompleted_showContextFalse_skipsContext() {
        TodoReporter noContextReporter = new TodoReporter(log, 10, false);
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 5, "Task", "Some Context", false));

        noContextReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertFalse(warnings.stream().anyMatch(s -> s.contains("Context:")),
                "output should not include Context: when showContext is false");
    }

    @Test
    @DisplayName("Reporter should report uncompleted max tasks limit shows more message")
    void reportUncompleted_maxTasksLimit_showsMoreMessage() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Task 1", null, false));
        report.addTask(new TodoTask("TODO.md", 2, "Task 2", null, false));
        report.addTask(new TodoTask("TODO.md", 3, "Task 3", null, false));
        report.addTask(new TodoTask("TODO.md", 4, "Task 4", null, false));

        final TodoReporter limitedReporter = new TodoReporter(log, 2, true);
        limitedReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertTrue(warnings.stream().anyMatch(s -> s.contains("2 more task(s)")),
                "output should include 2 more task(s) notice for limit because output is truncated");
    }

    @Test
    @DisplayName("Reporter should report uncompleted max tasks zero shows all")
    void reportUncompleted_maxTasksZero_showsAll() {
        TodoReporter unlimitedReporter = new TodoReporter(log, 0, true);
        TodoReport report = new TodoReport();
        for (int i = 1; i <= 20; i++) {
            report.addTask(new TodoTask("TODO.md", i, "Task " + i, null, false));
        }

        unlimitedReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Should not show "more tasks" message
        assertFalse(warnings.stream().anyMatch(s -> s.contains("more task(s)")),
                "output should not include more task(s) when unlimited because all entries are shown");
    }

    @Test
    @DisplayName("Reporter should report uncompleted multipl files groups by file")
    void reportUncompleted_multiplFiles_groupsByFile() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("file1.md", 1, "Task 1", null, false));
        report.addTask(new TodoTask("file2.md", 1, "Task 2", null, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertTrue(warnings.stream().anyMatch(s -> s.contains("file1.md")),
                "output should include file1.md header in warnings");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("file2.md")),
                "output should include file2.md header in warnings");
    }

    @Test
    @DisplayName("Reporter should report uncompleted includes instructions")
    void reportUncompleted_includesInstructions() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Task", null, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertTrue(warnings.stream().anyMatch(s -> s.contains("TO RESOLVE")),
                "output should include TO RESOLVE instructions header");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("- [ ]")),
                "output should include - [ ] instruction example");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("- [x]")),
                "output should include - [x] instruction example");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("mvn verify")),
                "output should include mvn verify instruction line");
    }

    @Test
    @DisplayName("Reporter should report uncompleted task without tags omits tags")
    void reportUncompleted_taskWithoutTags_omitsTags() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Simple task", null, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Should contain task text but not priority/effort markers
        assertTrue(warnings.stream().anyMatch(s -> s.contains("Simple task")),
                "output should include Simple task text in output");
        String taskLine = warnings.stream()
                .filter(s -> s.contains("Simple task"))
                .findFirst()
                .orElse("");
        assertFalse(taskLine.contains("[P"), taskLine + " should not contain [P tag");
        assertFalse(taskLine.contains("[E:"), taskLine + " should not contain [E: tag");
    }

    @Test
    @DisplayName("Reporter should generate summary null report throws exception")
    void generateSummary_nullReport_throwsException() {
        assertThrows(NullPointerException.class, () ->
                reporter.generateSummary(null), "output should throw when summary report is null");
    }

    @Test
    @DisplayName("Reporter should generate summary empty report")
    void generateSummary_emptyReport() {
        TodoReport report = new TodoReport();

        String summary = reporter.generateSummary(report);

        assertEquals("0 task(s) (0 completed, 0 uncompleted) in 0 file(s)", summary,
                "output should summarise empty report with zeros");
    }

    @Test
    @DisplayName("Reporter should generate summary mixed tasks")
    void generateSummary_mixedTasks() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("f.md", 1, "t1", null, true));
        report.addTask(new TodoTask("f.md", 2, "t2", null, false));
        report.addTask(new TodoTask("f.md", 3, "t3", null, false));
        report.addFileProcessed("f.md");

        String summary = reporter.generateSummary(report);

        assertEquals("3 task(s) (1 completed, 2 uncompleted) in 1 file(s)", summary,
                "output should summarise mixed tasks with totals");
    }

    @Test
    @DisplayName("Reporter should report no files found outputs correct message")
    void reportNoFilesFound_outputsCorrectMessage() {
        List<String> paths = Arrays.asList("TODO.md", "todo/TODO.md");

        reporter.reportNoFilesFound(paths);

        verify(log, times(2)).info(infoCaptor.capture());
        List<String> infos = infoCaptor.getAllValues();

        assertTrue(infos.get(0).contains("No TODO files found"),
                "output should mention No TODO files found message");
        assertTrue(infos.get(0).contains("TODO.md"), "output should list TODO.md in missing paths");
        assertTrue(infos.get(0).contains("todo/TODO.md"), "output should list todo/TODO.md in missing paths");
        assertTrue(infos.get(1).contains("Check passed"), "output should include Check passed status line");
    }

    @Test
    @DisplayName("Reporter should report all completed outputs success message")
    void reportAllCompleted_outputsSuccessMessage() {
        reporter.reportAllCompleted();

        verify(log, times(2)).info(infoCaptor.capture());
        List<String> infos = infoCaptor.getAllValues();

        assertTrue(infos.get(0).contains("No uncompleted tasks"),
                "output should mention No uncompleted tasks line");
        assertTrue(infos.get(1).contains("Check passed"),
                "output should include Check passed status line for completion");
    }

    @Test
    @DisplayName("Reporter should report skipped outputs skip message")
    void reportSkipped_outputsSkipMessage() {
        reporter.reportSkipped();

        verify(log).info(infoCaptor.capture());
        assertTrue(infoCaptor.getValue().contains("Skipping execution"),
                "output should mention Skipping execution in info");
    }

    @Test
    @DisplayName("Reporter should report uncompleted max tasks reached across files stops at limit")
    void reportUncompleted_maxTasksReachedAcrossFiles_stopsAtLimit() {
        // Create tasks in multiple files - maxTasks=1 should stop after first task
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("file1.md", 1, "Task in file 1", null, false));
        report.addTask(new TodoTask("file2.md", 1, "Task in file 2", null, false));
        report.addTask(new TodoTask("file3.md", 1, "Task in file 3", null, false));

        TodoReporter limitedReporter = new TodoReporter(log, 1, false);
        limitedReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Should show "2 more" message (3 total - 1 displayed = 2 remaining)
        assertTrue(warnings.stream().anyMatch(s -> s.contains("2 more task(s)")),
                "output should include 2 more task(s) notice across files");
    }

    @Test
    @DisplayName("Reporter should report uncompleted exactly at max tasks shows no more message")
    void reportUncompleted_exactlyAtMaxTasks_showsNoMoreMessage() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Task 1", null, false));
        report.addTask(new TodoTask("TODO.md", 2, "Task 2", null, false));

        // maxTasks=2, exactly 2 tasks -> should NOT show "more" message
        TodoReporter exactReporter = new TodoReporter(log, 2, false);
        exactReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Both tasks shown, no "more" message
        assertFalse(warnings.stream().anyMatch(s -> s.contains("more task(s)")),
                "output should not include more task(s) at exact limit");
    }

    @Test
    @DisplayName("Reporter should report uncompleted one over max tasks shows one more message")
    void reportUncompleted_oneOverMaxTasks_showsOneMoreMessage() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Task 1", null, false));
        report.addTask(new TodoTask("TODO.md", 2, "Task 2", null, false));
        report.addTask(new TodoTask("TODO.md", 3, "Task 3", null, false));

        // maxTasks=2, 3 tasks -> should show "1 more" message
        TodoReporter exactReporter = new TodoReporter(log, 2, false);
        exactReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        assertTrue(warnings.stream().anyMatch(s -> s.contains("1 more task(s)")),
                "output should include 1 more task(s) notice at limit");
    }

    @Test
    @DisplayName("Reporter should report uncompleted task with priority only shows priority not effort")
    void reportUncompleted_taskWithPriorityOnly_showsPriorityNotEffort() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Priority task", null, false, 2, null));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        String taskLine = warnCaptor.getAllValues().stream()
                .filter(s -> s.contains("Priority task"))
                .findFirst().orElse("");

        String expectedPriority = "[P2]";
        assertTrue(taskLine.contains(expectedPriority),
                "output should include priority marker " + expectedPriority + " in " + taskLine);
        assertFalse(taskLine.contains("[E:"), taskLine + " should not contain [E: tag");
    }

    @Test
    @DisplayName("Reporter should report uncompleted task with effort only shows effort not priority")
    void reportUncompleted_taskWithEffortOnly_showsEffortNotPriority() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Effort task", null, false, null, 'L'));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        String taskLine = warnCaptor.getAllValues().stream()
                .filter(s -> s.contains("Effort task"))
                .findFirst().orElse("");

        assertFalse(taskLine.contains("[P"), taskLine + " should not contain [P tag");
        String expectedEffort = "[E:L]";
        assertTrue(taskLine.contains(expectedEffort),
                "output should include effort marker " + expectedEffort + " in " + taskLine);
    }

    @Test
    @DisplayName("Reporter should number tasks sequentially starting from 1")
    void reportUncompleted_multipleTasks_numbersSequentially() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 10, "First task", null, false));
        report.addTask(new TodoTask("TODO.md", 20, "Second task", null, false));
        report.addTask(new TodoTask("TODO.md", 30, "Third task", null, false));

        reporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Verify sequential numbering 1, 2, 3
        assertTrue(warnings.stream().anyMatch(s -> s.contains("1.") && s.contains("First task")),
                "First task should be numbered 1");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("2.") && s.contains("Second task")),
                "Second task should be numbered 2");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("3.") && s.contains("Third task")),
                "Third task should be numbered 3");
    }

    @Test
    @DisplayName("Reporter should display exactly maxTasks tasks when more exist")
    void reportUncompleted_moreThanMax_displaysExactlyMaxTasks() {
        TodoReport report = new TodoReport();
        report.addTask(new TodoTask("TODO.md", 1, "Task A", null, false));
        report.addTask(new TodoTask("TODO.md", 2, "Task B", null, false));
        report.addTask(new TodoTask("TODO.md", 3, "Task C", null, false));

        TodoReporter limitedReporter = new TodoReporter(log, 2, false);
        limitedReporter.reportUncompleted(report);

        verify(log, atLeastOnce()).warn(warnCaptor.capture());
        List<String> warnings = warnCaptor.getAllValues();

        // Should show tasks 1 and 2, but NOT task 3
        assertTrue(warnings.stream().anyMatch(s -> s.contains("1.") && s.contains("Task A")),
                "output should display task 1");
        assertTrue(warnings.stream().anyMatch(s -> s.contains("2.") && s.contains("Task B")),
                "output should display task 2");
        assertFalse(warnings.stream().anyMatch(s -> s.contains("Task C")),
                "output should NOT display task 3 beyond limit");
    }
}
