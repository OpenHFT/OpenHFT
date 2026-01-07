# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

OpenHFT is a multi-module Maven aggregator for Chronicle Software's high-frequency trading libraries. It provides shared build infrastructure, quality rules, and dependency management for the Chronicle ecosystem.

## Build Commands

```bash
# Full build with all quality checks
mvn -q verify

# Build specific module (with dependencies)
mvn -q verify -am -pl :todo-tracker

# Skip quality enforcement (development only)
mvn -q verify -Dquality.enforced=false -Dlicense.skip=true -Dcheckstyle.skip=true

# Run mutation testing (slow, optional)
mvn -q verify -Ppitest
```

## Module Structure

```
OpenHFT (aggregator)
+-- root-parent-pom      # Base POM: plugin versions, JVM args, profiles
+-- third-party-bom      # External dependency versions (JUnit, Mockito, etc.)
+-- quality              # Checkstyle, SpotBugs, PMD, JaCoCo rules
+-- java-parent-pom      # Parent for Java projects (inherits quality)
+-- chronicle-bom        # Chronicle library versions
+-- todo-tracker         # Maven plugin: enforces TODO.md completion
+-- third-party-smoke    # Smoke tests for dependency compatibility
```

**Inheritance**: `root-parent-pom` <- `java-parent-pom` <- individual Java modules

## Quality Rules

Quality enforcement is centralised in the `quality` module and applied via parent POMs:

- **Checkstyle**: `net/openhft/quality/checkstyle26/chronicle-baseline-checkstyle.xml`
- **SpotBugs**: `net/openhft/quality/spotbugs26/chronicle-spotbugs-*.xml`
- **PMD**: `net/openhft/quality/pmd26/pmd-ruleset.xml`
- **JaCoCo**: Coverage thresholds per module (typically 90% line / 80% branch)

## Language and Style Requirements

From `AGENTS.md`:

- **British English** spelling (organisation, licence, behaviour)
- **ISO-8859-1 only** - no smart quotes, Unicode symbols, or extended characters
- **Javadoc**: Document contracts, edge-cases, thread-safety - not obvious facts
- **Commit messages**: <=72 chars, imperative mood, reference issues

## Test Framework

- JUnit 5 (Jupiter) with Mockito
- Run single test: `mvn -q test -Dtest=ClassName#methodName -pl :module-name`
- Coverage reports: `target/site/jacoco/index.html`
- Mutation reports: `target/pit-reports/index.html` (with `-Ppitest`)

## Key Files

- `AGENTS.md` - Detailed AI agent guidelines and documentation standards
- `quality/README.adoc` - Quality rules configuration guide
- `docs/Anatomy.adoc` - Visual dependency graph of all projects

## Development Guidelines

### Test Development

- **Never delete reasonable tests that fail** - add `@Disabled("reason")` with a meaningful message instead. Failing tests may indicate genuine bugs rather than test setup issues.
- **Run tests early and frequently** - verify existing tests pass before making changes; run tests after each significant change rather than batching at the end.
- **Use the full test cycle** - run `mvn test` to catch issues, not just compilation.

### Code Exploration

- **Avoid excessive codebase exploration** - gather requirements and understand the task scope before deep-diving into code.
- **Start with interfaces and contracts** - understand the public API before implementation details.
- **Read test files alongside implementation** - they document expected behaviour and edge cases.

### Implementation Workflow

- **Log key decisions** - when multiple approaches exist, document the choice and rationale.
- **Incremental verification** - verify each step works before proceeding to the next.
- **Build on existing patterns** - look for similar implementations in the codebase to maintain consistency.
