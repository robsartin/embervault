# ADR-0001: Use Pure TDD as Development Process

## Date

2026-03-27

## Status

Accepted

## Context

EmberVault is a new JavaFX project starting from an empty repository. We need to establish a development workflow before any production code is written. Several approaches were considered:

1. **Write code first, add tests later.** This is the most common workflow in practice. Tests tend to be written after the fact (if at all), leading to gaps in coverage and designs that are difficult to test.

2. **Write tests alongside code.** A middle-ground approach where developers write tests concurrently with production code. While better than test-after, it still allows production code to be merged without a corresponding test and gives no structural guarantee that tests drive the design.

3. **Pure TDD (test-first, one cycle at a time).** Every line of production code is preceded by a failing test. Development proceeds in small Red-Green-Refactor cycles. Commits capture each passing state.

Key factors influencing the decision:

- The project is greenfield, so there is no legacy code to work around.
- The team values high confidence in correctness, especially as the codebase grows.
- JavaFX applications are often difficult to test retroactively because UI and logic become entangled. A test-first discipline encourages separation of concerns from the start.
- Small, well-tested commits make code review faster and reduce the risk of large, hard-to-debug merges.

## Decision

We will use **pure TDD** as the mandatory development process for all production code in EmberVault.

Concretely this means:

- No production code is written without a failing test that motivates it.
- The development cycle is: **Red** (write a failing test) -> **Green** (write the minimum code to pass) -> **Commit** -> **Refactor** -> **Repeat**.
- Every commit on a feature branch must leave all tests passing.
- Pull requests are expected to show a commit history that reflects the TDD cycle.

### Branch and PR Workflow

All development work is done on **issue branches** and submitted as **pull requests to main**:

- **No direct commits to main.** Branch protection blocks direct pushes.
- **Branch naming:** `issue-<number>/<short-description>` (e.g., `issue-42/map-outline-views`) or `fix/<description>` for non-issue fixes.
- **PRs require passing CI:** tests, checkstyle, JaCoCo coverage, and ArchUnit rules must all pass before merge.
- **Manual merge only.** Auto-merge is disabled. PRs are reviewed and merged by pressing the merge button.
- **Exploratory spikes** use throwaway branches that are never merged directly — results are reimplemented via TDD on a proper issue branch.

## Consequences

### Positive

- **Design feedback is immediate.** Hard-to-test code surfaces instantly, encouraging cleaner interfaces and better separation of concerns.
- **High test coverage by construction.** Because every behavior starts as a test, coverage gaps are structurally unlikely.
- **Small commits reduce risk.** Each commit is a known-good state, making bisecting regressions straightforward.
- **Living documentation.** The test suite describes what the system does in executable form.
- **Onboarding is easier.** New contributors can read the tests to understand expected behavior and follow a well-defined workflow from day one.

### Negative

- **Steeper learning curve.** Contributors unfamiliar with TDD will need time to adopt the discipline.
- **Slower initial velocity.** Writing tests first feels slower at the start, especially for UI work where feedback loops are longer.
- **Rigid process.** Exploratory spikes and prototypes still need a separate throwaway branch; the TDD rule applies to all code that targets the main branch.

### Neutral

- Testing frameworks and tooling (JUnit 5, TestFX, or equivalents) must be set up early, before any feature work begins.
- The team will need to agree on conventions for test naming, file layout, and acceptable test scope.
