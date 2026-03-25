# Dockerfile Guidelines

## General Rule

Images are self-contained.
Containers do not load libraries, tools, or plug-ins etc. at runtime.
Of cause, containers may operate on files mountend into them.
For example, a container from an build image operates on source code mounted into the container.

## Multi-Stage Builds

Always use multi-stage builds — separate build and runtime concerns:

1. **Build stage:** full SDK image (language-specific builder or `debian:bookworm-slim` with tools installed).
2. **Runtime stage:** minimal or distroless image. Copy only the compiled artifact and required runtime files.

Preferred runtime base images:

| Language | Runtime image                        |
| -------- | ------------------------------------ |
| C++      | `gcr.io/distroless/cc-debian12`      |
| Go       | `gcr.io/distroless/static-debian12`  |
| Java     | `gcr.io/distroless/java25-debian13`  |

## Base Images

- Pin base images to a specific version tag — never use `latest`.
- Keep base images up to date; automate with Dependabot or Renovate.

## Layer Caching

- Copy dependency manifests first, install dependencies, then copy source — maximize cache reuse.
- Each `RUN` instruction should do one logical thing.

## Security

- Run as a non-root user: create a dedicated user in the build stage; apply it in the runtime stage with `USER`.
- No secrets in any image layer; use `--mount=type=secret` for build-time secrets, environment variables at runtime.
- Scan images for vulnerabilities in CI (e.g., Trivy).

## Labels

Apply standard OCI labels:

```
LABEL org.opencontainers.image.version="<version>"
LABEL org.opencontainers.image.revision="<git-sha>"
LABEL org.opencontainers.image.source="<repo-url>"
```

## Entrypoint

- Use `ENTRYPOINT` for the main binary; `CMD` for default arguments.
- Use exec form `["binary", "arg"]`, not shell form — avoids signal propagation issues.
- Entrypoint scripts (if needed) reside in `image/`.
