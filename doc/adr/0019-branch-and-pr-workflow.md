# 19. Branch and PR Workflow

Date: 2026-03-27

## Status

Accepted

## Context

EmberVault needs a consistent, documented workflow for how code changes flow into the main branch. Without clear rules, developers may push directly to main, skip CI checks, or use inconsistent branch naming, leading to integration issues, broken builds, and difficulty tracing changes back to issues.

We want every change to be reviewed, tested by CI, and traceable to an issue or fix before it lands on main.

## Decision

All development work is done on branches and submitted as pull requests to main. The following rules apply:

1. **No direct pushes to main.** Branch protection rules block direct commits to the main branch.

2. **All code changes on feature/issue branches.** Every change -- features, refactors, documentation -- is developed on a dedicated branch, not on main.

3. **Branch naming convention.** Branches follow one of these patterns:
   - `issue-<number>/<short-description>` -- for work tied to a GitHub issue (e.g., `issue-42/map-outline-views`)
   - `fix/<description>` -- for small fixes not tied to a specific issue (e.g., `fix/typo-in-readme`)

4. **Changes submitted as pull requests.** Every branch is submitted as a PR targeting main. PRs provide a record of what changed, why, and any review discussion.

5. **PRs require passing CI.** All quality gates -- JUnit 5 tests, Checkstyle, JaCoCo coverage, ArchUnit architecture rules -- must pass before a PR can be merged.

6. **Manual merge only.** Auto-merge is not used. A human reviews and explicitly merges each PR to ensure intentional integration.

## Consequences

### Positive

- Every change is reviewed and tested before reaching main, reducing the risk of broken builds
- Branch protection enforces the workflow at the repository level, not just by convention
- Consistent branch naming makes it easy to trace branches back to issues
- CI enforcement ensures quality gates are never skipped
- Manual merge keeps a human in the loop for final integration decisions

### Negative

- Small documentation or config changes still require the full PR workflow, adding overhead for trivial changes
- Developers must wait for CI to pass before merging, which may slow down urgent fixes
- Branch naming conventions require discipline and may need enforcement tooling over time
