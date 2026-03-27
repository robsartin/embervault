# 11. Follow SOLID Principles

Date: 2026-03-27

## Status

Accepted

## Context

As EmberVault grows, maintaining a codebase that is easy to understand, extend, and refactor becomes increasingly important. The SOLID principles are a well-established set of object-oriented design guidelines that promote flexible, maintainable software. Adopting them early establishes a shared design vocabulary and prevents the accumulation of tightly coupled, rigid code.

## Decision

We will follow the SOLID principles across the codebase:

- **Single Responsibility Principle (SRP)** — Each class has one reason to change. Domain entities model domain concepts, adapters handle infrastructure translation, and use cases orchestrate application workflows.
- **Open/Closed Principle (OCP)** — Classes are open for extension but closed for modification. New behavior is added through new implementations of existing interfaces rather than modifying existing classes.
- **Liskov Substitution Principle (LSP)** — Subtypes are substitutable for their base types without altering the correctness of the program. Implementations of ports and domain interfaces honor the contracts defined by their supertypes.
- **Interface Segregation Principle (ISP)** — Clients are not forced to depend on interfaces they do not use. Inbound and outbound ports are fine-grained, each representing a specific use case or capability.
- **Dependency Inversion Principle (DIP)** — High-level modules do not depend on low-level modules; both depend on abstractions. The domain defines port interfaces, and adapters provide implementations, following the dependency direction mandated by the Hexagonal Architecture (ADR-0009).

These principles are enforced via code review. Automated enforcement through static analysis or ArchUnit is not practical for most SOLID principles, which require human judgment about design intent.

## Consequences

- The codebase favors small, focused classes and interfaces, which are easier to test and reason about.
- New features can often be added by implementing new classes rather than modifying existing ones, reducing regression risk.
- Code reviews have a shared vocabulary for discussing design quality.
- Strict adherence can lead to over-abstraction; the team will apply these principles pragmatically, guided by actual complexity rather than speculative future needs.
