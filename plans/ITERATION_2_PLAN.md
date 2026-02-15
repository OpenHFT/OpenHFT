# OpenHFT Iteration 2 plan (targeted fixes and minor improvements)

This iteration focuses on small, high-confidence fixes and tidy follow-up improvements in `OpenHFT/quality`.
Scope is intentionally limited to the `quality` module to reduce risk and keep changes reviewable.

## Goals
- Close remaining edge cases in `OpenHFT/quality` message rules and tests.
- Improve `OpenHFT/quality` documentation and test clarity where recent behaviour changed.
- Keep `OpenHFT/quality` build/test evidence clean and reproducible.

## Fixes
- [ ] `MMMissingStringSearchValue`: verify/adjust edge handling for token boundaries and literal parsing (for example punctuation boundaries, escaped quotes, mixed alnum/underscore tokens) in `OpenHFT/quality`.
- [ ] `MMDuplicatesInput`: verify/adjust behaviour for null/blank input values to avoid false positives and keep intended detections in `OpenHFT/quality`.
- [ ] `MeaningfulMessageCheck` unexpected-path tests: confirm failure-path diagnostics stay stable (line number, key, and count expectations) under current rule output mode in `OpenHFT/quality`.
- [ ] If any rule behaviour changes, update associated tests in `OpenHFT/quality/src/test/java/net/openhft/quality/mm/` and `OpenHFT/quality/src/test/java/net/openhft/quality/` in the same change.

## Minor improvements
- [ ] Update `OpenHFT/quality/src/main/docs/message-quality-guide.adoc` with any rule clarifications introduced by the fixes above (especially search-value and duplicate-input guidance).
- [ ] Tighten test names and messages where they are currently generic, so failures remain self-explanatory.
- [ ] Remove any dead/redundant assertions introduced by iterative edits, keeping tests lean and deterministic.
- [ ] Run module-scoped verification first for fast feedback, then full verification before sign-off.

## Validation
- [ ] Module-focused: `mvn -pl quality -am verify -l logs/mvn-verify-quality.log`
- [ ] Log scan: `rg -n '^\\[(WARNING|ERROR)\\]|SLF4J\\(W\\)|\\bWARNING:|\\bwarning:' logs/mvn-verify-quality.log`

## Acceptance criteria
- Each completed item includes a clear problem statement and a minimal fix.
- Behaviour changes include regression coverage in the same commit.
- Relevant docs are updated when rule behaviour or guidance changes.
- Build evidence is recorded in `logs/` and contains no unresolved warnings/errors.
