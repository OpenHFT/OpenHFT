# OpenHFT Iteration 3 plan (branch coverage, test hardening, and minor fixes)

This iteration improves branch coverage toward the 81% target, hardens existing tests, and addresses minor code and documentation issues discovered during iteration 2 review.
Scope is limited to the `quality` module.

## Goals
- Close the branch coverage gap from 80.4% to at least 81% (approximately 12+ new branches covered).
- Harden test assertions and add missing edge-case coverage in extractors and processor tests.
- Fix minor code quality issues and documentation gaps in `OpenHFT/quality`.

## Branch coverage improvements

Refer to `OpenHFT/quality/src/main/docs/PLAN-branch-coverage.md` for the full gap analysis.

### Phase 1: Quick wins (target: +20 branches)

- [ ] `MeaningfulMessageProcessorTest`: add tests for `buildScope()` with null context, empty class name, and empty method name (9 missed branches).
- [ ] `MeaningfulMessageProcessorTest`: add tests for `emitUnhandled()` with disabled flag, null AST, and blank reason (8 missed branches).
- [ ] `SuppressionTrackerTest`: add tests for array-valued `@SuppressWarnings` annotations and annotations without a value attribute (13 missed branches across `findAnnotationValue` and `collectStringValues`).
- [ ] `ThrowMessageExtractorTest`: add tests for `isThrowableTypeName()` edge cases (e.g. single-segment names, names ending in "Error" vs "Exception") and throw-variable (not `new`) patterns (5 missed branches).

### Phase 2: Test input file enhancements (target: +15 branches)

- [ ] Enhance `InputMissingMessageVariants.java` to cover remaining `isMissingAssertionMessage()` branches: add JUnit no-message variants, AssertJ fluent-chain variants, and static-import assertion variants where not already present.
- [ ] Enhance `InputLambdaSupplierVariants.java` to cover `extractConstantSupplierMessage()` branches: add block-bodied lambdas (`() -> { return "msg"; }`) and format-call lambdas (`() -> String.format(...)`).
- [ ] Enhance `InputLogCallVariants.java` to cover `checkJvmLogCall()` branches: add `Jvm.warn().on(getClass(), ...)` and `Jvm.debug().on(getClass(), ...)` patterns if not already present.

## Fixes

- [ ] `MeaningfulMessageProcessor.globToRegex()`: document the limited scope of manual regex escaping (parentheses and other metacharacters not escaped) or switch to `Pattern.quote()` for literal segments. Add a targeted unit test for glob patterns containing parentheses.
- [ ] `MeaningfulMessageProcessor`: remove the redundant `i + 1 < tokens.size() - 1` guard in the "so that" / "in order" purpose-cue logic (approximately line 1232-1239) where the same bound is checked twice.
- [ ] `ViolationCollectorTest`: add a test for duplicate violations with different priorities to cover the 3 missed branches in priority-tie handling.
- [ ] `LambdaMessageExtractorTest`: add a test for block-bodied lambdas with empty expressions to cover null-guard branches added recently (8 missed branches).

## Minor improvements

- [ ] Add a file-header comment to `mm-advice-ranks.properties` documenting: rank semantics (lower = rarer, selected first), the meaning of `-1` (unranked), and expected range.
- [ ] Add a file-header comment to `mm-advice.properties` documenting: required keys per AdviceId, placeholder syntax (`{0}`, `{1}`), and encoding (ISO-8859-1 per Java Properties convention).
- [ ] `AdviceRankingsTest`: add a cross-check test that verifies every `AdviceId` (except `UNKNOWN`) has a corresponding entry in `mm-advice-ranks.properties`, and that no unknown keys exist in the properties file. This catches configuration drift without runtime overhead.
- [ ] `AdviceTextLoaderTest` or `AdviceTextSemanticsTest`: add a cross-check test that verifies every `AdviceId` (except `UNKNOWN`) has both `.intent` and verbose entries in `mm-advice.properties`.
- [ ] Tighten generic test names in `MeaningfulMessageProcessorTest` and `AssertionMessageExtractorTest` where method names do not describe the specific scenario (e.g. `testEdgeCase1` → `extractorSkipsStaticImportWithoutMessage`).
- [ ] Remove any dead assertions or redundant `assertNotNull` calls that duplicate later `assertEquals` checks in test files touched by this iteration.

## Out of scope

- Extracting testable units from large methods (structural refactoring deferred to a later iteration).
- Adding `@Generated` annotations to exclude untestable code from coverage (requires team agreement on policy).
- Changes outside the `quality` module.

## Validation

- [ ] Module-focused: `mvn -pl quality -am verify -l logs/mvn-verify-quality.log`
- [ ] Log scan: `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify-quality.log`
- [ ] Branch coverage check: verify JaCoCo branch coverage >= 81% in `quality/target/site/jacoco/index.html`.

## Acceptance criteria

- Branch coverage reaches at least 81% (currently 80.4%).
- Each new test method has a descriptive name explaining the scenario it covers.
- No new warnings or errors in the module verification log.
- Properties file header comments are present and accurate.
- Cross-check tests for AdviceId-to-properties alignment pass.
- Relevant docs updated if any rule behaviour changes (none expected this iteration).
