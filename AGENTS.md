# OpenHFT (multi-module) agent notes

## Goals
- Prefer **low cognitive load per unit change** over "small PRs".
- Treat **warnings as defects**: aim for **clean build logs**, not just green exit codes.
- Preserve public APIs unless explicitly requested; keep diffs minimal and avoid incidental formatting noise.
- Consider inline comment suppressions only when a performance or compatibility requirement makes a message impractical.

## Language & character-set policy
- Use **British English** spelling for docs/comments (except technical US spellings like `synchronized`).
- **Source code:** write files in **ISO-8859-1** (code-points 0-255); avoid smart quotes and non-breaking spaces; prefer plain ASCII where possible.
- **Application I/O:** use **UTF-8** for inputs and outputs unless a protocol specifies otherwise.
- If a symbol is not available in ISO-8859-1, spell it out; do not paste raw Unicode into source files.

## Maven workflow (required)
- Always capture Maven output with `-l` (for example: `mvn -pl <module> -am verify -l logs/<name>.log`).
- **Review the whole log** for warnings/errors; do not rely on the exit code alone.
- Use a quick scan to find issues, then still read the surrounding context in the log:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/<name>.log`
- Keep logs under `logs/` for easier maintenance (log files are ignored by `.gitignore`); do not commit logs.

## Scope-first builds/tests
- If touching a single module, prefer module-scoped runs:
  - `mvn -pl <module> -am verify -l logs/<name>.log`
- If changing shared parents/BOMs, validate downstream impact (at least):
  - `mvn -pl third-party-smoke -am verify -l logs/<name>.log`

## Key repo-specific checks
- `third-party-smoke` validates dependencies on Java 8 and Java 21.
  - Default SLF4J binding is `-Pslf4j-simple`; alternate is `-Pslf4j-nop` (run both when changing logging/BOM).
- JDK 21+ test runs use `root-parent-pom` `java21` profile to keep logs clean (dynamic-agent warnings).
- Quality plugins are non-blocking by default in parent POMs; enable enforcement via `-Pquality` when needed.

## Reviewability for large changes (mechanical sweeps)
- Declare the transformation rule in one sentence ("X -> Y") and avoid exceptions.
- Layer commits to match review: prep (optional) -> mechanical sweep -> tidy-up -> verification-only.
- Provide machine-checkable evidence (commands + log paths + invariants) and a sampling plan.

## Duplication / CPD work
- Follow `/prompts:CPD_PLAYBOOK` and run `mvn -q pmd:cpd-check -l logs/<name>.log` before/after.
