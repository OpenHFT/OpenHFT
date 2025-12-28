# Meaningful Message Extensions TODO

This plan covers broadening MeaningfulMessage checks to log sites and display names, with suppression rules:

- `@SuppressWarnings("MMTooShort")` suppresses a specific MeaningfulMessage rule.
- `@SuppressWarnings("MM-all")` suppresses all MeaningfulMessage rules.
- `Jvm.*.on(...)` and logging APIs (SLF4J, Log4j2, JUL, `System.Logger`) are in scope (same standard).
- A message is required when **no throwable is present**.
- When a throwable is present, the message is optional; if present, it must be meaningful.

## 1) Scope and behaviour design

- [x] Confirm log site coverage:
  - [x] SLF4J: `Logger.trace`, `Logger.debug`, `Logger.info`, `Logger.warn`, `Logger.error`.
  - [x] Log4j2: `Logger.trace`, `Logger.debug`, `Logger.info`, `Logger.warn`, `Logger.error`, `Logger.fatal`.
  - [x] JUL: `java.util.logging.Logger.log(Level, ...)` plus convenience methods.
  - [x] `System.Logger.log(Level, ...)` including supplier variants.
  - [x] Chronicle: `Jvm.debug/warn/error/startup/perf().on(...)` callsites only (class and logger overloads).
  - [x] `trace` and `info` are in scope (same standard as debug).
  - [x] Include JUL, `System.Logger`, and Log4j2 direct API in the initial rollout.
- [x] Define the MeaningfulMessage subset for logs:
  - [x] `MMGenericMessage`, `MMTooShort`, `MMTooFewMeaningfulWords`, `MMContextless`,
        `MMIndexOnly`, `MMMissingSubject`, `MMWhitespaceRun`.
  - [x] Treat `MMDuplicate` as lower severity for logs.
  - [x] Use a dedicated rule code for missing messages: `MMMissingMessage`.
- [x] Define message-required rules:
  - [x] If no throwable argument is present, a message is required.
  - [x] If a throwable is present, the message is optional; if provided, it must be meaningful.
  - [x] Treat empty templates as present and evaluate them via MMTooShort/MMTooFewMeaningfulWords,
        even when a throwable is supplied.
  - [x] Add MMMissingMessage for null log templates (no known throwable) and missing annotation reasons;
        placeholders do not satisfy it.
  - [x] Apply missing-message rule to all `Jvm.*.on(...)` callsites (class and logger overloads; null message without throwable fails).
  - [x] For SLF4J, empty templates are handled by MMTooShort/MMTooFewMeaningfulWords, not the missing-message rule.
  - [x] Inline `/* reason */` comments inside argument lists suppress MMMissingMessage
        (use sparingly; throws are the key use case).
- [x] Define annotation coverage:
  - [x] Apply the core subset to `@DisplayName`, `@Disabled`, and parameterised names.
  - [x] Allow JUnit standard placeholders (`{index}`, `{arguments}`, `{argumentsWithNames}`,
        `{displayName}`, `{0}`, `{1}`) as meaningful tokens.
- [x] Enable Surefire phrased-name reporting via the `quality` profile so `@DisplayName`
        appears in JUnit XML (method names phrased, class names stable for TeamCity).
  - [x] SLF4J `{}` placeholders and numeric `{0}` count as meaningful tokens in logs.
  - [x] printf-style `%s` placeholders count as meaningful tokens in logs.
  - [x] Dynamic concatenations treat non-literal elements as placeholders (non-filler).
  - [x] Use source-specific minimums: assertions/preconditions 4/2, throws 2/1,
        annotations 6/4, logs 4/2.

## 2) Suppression model

- [x] Support `@SuppressWarnings("MMTooShort")` for the too-short rule.
- [x] Support `@SuppressWarnings("MM-all")` to suppress all MeaningfulMessage rules.
- [x] Scope suppression to class or method.
- [x] Ensure suppression applies to log and display-name checks, not just assertions.
- [x] Document how suppression interacts with log message-required checks.
- [x] Document that `MM-all` must only suppress MeaningfulMessage (use `SuppressWarningsHolder` aliases,
      not `@SuppressWarnings("all")`).
- [x] Missing-message rules for `Jvm.*.on(...)` can be suppressed, but should not be suppressed
      except as a last resort.

## 3) Checkstyle implementation updates

- [x] Extend `MeaningfulMessageCheck` to detect log sites:
  - [x] SLF4J `trace/debug/info/warn/error` method calls.
  - [x] Log4j2 `trace/debug/info/warn/error/fatal` method calls.
  - [x] JUL `java.util.logging.Logger.log(Level, ...)` and convenience methods.
  - [x] `System.Logger.log(Level, ...)` including supplier variants.
  - [x] `Jvm.*.on(...)` overloads (with and without throwable).
  - [x] Ignore `ExceptionHandler.on(...)` callsites (out of scope).
- [x] Add log-specific rule evaluation:
  - [x] When no throwable argument exists, require a message.
  - [x] When message exists, run the log-appropriate MM subset.
  - [x] When the message is null, apply the missing-message rule for `Jvm.*.on(...)` (no throwable)
        and for SLF4J/Log4j2/JUL/System.Logger calls when no known throwable is supplied.
  - [x] Treat `Jvm.*.on(clazz, null)` and `Jvm.*.on(clazz, null, null)` as missing-message failures.
  - [x] Treat `Jvm.*.on(clazz, null, throwable)` as allowed (missing message with throwable).
  - [x] Treat empty templates as MMTooShort/MMTooFewMeaningfulWords for SLF4J/Log4j2/JUL/System.Logger.
  - [x] For SLF4J/Log4j2/JUL/System.Logger calls with no varargs and a non-null template,
        always apply the MM subset
        (including constant strings like "ok").
  - [x] For SLF4J/Log4j2/JUL/System.Logger null templates, allow only when any argument is
        a known throwable; otherwise raise MMMissingMessage (additional args do not satisfy it).
        "Known throwable" means any subtype of `Throwable` (including `StackTrace`), type-based
        only (no name-based guessing).
  - [x] For `System.Logger` supplier variants, treat non-literal suppliers as dynamic placeholders;
        only evaluate the message when the supplier returns a compile-time constant string.
  - [x] Apply MMMissingMessage to `@Disabled`/`@Ignore` with no reason.
  - [x] Apply MMMissingMessage to JUnit `fail()` and `assertXxx(...)` callsites
        when no message argument is supplied or the message is explicitly `null`.
  - [x] Ensure MMMissingMessage is highest priority and suppresses other MM warnings at that site.
- [x] Add new message keys for missing-message warnings (MMMissingMessage).
- [x] Implement suppression handling for `MMTooShort`.
- [x] Implement suppression handling for `MM-all`.
- [x] Ensure existing assertion and annotation checks are unaffected.

## 4) Tests and fixtures

- [x] Add log-site fixtures:
  - [x] SLF4J `trace/debug/info/warn/error` cases with and without throwables.
  - [x] Log4j2 `trace/debug/info/warn/error/fatal` cases with and without throwables.
  - [x] JUL `Logger.log(Level, ...)` plus convenience methods (severe/warning/info/fine/finer/finest).
  - [x] `System.Logger.log(Level, ...)` string and supplier variants.
  - [x] `Jvm.debug/warn/error/startup/perf().on(...)` cases.
  - [x] Missing-message cases (no throwable).
  - [x] Empty message without throwable triggers MMTooShort/MMTooFewMeaningfulWords for `Jvm.*.on(...)`.
  - [x] Null message without throwable triggers missing-message rule for `Jvm.*.on(...)`.
  - [x] Logger overloads for `Jvm.*.on(logger, ...)` follow the same missing-message rules.
  - [x] Empty templates for SLF4J/Log4j2/JUL/System.Logger trigger MMTooShort/MMTooFewMeaningfulWords.
  - [x] Null templates for SLF4J/Log4j2/JUL/System.Logger trigger missing-message rule.
  - [x] Null templates with throwable for SLF4J/Log4j2/JUL/System.Logger are allowed.
  - [x] Null templates with non-throwable args for SLF4J/Log4j2/JUL/System.Logger trigger
        MMMissingMessage.
  - [x] Throwable-present cases (message optional).
  - [x] Empty message with throwable is evaluated via MMTooShort/MMTooFewMeaningfulWords.
  - [x] Null message with throwable is allowed.
  - [x] `Jvm.*.on(clazz, throwable)` is allowed (throwable message assumed meaningful).
  - [x] Non-blank message cases that fail MM rules.
- [x] Add missing-message fixtures for JUnit `fail()` and `assertXxx(...)` callsites.
- [x] Add suppression fixtures:
  - [x] `@SuppressWarnings("MMTooShort")` at class and method scope.
  - [x] `@SuppressWarnings("MM-all")` at class and method scope.
- [x] Add display-name fixtures:
  - [x] `@DisplayName` with too-short, generic, line-number, and whitespace cases.
  - [x] Parameterised display name placeholders that should pass.
  - [x] `@Disabled`/`@Ignore` without a reason triggers MMMissingMessage.

## 5) Documentation updates

- [x] Update `quality/src/main/docs/meaningful-message-checker-requirements.adoc`:
  - [x] Log site coverage and message-required rule.
  - [x] Display-name coverage and placeholder allowances.
  - [x] Suppression keys (`MMTooShort`, `MM-all`) and scoping.
- [ ] Update `quality/src/main/docs/assertion-messages.adoc` with log-specific guidance:
  - [ ] Examples for log messages with and without throwables.
  - [x] Document that `info` is in scope (same standard as debug).
- [x] Update `quality/src/main/resources/net/openhft/quality/messages.properties` if new message keys are added.

## 6) Maven verification

- [x] Run `mvn -pl quality -am verify -l logs/quality-verify.log`.
- [x] Scan logs: `rg -n '^\\[(WARNING|ERROR)\\]|SLF4J\\(W\\)|\\bWARNING:|\\bwarning:' logs/quality-verify.log`.

## 6a) Evidence snapshot (release/2026.XYZ)

- [ ] Record SLF4J version and API usage (2.0.17, no fluent API observed).
- [ ] Capture log-site counts (`Jvm.*.on`, SLF4J `trace/debug/info/warn/error`, Log4j2,
      JUL, and `System.Logger`).
- [ ] Note observed `LOG.info`/`LOG.trace` call shapes in Chronicle-Network.
- [ ] Capture `LOG.trace` and `LOG.info` counts by repo and `src/main` vs `src/test`.
- [ ] Record usage counts for JUL `Logger`, `System.Logger`, and Log4j2 direct API.
- [ ] Capture `ExceptionHandler` indirect usage counts (locals vs fields vs params, src/main vs src/test).
- [ ] Capture per-rule MM violation counts (MMGeneric/TooShort/etc.) by repo and `src/main` vs `src/test`.
- [ ] Record JUnit parameterised name patterns (dominant `{0}` and `{index}`).

## 6b) Impact scan (junit5)

- [ ] Review how many occurrences exist in `/home/peter/junit5/` to assess impact on `src/main` vs `src/test`.
- [ ] Capture separate counts for `src/main` and `src/test` to identify where checks will be most disruptive.
- [ ] Use the counts to rank rule priority (rarest messages first).

## 7) Follow-ups (optional)

- [ ] Add a dedicated section in `metadata-diagnostics-research-topics.adoc` linking to this plan.
- [ ] Propose a playbook entry for log message linting and suppressions.

## 8) Plan review (completeness and consistency)

- [ ] Confirm the ruleset is consistent across assertions, exceptions, logs, and display names.
- [ ] Confirm suppression semantics are consistent:
  - [x] `MM-all` suppresses only MeaningfulMessage rules.
  - [ ] `MMTooShort` suppresses only the too-short rule.
  - [ ] Suppressing message-required does not bypass message-quality checks if a message exists.
- [x] Missing-message rules for `Jvm.*.on(...)` can be suppressed, but should not be suppressed
      except as a last resort.
- [x] Confirm throwable handling logic:
  - [x] SLF4J varargs treat each argument as a placeholder; do not infer a throwable by position.
        Missing-message checks apply when the template is null and no known throwable is present,
        even when varargs exist.
  - [x] `Jvm.*.on` overloads map correctly to message vs throwable presence.
- [x] Confirm placeholder allowlists:
  - [x] JUnit parameterised placeholders for display names.
  - [x] SLF4J `{}` and numeric `{0}` placeholders, plus printf-style `%s`.
- [x] Confirm severity is warning for all sources; build fails only when `quality.enforced=true`.
- [ ] Confirm low-latency/high-performance packages that may use suppression for message-required.
- [x] Confirm other logging APIs are in scope for the initial rollout (JUL, `System.Logger`, Log4j2).
- [x] Confirm handling of dynamic concatenations (non-literal elements count as placeholders).
- [x] Confirm `ExceptionHandler.on(...)` callsites remain out of scope (direct `Jvm.*.on(...)` only).
- [x] Confirm no special empty-message rule; blank messages rely on MMTooShort/MMTooFewMeaningfulWords.

## 9) Research inputs and practical considerations

- [x] Confirm SLF4J varargs are treated as placeholders with no positional throwable detection.
- [x] Confirm Surefire reporting configuration and enable phrased names in the `quality` profile.
- [ ] Review `CoreTestCommon` log-capture patterns to ensure tests can assert logged messages.
- [x] Confirm that `trace` and `info` are in scope (same standard as debug).
- [x] Confirm `ExceptionHandler.on(...)` callsites remain out of scope beyond direct `Jvm.*.on(...)`.
- [ ] Establish per-rule counts (src/main vs src/test) in `/home/peter/junit5/` for rarity-based rollout.
- [x] Apply checks to existing code to address tech debt (no "new code only" gating).
