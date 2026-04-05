# 19. Use Maven as Build Tool

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault needs a build tool to manage compilation, testing, dependency management, and packaging. The main candidates are Maven and Gradle, both widely used in the Java ecosystem.

Maven provides a well-established, convention-over-configuration approach with a declarative POM model. It has mature IDE support, a vast plugin ecosystem, and a standardized project structure that most Java developers recognize immediately. Gradle offers more flexibility and faster incremental builds but introduces a Groovy/Kotlin DSL that adds learning curve and can lead to non-standard build configurations.

For a new project where build simplicity and long-term maintainability are priorities, a declarative build system with strong conventions reduces onboarding friction and minimizes build-related surprises.

## Decision

We will use Apache Maven as the build tool for EmberVault.

## Consequences

- The project follows a standard Maven directory layout (src/main/java, src/test/java, etc.), which is immediately familiar to Java developers.
- Dependency management is handled declaratively via pom.xml with a dependencyManagement section for version control.
- The Maven Wrapper is included so contributors do not need a pre-installed Maven version.
- Build configuration is verbose compared to Gradle, but explicit and predictable.
- Plugin versions are pinned in pluginManagement to ensure reproducible builds.
