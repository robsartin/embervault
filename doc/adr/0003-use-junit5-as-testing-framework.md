# 3. Use JUnit 5 as testing framework

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault needs a testing framework to support the TDD workflow established in ADR-0020. The project targets Java 25 and uses Maven as its build tool. We need a modern, extensible testing framework that integrates well with this stack and supports current best practices such as display names, parameterized tests, and nested test classes.

## Decision

We will use JUnit 5 (Jupiter) as the project's testing framework. The JUnit BOM is imported in dependency management to ensure consistent versioning across all JUnit modules. The `junit-jupiter` aggregate artifact is declared with test scope, and Maven Surefire plugin 3.5.2 is configured to discover and run Jupiter tests.

## Consequences

- Test classes use JUnit 5 annotations (`@Test`, `@DisplayName`, `@Nested`, `@ParameterizedTest`, etc.) and the `org.junit.jupiter.api.Assertions` API.
- The JUnit 5 extension model is available for custom lifecycle hooks and dependency injection in tests.
- Adding additional JUnit 5 modules (e.g., `junit-jupiter-params`) requires only a dependency declaration; the BOM controls the version.
- Legacy JUnit 4 tests are not supported unless the `junit-vintage-engine` is explicitly added.
- Maven Surefire automatically discovers test classes following the `*Test` / `Test*` / `*Tests` naming conventions.
