# ADR-006: Enforce Clean Architecture with Four-Layer Packages and ArchUnit

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-03-24    |
| **Status**        | Accepted      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

The `general_coding.md` guideline mandates clean architecture with a strict inward dependency
rule: `app → infra → usecase → domain`. It further states: "Enforce in the pipeline
(architecture check): ArchUnit (Java)."

The project currently has a single class in the root package
(`io.github.hzahnlei.McpApplication`). A package structure must be established before
implementation begins so that every new class is placed in the correct layer from the start,
and the dependency rule is verifiable on every `mvn test` run.

## Decision Drivers

- Clean architecture is a project-wide non-negotiable per the guidelines.
- The MCP tool handlers (`infra`) must delegate to use-case services; this boundary must be
  structurally enforced, not merely conventional.
- Spring Boot's component scanning works from the annotated main class's package downward;
  the structure must remain compatible.
- ArchUnit tests must run as plain JUnit 5 tests with no additional CI tooling.
- The base package is `io.github.hzahnlei`, established by `McpApplication.java`.

## Considered Options

### Package mapping

1. **Layer-first top-level packages** —
   `io.github.hzahnlei.domain`, `.usecase`, `.infra`, `.app`.
   Maps directly to the architecture diagram; all domain classes are in `.domain` regardless
   of feature.
2. **Feature-first packages with layer sub-packages** —
   `io.github.hzahnlei.task.domain`, `io.github.hzahnlei.task.usecase`, etc.
   Groups all code for one feature together; scales for multi-bounded-context projects.
3. **Flat single package** — all classes in `io.github.hzahnlei`. No structure enforced.

### Enforcement mechanism

A. **ArchUnit `LayeredArchitecture` DSL** — declares layers by package pattern and asserts
the allowed access directions in a single JUnit 5 test class.
B. **Granular ArchUnit slice rules** — individual `noClasses().that()...should()` assertions
per dependency direction.
C. **No enforcement** — rely on code review only.

## Decision Outcome

**Chosen: Option 1 (layer-first packages) + Option A (ArchUnit `LayeredArchitecture`).**

- **Layer-first packages**: this project has a single bounded context (`task`). Feature-first
  packaging is appropriate when multiple bounded contexts coexist; here it adds a superfluous
  nesting level. Layer-first makes the layer of any class visible from its package name alone.
- **ArchUnit `LayeredArchitecture`**: concise DSL, clear violation messages naming the
  offending class and violated rule, runs in the standard test phase with no external tooling.

### Package structure

```
io.github.hzahnlei/
├── McpApplication.java                        (Spring Boot entry point — root package)
├── app/                                       (Spring wiring, top-level @Configuration beans)
├── domain/
│   └── task/
│       ├── Task.java                          (immutable domain record)
│       └── TaskStatus.java                    (enum: TODO, IN_PROGRESS, DONE)
├── usecase/
│   └── task/
│       ├── TaskRepository.java                (output port interface)
│       ├── InsertTasksUseCase.java
│       ├── GetTasksSummaryUseCase.java
│       └── TaskSchemaProvider.java            (see ADR-005)
└── infra/
    ├── persistence/
    │   ├── TaskJpaEntity.java
    │   ├── TaskJpaRepository.java             (Spring Data interface)
    │   └── TaskRepositoryAdapter.java         (implements usecase TaskRepository)
    └── mcp/
        └── McpToolsConfiguration.java         (registers all four MCP tools)
```

### ArchUnit test (`src/test/java/.../ArchitectureTest.java`)

```java
layeredArchitecture()
    .consideringOnlyDependenciesInLayers()
    .layer("Domain").definedBy("..domain..")
    .layer("UseCase").definedBy("..usecase..")
    .layer("Infra").definedBy("..infra..")
    .layer("App").definedBy("..app..")
    .whereLayer("Domain").mayNotAccessAnyLayer()
    .whereLayer("UseCase").mayOnlyAccessLayers("Domain")
    .whereLayer("Infra").mayOnlyAccessLayers("UseCase", "Domain")
    .whereLayer("App").mayOnlyAccessLayers("Infra", "UseCase", "Domain")
    .check(importedClasses);
```

A second rule asserts that domain and use-case classes do not import Spring framework
packages (`org.springframework..`), keeping those layers free of framework coupling.

### Positive Consequences

- Any accidental layer violation is caught immediately on `mvn test`.
- The package name is self-documenting: the layer of any class is visible without reading it.
- `TaskRepository` interface is in `usecase`; `TaskRepositoryAdapter` is in `infra` — the
  dependency inversion is structurally enforced.
- New contributors have a clear, enforced map of where each type belongs.

### Negative Consequences / Risks

- ArchUnit adds a test dependency and a short classpath-scan overhead per test run
  (negligible).
- `McpApplication.java` lives in the root package, not in `app/`. Spring Boot requires the
  main class to be at or above all component-scanned packages; keeping it in the root is
  idiomatic and does not violate the architecture rule (it is the entry point, not a layer
  component). The `consideringOnlyDependenciesInLayers()` flag scopes ArchUnit checks to
  classes inside the declared layers.
- Third-party library classes (Spring AI auto-configuration) that cross layer boundaries are
  excluded from the check by `consideringOnlyDependenciesInLayers()`.

## Pros and Cons of the Options

### Option 1 — Layer-first packages

- **Pro**: Maps directly to architecture documentation; single bounded context fits naturally.
- **Con**: All domain types share one `domain` package; sub-packages needed as project grows.

### Option 2 — Feature-first packages

- **Pro**: All code for one feature co-located.
- **Con**: Adds a superfluous level for a single-bounded-context project; layer boundary is
  less visible from the package name.

### Option 3 — Flat single package

- **Pro**: No structure to learn.
- **Con**: No enforceability; architecture degrades silently over time.

### Option A — ArchUnit `LayeredArchitecture`

- **Pro**: Concise; clear violation messages; standard test phase.
- **Con**: Adds `archunit-junit5` test dependency.

### Option B — Granular ArchUnit slice rules

- **Pro**: Fine-grained control per dependency direction.
- **Con**: More verbose; `LayeredArchitecture` already covers the same ground more concisely.

### Option C — No enforcement

- **Pro**: Zero overhead.
- **Con**: Layer violations accumulate as silent tech debt; code review is insufficient for
  systematic enforcement.

## Notes

ArchUnit dependency to add to `pom.xml` (test scope):

```xml
<dependency>
  <groupId>com.tngtech.archunit</groupId>
  <artifactId>archunit-junit5</artifactId>
  <version>1.3.0</version>
  <scope>test</scope>
</dependency>
```

Spring annotations (`@Service`, `@Component`, `@Repository`, `@Configuration`) are permitted
only in `infra.*` and `app.*` packages. The second ArchUnit rule should assert:

```java
noClasses()
    .that().resideInAPackage("..domain..")
    .or().resideInAPackage("..usecase..")
    .should().dependOnClassesThat()
    .resideInAPackage("org.springframework..")
    .check(importedClasses);
```
