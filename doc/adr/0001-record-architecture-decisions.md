# 1. Record Architecture Decisions

Date: 2026-03-27

## Status

Accepted

## Context

We need to record the architectural decisions made on this project. These
decisions affect the structure and direction of the codebase, and future
contributors (including our future selves) need to understand why certain
choices were made.

Without a clear record, knowledge about past decisions is lost when team
members move on or memories fade. This leads to repeated discussions,
accidental reversals of decisions, and difficulty onboarding new contributors.

## Decision

We will use Architecture Decision Records (ADRs), as described by Michael
Nygard in his article "Documenting Architecture Decisions."

Each ADR will be a short Markdown file in the `doc/adr/` directory. Each record
will contain a title, date, status, context, decision, and consequences. ADRs
will be numbered sequentially and will not be removed; if a decision is
reversed, a new ADR will supersede the old one.

## Consequences

- All significant architectural decisions will be documented in one place.
- New contributors can read through past ADRs to understand the reasoning
  behind the current state of the project.
- The cost of writing an ADR is low, but the discipline must be maintained
  by the team.
- ADRs are immutable once accepted; changes require a new ADR that supersedes
  the previous one.
