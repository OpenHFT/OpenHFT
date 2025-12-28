## Overview
This ruleset (`pmd-ruleset.xml`) serves as the baseline static analysis configuration for all OpenHFT projects.
It targets **PMD 7.19.0** (see `pmdVersion` in `java-parent-pom`).

## Maintenance Strategy
1. **Centralized Config:** Changes here propagate to all projects inheriting from `root-parent-pom` or referencing this ruleset.
2. **Warn by default:** In the Chronicle parent POMs, failing the build is controlled by `quality.enforced` (wired to `failOnViolation`).
   The default is typically non-blocking to allow incremental cleanup, but CI can set `quality.enforced=true`.
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

For review-friendly builds, capture output with `-l <log>` and scan the log for warnings and errors rather than relying on the exit code alone.

---

## Maven PMD Plugin & CPD Configuration

### Plugin Version & Runtime
* **Plugin**: `maven-pmd-plugin` (configured in `java-parent-pom`)
* **PMD Runtime**: `net.sourceforge.pmd:pmd-core` / `pmd-java` pinned to **7.19.0** via plugin `<dependencies>`
* **Ruleset**: Modules use `${pmd.ruleset}` (`pmd-ruleset.xml`) with categories + per-rule excludes

### PMD Analysis
* **Goal**: `pmd:check` bound to `verify` phase
* **Failure Mode**: `failOnViolation=${quality.enforced}` (non-blocking unless `quality.enforced=true`)
* **Performance**:
  * `analysisCache=true` – incremental analysis (only changed files)
  * Multi-threaded analysis automatic in PMD 7.x
* **Excludes**: Generated sources, target directories, build artifacts

### CPD (Copy-Paste Detector)
* **Goal**: `pmd:cpd-check` bound to `verify` phase
* **Failure Mode**: `failOnViolation=${quality.enforced}` (non-blocking unless `quality.enforced=true`)
* **Sensitivity**: Uses PMD defaults unless a module overrides `<minimumTokens>` in its own POM.
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

### ISO 9001 Compliance Mapping

| Tool | ISO Clause | Evidence |
|------|-----------|----------|
| **PMD** | 8.5.1 (Production Control) | Code quality enforcement |
| **PMD** | 9.1 (Monitoring) | Quality metrics in reports |
| **CPD** | 10.3 (Improvement) | Duplicate code detection |
| **Reports** | 8.5.2 (Traceability) | `target/site/*.html` |

## Profiles / layering guidance

- **Baseline** (`pmd-ruleset.xml`): Excludes noisy rules and now includes the former core and strict additions (including `UnitTestAssertionsShouldIncludeMessage`, `UnitTestShouldIncludeAssert`, `UnitTestShouldUseTestAnnotation`, `MissingOverride`, `AvoidUsingHardCodedIP`, `UnnecessaryWarningSuppression`, `FinalizeOverloaded`, `PackageCase`, `ImplicitSwitchFallThrough`, and the multithreading additions).
- **Core/Strict**: The separate core and strict rulesets have been removed; use the baseline ruleset everywhere.

Use `-Dpmd.ruleset=net/openhft/quality/pmd26/pmd-ruleset.xml` to point at the consolidated ruleset.
