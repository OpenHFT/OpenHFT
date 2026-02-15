## Summary

<!-- What changes and why? Keep it short. -->

## Change Type (pick one)

- [ ] Functional change (feature/bugfix)
- [ ] Non-functional change (build/tooling/docs/refactor)
- [ ] Mechanical transformation (semantics-preserving)

## Mechanical Transformation (required if selected)

**Transformation rule:** `X -> Y` (one sentence)

**Non-goals:** No behavioural change intended.

**Tooling used:** (OpenRewrite / IDE structural replace / script / etc.)

**No exceptions:** If exceptions exist, isolate them in a separate commit or follow-up PR.

## Evidence (clean builds, not just exit codes)

**Maven commands run (capture logs):**

```bash
# example
mkdir -p logs
mvn -pl <module> verify -l logs/<name>.log
```

**Log review:** confirm there are no warnings/errors in the log.

```bash
rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/<name>.log
```

**Invariants (if applicable):**

- old pattern occurrences: `<N> -> 0` (command: `rg -n '<old>' ...`)
- new pattern occurrences: `<N>` (command: `rg -n '<new>' ...`)

## How To Review (fast path)

1. Review the "mechanical sweep" commit only (if present).
2. Spot-check a small sample across modules (include your sampling plan here).
3. Confirm invariants + CI results.

## Risk / Notes

<!-- Call out any hot-path, concurrency, wire-format, or off-heap impacts. -->
