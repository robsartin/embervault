# JUnit 6 Migration — Design

**Date:** 2026-04-23
**Status:** Approved; ready for implementation planning
**Scope:** Prepare EmberVault for an eventual JUnit Jupiter 6 upgrade, and defer the actual 5→6 bump until upstream test dependencies publish JUnit 6 artifacts.

## Context

EmberVault currently uses:

- `junit-jupiter` 5.11.4 (via `junit-bom`)
- `archunit-junit5` 1.4.1
- `testfx-junit5` (TestFX TestFX/TestFX)

JUnit 6.0.0 was released on 2025-09-30; the latest is 6.0.3 (2026-02-15). The Jupiter 5.x line continues, with **5.14.3** the latest stable 5.x release at the time of writing. The latest `archunit-junit5` targeting Platform 1.x / Jupiter 5.x is **1.4.2** (2026-04-18).

### Upstream blockers for a full JUnit 6 migration

- **ArchUnit** — `archunit-junit5` links against JUnit Platform 1.x, which shares the `org.junit.platform.*` package namespace with Platform 6.x and therefore cannot coexist on the classpath. Tracking: [TNG/ArchUnit#1556](https://github.com/TNG/ArchUnit/issues/1556) (open, no `archunit-junit6` artifact released).
- **TestFX** — `testfx-junit5` has the same Platform 1.x coupling; no JUnit 6 artifact has been published.

Bumping Jupiter to 6.x today forces Platform 6.x onto the classpath and breaks both dependencies. The project therefore **stays on Jupiter 5.x** for now and modernizes within the 5.x line.

### Codebase audit findings

Run 2026-04-23. The codebase is already clean of the APIs that disappear in JUnit 6:

- No `MethodOrderer.Alphanumeric` usage.
- No `@RunWith(JUnitPlatform.class)` / `junit-platform-runner` usage.
- No `junit-platform-jfr` or `junit-jupiter-migrationsupport` usage.
- No `@CsvSource` / `@CsvFileSource` usage (the FastCSV switch and `lineSeparator` removal do not apply). Only `@EnumSource` is used, which is unaffected.
- `maven-surefire-plugin` and `maven-failsafe-plugin` are both 3.5.2 (JUnit 6 requires ≥ 3.0.0).
- Java 25 is already the build baseline (JUnit 6 requires ≥ 17).

### Implication

The actual pre-migration work is small: two dependency version bumps plus a long-running tracker issue that captures the plan for when upstream catches up.

## Decision

Create three GitHub issues and execute them in order:

1. Umbrella tracking issue (stays open until JUnit 6 is adopted).
2. Bump `archunit-junit5` to its latest 5.x-compatible version.
3. Bump JUnit Jupiter from 5.11.4 to 5.14.3.

No new ADR is required — the decision is reversible, scoped to dependency versions, and fully captured by the tracking issue.

## Issues

### Issue 1 — Umbrella: Track upgrade to JUnit 6

**Labels:** `documentation`
**Not labelled `ready`** — this is a long-running tracker, not implementable work.

**Body covers:**

- Current state (Jupiter 5.x, ArchUnit on junit5 module, TestFX on junit5 module).
- Why the migration is deferred: classpath incompatibility between Platform 1.x and Platform 6.x; upstream blockers.
- Links to upstream tracking: [TNG/ArchUnit#1556](https://github.com/TNG/ArchUnit/issues/1556); TestFX JUnit 6 status (check repo when revisiting).
- Prerequisites already satisfied: Java 25, Surefire/Failsafe 3.5.2, no removed-in-6 APIs in use.
- Links to Issues 2 and 3 as completed prep work.
- Execution plan to use when upstream unblocks: bump `junit-bom` to 6.x, switch ArchUnit/TestFX to their JUnit 6 variants, run full verify, fix CSV exception-message assertions if any have been added since the audit.

**Closed when:** the Jupiter 6 bump PR lands.

### Issue 2 — Bump `archunit-junit5` to latest 5.x-compatible version

**Labels:** `ready`
**Branch:** `issue-<n>/bump-archunit`

**Change:** Update `<archunit-junit5.version>` in `pom.xml` to the latest release that still targets JUnit Platform 1.x / Jupiter 5.x. Verify the chosen version via Maven Central before committing.

**Verification:**

- `JAVA_HOME=... ./mvnw verify` passes.
- `JAVA_HOME=... ./mvnw verify -Pui-tests` passes (run locally with display; CI will confirm under xvfb).

**Rationale for landing first:** Smallest isolated change; de-risks the Jupiter bump by removing any transitive-pin surprises from an older ArchUnit against a newer Jupiter.

**Acceptance:**

- pom updated and only the version literal changes.
- All tests green on both profiles.
- No source changes required.

### Issue 3 — Bump JUnit Jupiter 5.11.4 → 5.14.3

**Labels:** `ready`
**Branch:** `issue-<n>/bump-junit-jupiter`

**Change:** Update `<junit-jupiter.version>` in `pom.xml` to `5.14.3`. The `junit-bom` drives the rest of the Jupiter and Platform artifacts.

**Verification:**

- `JAVA_HOME=... ./mvnw verify` passes.
- `JAVA_HOME=... ./mvnw verify -Pui-tests` passes.
- Review compiler output for new deprecation warnings introduced between 5.11 and 5.14. Each warning is resolved in its own red-green-refactor commit per the project's TDD workflow (CLAUDE.md).

**Rationale for landing last:** Larger surface area; benefits from having ArchUnit already current.

**Acceptance:**

- pom updated.
- No deprecation warnings attributable to the Jupiter bump remain unaddressed.
- Both verify profiles green.

## Workflow

- Issues created via `gh issue create` with the labels and bodies above.
- Each actionable issue (2 and 3) follows the project PR workflow (ADR-0020): issue branch, PR to main, CI must pass, manual merge only — no auto-merge.
- Issue 2 must be merged before work on Issue 3 begins, so that Issue 3's branch starts from a main that already contains the ArchUnit bump.
- Issue 1 stays open; Issues 2 and 3 link back to it.

## Out of scope

- Any change to `archunit-junit5` that crosses into a JUnit 6-compatible module (blocked on upstream).
- Any change to `testfx-junit5` (blocked on upstream).
- A new ADR — deferred; the umbrella issue is sufficient.
- Broader test refactoring — the TDD cycles triggered by deprecation warnings stay minimal and targeted.

## Success criteria

- Three GitHub issues exist with the stated labels and bodies.
- Issue 2's PR is merged; pom carries the newer `archunit-junit5` version; full verify green.
- Issue 3's PR is merged; pom carries Jupiter 5.14.3; full verify green; no unresolved new deprecation warnings.
- Issue 1 remains open with links to the two merged PRs, ready to drive the real JUnit 6 migration when upstream publishes compatible artifacts.
