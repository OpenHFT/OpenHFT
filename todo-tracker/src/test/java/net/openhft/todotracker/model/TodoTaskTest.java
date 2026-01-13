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

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Todo model retains fields and status flags")
class TodoTaskTest {

    @Test
    @DisplayName("Constructor with all fields creates a valid entry")
    void constructorWithAllFields_createsValidTask() {
        TodoTask task = new TodoTask("TODO.md", 10, "Do something", "Phase 1", false, 1, 'M');

        assertEquals("TODO.md", task.getFilePath(), "model should retain file path from constructor");
        assertEquals(10, task.getLineNumber(), "model should retain line number from constructor");
        assertEquals("Do something", task.getText(), "model should retain text from constructor");
        assertEquals("Phase 1", task.getContext(), "model should retain context from constructor");
        assertFalse(task.isCompleted(), "model should mark completed flag as false");
        assertTrue(task.isUncompleted(), "model should mark entry as uncompleted state");
        assertEquals(1, task.getPriority(), "model should retain priority value from constructor");
        assertEquals('M', task.getEffort(), "model should retain effort value from constructor");
    }

    @Test
    @DisplayName("Constructor without priority effort creates a valid entry")
    void constructorWithoutPriorityEffort_createsValidTask() {
        TodoTask task = new TodoTask("TODO.md", 5, "Simple task", null, true);

        assertEquals("TODO.md", task.getFilePath(), "instance should retain file path without priority");
        assertEquals(5, task.getLineNumber(), "instance should retain line number without priority");
        assertEquals("Simple task", task.getText(), "instance should retain text without priority");
        assertNull(task.getContext(), "instance should allow null context without priority");
        assertTrue(task.isCompleted(), "instance should mark completed flag as true");
        assertFalse(task.isUncompleted(), "instance should mark entry as completed state");
        assertNull(task.getPriority(), "instance should leave priority value as null");
        assertNull(task.getEffort(), "instance should leave effort value as null");
    }

    @Test
    @DisplayName("Constructor with null file path throws exception")
    void constructorWithNullFilePath_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new TodoTask(null, 1, "text", null, false), "constructor should throw when file path is null");
    }

    @Test
    @DisplayName("Constructor with null text throws exception")
    void constructorWithNullText_throwsException() {
        assertThrows(NullPointerException.class, () ->
                new TodoTask("file.md", 1, null, null, false), "constructor should throw when text is null");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    @DisplayName("Constructor with invalid line number throws exception")
    void constructorWithInvalidLineNumber_throwsException(int lineNumber) {
        assertThrows(IllegalArgumentException.class, () ->
                        new TodoTask("file.md", lineNumber, "text", null, false),
                "constructor should throw when line number is invalid");
    }

    @Test
    @DisplayName("Constructor with null context value is allowed because headings are optional")
    void constructorWithNullContext_isAllowed() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNull(task.getContext(), "constructor should allow null context values because headings are optional");
    }

    @Test
    @DisplayName("Priority flag should be present when value is set explicitly")
    void hasPriority_returnsTrueWhenSet() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, 2, null);
        assertTrue(task.hasPriority(), "priority flag should be present when set because the value is provided");
        assertEquals(2, task.getPriority(), "priority value should return when set");
    }

    @Test
    @DisplayName("Priority flag should be absent when value is null or missing")
    void hasPriority_returnsFalseWhenNull() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, null, 'S');
        assertFalse(task.hasPriority(), "priority flag should be absent when null because tag is missing");
        assertNull(task.getPriority(), "priority value should be null when missing");
    }

    @Test
    @DisplayName("Effort flag should be present when value is set explicitly")
    void hasEffort_returnsTrueWhenSet() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, null, 'L');
        assertTrue(task.hasEffort(), "effort flag should be present when set because the value is provided");
        assertEquals('L', task.getEffort(), "effort value should return when set");
    }

    @Test
    @DisplayName("Effort flag should be absent when value is null or missing")
    void hasEffort_returnsFalseWhenNull() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false, 1, null);
        assertFalse(task.hasEffort(), "effort flag should be absent when null because tag is missing");
        assertNull(task.getEffort(), "effort value should be null when missing");
    }

    @Test
    @DisplayName("Location string should format correctly for display")
    void toLocationString_formatsCorrectly() {
        TodoTask task = new TodoTask("path/to/TODO.md", 42, "text", null, false);
        assertEquals("path/to/TODO.md:42", task.toLocationString(),
                "location string should format as path and line");
    }

    @Test
    @DisplayName("Formatted string should include priority and effort")
    void toFormattedString_withPriorityAndEffort() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, 1, 'M');
        assertEquals("[P1] [E:M] Implement feature", task.toFormattedString(),
                "formatted string should include priority and effort tags");
    }

    @Test
    @DisplayName("Formatted string should include priority tag only")
    void toFormattedString_withPriorityOnly() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, 2, null);
        assertEquals("[P2] Implement feature", task.toFormattedString(),
                "formatted string should include priority when effort missing");
    }

    @Test
    @DisplayName("Formatted string should include effort tag only")
    void toFormattedString_withEffortOnly() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false, null, 'S');
        assertEquals("[E:S] Implement feature", task.toFormattedString(),
                "formatted string should include effort when priority missing");
    }

    @Test
    @DisplayName("Formatted string should exclude tags when absent")
    void toFormattedString_withNoTags() {
        TodoTask task = new TodoTask("file.md", 1, "Implement feature", null, false);
        assertEquals("Implement feature", task.toFormattedString(),
                "formatted string should keep text with no tags");
    }

    @Test
    @DisplayName("Entries should compare equal when values match")
    void equals_sameValues_returnsTrue() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", "context", true, 1, 'M');
        TodoTask task2 = new TodoTask("file.md", 1, "text", "context", true, 1, 'M');
        assertEquals(task1, task2, "entries should be equal for identical values");
        assertEquals(task1.hashCode(), task2.hashCode(), "hashCode should match for identical values");
    }

    @Test
    @DisplayName("Equality should fail when file path differs")
    void equals_differentFilePath_returnsFalse() {
        TodoTask task1 = new TodoTask("file1.md", 1, "text", null, false);
        TodoTask task2 = new TodoTask("file2.md", 1, "text", null, false);
        assertNotEquals(task1, task2,
                "equality should fail when file path differs because path is part of identity");
    }

    @Test
    @DisplayName("Equality should fail when line number differs between entries")
    void equals_differentLineNumber_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false);
        TodoTask task2 = new TodoTask("file.md", 2, "text", null, false);
        assertNotEquals(task1, task2,
                "equality should fail when line numbers differ because location is part of identity");
    }

    @Test
    @DisplayName("Equality should fail when text differs between entries")
    void equals_differentText_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text1", null, false);
        TodoTask task2 = new TodoTask("file.md", 1, "text2", null, false);
        assertNotEquals(task1, task2,
                "equality should fail when text differs because content is part of identity");
    }

    @Test
    @DisplayName("Equality should fail when context differs between entries")
    void equals_differentContext_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", "context1", false);
        TodoTask task2 = new TodoTask("file.md", 1, "text", "context2", false);
        assertNotEquals(task1, task2,
                "equality should fail when context differs because context is part of identity");
    }

    @Test
    @DisplayName("Equality should fail when completion flag differs")
    void equals_differentCompleted_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, true);
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals(task1, task2, "equality should fail when completed flag differs");
    }

    @Test
    @DisplayName("Equality should fail when priority differs between entries")
    void equals_differentPriority_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false, 1, null);
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false, 2, null);
        assertNotEquals(task1, task2,
                "equality should fail when priority differs because priority affects identity");
    }

    @Test
    @DisplayName("Equality should fail when effort differs between entries")
    void equals_differentEffort_returnsFalse() {
        TodoTask task1 = new TodoTask("file.md", 1, "text", null, false, null, 'S');
        TodoTask task2 = new TodoTask("file.md", 1, "text", null, false, null, 'L');
        assertNotEquals(task1, task2,
                "equality should fail when effort differs because effort affects identity");
    }

    @Test
    @DisplayName("Instance should not equal a null reference in equality checks")
    void equals_null_returnsFalse() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals(null, task, "instance should not equal a null reference because null is not a valid entry");
    }

    @Test
    @DisplayName("Instance should not equal a different object type")
    void equals_differentType_returnsFalse() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertNotEquals("not a task", task,
                "instance should not equal a different object type because types are incompatible");
    }

    @Test
    @DisplayName("Instance should equal the same reference object")
    void equals_sameInstance_returnsTrue() {
        TodoTask task = new TodoTask("file.md", 1, "text", null, false);
        assertEquals(task, task, "instance should equal the same reference value because identity is stable");
    }

    @Test
    @DisplayName("Different entries should produce different hash codes")
    void hashCode_differentTasks_produceDifferentHashes() {
        TodoTask task1 = new TodoTask("file1.md", 1, "text1", null, false);
        TodoTask task2 = new TodoTask("file2.md", 2, "text2", null, true);

        Set<TodoTask> set = new HashSet<>();
        set.add(task1);
        set.add(task2);

        assertEquals(2, set.size(), "HashSet should contain both entries with different hashes");
    }

    @Test
    @DisplayName("ToString output should include file, context, and flag values")
    void toString_containsAllFields() {
        TodoTask task = new TodoTask("file.md", 10, "Do it", "Phase 1", true, 1, 'M');
        String str = task.toString();

        String expectedFile = "file.md";
        assertTrue(str.contains(expectedFile),
                "text form should include file path " + expectedFile + " in: " + str);
        String expectedLine = "10";
        assertTrue(str.contains(expectedLine),
                "text form should include line number " + expectedLine + " in: " + str);
        String expectedText = "Do it";
        assertTrue(str.contains(expectedText),
                "text form should include text value " + expectedText + " in: " + str);
        String expectedContext = "Phase 1";
        assertTrue(str.contains(expectedContext),
                "text form should include context " + expectedContext + " in: " + str);
        String expectedCompleted = "true";
        assertTrue(str.contains(expectedCompleted),
                "text form should include completion flag " + expectedCompleted + " in: " + str);
        String expectedPriority = "1";
        assertTrue(str.contains(expectedPriority),
                "text form should include priority value " + expectedPriority + " in: " + str);
        String expectedEffort = "M";
        assertTrue(str.contains(expectedEffort),
                "text form should include effort value " + expectedEffort + " in: " + str);
    }

    @Test
    @DisplayName("Blank text is accepted by model")
    void emptyText_isAllowed() {
        TodoTask task = new TodoTask("file.md", 1, "", null, false);
        assertEquals("", task.getText(), "model should allow empty text values");
    }
}
