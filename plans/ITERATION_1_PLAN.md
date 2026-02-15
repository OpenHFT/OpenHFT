# OpenHFT Iteration 1 plan (issues and bugs)

This iteration addresses outstanding issues and bugs discovered after establishing the Iteration 0 baseline.

## Goals
- Capture and prioritise outstanding issues/bugs.
- Fix selected issues with regression tests.
- Keep repo docs and constraints aligned with changes.

## Worklist
Add items as they are discovered/triaged.

- [ ] Triage: collect issues from CI/build logs, reviewer notes, and user reports.
- [ ] Categorise: bug vs tech debt vs documentation gap; note affected modules.
- [ ] Fix: implement smallest safe change; preserve public APIs unless explicitly approved.
- [ ] Test: add/adjust regression tests for each behaviour change.
- [ ] Validate: `mvn verify -l logs/mvn-verify.log` (and Java 21 `third-party-smoke` when applicable).
- [ ] Docs: update relevant `.adoc` files when behaviour changes.

## Acceptance criteria
- Each completed item has:
  - A clear problem statement.
  - A minimal fix.
  - Regression test coverage (where applicable).
  - Build evidence (`logs/mvn-verify.log` references).
