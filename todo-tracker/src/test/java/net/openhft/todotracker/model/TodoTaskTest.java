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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Todo task model retains fields and status flags")
class TodoTaskTest {

    @Test
    @DisplayName("Task should constructor with all fields creates valid task")
    void constructorWithAllFields_createsValidTask() {
        TodoTask task = new TodoTask("TODO.md", 10, "Do something", "Phase 1", false, 1, 'M');

        assertEquals("TODO.md", task.getFilePath(), "task should retain file path from constructor");
        assertEquals(10, task.getLineNumber(), "task should retain line number from constructor");
        assertEquals("Do something", task.getText(), "task should retain task text from constructor");
        assertEquals("Phase 1", task.getContext(), "task should retain context from constructor");
        assertFalse(task.isCompleted(), "task should mark completed flag as false");
        assertTrue(task.isUncompleted(), "task should mark task as uncompleted state");
        assertEquals(1, task.getPriority(), "task should retain priority value from constructor");
        assertEquals('M', task.getEffort(), "task should retain effort value from constructor");
    }

    @Test
    @DisplayName("Task should constructor without priority effort creates valid task")
    void constructorWithoutPriorityEffort_createsValidTask() {
        TodoTask task = new TodoTask("TODO.md", 5, "Simple task", null, true);

        assertEquals("TODO.md", task.getFilePath(), "task should retain file path without priority");
        assertEquals(5, task.getLineNumber(), "task should retain line number without priority");
        assertEquals("Simple task", task.getText(), "task should retain task text without priority");
        assertNull(task.getContext(), "task should allow null context without priority");
        assertTrue(task.isCompleted(), "task should mark completed flag as true");
        assertFalse(task.isUncompleted(), "task should mark task as completed state");
        assertNull(task.getPriority(), "task should leave priority value as null");
        assertNull(task.getEffort(), "task should leave effort value as null");
    }

    @Test
    @DisplayName("Task should constructor with null file path throws exception")
    void constructorWithNullFilePath_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new TodoTask(null, 1, "text", null, false), "task should throw when file path is null");
    }

    @Test
    @DisplayName("Task should constructor with null text throws exception")
    void constructorWithNullText_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new TodoTask("file.md", 1, null, null, false), "task should throw when task text is null");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Task should constructor with invalid line number throws exception")
    void constructorWithInvalidLineNumber_throwsException(int lineNumber) {
        assertThrows(IllegalArgumentException.class, () ->
                        new TodoTask("file.md", lineNumber, "text", null, false),
                "task should throw when line number is invalid");
    }

    @Test
    @DisplayName("Task should constructor with null context is allowed")
    void constructorWithNullContext_isAllowed() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNull(task.getContext(), "task should allow null context in constructor");
    }

    @Test
    @DisplayName("Task should report priority flag as present when value is set")
    void hasPriority_returnsTrueWhenSet() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, 2, null);
        assertTrue(task.hasPriority(), "task should report priority present when set");
        assertEquals(2, task.getPriority(), "task should return priority value when set");
    }

    @Test
    @DisplayName("Task should report priority as absent when value is null")
    void hasPriority_returnsFalseWhenNull() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, null, 'S');
        assertFalse(task.hasPriority(), "task should report no priority when null");
        assertNull(task.getPriority(), "task should return null priority when missing");
    }

    @Test
    @DisplayName("Task should report effort flag as present when value is set")
    void hasEffort_returnsTrueWhenSet() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, null, 'L');
        assertTrue(task.hasEffort(), "task should report effort present when set");
        assertEquals('L', task.getEffort(), "task should return effort value when set");
    }

    @Test
    @DisplayName("Task should report effort as absent when value is null")
    void hasEffort_returnsFalseWhenNull() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, 1, null);
        assertFalse(task.hasEffort(), "task should report no effort when null");
        assertNull(task.getEffort(), "task should return null effort when missing");
    }

    @Test
    @DisplayName("Task should to location string formats correctly")
    void toLocationString_formatsCorrectly() {
        TodoTask task = new TodoTask("path/to/TODO.md", 42, "text", null, false);
        assertEquals("path/to/TODO.md:42", task.toLocationString(),
                "task should format location as path and line");
    }

    @Test
    @DisplayName("Task should to formatted string with priority and effort")
    void toFormattedString_withPriorityAndEffort() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, 1, 'M');
        assertEquals("[P1] [E:M] Implement feature", task.toFormattedString(),
                "task should format priority and effort tags in text");
    }

    @Test
    @DisplayName("Task should to formatted string with priority only")
    void toFormattedString_withPriorityOnly() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, 2, null);
        assertEquals("[P2] Implement feature", task.toFormattedString(),
                "task should format priority tag when effort missing");
    }

    @Test
    @DisplayName("Task should to formatted string with effort only")
    void toFormattedString_withEffortOnly() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, null, 'S');
        assertEquals("[E:S] Implement feature", task.toFormattedString(),
                "task should format effort tag when priority missing");
    }

    @Test
    @DisplayName("Task should to formatted string with no tags")
    void toFormattedString_withNoTags() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false);
        assertEquals("Implement feature", task.toFormattedString(),
                "task should format text with no tags");
    }

    @Test
    @DisplayName("Task should compare equal when all values match")
    void equals_sameValues_returnsTrue() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", "context", true, 1, 'M');
        TodoTask task2 = new TodoTask("file.md", 1, "text", "context", true, 1, 'M');
        assertEquals(task1, task2, "task should be equal for identical values");
        assertEquals(task1.hashCode(), task2.hashCode(), "hashCode should match for identical values");
    }

    @Test
    @DisplayName("Task should equals different file path returns false")
    void equals_differentFilePath_returnsFalse() {
        TodoTask task1 = new TodoTask("file1.md", 1, "text", null, false);
        TodoTask task2 = new TodoTask("file2.md", 1, "text", null, false);
        assertNotEquals(task1, task2, "task should not equal when file path differs");
    }

    @Test
    @DisplayName("Task should not equal when line number differs")
    void equals_differentLineNumber_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false);
        TodoTask task2 = new TodoTask("file.md", 2, "text", null, false);
        assertNotEquals(task1, task2,
                "task should not equal when line numbers differ between tasks");
    }

    @Test
    @DisplayName("Task should not equal when task text differs")
    void equals_differentText_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text1", null, false);
        TodoTask task2 = new TodoTask("file.md", 1, "text2", null, false);
        assertNotEquals(task1, task2,
                "task should not equal when task text differs between instances");
    }

    @Test
    @DisplayName("Task should not equal when context value differs")
    void equals_differentContext_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", "context1", false);
        TodoTask task2 = new TodoTask("file.md", 1, "text", "context2", false);
        assertNotEquals(task1, task2, "task should not equal when context differs");
    }

    @Test
    @DisplayName("Task should not equal when completion flag differs")
    void equals_differentCompleted_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, true);
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals(task1, task2, "task should not equal when completed flag differs");
    }

    @Test
    @DisplayName("Task should not equal when priority value differs")
    void equals_differentPriority_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false, 1, null);
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false, 2, null);
        assertNotEquals(task1, task2, "task should not equal when priority differs");
    }

    @Test
    @DisplayName("Task should not equal when effort value differs")
    void equals_differentEffort_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false, null, 'S');
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false, null, 'L');
        assertNotEquals(task1, task2, "task should not equal when effort differs");
    }

    @Test
    @DisplayName("Task instance should not equal a null object reference")
    void equals_null_returnsFalse() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals(null, task, "task should not equal a null instance");
    }

    @Test
    @DisplayName("Task should not equal a different object type")
    void equals_differentType_returnsFalse() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals("not a task", task, "task should not equal different object type");
    }

    @Test
    @DisplayName("Task should equal the same instance reference")
    void equals_sameInstance_returnsTrue() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertEquals(task, task, "task should equal same instance reference value");
    }

    @Test
    @DisplayName("Task should render toString with all fields")
    void toString_containsAllFields() {
        TodoTask task = new TodoTask("file.md", 10, "Do it", "Phase 1", true, 1, 'M');
        String str = task.toString();

        String expectedFile = "file.md";
        assertTrue(str.contains(expectedFile),
                "task should include file path " + expectedFile + " in toString: " + str);
        String expectedLine = "10";
        assertTrue(str.contains(expectedLine),
                "task should include line number " + expectedLine + " in toString: " + str);
        String expectedText = "Do it";
        assertTrue(str.contains(expectedText),
                "task should include task text " + expectedText + " in toString: " + str);
        String expectedContext = "Phase 1";
        assertTrue(str.contains(expectedContext),
                "task should include context " + expectedContext + " in toString: " + str);
        String expectedCompleted = "true";
        assertTrue(str.contains(expectedCompleted),
                "task should include completion flag " + expectedCompleted + " in toString: " + str);
        String expectedPriority = "1";
        assertTrue(str.contains(expectedPriority),
                "task should include priority value " + expectedPriority + " in toString: " + str);
        String expectedEffort = "M";
        assertTrue(str.contains(expectedEffort),
                "task should include effort value " + expectedEffort + " in toString: " + str);
    }

    @Test
    @DisplayName("Blank task text is accepted by model")
    void emptyText_isAllowed() {
        TodoTask task = new TodoTask("file.md", 1, "", null, false);
        assertEquals("", task.getText(), "task should allow empty task text value");
    }
}
