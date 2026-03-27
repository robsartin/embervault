# 15. Use Maven profiles for dev and prod

Date: 2026-03-27

## Status

Accepted

## Context

The project needs different configurations for development and production
environments. During development, verbose DEBUG-level logging aids
troubleshooting, while production requires quieter INFO/WARN-level logging
with a cleaner output format. Without a profile mechanism, developers must
manually swap configuration files or remember to change settings before
building a release artifact.

## Decision

We will use Maven profiles (`dev` and `prod`) combined with resource filtering
to select the appropriate Logback configuration at build time.

- The `dev` profile is active by default and sets `logback.profile` to
  `logback-dev.xml` (DEBUG level, verbose console output with thread names and
  full timestamps).
- The `prod` profile sets `logback.profile` to `logback-prod.xml` (WARN root
  level, INFO for `com.embervault`, compact console output).
- The main `logback.xml` uses a filtered `<include resource="${logback.profile}"/>`
  directive so the Maven-selected profile configuration is loaded at runtime.
- Resource filtering is scoped to `logback.xml` only, avoiding unintended
  token replacement in other resource files (e.g., FXML).

Usage:

    ./mvnw verify              # builds with dev profile (default)
    ./mvnw verify -Pprod       # builds with prod profile

## Consequences

- Developers get verbose logging out of the box without any extra flags.
- Production builds are created with a single `-Pprod` flag, reducing the
  chance of shipping a DEBUG-level artifact.
- Adding new profile-specific properties (e.g., database URLs, feature flags)
  follows the same pattern: declare a property in each `<profile>` block and
  reference it in a filtered resource.
- Logback configuration is now split across three files (`logback.xml`,
  `logback-dev.xml`, `logback-prod.xml`), which adds a small amount of
  indirection compared to a single file.
