# 7. Use JaCoCo for code coverage

Date: 2026-03-27

## Status

Accepted

## Context

As we adopt a TDD development process (ADR-0001), we need visibility into how much of the
codebase is exercised by our test suite. Without a code coverage tool, it is difficult to identify
untested code paths, and coverage can silently regress over time. We need an automated mechanism
integrated into the Maven build that measures coverage, generates reports, and enforces minimum
thresholds to prevent regressions.

## Decision

We will use [JaCoCo](https://www.jacoco.org/) integrated via the `jacoco-maven-plugin` to measure
and enforce code coverage. The plugin is configured with three executions:

- **prepare-agent**: Instruments the bytecode before tests run so that coverage data is collected.
- **report**: Generates both HTML and XML coverage reports during the `verify` phase. HTML reports
  provide a human-readable view; XML reports enable integration with CI tools and coverage services.
- **check**: Enforces minimum coverage thresholds during the `verify` phase. The build fails if
  line coverage or branch coverage falls below 80% at the bundle level.

The `App` class (JavaFX `Application` subclass) is excluded from coverage checks because it
requires a running JavaFX runtime and cannot be meaningfully unit tested.

## Consequences

- Developers get immediate feedback on test coverage via HTML reports in `target/site/jacoco/`.
- The build fails if coverage drops below the configured thresholds, preventing silent regressions.
- CI pipelines can consume the XML report for coverage badges and trend tracking.
- The JavaFX `App` class exclusion means that UI launch code is not subject to coverage enforcement;
  this is acceptable since it will be verified through integration or manual testing.
- Thresholds may need to be adjusted as the codebase grows and more complex untestable code
  (e.g., UI controllers) is introduced.
