# OpenHFT Iteration 0 plan (baseline)

This iteration establishes the baseline requirements for working on OpenHFT with AIDE.
All future iterations must meet these requirements unless an iteration plan explicitly records and justifies a deviation.

## Planning location
- Shared iteration plans live in `plans/` at the repo root.

## Documented requirements (baseline)
The items below are derived from repo docs (notably `AGENTS.md`, `README.adoc`, and `docs/`).

### Build and test
- Run the preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Review logs for warnings/errors:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit `logs/`.

### Java compatibility
- Maintain Java 8 as the baseline.
- Validate Java 21 behaviour via the `third-party-smoke/` module when changes could affect runtime or dependencies.

### Charset and formatting
- Source files must remain ISO-8859-1 (code points 0-255).
- Prefer ASCII; avoid smart quotes and non-breaking spaces.

### API and performance constraints
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.

### Docs and review hygiene
- Keep docs, tests, and code in sync; update relevant `.adoc` files when behaviour changes.
- Prefer intent-first messages for diagnostics; only add suppressions when a performance/compatibility requirement makes a message impractical.

## Acceptance criteria
- `plans/ITERATION_0_PLAN.md` and `plans/ITERATION_1_PLAN.md` exist in-repo.
- Baseline build passes (`mvn verify`) with no unresolved warnings/errors.
- Any deviations from the documented requirements are recorded in the active iteration plan.
