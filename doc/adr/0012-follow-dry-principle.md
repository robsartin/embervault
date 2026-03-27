# 12. Follow DRY Principle

Date: 2026-03-27

## Status

Accepted

## Context

Duplicated logic is a recurring source of bugs: when a rule or calculation is expressed in multiple places, changes must be applied everywhere, and missed updates lead to inconsistent behavior. The Don't Repeat Yourself (DRY) principle addresses this by ensuring that every piece of knowledge has a single, authoritative representation in the codebase.

## Decision

We will follow the DRY principle: every distinct piece of domain knowledge, business logic, or structural pattern should be expressed in exactly one place. When duplication is detected, it should be extracted into a shared abstraction — a method, class, or module — that can be reused.

DRY applies to:

- **Domain logic** — Business rules are expressed once in the domain layer, not duplicated across adapters or use cases.
- **Configuration** — Shared constants and settings are defined in a single location.
- **Test utilities** — Common test setup and assertion logic is extracted into helper classes or methods rather than copied between test files.

This principle is enforced via code review. Automated detection of duplication (e.g., CPD) may be introduced in the future but is not part of this decision.

## Consequences

- Changes to business rules require modification in only one place, reducing the risk of inconsistency.
- The codebase is smaller and easier to navigate.
- Code reviewers actively look for duplication and suggest extraction during review.
- Over-application of DRY can lead to premature abstraction; the team will tolerate minor duplication when the duplicated pieces are likely to diverge in the future (the "Rule of Three" heuristic).
