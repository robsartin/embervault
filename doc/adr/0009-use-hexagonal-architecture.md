# 9. Use Hexagonal Architecture

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault needs a clear separation between domain logic and external concerns such as the UI (JavaFX), persistence, and any future integrations. Without an explicit architectural boundary, domain logic tends to leak into framework-specific code, making the system difficult to test, refactor, and evolve. The Hexagonal Architecture (Ports & Adapters) pattern addresses this by placing the domain at the center and defining explicit interfaces (ports) through which all external interaction flows.

## Decision

We will structure EmberVault following the Hexagonal Architecture (Ports & Adapters) pattern. Domain logic is isolated from all external concerns via ports (interfaces defined by the domain) and adapters (implementations that bridge the domain to infrastructure such as UI, persistence, and external services).

The package structure is:

- `com.embervault.domain` — Core domain model: entities, value objects, aggregates, domain services, and domain events. This package has zero dependencies on frameworks or infrastructure.
- `com.embervault.application.port.in` — Inbound ports (use case interfaces) that define how the outside world drives the application.
- `com.embervault.application.port.out` — Outbound ports (repository and gateway interfaces) that define what the domain needs from infrastructure.
- `com.embervault.adapter.in.ui` — Inbound adapters: JavaFX controllers and views that translate user actions into use case calls.
- `com.embervault.adapter.out.persistence` — Outbound adapters: implementations of outbound ports for data storage.

Dependency flow is strictly inward: adapters depend on ports, ports depend on the domain, and the domain depends on nothing outside itself.

## Consequences

- Domain logic can be tested in isolation without starting JavaFX or connecting to a database.
- Swapping an adapter (e.g., replacing file-based persistence with a database) requires only a new adapter implementation; no domain changes are needed.
- The explicit port interfaces serve as a contract between layers, making the system easier to reason about.
- Developers must place new code in the correct package and follow the dependency direction, which adds a small learning curve.
- ArchUnit tests enforce the dependency rules automatically as part of the build (see ADR-0008).
