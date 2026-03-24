# Library Guidelines

## Public API

- Minimize the public surface — expose only what consumers genuinely need.
- Never break a published public API without a major version bump ([semantic versioning](https://semver.org)).
- No global mutable state; all dependencies injected via constructor or factory.
- No side effects at import or load time.

## Documentation

- Every public symbol has a documentation comment (GoDoc, Doxygen, Javadoc).
- Provide runnable examples in the documentation.
- Document error conditions and what callers are expected to do with them.

## Versioning

- Follow semantic versioning: `MAJOR.MINOR.PATCH`.
- Deprecate before removing; mark deprecated symbols and provide migration guidance.
- Maintain a `CHANGELOG.md` generated from [Conventional Commits](https://www.conventionalcommits.org) (e.g., via `git-cliff`).

## Testing

- Achieve high unit-test coverage of the public API.
- Include integration tests that exercise the library as a consumer would.
- Test with multiple runtime versions if the library supports a version range.
