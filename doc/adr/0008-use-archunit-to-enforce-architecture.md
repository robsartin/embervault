# 8. Use ArchUnit to Enforce Architecture

Date: 2026-03-27

## Status

Accepted

## Context

Architecture Decision Records document intended constraints, but nothing prevents developers from inadvertently violating them. Manual code review catches some violations, but automated enforcement is more reliable and provides faster feedback. The project already uses JUnit 5 and Maven Surefire, so an architecture testing library that integrates with these tools would fit naturally into the existing build pipeline.

## Decision

We will use ArchUnit (`com.tngtech.archunit:archunit-junit5`) to write executable architecture tests that verify compliance with our ADRs.

- Architecture tests live under `src/test/java/com/embervault/architecture/` and run as part of the standard test suite.
- Each rule references the ADR it enforces (e.g., ADR-0005 for SLF4J logging).
- New ADRs that introduce enforceable constraints should be accompanied by corresponding ArchUnit rules where practical.

## Consequences

- Architectural violations are caught automatically during `mvn test`, preventing them from reaching the main branch.
- Developers get immediate feedback when a change breaks an architectural constraint, with a clear message explaining the rule and the ADR behind it.
- Adding new architecture rules requires only writing declarative ArchUnit test methods; no build plugin configuration is needed.
- The test suite execution time increases slightly, but ArchUnit analysis is fast and the overhead is negligible.
- Because the project uses Java modules (JPMS), the architecture tests use `ClassFileImporter.importPath()` to scan the compiled classes directory directly rather than `@AnalyzeClasses`, which cannot see into named modules via package-based class loading.
