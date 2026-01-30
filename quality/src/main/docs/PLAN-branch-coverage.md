# Plan: Improve Branch Coverage in OpenHFT/quality

## Current Status

- **Branch coverage**: 80.4% (1795 covered / 437 missed)
- **Target**: 81% (need ~12 more branches covered)
- **Line coverage**: 93.8% (on target)

## Branch Gap Analysis by Class

| Class | Missed | Covered | % | Priority |
|-------|--------|---------|---|----------|
| AssertionMessageExtractor | 112 | 269 | 71% | HIGH |
| MeaningfulMessageProcessor | 63 | 111 | 64% | HIGH |
| MessageExtractionContext | 44 | 186 | 81% | MEDIUM |
| LogMessageExtractor | 40 | 166 | 81% | MEDIUM |
| MessageTemplateExtractor | 31 | 139 | 82% | LOW |
| SuppressionTracker | 23 | 59 | 72% | MEDIUM |
| AssertionOperandExtractor | 19 | 56 | 75% | MEDIUM |
| MessageAstSupport | 12 | 96 | 89% | LOW |
| ThrowMessageExtractor | 11 | 29 | 73% | MEDIUM |
| LoopIndexAnalyzer | 10 | 46 | 82% | LOW |

## Top Methods with Missed Branches

### Category A: Reasonably Testable (should add tests)

| Method | Missed | Why Testable | Suggested Test |
|--------|--------|--------------|----------------|
| `MeaningfulMessageProcessor.buildScope()` | 9 | Simple null/empty checks | Test with null context, empty class/method names |
| `MeaningfulMessageProcessor.emitUnhandled()` | 8 | Flag and null checks | Test with emitUnhandled=false, null AST, null reason |
| `MeaningfulMessageProcessor.extractionTarget()` | 12 | Fallback chain | Test system property fallback, empty values |
| `SuppressionTracker.findAnnotationValue()` | 8 | AST traversal | Test various annotation shapes |
| `SuppressionTracker.collectStringValues()` | 5 | Array vs single value | Test array annotations |
| `ThrowMessageExtractor.isThrowableTypeName()` | 5 | String matching | Test edge case exception names |
| `LambdaMessageExtractor` branches | 8 | Null guards added recently | Test block-bodied lambdas, empty expressions |
| `ViolationCollector` branches | 3 | Priority handling | Test duplicate violations with different priorities |

### Category B: Complex AST Shapes (requires crafted input files)

| Method | Missed | Why Complex | Suggested Approach |
|--------|--------|-------------|-------------------|
| `isMissingAssertionMessage()` | 31 | Many assertion styles | Add InputMissingMessageEdgeCases.java with all JUnit/AssertJ variants |
| `selectByStyle()` | 12 | Style-specific logic | Add assertions for each AssertionStyle enum value |
| `selectMessageExpression()` | 10 | Argument position logic | Test first/last string arg selection |
| `extractConstantSupplierMessage()` | 13 | Lambda body shapes | Test lambda with SLIST body, method call returning format |
| `findArgumentListRangeByScan()` | 24 | Parenthesis scanning | Test nested calls, multiline args |

### Category C: Defensive/Edge Cases (low priority)

| Method | Missed | Why Low Priority |
|--------|--------|------------------|
| `isParameterNameMessage()` | 6 | Rarely hit in practice - requires exact param name as message |
| `renderMethodCallExpression()` | 8 | Debug/diagnostic rendering |
| `renderDotExpression()` | 4 | Debug/diagnostic rendering |
| `renderArgumentExpression()` | 4 | Debug/diagnostic rendering |
| `emitRuleSummary()` | 4 | Only runs when verbose=true |
| `recordMessageExtractionFailure()` | 4 | Already has test for file failure path |

### Category D: Unreasonable to Test (consider excluding or accepting)

| Method | Missed | Why Unreasonable |
|--------|--------|------------------|
| `logUnexpected()` | 4 | Requires forcing RuntimeException during AST processing |
| `extractionTarget()` system property path | 2 | Requires setting system property which affects other tests |
| Null AST fallbacks in exception handlers | ~10 | Would require mocking Checkstyle internals |

## Recommended Action Plan

### Phase 1: Quick Wins (target: +20 branches)

1. **Add `MeaningfulMessageProcessorTest` coverage**:
   - Test `buildScope()` with null context, null/empty class, null/empty method
   - Test `emitUnhandled()` with disabled flag, null AST, blank reason
   - Test `extractionTarget()` fallback chain

2. **Add `SuppressionTrackerTest` coverage**:
   - Test array-valued @SuppressWarnings
   - Test annotation without value attribute

3. **Add `ThrowMessageExtractorTest` coverage**:
   - Test `isThrowableTypeName()` with edge case class names
   - Test throw statement re-throwing variable (not `new`)

### Phase 2: Input File Additions (target: +30 branches)

4. **Create `InputMissingMessageVariants.java`**:
   ```java
   // Cover all assertion styles for isMissingAssertionMessage
   assertTrue(condition);  // JUnit no-message
   assertThat(x).isTrue(); // AssertJ fluent
   Assertions.assertThat(x).isEqualTo(y); // AssertJ static import
   ```

5. **Create `InputLambdaSupplierVariants.java`**:
   ```java
   // Cover extractConstantSupplierMessage branches
   log.debug(() -> "constant");           // simple
   log.debug(() -> String.format("x=%d", y)); // format call
   log.debug(() -> { return "block"; });  // block body
   ```

6. **Create `InputLogCallVariants.java`**:
   ```java
   // Cover checkJvmLogCall branches
   Jvm.warn().on(getClass(), "message");
   Jvm.debug().on(getClass(), "message with {} placeholder", arg);
   ```

### Phase 3: Structural Improvements (optional)

7. **Extract testable units from large methods**:
   - `isMissingAssertionMessage()` could delegate to smaller methods
   - `findArgumentListRangeByScan()` scan logic could be unit-tested separately

8. **Add `@Generated` or exclusion for truly untestable code**:
   - Exception handler fallbacks that require mocking
   - Debug rendering methods

## Coverage Target Calculation

| Phase | Branches Added | New Total | New % |
|-------|----------------|-----------|-------|
| Current | - | 1795 | 80.4% |
| Phase 1 | +20 | 1815 | 81.3% |
| Phase 2 | +30 | 1845 | 82.6% |
| Phase 3 | +10 | 1855 | 83.1% |

## Files to Create/Modify

### New Test Input Files
- `src/test/resources/net/openhft/quality/InputMissingMessageVariants.java`
- `src/test/resources/net/openhft/quality/InputLambdaSupplierVariants.java`
- `src/test/resources/net/openhft/quality/InputLogCallVariants.java`

### Test Files to Enhance
- `MeaningfulMessageProcessorTest.java` - add buildScope, emitUnhandled, extractionTarget tests
- `SuppressionTrackerTest.java` - add array annotation tests
- `ThrowMessageExtractorTest.java` - add edge case tests
- `LogMessageExtractorTest.java` - add Jvm logger and supplier lambda tests
- `LambdaMessageExtractorTest.java` - add block-body lambda tests

## Branches NOT Worth Testing

The following branches should be accepted as uncovered or excluded:

1. **System property fallback in `extractionTarget()`** - Would pollute test environment
2. **Null AST in `logUnexpected()`** - Requires Checkstyle internal failure
3. **Debug rendering methods** - Low value, high effort
4. **Already-failed writer checks** - Would need to simulate I/O failures mid-stream

Total accepted uncovered: ~25 branches (keeps target achievable at 83%+)
