# Testing Guidelines

## Principles

- Test cases are independent — no shared mutable state between tests.
- Test public interfaces, not internal implementation.
- Unit-test the happy path and all boundary cases.
- Tests are deterministic — no `sleep`, no wall-clock time, no unfixed random seeds.
  - Inject clocks, UUID generators etc. that are controllable by the test driver
- No network or file-system access in unit tests.

## Test Pyramid ([Martin Fowler](https://martinfowler.com/bliki/TestPyramid.html))

- **Unit tests** — fast, isolated, the majority. Mock only at architectural boundaries.
- **Integration tests** — test component interactions at boundaries; always use BDD; may be written in a different language (e.g., Python/Behave).
- **End-to-end tests** — minimal; critical user journeys only.

## BDD

- Use cases are covered by BDD integration tests.
- Structure: `Given` / `When` / `Then`.
- Preferred frameworks: Godog (Go), Behave (Python), Catch2 (C++), Cucumber + JUnit 5 (Java).

## Mocking

- Mock only at layer boundaries (e.g., `infra` interfaces defined in `usecase`).
- Never mock domain or use-case logic — test it directly.

## Test Data

- Use builders or factories; avoid large inline literals.
- Never depend on test execution order.
