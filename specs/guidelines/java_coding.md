# Java Coding Guidelines

## Language Version

Java 25 (LTS). Adopt new language features actively.

## Modern Java Features

- **Records** for immutable data carriers — prefer over plain POJOs.
- **Sealed classes** for closed type hierarchies.
- **Pattern matching** (`instanceof`, `switch` expressions) — prefer over casting.
- **Text blocks** for multiline strings.
- **`Optional<T>`** as return type instead of returning `null`; never pass `Optional` as a parameter.
- **Stream API** for collection operations; prefer over manual loops.
- **Immutable collections:** `List.of()`, `Map.of()`, `Set.of()`.

## Naming

- `camelCase` — methods, variables, parameters.
- `PascalCase` — classes, interfaces, enums, records.
- `SCREAMING_SNAKE_CASE` — constants.
- Interface names: noun or adjective (`Repository`, `Serializable`), not `IRepository`.

## Type Aliases and Domain Modelling

- Use single-field records as type aliases for domain concepts: `record Amount(BigDecimal value) {}`.
- Prefer `BigDecimal` over `double` for monetary values.

## Dependency Injection

- Constructor injection only — never field injection (`@Autowired` on fields is forbidden).
- Prefer `final` fields for all injected dependencies.

## Error Handling

- Prefer unchecked exceptions for programming errors; checked exceptions for recoverable conditions callers must handle.
- Never declare `throws Exception` — declare specific exception types.
- Never swallow exceptions with empty `catch` blocks.

## Build

- Gradle (Kotlin DSL) preferred for new projects; Maven acceptable.
- Enforce code style with Checkstyle in CI; config at `checkstyle.xml` (copy from `templates/`).

## Frameworks

- **Services:** Spring Boot — preferred.
- **Configuration:** `application.yml`; override with environment variables via `spring-dotenv` or OS env.

## Testing

- JUnit 6 for unit tests (JUnit 5 still supported for migration).
- Mockito for mocking at architectural boundaries only.
- AssertJ for fluent assertions.
- Cucumber JVM + JUnit Platform for BDD integration tests.
- Structure test bodies with `// GIVEN`, `// WHEN`, `// THEN`.

## General Advice

- Avoid to much hidden magic, e.g. Java Annotation Processing.
  Better make things more explicit and visible.
  Never use Mapstruct.
- When using Spring (Boot)
  - Use constructor injection
  - `@autowire` prohibited
