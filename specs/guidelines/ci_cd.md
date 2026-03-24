# CI/CD Guidelines

## Pipeline Stages

Run in order; parallelize within each stage:

1. **Check** — architecture check | lint & static analysis | secret scan | unit tests
2. **Build** — release Docker image; build once, promote across environments
3. **Integration tests** — test the image from stage 2
4. **Deploy** — push to registry; manual trigger for production

Never recompile between staging and production. Inject all configuration via environment variables.

## Registry Tagging

| Event            | Tags                                |
| ---------------- | ----------------------------------- |
| Pull Request     | `pr-<N>`, `sha-<short>` (ephemeral) |
| Merge to main    | `latest`, `build-<N>`               |
| Git tag `v1.0.0` | `1.0.0`, `stable`                   |

## Makefile Targets

- `debug-build` — debug symbols, no optimizations; used for development and testing.
- `release-build` — no debug symbols, speed optimizations; containerized and integration-tested.

## Releases

A Git tag `vX.Y.Z` triggers the `release` pipeline job, which:

1. Generates `RELEASE_NOTES.md` (latest tag only) for the GitLab release description.
2. Regenerates `CHANGELOG.md` (full history) and commits it back to the default branch.
3. Creates a GitLab release entry linked to the tag.

**Required CI/CD variable:** `CI_ACCESS_TOKEN`

Set this as a protected project CI/CD variable with `write_repository` scope (Project Access Token
or Personal Access Token). It allows the pipeline to push the updated `CHANGELOG.md` back to the
default branch. Without it, the changelog commit step is skipped and the job fails.

## Documentation

A Git tag `vX.Y.Z` triggers publishing the documentation as a GitLab Pages site.
Publish on release tags only — not on every commit — so published docs always match
a stable, released version.

### GitLab Pages requirements

- **Job name must be exactly `pages`** — GitLab Pages ignores any other job name.
- **Artifacts must be in `public/`** — place the built site output there.
- **`expire_in: never`** — expiring Pages artifacts takes the site offline.
- Use `needs: [build]` as a quality gate so docs publish only when the binary is healthy.

### Skeleton

```yaml
pages:
  stage: release
  image: <your-mkdocs-image>
  needs: [build]
  rules:
    - if: $CI_COMMIT_TAG =~ /^v[0-9]+\.[0-9]+\.[0-9]+$/
  cache:
    paths: []
  script:
    - mkdocs build --site-dir public
  artifacts:
    paths:
      - public
    expire_in: never
```

## GitLab Hints

### CI_ACCESS_TOKEN setup (required for the release job)

The `release` job pushes the updated `CHANGELOG.md` back to the default branch. This requires a
token with push access and matching branch protection settings.

Required setup (once per project):

1. **Protected Branch setting** — Settings → Repository → Protected Branches → `main`
   - "Allowed to push": **Maintainers**
   (Developers still cannot push directly and must use Merge Requests.)
2. **Create a Project Access Token** — Settings → Access Tokens
   - Role: Maintainer
   - Scope: `write_repository`
3. **Add CI/CD variable** — Settings → CI/CD → Variables
   - Name: `CI_ACCESS_TOKEN`
   - Value: the token
   - Enable **Protected**
4. **Protect the version tag pattern** — Settings → Repository → Protected Tags
   - Add pattern `v*`

Protected variables are only injected into pipelines triggered by protected branches or tags.
If the tag is not protected, `CI_ACCESS_TOKEN` is empty and the push fails with 401.

### Reserved job name `image:`

`image:` is a **reserved keyword** in GitLab CI/CD — it sets the Docker executor image for a job,
not a job name. Using it as a job name causes a pipeline parse error.

Name Docker image build jobs `build-image:` and `release-image:` instead.

### Overriding ENTRYPOINT for non-shell images

Some CI images declare a custom `ENTRYPOINT` (e.g. `aquasec/trivy` uses `trivy`,
`gcr.io/kaniko-project/executor` uses `/kaniko/executor`). GitLab CI injects commands
via `sh -c`, which fails when a custom ENTRYPOINT is set. Override it with `entrypoint: [""]`
to restore normal script execution.

Always use the long-form `image:` block for these images:

```yaml
scan:
  image:
    name: aquasec/trivy:latest
    entrypoint: [""]
  script:
    - trivy fs ...
```

### Quality report artifact convention

All quality gate outputs must be written to `test_reports/` and exposed as CI artifacts
with `when: always` and `expire_in: 7 days`. This makes reports downloadable from any
pipeline run — including failed runs where the report is most needed.

| Sub-directory             | Content                              |
| ------------------------- | ------------------------------------ |
| `test_reports/linting/`   | Linter output (checkstyle XML, text) |
| `test_reports/arch-test/` | Architecture check output            |
| `test_reports/test/`      | Unit / BDD test results (JUnit XML)  |
| `test_reports/coverage/`  | Coverage reports (Cobertura XML)     |

CI artifact block for quality gate jobs:

```yaml
  artifacts:
    when: always
    paths:
      - test_reports/<subdir>/
    expire_in: 7 days
```
