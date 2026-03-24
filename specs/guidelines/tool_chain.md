# Tool Chain Guidelines

Applies to all projects, independent of programming language or artifact type.

## Local Development

- The project tool chain is managed by [Devbox](https://www.jetify.com/devbox) (`devbox.json` checked into the repository).
- Run `devbox shell` to enter an isolated shell with all tools available at the pinned versions.
- Alternatively, use the CI build image locally via `docker run` to achieve full parity with CI builds.

## CI Build Images

Reproducible CI builds require a self-contained build environment. The project uses pre-built, language-specific Docker images hosted in the GitLab Container Registry at `registry.gitlab.com/hzahnlei/build-images`.

The Dockerfiles and CI pipeline for these images are maintained in the dedicated
[`build-images`](https://gitlab.com/hzahnlei/build-images) repository. This project does not embed build image Dockerfiles.

### Core Invariants

- **No installation during CI runs.** Commands such as `apt install`, `go install`, `pip install`, `npm install -g`, or equivalent are forbidden inside pipeline jobs. All tools must already be present in the build image.
- **Self-contained and hermetic.** Builds run without outbound internet access. All dependencies are either in the image or in a vendored/cached artifact layer.
- **Pinned versions.** Every tool is version-pinned in the image's `Dockerfile`. Never use `latest` tags in pipeline job definitions; pin by image digest in critical stages.

### Images

| Image | Base | Contents |
| --- | --- | --- |
| `build-cpp:alpine-<ver>` | Alpine (musl libc) | GCC, Clang, CMake, Ninja, Conan, clang-tidy, clang-format, ctest |
| `build-cpp:bookworm-<ver>` | Debian bookworm-slim (glibc) | same as Alpine variant plus Valgrind |
| `build-go:<ver>` | Debian bookworm-slim | Go toolchain, golangci-lint, govulncheck |
| `build-java:<ver>` | Debian bookworm-slim | JDK, Maven, Checkstyle, SpotBugs |

### C++ — Dual-Platform Build

C++ code must be compiled and tested on **both** musl and glibc to catch ABI and portability issues early:

- `build-cpp:alpine-<ver>` (musl libc) — detects musl-specific incompatibilities
- `build-cpp:bookworm-<ver>` (glibc) — matches most production runtime environments

Final runtime and service images are typically distroless or `debian:bookworm-slim` (glibc-based).
See the [Containerization Guidelines](containerization.md) for runtime image guidance.

### Image Versioning

- Tag images with a date-based version (`YYYY.MM.DD`) or a semantic version (`v<major>.<minor>`).
- Rebuild and republish images whenever tool versions change; automate rebuilds via a scheduled or trigger-based pipeline in the `build-images` repository.

## Versions

- Use the most recent stable versions of tools and libraries when creating a new project.
- Update tool and library versions when revisiting an existing project.
- Track versions explicitly in `devbox.json` and the build image `Dockerfile`s — never rely on implicit `latest` resolution.
