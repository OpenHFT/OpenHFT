# AGENTS.md

## Scope
- Brief summary of the repo purpose and audience.
- Mention any non-obvious constraints or expectations.

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
- Optional section. Include 2-6 bullets if the layout is non-trivial.

## Constraints
- Java baseline: <version> (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.

## Docs and review checklist
- Keep docs, tests, and code in sync.
- For large mechanical changes, declare the transformation rule and keep it consistent.
- Add clarifying comments only when intent is non-obvious.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and decision record templates.
- Repo-specific docs or decision logs.
