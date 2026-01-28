# AGENTS.md

## Scope
- Multi-module Maven build for OpenHFT. Durable documentation lives in `README.adoc`, `docs/`, and module-specific `src/main/docs/`.
- Prefer low cognitive load per unit change and avoid incidental formatting noise.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Module-scoped example:
  - `mvn -pl <module> -am verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -pl <module> -Dtest=<TestClass> test -l logs/mvn-test.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Repo map
- `docs/` and `README.adoc` are the primary references.
- `quality/` holds quality rules with docs under `quality/src/main/docs/`.
- `third-party-smoke/` validates dependencies across Java 8 and Java 21.
- Module docs and decision logs live under `src/main/docs/`.
- MeaningfulMessage advice text and ranks live in `quality/src/main/resources/net/openhft/quality/mm-advice.properties` and `quality/src/main/resources/net/openhft/quality/mm-advice-ranks.properties`.

## Constraints
- Java baseline: 8 (also validate Java 21 via `third-party-smoke`).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.
- Use inline comment suppressions only when a performance or compatibility requirement makes a message impractical.

## Docs and review checklist
- Keep docs, tests, and code in sync; update `.adoc` files when behaviour changes.
- MeaningfulMessageCheck emits intent-first messages by default; set `verbose=true` for detailed diagnostics.
- Aggregated advice uses AdviceId suppression; Checkstyle violations still use RuleId/Checkstyle suppression rules.
- Javadoc must add behavioural contracts, edge cases, thread safety, units, or performance notes.
- For large mechanical changes, declare the transformation rule and keep it consistent.
- For CPD work, follow `/prompts:CPD_PLAYBOOK` and run `mvn -q pmd:cpd-check -l logs/mvn-cpd.log`.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging, decision logs, and AsciiDoc conventions.
- `OpenHFT/docs/AGENTS-template.md` for the canonical structure.
