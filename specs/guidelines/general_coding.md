# General Coding Guidelines

Applies to all projects, independent of programming language or artifact type.
OOP-specific guidance is in the [OO Design and Programming Guidelines](oo_design_and_programming.md).
Tool chain and build environment guidance is in the [Tool Chain Guidelines](tool_chain.md).

## Clean Code

- Clarity, readability, and maintainability over performance (unless performance is explicitly critical).
- Comments in English. Explain _what_ and _why_, never _how_ (if you need to explain how, rewrite the code).
- Idiomatic code following the community standards of each language/ecosystem.
- Define type aliases to give the programmer better hints and to improve type safety.
- Move complex conditionals to separate, testable function with self-explanatory name.

## Clean Architecture

- Dependencies point inward only: `app` → `infra` → `usecase` → `domain`. Never outward.
  - Main program - Handles command line arguments, environment variables, signals and exit codes.
    Instanciates and runs app
  - `app` - An abstraction for the application, instactiates and wires components
  - `domain` — technology-agnostic data structures and domain logic; immutable/functional, no side-effects
  - `usecase` — orchestrates domain objects; defines interfaces implemented by `infra`; spans transaction boundaries, side-effects via `infra`
    - Repository and other output-port interfaces are defined here, not in `domain/`. The domain layer has no knowledge of persistence.
  - `infra` — implements side effects: HTTP handlers, DB clients, message brokers, etc.
- Enforce in the pipeline (architecture check): ArchUnit (Java), GoArch (Go), Clang-Tidy (C++).

## Immutability/Functional Programming

- Prefer immutability over mutation
- Prefer functional style over procedural style

