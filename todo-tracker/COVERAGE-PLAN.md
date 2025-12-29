# Plan: Cover Remaining 15 Branches

## Summary

Current: 90% branch coverage (15/152 missed)
Target: Increase coverage by testing or removing unreachable code

---

## TodoFileLocator.java (8 missed branches)

### 1. Lines 140-150: Two-arg constructor (2 branches)

**Status**: Never used
**Action**: **REMOVE** - This constructor is not called anywhere in the codebase
**Impact**: -2 missed branches

### 2. Line 197: `getFileName() != null` (1 branch)

**Status**: Already changed to `Objects.requireNonNull`
**Action**: Verify the change compiled - stale report may show old code
**Impact**: Branch removed by code change

### 3. Lines 199-200: Directory skip conditions (4 branches missed across both)

**Status**: Tests exist but `||` short-circuits
**Issue**: Tests for `out`, `dist`, `build` exist but earlier conditions fire first
**Action**: Create isolated tests where ONLY the target directory exists (no .git, node_modules, target)

```java
// Test out in isolation (no other skip directories)
@Test
void findTodoFiles_skipsOutDirectory_inIsolation() throws IOException {
    createFile("out/subdir/TODO.md");  // Put TODO deeper so out/ is visited
    createFile("normal/TODO.md");

    List<File> found = locator.findTodoFiles(tempDir.toFile());

    assertEquals(1, found.size());
    assertTrue(found.get(0).getPath().contains("normal"));
}
```

**Impact**: Cover 4 branches

### 4. Line 209: `visitFileFailed()` callback (1 branch)

**Status**: Never triggered
**Difficulty**: Requires symlink loops or permission denied - platform-specific
**Action**: Skip - defensive code for edge cases
**Alternative**: Could mock `Files.walkFileTree` but adds complexity

### 5. Line 212: IOException catch (1 branch)

**Status**: Never triggered
**Difficulty**: Requires `walkFileTree` to throw IOException
**Action**: Skip - defensive error handling

### 6. Lines 240/243: `getTodoFilePaths()` else branch (1 branch)

**Status**: Never called on exact-path locator
**Action**: Add test calling `getTodoFilePaths()` on locator created with custom paths

```java
@Test
void getTodoFilePaths_exactPathMode_returnsConfiguredPaths() {
    TodoFileLocator locator = new TodoFileLocator(Arrays.asList("A.md", "B.md"));
    List<String> paths = locator.getTodoFilePaths();
    assertEquals(2, paths.size());
    assertTrue(paths.contains("A.md"));
}
```

**Impact**: +1 branch (test already added but may not be running correctly)

---

## CheckTodoMojo.java (5 missed branches)

### 7. Lines 123/127-130: Default pattern locator path (3 branches)

**Status**: Tests always set `todoFiles`, so default path never taken
**Action**: Add test that doesn't set `todoFiles` and has `usePatterns=true`

```java
@Test
void execute_defaultPatternsMode_usesPatternMatching() throws Exception {
    // Don't call mojo.setTodoFiles() - leave it null
    // Create TODO.md that default patterns will find
    createFile("TODO.md", "- [ ] Task");

    // Reset todoFiles to null (don't use custom files)
    mojo.setTodoFiles(null);

    assertThrows(MojoFailureException.class, () -> mojo.execute());
}
```

**Impact**: Cover 3 branches

### 8. Line 159: AsciiDoc format branch

**Status**: Never parses an actual .adoc file through mojo
**Action**: Add test that creates and parses an .adoc file

```java
@Test
void execute_asciidocFile_usesAsciidocParser() throws Exception {
    createFile("todo/sprint.adoc", "== Tasks\n* [ ] AsciiDoc task");
    mojo.setTodoFiles(null);  // Use default patterns

    assertThrows(MojoFailureException.class, () -> mojo.execute());
}
```

**Impact**: +1 branch

### 9. Lines 163-165: IOException handling

**Status**: Requires parser to throw
**Difficulty**: Would need to mock TodoParser or create unreadable file
**Action**: Skip - defensive error handling

### 10. Lines 214-219: Setter methods

**Status**: `setAsciidocContextPattern` and `setUsePatterns` not called
**Action**: Already called in `@BeforeEach` - verify they're being invoked
**Note**: These are test utilities, coverage of setters is low value

---

## TodoTask.java (1 missed branch)

### 11. Line 134: `equals()` with wrong type

**Status**: Never tested with non-TodoTask object
**Action**: Add test

```java
@Test
void equals_wrongType_returnsFalse() {
    TodoTask task = new TodoTask("file.md", 1, "text", null, false);
    assertFalse(task.equals("not a task"));
    assertFalse(task.equals(new Object()));
}
```

**Impact**: +1 branch

---

## TodoReporter.java (1 missed branch)

### 12. Line 81: `displayCount >= effectiveMax` at file loop level

**Status**: Only triggered when maxTasks reached mid-iteration
**Action**: Add test with maxTasks=1 and tasks spread across 2+ files

```java
@Test
void reportUncompleted_stopsAtMaxTasksAcrossFiles() {
    // Create report with tasks in multiple files
    TodoReport report = new TodoReport();
    report.addTask(new TodoTask("file1.md", 1, "Task 1", null, false));
    report.addTask(new TodoTask("file2.md", 1, "Task 2", null, false));

    // Reporter with maxTasks=1
    TodoReporter reporter = new TodoReporter(mockLog, 1, false);
    reporter.reportUncompleted(report);

    // Verify only 1 task was reported
}
```

**Impact**: +1 branch

---

## Implementation Priority

### High Value (Easy, High Impact)

1. Remove two-arg constructor from TodoFileLocator (-2 branches)
2. Add `equals()` wrong type test (+1 branch)
3. Add default patterns mode test (+3 branches)

### Medium Value (Moderate Effort)

4. Fix directory skip tests to work in isolation (+4 branches)
5. Add TodoReporter maxTasks across files test (+1 branch)
6. Verify getTodoFilePaths test is working (+1 branch)

### Low Value (Skip)

7. visitFileFailed - platform-specific, hard to trigger
8. IOException catch - requires complex mocking
9. Setter coverage - test utilities only

---

## Expected Outcome

| Action                     | Branches Fixed |
|----------------------------|----------------|
| Remove two-arg constructor | -2             |
| Fix/add tests              | +10            |
| Skip (defensive code)      | 3 remain       |

**Projected Coverage**: ~97% (3/150 missed)
