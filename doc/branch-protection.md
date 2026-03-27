# Branch Protection Policy

## Protected Branch: `main`

The `main` branch has the following protection rules configured via the GitHub API.

### Rules

| Rule | Setting |
|------|---------|
| Pull requests required before merging | Yes |
| Required status checks | `build` (CI must pass) |
| Branches must be up to date before merging | Yes (strict mode) |
| Approving reviews required | No (solo developer) |
| Enforce for admins | No (admin bypass allowed) |
| Direct pushes | Blocked |
| Force pushes | Blocked |
| Branch deletion | Blocked |

### Rationale

- **Pull requests required** -- All changes flow through PRs so there is a clear review history and CI runs on every change.
- **CI `build` check required** -- Prevents merging code that does not compile or pass tests.
- **Strict status checks** -- The branch must be up to date with `main` before merging, ensuring CI validates the final merged state.
- **No required reviewers** -- As a solo project, requiring approvals would block all progress. This can be increased later when more contributors join.
- **Admin bypass enabled** -- The repository owner can force-merge in emergencies without waiting for status checks.

### Changing These Rules

Protection rules are managed via the GitHub API. To update them, use:

```bash
gh api repos/robsartin/embervault/branches/main/protection -X PUT --input - <<'JSON'
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["build"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": null,
  "restrictions": null
}
JSON
```

See the [GitHub branch protection API docs](https://docs.github.com/en/rest/branches/branch-protection) for all available options.
