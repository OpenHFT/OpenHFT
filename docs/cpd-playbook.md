# CPD (Copy-Paste Detector) Elimination Playbook

## Mission

Systematically eliminate code duplications across Chronicle Software's 1.6M LOC Java codebase to improve maintainability
and comply with ISO 9001 quality standards.

## Current Status (2025-12-09)

### Configuration

- **Tool**: PMD 7.19.0 with maven-pmd-plugin 3.25.0
- **Location**: `/home/peter/Build-All/OpenHFT/root-parent-pom/pom.xml`
- **Current Threshold**: `minimumTokens=100` (~15-20 lines)
- **Mode**: Warn-only (`failOnViolation=false`)
- **Scope**: Includes test code

### Progress Summary

- **Chronicle-Queue**: ✅ **Production code CPD clean** - All major duplications eliminated
    - Eliminated 57-line RollCycles enum duplication across 5 files using Java 8 default interface methods
    - Eliminated test code duplications: RollingResourcesCache (52 lines), QueueWriteDocumentContextTest (75 lines),
      TestDeleteQueueFile (69 lines), PartialUpdateTest (19 lines)
    - Remaining: 2 medium duplications in ChronicleReaderMain/ChronicleHistoryReaderMain (~30 lines each),
      DirectoryListing (~28 lines)
- **Chronicle-Core**: ✅ CPD clean after consolidating test helpers (BackgroundResourceReleaser, reference-counted,
  CleaningRandomAccessFile, StackTrace, time providers) and deduplicating `Maths.hash64`/`StringUtils` radix parsing.
- **Chronicle-Wire**: 95 duplications (down from 106). Eliminated top 7 largest duplications.
    - Addressed 205-line duplication in `TextWireTest`/`YamlWireTest`
    - Addressed 115-line duplication in `OuterClass`
    - Addressed multiple 60-80 line duplications in `TextWireTest`/`YamlWireTest`
    - Addressed 61-line duplication in `WireBug37Test`/`WireBug38Test`
- **Chronicle-Bytes**: 21 duplications (down from 22). Eliminated largest duplication.
    - Addressed 89-line duplication in `BinaryIntArrayReference`/`BinaryLongArrayReference` by extracting
      `AbstractBinaryArrayReference`.

### Completed Refactorings

#### 1. TestFacadeInterfaces Extraction (1,371 tokens, 530 lines) - Queue

**Pattern**: Extracted shared test interfaces to utility class.

#### 2. RollingResourcesCacheTestBase (429 tokens, 34 lines) - Queue

**Pattern**: Template method with abstract base class.

#### 3. RollCycle Default Interface Methods (57 lines, 5 files) - Queue

**Files Modified:**

- `RollCycle.java` (interface) - Added `arithmetic()` abstract method and 6 default methods
- `RollCycles.java` (enum) - Removed 5 duplicated delegation methods
- `LargeRollCycles.java` (enum) - Removed 6 duplicated delegation methods (100 → 64 lines)
- `LegacyRollCycles.java` (enum) - Removed 6 duplicated delegation methods (96 → 60 lines)
- `SparseRollCycles.java` (enum) - Removed 6 duplicated delegation methods (100 → 64 lines)
- `TestRollCycles.java` (enum) - Removed 6 duplicated delegation methods (113 → 77 lines)
- `RollCycleDefaultingTest.MyRollcycle` (test) - Updated to implement `arithmetic()`
- `WeeklyRollCycle.java` (test) - Removed 6 duplicated delegation methods (64 → 38 lines)

**Pattern**: Java 8 default interface methods for shared behavior across enum implementations.
**Impact**: Eliminated ~144+ lines of duplication across production code. All 1,073 tests pass.

#### 4. Test Code Duplications - Queue

**Files Modified:**

- `RollingResourcesCacheTestBase.java` - Added 52 lines of shared test constants
- `RollingResourcesCacheCompatTest.java` - Removed 52 lines of duplicated constants
- `QueueWriteDocumentContextTest.java` - Extracted 75-line YAML assertion to constant
- `TestDeleteQueueFile.java` - Added 2 helper methods, saved 69 lines
- `PartialUpdateTest.java` - Extracted common logic with functional interface, saved 19 lines

**Pattern**: Multiple patterns - shared constants, helper methods, functional interfaces.
**Impact**: Eliminated ~215 lines of test code duplication.

#### 5. AbstractWireTest Extraction (Wire)

**Files Modified:**

- `TextWireTest.java`
- `YamlWireTest.java`
- `AbstractWireTest.java` (created/extended)

**Pattern**: Extracted common test methods (`testSimpleBool`, `testInt64`, `testArrays`, `testSortedSet`,
`testZonedDateTime`, etc.) from `TextWireTest` and `YamlWireTest` into their shared parent `AbstractWireTest`.

#### 6. AbstractPooledOuterClass Extraction (Wire)

**Files Modified:**

- `reordered/OuterClass.java`
- `reuse/OuterClass.java`
- `reuse/AbstractPooledOuterClass.java` (created)

**Pattern**: Extracted common logic for pooling nested marshallable objects into a generic abstract base class.

#### 7. MarshallableObj Extraction (Wire)

**Files Modified:**

- `issue/WireBug37Test.java`
- `issue/WireBug38Test.java`
- `issue/MarshallableObj.java` (created)

**Pattern**: Extracted identical inner class to a standalone class.

#### 8. AbstractBinaryArrayReference Extraction (Bytes)

**Files Modified:**

- `ref/BinaryIntArrayReference.java`
- `ref/BinaryLongArrayReference.java`
- `ref/AbstractBinaryArrayReference.java` (created)

**Pattern**: Extracted common layout constants, fields, and method implementations into an abstract base class for
binary array references.

## Threshold Strategy

### Phase 1 (Current): minimumTokens=300

**Goal**: Address top ~20 largest duplications (analysis-driven)
**Status**:

- Queue: Complete
- Wire: Addressed largest, moving to medium size.
- Bytes: Addressed largest.

### Phase 2 (Next): minimumTokens=200

**Goal**: Target 50-75th percentile (27-36 lines)

## Analysis Tools

### Running CPD Analysis

```bash
# From project root
cd /home/peter/Build-All/Chronicle-Wire
mvn pmd:cpd-check
```

### Python Analysis Script

```bash
# From Build-All directory
python3 analyze-cpd.py
```

## Refactoring Patterns & Guidelines

### Pattern 1: Extract Shared Interfaces/Classes

**When**: Two or more files have identical nested types (interfaces, inner classes, enums)

### Pattern 2: Template Method with Abstract Base Class

**When**: Multiple test classes have identical test logic but different constants

### Pattern 3: Java 8 Default Interface Methods ✨ NEW

**When**: Multiple enum/class implementations have identical delegation methods
**Example**: RollCycle interface with default methods for `toIndex()`, `toSequenceNumber()`, etc.
**Benefits**:

- Eliminates boilerplate delegation code
- Maintains type safety
- No runtime overhead
- Clean separation of interface contract from common implementation

### Pattern 4: Extract Helper Methods

**When**: Same code block appears in multiple methods within same class or test class

### Pattern 5: Functional Interfaces for Test Parameterization

**When**: Test methods differ only in a small variation of behavior
**Example**: `PartialUpdateSimulator` in `PartialUpdateTest`

### Pattern 6: Extract Utility Class

**When**: Same utility logic appears across multiple unrelated classes

## Exclusion Guidelines

### DO NOT Refactor:

1. **Generated code** - Check for `/target/` or `generated` in path
2. **Different semantics** - Code that looks similar but has different behavior
3. **Intentional patterns** - Parallel test implementations (e.g., TextWire vs YamlWire) - *Note: Careful refactoring
   into abstract base class is possible here.*
4. **Single-file duplications** - Within-file duplications often intentional for test variations

## Workflow for Next Session

### Chronicle-Queue (Optional - Minor Duplications Remaining)

1. **ChronicleReaderMain** vs **ChronicleHistoryReaderMain** (~30 lines each) - Extract AbstractChronicleReaderMain
2. **DirectoryListing** implementations (~28 lines) - Extract DirectoryListingHelper utility

### Chronicle-Bytes (Higher Priority)

1. **TextIntArrayReference** vs **TextLongArrayReference** (59 lines) - Extract AbstractTextArrayReference
2. **ChunkedMappedFile** vs **SingleMappedFile** (76 lines) - Investigate shared logic

### Chronicle-Wire (Higher Priority)

1. Address remaining duplications in the 50-60 line range
2. Continue medium-sized refactorings

## Key Learnings & Best Practices

### RollCycle Refactoring Case Study

**Problem**: Five enum classes implementing `RollCycle` interface all had identical delegation methods (~36 lines each).

**Solution**: Java 8 default interface methods

1. Added abstract `arithmetic()` method to interface
2. Implemented 6 default methods that delegate to `arithmetic()`
3. Each enum now only implements `arithmetic()`, `format()`, and `lengthInMillis()`
4. RollCycles.java kept `maxMessagesPerCycle()` override for performance (caches result)

**Testing**: Required updating test implementations (`MyRollcycle`, `WeeklyRollCycle`) to implement new abstract method.

**Result**:

- 144+ lines eliminated across production code
- All 1,073 tests pass
- Zero behavioral changes
- Cleaner, more maintainable code

### When to Use Default Interface Methods

✅ **DO use when:**

- Multiple implementations have identical delegation/boilerplate
- The common behavior can be expressed in terms of other interface methods
- You want type safety without abstract base classes
- Implementation classes are enums (can't extend abstract classes)

❌ **DON'T use when:**

- Implementations have subtle behavioral differences
- Common logic requires shared state/fields
- Abstract base class would be more appropriate

---

## Quick Start for Next AI Session

```bash
# 1. Navigate to Build-All
cd /home/peter/Build-All

# 2. Run analysis
python3 analyze-cpd.py

# 3. Select next module
cd Chronicle-Bytes

# 4. Identify largest duplication
mvn pmd:cpd-check
grep -E 'lines="[0-9]+".*tokens="[0-9]+"' target/cpd.xml | sed 's/.*lines="\([0-9]*\)".*tokens="\([0-9]*\)".*/\1 lines, \2 tokens/' | sort -rn | head -5
```
