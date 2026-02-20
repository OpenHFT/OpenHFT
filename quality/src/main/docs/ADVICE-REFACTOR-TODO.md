# Advice-First Refactor TODO (Prioritised)

This file is the running plan for the AdviceId refactor. It includes priorities, detailed steps, and acceptance notes so a new session can continue without extra context.

## Decisions Locked In (Summary)
- AdviceId is stable and user-facing. RuleId is internal only.
- AdviceId suppression applies to aggregated report + CLI, not to standard Checkstyle violations.
- Aggregated report is built from detector-level data (not AuditEvents only).
- AdviceSource list includes annotation subtypes (DisplayName, Disabled, Test order, JUnit4).
- Ranks are repo-owned; missing rank file is an error outside dry-run.
- Dry-run shows all candidates (treat all ranks equal). Rank file is generated from deduped AdviceIds per line.
- If multiple AdviceIds are rank -1, show all for the line; otherwise one advice per line.
- JSONL output only when --jsonl is set; console otherwise.
- Snippet is single line only; console/JSONL show trimmed line to highlight the relevant portion.
- Advice text is in a new mm-advice.properties; ranks in mm-advice-ranks.properties.
- Suppression scope matches current SuppressionTracker behavior.
- CLI flag for verbosity is --verbose. Rank output default goes to logs/ unless explicitly set.
- No i18n/localization in this phase (messages are single-language only).
- Properties file encoding is UTF-8.

## File Locations (Proposed)
- Advice text: quality/src/main/resources/net/openhft/quality/mm-advice.properties
- Ranks: quality/src/main/resources/net/openhft/quality/mm-advice-ranks.properties
- Rank output default: logs/mm-advice-ranks.properties (CLI), override with --rank-out

## Priority Legend
- P0: blocking core behavior
- P1: required for usable output
- P2: required for stability and test confidence
- P3: polish / future-proof

---

## Glossary (Keep Updated)
- AdviceId: user-visible identifier that drives advice text and suppression.
- RuleId: internal detector id for verbose diagnostics only.
- AdviceSource: call-site category used to select AdviceId (includes annotation subtypes).
- CandidateAdvice: detector output before ranking/selection.
- FileReport: aggregated output per file (file-level + line-level advice blocks).

---

## P0.5 - Rank Semantics (Cross-Cutting)
- [x] Define rank meaning explicitly in code/docs:
  - Lower rank = rarer = preferred.
  - -1 means unknown/unranked and should be treated as rarest when ties occur.
  - Valid range: integers; no upper bound, but use small positive values for known AdviceIds.
- [x] Define rarity scope:
  - Ranking is based on deduped AdviceIds per line across a run (dry-run).
  - Ordering inside a file uses this global rank, not per-file counts.

Acceptance notes:
- Rank semantics and rarity scope documented in code and docs.

---

## P0 - Core Architecture (Blocking)
- [x] Add AdviceId enum (stable naming, encoded source: MM<Source><Fix>). Document stability policy in code comments.
- [x] Add AdviceSource enum with values:
  - ASSERTION, PRECONDITION, THROW, LOG, COMMENT,
  - JAVADOC_CLASS, JAVADOC_MEMBER,
  - ANNOTATION_DISPLAY_NAME, ANNOTATION_DISABLED,
  - ANNOTATION_TEST_ORDER, ANNOTATION_JUNIT4
- [x] Decide grouping for AdviceSource in code docs (code constructs vs annotation subtypes) to keep taxonomy clear.
- [x] Add core data structures:
  - CandidateAdvice {file, line, AdviceSource, AdviceId, RuleId, message_literal, message_expr, snippet, metrics}
  - Metrics definition (minimum set):
    - wordCount, meaningfulWordCount, placeholderCount, keyValueLabelCount,
    - comparison/operator details if present, string-search details if present.
    - Keep metrics in verbose payload only.
  - AdviceOccurrence {line, source, message_literal, message_expr, snippet, details}
  - FileReport {file, file_level_advice[], line_level_advice[], verbose_payload}
- [x] Create per-file collector (file -> line -> candidate list) and dedupe by AdviceId within line.
- [x] Implement selection:
  - Normal mode: choose lowest rank per line; if multiple rank -1, keep all.
  - Dry-run: show all candidates per line (treat equal rank).
- [x] Enforce invariant: no duplicate AdviceId per line after dedupe (fail fast with a runtime exception and a test that asserts this never happens).
- [x] Implement suppression for AdviceId using existing suppression scopes.
- [x] Honor legacy RuleId suppressions with warning (flag-gated, to be deprecated).

Acceptance notes:
- One advice per line in normal mode (except multiple -1).
- File-level advice always appears first.

---

## P1 - Advice Text + Ranks (Usable Output)
- [x] Create mm-advice.properties with mandatory keys for every AdviceId:
  - title, intent_intro, intent_outro, hint_a, hint_b, checklist, anti_patterns, verbose
- [x] Create mm-advice-ranks.properties (AdviceId -> rank). Unseen AdviceIds are -1.
- [x] Implement strict validation:
  - All required keys present for every AdviceId.
  - intent_intro != intent_outro; hint_a != hint_b (allow an explicit override comment if a rule genuinely cannot vary).
- [x] Remove gameable numbers from default advice; keep metrics in verbose only.

Acceptance notes:
- Missing advice keys are a configuration error.

---

## P1 - Ranking (Read/Write)
- [x] Rank reader loads mm-advice-ranks.properties from resources.
- [x] Unknown AdviceIds resolve to rank -1.
- [x] Dry-run mode:
  - does not require ranks to exist
  - generates ranks from deduped AdviceIds per line
  - writes a rank file in the same format (default logs/; override --rank-out)
- [x] Ranking generation should include ALL AdviceIds, unseen -> -1.
- [x] Clarify rank meaning in code/doc comments (lower = rarer).

Acceptance notes:
- Normal mode fails fast when ranks are missing.
- Dry-run output is usable without prior rank file.

---

## P1 - Reporting (Console + JSONL)
- [x] Implement JSONL writer (only when --jsonl is set):
  - run record first
  - one file record per file, grouped by AdviceId
  - include occurrences list and advice definition text
  - verbose JSONL includes all candidates, RuleIds, metrics, thresholds
- [x] Implement console writer (derived from FileReport):
  - file header
  - file-level advice blocks
  - line-level advice groups ordered by rarity, occurrences sorted by line
  - include message_literal or message_expr

Acceptance notes:
- JSONL is canonical output for tests.
- Console format is stable and concise.

---

## P1 - Detector Integration (Existing Checks)
- [x] Update detectors to emit CandidateAdvice in addition to current Checkstyle violations.
- [x] Map each detector to AdviceId (start uncombined: source x fix).
- [x] Ensure message_literal vs message_expr are captured when possible.
- [x] Include snippet (single line) and keep raw line optional for verbose JSONL.

Acceptance notes:
- Standard Checkstyle output remains unchanged.
- Aggregated report uses CandidateAdvice collector.

---

## P2 - Checkstyle Integration Strategy
- [x] Keep Checkstyle standard output unchanged (legacy assert.message.*).
- [x] Aggregated report should NOT rely on AuditEvent content.
- [x] Use AuditListener only for file boundary signals if needed.
- [x] Document that AdviceId suppression affects aggregated report only.

Acceptance notes:
- No moduleId explosion; no AdviceId-based Checkstyle suppression.

---

## P2 - CLI Flags + Defaults
- [x] Add flags:
  - --verbose (include full diagnostics)
  - --dry-run (stats mode; no rank file required)
  - --jsonl <path> (emit JSONL only when set)
  - --rank-out <path> (default logs/mm-advice-ranks.properties)
- [x] Default output: console only unless --jsonl is set.
- [x] Allow --rank-out to target resources path explicitly.

Acceptance notes:
- CLI usable outside repo (rank output path configurable).

---

## P2 - Tests (Golden + Unit)
- [x] JSONL golden tests (run record + file records).
- [x] Console smoke tests (structure only, not full text).
- [x] Unit tests for rank parsing, -1 handling, and tie behavior.
- [x] Unit tests for suppression (AdviceId + legacy RuleId with warning).
- [x] Validation tests for mm-advice.properties completeness and RE2 variation.
- [x] Keep detector unit tests separate from reporting.

Acceptance notes:
- JSONL is canonical; console tests are minimal.

---

## P3 - Documentation Updates
- [x] Update message-quality-guide.adoc for advice-first reporting.
- [x] Update developer-guide.adoc for AdviceId pipeline.
- [x] Update AGENTS.md with AdviceId/rank file locations and suppression behavior.
- [x] Add JSONL schema docs and sample output.
- [x] Add error-handling section (missing advice keys, malformed ranks).
- [ ] Document handling of invalid/unknown suppression tokens (currently ignored).
- [x] Add performance note (streaming vs batch, output-size guidance).
- [x] Add JSONL schema versioning note.
- [x] Add rollback guidance (how to disable aggregated report if needed).

---

## P3 - Migration/Polish
- [x] Provide a deprecation window for RuleId suppressions (warn in verbose).
- [x] Consider a tooling script to generate initial AdviceId list (not required while AdviceId enum is authoritative).
- [x] Add a lint task to verify advice text key completeness.
- [ ] Add perf acceptance criteria (baseline differs between model-era code and maintained code; define after post-rollout baselines exist).
