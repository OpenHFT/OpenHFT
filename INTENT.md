# OpenHFT Intent

## Purpose

This file is the repository-level prioritization policy for OpenHFT.
Use it to decide what should be done first when work competes for time.

## Mission

Keep OpenHFT build and dependency infrastructure reliable, reproducible, and safe for downstream projects.

## Priority order

1. Correctness and compatibility of shared parent/BOM configuration.
2. Deterministic and maintainable build behavior across modules.
3. Clear, durable engineering documentation for decisions and support.
4. Contributor ergonomics that do not weaken items 1-3.

## Non-goals

1. Convenience changes that hide dependency or versioning risks.
2. Mechanical churn with no measurable correctness or maintainability gain.
3. Build shortcuts that reduce verification quality.

## Intent pillars

Use these IDs in plans, docs, and commit notes when useful.

1. `OH-INT-01` Dependency correctness:
   - parent/BOM rules are coherent and non-conflicting.
2. `OH-INT-02` Build determinism:
   - CI/local execution produces stable, explainable outcomes.
3. `OH-INT-03` Compatibility safety:
   - preserve expected Java/runtime compatibility contracts.
4. `OH-INT-04` Evidence-first changes:
   - behavior changes include targeted tests and verifiable outcomes.
5. `OH-INT-05` Documentation quality:
   - key behavior and policy are discoverable in repo docs.

## Required evidence for change classes

General build evidence rule:

1. Run Maven commands with `-l <logfile>` and review the log after the build.
   - Example: `mvn verify -l logs/mvn-verify.log`

1. Build/pom/dependency change:
   - run at least affected-module `mvn verify`,
   - include warning/error scan evidence.
2. Quality-rule or static-analysis change:
   - include focused unit test coverage for new and regression paths.
3. Documentation-only change:
   - ensure docs match current code and command behavior.

## Change control

`INTENT.md` should change rarely. Update only when:

1. current policy causes repeated prioritization mistakes, or
2. repository scope materially changes.
