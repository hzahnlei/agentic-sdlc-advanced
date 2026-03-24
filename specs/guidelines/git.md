# Git & Versioning Guidelines

## Branching

- `main` is always in a releasable state — never commit broken code directly.
- Use short-lived `feature/<ticket>-<slug>` branches; merge within days, not weeks.
- No long-lived branches (no permanent `develop`, `staging`, etc.).
- Hotfixes branch from `main` and merge back into `main`.

## Conventional Commits

All commits must follow [Conventional Commits](https://www.conventionalcommits.org):

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

Allowed types:

| Type | When to use |
| --------- | ------------------------------------------------- |
| `feat` | New feature visible to users |
| `fix` | Bug fix |
| `perf` | Performance improvement |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `test` | Adding or updating tests |
| `docs` | Documentation only |
| `build` | Build system or dependency changes |
| `ci` | CI/CD configuration changes |
| `chore` | Maintenance (version bumps, config, tooling) |

Breaking changes: append `!` to the type (`feat!:`) or add a `BREAKING CHANGE:` footer.

## Commit Hygiene

- One logical change per commit — keep commits atomic and reviewable.
- Use present-tense imperative subject: "add login endpoint", not "added login endpoint".
- Subject line ≤ 72 characters; use the body for motivation and context.
- No "WIP", "fix typo", or "misc" commits in `main` history — squash before merge.

## Merge / Squash Strategy

- **Squash-merge** feature branches into `main` — one clean Conventional Commit per feature.
- The squash commit message must itself follow Conventional Commits.
- Preserve individual commits only for branches with multiple independent, meaningful changes.

## Pull / Merge Requests

- All changes reach `main` via a Merge Request — no direct pushes.
- CI pipeline must be green before merge.
- At least one peer approval required.
- Delete the source branch after merge.

## Semantic Versioning

Follow [SemVer](https://semver.org): `MAJOR.MINOR.PATCH`.

| Commit type | Version bump |
| -------------------- | ------------ |
| `feat` | MINOR |
| `fix`, `perf` | PATCH |
| Breaking change (`!`) | MAJOR |

- Start at `0.1.0`; `1.0.0` signals a stable public API.
- Pre-release: `1.0.0-alpha.1`, `1.0.0-rc.1`.
- Never decrement a version or reuse a released tag.

## Tags & Releases

- Tag only on `main` after a squash-merge.
- Format: `vX.Y.Z` (annotated tag: `git tag -a vX.Y.Z -m "Release vX.Y.Z"`).
- A `vX.Y.Z` tag triggers the release pipeline (see [CI/CD Guidelines](ci_cd.md)).
- Never delete or move a released tag.

## CHANGELOG

- Auto-generated from Conventional Commits via `git-cliff` (configured in `cliff.toml`).
- Do not edit `CHANGELOG.md` manually — the release pipeline regenerates it on every tag.
- `RELEASE_NOTES.md` covers the latest release only; `CHANGELOG.md` covers full history.

## History Hygiene

- No force-push to `main` or any protected branch.
- Rebase feature branches on `main` before merge — do not merge `main` into the feature branch.
- Keep `main` linear (no merge commits).
- Use `git rebase -i` locally to clean up commits before opening a Merge Request.
