# 4. Use Checkstyle for code style enforcement

Date: 2026-03-27

## Status

Accepted

## Context

As the project grows and more contributors join, maintaining a consistent code style becomes
increasingly important. Inconsistent formatting leads to noisy diffs, harder code reviews, and
unnecessary debates about style preferences. We need an automated tool that enforces a consistent
coding standard as part of the build process so that style issues are caught before code is merged.

## Decision

We will use [Checkstyle](https://checkstyle.sourceforge.io/) integrated via the
`maven-checkstyle-plugin` to enforce code style rules. The configuration is based on the
Google Java Style Guide with the following project-specific adjustments:

- **Line length**: 120 characters (instead of Google's 100) to better accommodate modern wide
  displays.
- **Indentation**: 4 spaces (instead of Google's 2) to match standard Java conventions.
- **Javadoc**: Required on public types and methods in production code, but not in test code.
- **Test relaxations**: Test classes are exempt from method naming restrictions (to allow
  descriptive `should_when` style names), file length limits, and Javadoc requirements.

Checkstyle runs during the `verify` phase so it executes as part of `mvn verify` and CI builds.
The build is configured to fail on any violation.

The Checkstyle configuration lives in `config/checkstyle/checkstyle.xml` with suppressions in
`config/checkstyle/suppressions.xml`.

## Consequences

- All code merged into the repository will follow a consistent style.
- Style discussions in code reviews are eliminated since the tool enforces the rules automatically.
- Contributors must ensure their code passes Checkstyle before submitting changes.
- IDE formatter configurations should be aligned with the Checkstyle rules to avoid friction.
- Adding new rules or relaxing existing ones requires updating the configuration files and
  potentially reformatting existing code.
