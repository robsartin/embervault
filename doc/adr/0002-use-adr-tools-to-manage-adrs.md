# 2. Use adr-tools to Manage ADRs

Date: 2026-03-27

## Status

Accepted

## Context

Now that we have decided to use ADRs (see ADR-0001), we need a consistent way
to create and manage them. Manually creating ADR files is error-prone: numbering
can drift, formatting can vary, and the process adds friction that discourages
documentation.

adr-tools is a lightweight command-line package that automates the creation and
management of ADR files. It provides commands to create new ADRs from a
template, link related decisions, and generate a table of contents.

## Decision

We will use adr-tools (https://github.com/npryce/adr-tools) to manage our
Architecture Decision Records. The ADR directory is configured as `doc/adr/`.
A template file is provided at `doc/adr/template.md` for consistent formatting.

Team members should install adr-tools locally and use `adr new "Title"` to
create new records rather than copying files by hand.

## Consequences

- ADR creation is standardized and automated, reducing formatting inconsistencies.
- New ADRs are automatically numbered, preventing numbering conflicts.
- Team members need to install adr-tools, adding a lightweight developer
  dependency.
- The template can be customized to fit the project's needs over time.
- Superseding and linking ADRs is handled by the tooling rather than manual edits.
