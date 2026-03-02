# Quality Module Intent

## Purpose

This file defines prioritization for changes in `quality/` (Checkstyle, PMD, SpotBugs, and MeaningfulMessage logic).

## Mission

Deliver high-signal quality rules that catch real defects with low false-positive noise and predictable diagnostics.

## Priority order

1. Rule correctness (real issues detected reliably).
2. False-positive and false-negative reduction.
3. Deterministic, actionable diagnostics for developers and CI.
4. Backward-compatible behavior for existing Chronicle usage patterns.
5. Performance and implementation ergonomics that preserve items 1-4.

## Non-goals

1. Rule strictness increases without evidence of value.
2. Clever extraction logic that is hard to reason about or validate.
3. Behavioral changes shipped without fixture-based regression coverage.

## Intent pillars

1. `Q-INT-01` Signal quality:
   - maximize true positives, minimize false positives.
2. `Q-INT-02` Deterministic output:
   - same input produces same rule IDs/messages.
3. `Q-INT-03` Migration-safe guidance:
   - advice text helps users fix issues without guesswork.
4. `Q-INT-04` Compatibility:
   - preserve supported Java and build-tool constraints.
5. `Q-INT-05` Evidence-first evolution:
   - every rule behavior change has targeted tests.

## Rule-change acceptance checklist

1. Add or update focused fixtures in `src/test/resources`.
2. Add regression tests in `MeaningfulMessageCheckTest` or equivalent rule tests.
3. Verify no unintended rule ID/message drift.
4. Run module tests and ensure clean output for changed scenarios.

## Preferred evidence commands

1. Focused test:
   - `mvn -pl quality -Dtest=<TestClass>#<testMethod> test`
2. Module verification:
   - `mvn -pl quality verify`

## Notes for MeaningfulMessage work

1. Prefer explicit, auditable extraction logic over broad heuristics.
2. Treat "unhandled" category changes as high-risk; require regression fixtures.
3. Keep intent/hint advice text aligned with implemented rule behavior.

