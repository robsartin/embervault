# 10. Use Domain-Driven Design

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault manages interconnected notes, links, and metadata — a domain with meaningful business rules and relationships. A naive approach of scattering logic across controllers and utility classes leads to an anemic domain model that is hard to understand and maintain. Domain-Driven Design (DDD) provides tactical patterns that give structure to the domain layer, making the codebase a direct expression of the problem space.

## Decision

We will apply DDD tactical patterns within the `com.embervault.domain` package:

- **Entities** — Objects with a distinct identity that persists over time (e.g., a Note). Identity is based on a unique identifier, not attribute equality.
- **Value Objects** — Immutable objects defined entirely by their attributes with no conceptual identity (e.g., a NoteTitle, a Tag). They are compared by value, not reference.
- **Aggregates** — Clusters of entities and value objects that form a consistency boundary. External code references an aggregate only through its root entity.
- **Repositories** — Interfaces (outbound ports) that provide collection-like access to aggregates. Implementations live in the adapter layer, not in the domain.
- **Domain Services** — Stateless operations that express domain logic which does not naturally belong to a single entity or value object.

All domain types reside in `com.embervault.domain` and its sub-packages. They must not depend on infrastructure frameworks.

## Consequences

- The domain model directly reflects the problem space, making the code easier to understand and discuss with non-technical stakeholders.
- Aggregates enforce consistency boundaries, reducing the risk of invalid state.
- Value objects eliminate a broad class of bugs related to mutable shared state.
- Repository interfaces decouple the domain from persistence details, supporting the Hexagonal Architecture (ADR-0009).
- Developers must understand DDD tactical patterns, which requires an initial learning investment.
- Over-engineering is a risk; we will apply these patterns pragmatically, introducing aggregates and domain services only when the complexity warrants them.
