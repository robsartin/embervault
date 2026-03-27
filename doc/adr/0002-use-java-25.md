# 2. Use Java 25

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault is a new project and can freely choose its target Java version. Java 25, released in September 2025, is a Long-Term Support (LTS) release that brings significant language and platform improvements including continued evolution of pattern matching, virtual threads (stabilized), structured concurrency, and scoped values.

Choosing a current LTS release maximizes the support window and gives the project access to the latest stable language features, performance improvements, and security patches. Older LTS versions (17, 21) are still supported but would forgo years of language evolution without a compelling compatibility reason.

## Decision

We will target Java 25 as the minimum required Java version for building and running EmberVault.

## Consequences

- The project can use all language features up to and including Java 25 (records, sealed classes, pattern matching, virtual threads, structured concurrency, etc.).
- Contributors must have JDK 25 or later installed; the Maven compiler plugin enforces this via the `release` flag.
- Library and framework choices must be compatible with Java 25, though the vast majority of the ecosystem supports it.
- The long-term support window of Java 25 provides a stable foundation for years of development without forced upgrades.
