## Overview
This ruleset (`pmd-ruleset.xml`) serves as the baseline static analysis configuration for all OpenHFT projects.
It targets **PMD 7.19.0** (via `maven-pmd-plugin` 3.25.0).

## Maintenance Strategy
1. **Centralized Config:** Changes here propagate to all projects inheriting from `root-parent-pom` or referencing this ruleset.
2. **Warn Only:** The build is configured to **warn** (not fail) on violations (`failOnViolation=false`) to allow for gradual cleanup.
3. **Exclusions:**
    - To **disable a rule entirely**, add it to the `<exclude>` list inside the relevant `<rule ref="category/...">` block.
    - **Do not** add duplicate `<rule>` tags for the same category; use the existing reference.
    - Note that some rules (e.g., `AccessorClassGeneration`, `DataflowAnomalyAnalysis`) have been removed in PMD 7.

## Key Changes in PMD 7
- **JUnit Rules:** Many `JUnit...` rules are renamed to `UnitTest...` (e.g., `UnitTestShouldIncludeAssert`).
- **Removed Rules:** Deprecated rules like `AccessorClassGeneration` are gone.
- **Strictness:** Type resolution is more strict; ensure dependencies are correct.

## Common False Positives
- **Law of Demeter:** Often noisy in builder patterns (suppressed).
- **Bean Members:** Often flags intentional public fields in value objects (suppressed).

## Testing Changes
Run the following command from the root of any project to verify changes:
```bash
mvn pmd:check
```
Check `target/site/pmd.html` or the console output for details.

---

## Maven PMD Plugin & CPD Configuration

### Plugin Version & Runtime
* **Plugin Version**: `maven-pmd-plugin:3.25.0` (**Java 8 compatible**)
* **PMD Runtime**: Overridden to `net.sourceforge.pmd:pmd-core` / `pmd-java` **7.19.0** via plugin `<dependencies>`
* **Ruleset**: All modules use the central `${pmd.ruleset}` (`pmd-ruleset.xml`) with categories + per-rule excludes

### PMD Analysis
* **Goal**: `pmd:check` bound to `verify` phase
* **Failure Mode**: `pmd.failOnViolation=false` – non-blocking, violations tracked in reports
* **Performance**:
  * `analysisCache=true` – incremental analysis (only changed files)
  * Multi-threaded analysis automatic in PMD 7.x
* **Excludes**: Generated sources, target directories, build artifacts

### CPD (Copy-Paste Detector)
* **Goal**: `pmd:cpd-check` bound to `verify` phase
* **Failure Mode**: `failOnViolation=false` – **warn mode** (non-blocking)
* **Sensitivity**: `minimumTokens=300` (lenient threshold for 1.6M LOC codebase)
  * Targets top ~20 largest duplications (analysis-driven)
  * Will be reduced incrementally as large duplications are addressed
* **Scope**: `includeTests=true` – checks both main and test code
* **Report**: Available at `target/site/cpd.html` after `mvn verify`

### Quick Commands

```bash
# Run PMD analysis only
mvn pmd:check

# Run CPD analysis only
mvn pmd:cpd-check

# Run both PMD + CPD (via verify)
mvn verify -DskipTests

# Generate HTML reports
mvn site
# View: target/site/pmd.html and target/site/cpd.html

# Check specific module
cd Chronicle-Core && mvn verify -DskipTests
```

### Tuning CPD per Module

To adjust CPD sensitivity for specific modules, override in the module's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <executions>
                <execution>
                    <id>cpd-check</id>
                    <configuration>
                        <!-- Example: Stricter after addressing baseline=300 issues -->
                        <minimumTokens>200</minimumTokens>
                        <!-- Or more lenient for intentional patterns -->
                        <!-- <minimumTokens>500</minimumTokens> -->
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Baseline Strategy (1.6M LOC):**
- **Phase 1 (Current)**: `minimumTokens=300` – Address top ~20 largest duplications
- **Phase 2**: Reduce to `200` – Target 50-75th percentile (27-36 lines)
- **Phase 3**: Reduce to `150` – Approach median (20+ lines)
- **Ongoing**: Core modules can set stricter thresholds as quality improves

### ISO 9001 Compliance Mapping

| Tool | ISO Clause | Evidence |
|------|-----------|----------|
| **PMD** | 8.5.1 (Production Control) | Code quality enforcement |
| **PMD** | 9.1 (Monitoring) | Quality metrics in reports |
| **CPD** | 10.3 (Improvement) | Duplicate code detection |
| **Reports** | 8.5.2 (Traceability) | `target/site/*.html` |

## Profiles / layering guidance

- **Baseline** (`pmd-ruleset.xml`): Excludes noisy rules; explicitly excludes `UnitTestAssertionsShouldIncludeMessage`, `UnitTestShouldIncludeAssert`, and `MissingOverride`.
- **Core** (`pmd-ruleset-core.xml`): Baseline + core additions. Does not re-enable the excluded unit-test/missing override rules.
- **Strict** (`pmd-ruleset-strict.xml`): Inherits **core** and then adds the strict-only rules (including `UnitTestAssertionsShouldIncludeMessage`, `UnitTestShouldIncludeAssert`, `MissingOverride`, and other low-count additions). Use this for new/greenfield modules.

Use `-Dpmd.ruleset=...` to point at the desired ruleset; keep baseline/core clean and layer strict on top to avoid duplicates.
