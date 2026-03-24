# ADR-005: Expose Task DB Schema via Programmatic JSON Schema Builder

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-03-24    |
| **Status**        | Proposed      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

The `mcp-schema-tasks` tool must return the Task table schema as a JSON Schema document. The
AI agent uses this schema to understand which fields are required when calling `mcp-tasks` to
insert task data.

The implementation must decide how the JSON Schema is constructed and returned. Options range
from a static hard-coded string to a schema dynamically derived from JPA metadata. The choice
has correctness, maintainability, and architectural implications: the schema must stay in sync
with the actual `task` table (ADR-004), must be AI-readable, and must not couple domain or
use-case layers to persistence concerns.

## Decision Drivers

- The schema returned to the AI must be accurate. When the Task domain model changes, the
  `mcp-schema-tasks` tool must reflect that change.
- Clean architecture (general_coding.md): the tool handler (`infra`) may call a use-case
  service, which may consult domain types — but domain types must not know about JSON Schema.
- The `java_coding.md` guideline: "Avoid hidden magic / annotation processing." Schema
  generation via reflection is opaque and may include persistence-layer noise.
- No additional library dependency should be introduced if Jackson (already present via
  Spring Boot) is sufficient.
- A second source of truth for the schema (e.g., a hard-coded string alongside the domain
  class) is a maintenance risk; divergence is silent until the AI agent submits invalid data.

## Considered Options

1. **Hard-coded JSON string** — a `String` literal in the tool handler contains the JSON
   Schema. Fast to implement; creates a detached, silently stale second source of truth.
2. **JPA metadata introspection** — use Hibernate's `Metamodel` or `SchemaExport` at runtime
   to derive column names and types, then map to JSON Schema types.
3. **Reflection over the JPA entity class** — inspect annotated fields of `TaskJpaEntity` to
   build the JSON Schema programmatically.
4. **Programmatic JSON Schema builder in the use-case layer** — a `TaskSchemaProvider`
   use-case service builds an immutable `Map<String, Object>` representing the JSON Schema
   from explicit field definitions that mirror the `TaskDomain` record. Jackson serializes
   the result. The tool handler calls this service.
5. **Jackson `JsonSchemaGenerator` / `jsonschema-generator` library** — generate JSON Schema
   from a Jackson-annotated DTO class automatically.

## Decision Outcome

**Chosen option: Option 4 — Programmatic JSON Schema builder in `TaskSchemaProvider`**,
because it is the single explicit source of truth for the Task insertion schema, carries no
JPA coupling, uses no reflection, requires no extra library, and is co-located with the domain
model in the use-case layer where it is easiest to keep in sync.

### Structure

```
usecase/task/
  TaskSchemaProvider.java   ← builds and returns Map<String,Object> JSON Schema

infra/mcp/
  McpToolsConfiguration.java ← calls TaskSchemaProvider, serializes result to String
```

The JSON Schema describes the shape of one Task element in the `mcp-tasks` insertion array:

```json
{
  "type": "object",
  "required": ["title", "status"],
  "properties": {
    "title":       { "type": "string" },
    "description": { "type": "string" },
    "status":      { "type": "string", "enum": ["TODO", "IN_PROGRESS", "DONE"] }
  }
}
```

### Positive Consequences

- Single, explicit, human-readable definition of the Task schema in one class.
- No coupling to JPA, Hibernate, or reflection.
- Changes to the schema are localized and obvious in code review.
- No extra library dependency beyond Jackson.
- The AI agent receives a clean, minimal JSON Schema — no framework-generated noise.

### Negative Consequences / Risks

- `TaskSchemaProvider` must be updated manually when `TaskDomain` fields change; there is no
  compile-time enforcement of the sync. Mitigation: a unit test asserts that every field
  declared in `TaskSchemaProvider` has a corresponding field in `TaskDomain`, and vice versa.

## Pros and Cons of the Options

### Option 1 — Hard-coded JSON string

- **Pro**: Simplest possible implementation.
- **Con**: Completely decoupled from the domain model; silent staleness on domain changes.

### Option 2 — JPA metadata introspection

- **Pro**: Automatically reflects DB column changes.
- **Con**: JPA metadata includes persistence concerns irrelevant to the AI's JSON Schema.
- **Con**: Couples the tool response to Hibernate internals.

### Option 3 — Reflection over JPA entity

- **Pro**: Automatically includes new fields.
- **Con**: JPA annotations are not JSON Schema annotations; mapping is non-trivial and opaque.
- **Con**: `TaskJpaEntity` lives in `infra`; reflecting on it from a use-case service inverts
  the dependency direction.

### Option 4 — Programmatic builder in use-case layer

- **Pro**: Explicit, visible, layer-correct, no extra dependencies.
- **Con**: Manual sync required when domain changes (mitigated by unit test).

### Option 5 — Jackson `JsonSchemaGenerator` / library

- **Pro**: Automatic schema generation from an annotated DTO.
- **Con**: Adds a library dependency; generated schemas often include framework noise that may
  confuse the AI model.
- **Con**: Still requires a dedicated DTO annotated for schema generation — similar boilerplate
  to option 4, but less transparent.

## Notes

The JSON Schema returned by `mcp-schema-tasks` describes the input shape for `mcp-tasks` —
the insertion tool. Required fields: `title` (string), `status` (string enum). Optional:
`description` (string). The `id`, `created_at`, and `updated_at` fields are server-generated
and must not appear as required inputs in the schema.
