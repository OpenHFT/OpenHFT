# TODO Tracker - Implementation Tasks

This file serves as the first test case for the plugin, demonstrating all supported formats.

## Phase 1: Project Setup

- [x] [P1] [E:S] Create module directory structure
- [x] [P1] [E:M] Configure pom.xml with maven-plugin packaging
- [x] [P1] [E:S] Add module to root aggregator pom.xml

## Phase 2: Core Model

- [x] [P1] [E:M] Implement TodoTask immutable model class with priority/effort
- [x] [P1] [E:S] Implement TodoReport aggregation class
- [x] [P2] [E:M] Write unit tests for model classes

## Phase 3: Parser Implementation

- [x] [P1] [E:L] Implement TodoParser for all checkbox formats (-, *, numbered)
- [x] [P2] [E:M] Add context extraction for section headers
- [x] [P2] [E:M] Add priority/effort tag extraction
- [x] [P2] [E:S] Create test resource markdown files
- [x] [P1] [E:M] Achieve 80% branch coverage for parser
- [x] [P2] [E:S] Add dropped state [-] syntax support

## Phase 4: File Discovery

1. [x] [P1] [E:M] Implement TodoFileLocator
2. [x] [P2] [E:S] Support configurable search paths
3. [x] [P2] [E:S] Handle todo/ subdirectory
4. [x] [P2] [E:M] Add glob pattern matching for *TODO.md, todo/*.md
5. [x] [P2] [E:M] Add AsciiDoc patterns: todo/*.adoc, src/main/docs/*plan.adoc

## Phase 5: Reporting

* [x] [P1] [E:M] Implement TodoReporter output formatter
* [x] [P2] [E:S] Add max tasks limiting logic
* [x] [P2] [E:S] Include resolution instructions in output
* [x] [P3] [E:S] Show priority/effort in output

## Phase 6: Maven Mojo

- [x] [P1] [E:L] Implement CheckTodoMojo with @Mojo annotation
- [x] [P2] [E:S] Add all configuration parameters
- [x] [P1] [E:S] Implement MojoFailureException for build failure
- [x] [P2] [E:S] Add usePatterns parameter for glob pattern mode

## Phase 7: Testing & Quality

- [x] [P1] [E:M] Configure JaCoCo for 80% branch coverage
- [x] [P2] [E:L] Write integration tests
- [x] [P2] [E:S] Verify plugin works on itself (dogfooding)

## Phase 8: AsciiDoc Support

- [x] [P2] [E:M] Add .adoc file discovery patterns
- [x] [P1] [E:M] Add format-specific parsing (Markdown vs AsciiDoc)
- [x] [P2] [E:M] Add AsciiDoc heading context extraction (=, ==, ===)
- [x] [P2] [E:S] Add AsciiDoc [*] completed checkbox support
- [x] [P2] [E:M] Add format-aware context pattern configuration
- [x] [P2] [E:M] Write .adoc parser tests
- [x] [P2] [E:S] Write .adoc locator tests
- [x] [P3] [E:S] Update documentation for AsciiDoc support

## Phase 9: Future Enhancements (Planned)

- [ ] [P3] [E:L] JSON output format
- [ ] [P3] [E:L] SARIF output format for GitHub code scanning
- [ ] [P3] [E:L] Task ID parsing and validation
- [ ] [P3] [E:L] Dependency graph support
- [ ] [P3] [E:L] MCP server integration
