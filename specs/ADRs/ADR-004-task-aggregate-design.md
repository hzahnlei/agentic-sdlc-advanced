# ADR-004: Task as a Single-Aggregate Root with `enum` Status and `Long` PK

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-03-24    |
| **Status**        | Accepted      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

The domain contains a Task entity that an AI agent creates, stores, and queries. The data
model must be defined before implementing persistence (JPA) and the `mcp-tasks` insertion
tool. Four sub-decisions interact and must be resolved together:

1. **Aggregate boundary** — does Task own child entities, or is it a standalone root?
2. **Status representation** — `String`, `enum`, or sealed class hierarchy?
3. **Primary key type** — `Long` (auto-increment) or `UUID`?
4. **Status transition enforcement** — guarded server-side, or freely settable on creation?

The `data_modeling.md` guideline mandates DDD aggregates, surrogate PKs, singular table
names, `snake_case` columns, `created_at`/`updated_at` timestamps, and 3NF. The
`java_coding.md` guideline mandates records for immutable data carriers and sealed classes
for closed type hierarchies with per-variant behaviour.

## Decision Drivers

- The functional requirements describe no child entities (no sub-tasks, tags, or comments);
  YAGNI applies.
- `mcp-tasks-summary` returns task counts grouped by status — the domain must be queryable
  by status without string parsing.
- The AI agent is the sole creator of tasks and supplies the initial status on creation;
  there is no server-side workflow engine in scope.
- This is a single-node PostgreSQL service; no distributed ID generation is required.
- Guideline: avoid NULL where possible; `title` and `status` are mandatory.

## Considered Options

### Aggregate boundary

1. **Standalone aggregate root** — one table `task`, no child entities.
2. **Task with child entities** — e.g., `Tag` or `Comment` owned by the Task aggregate.

### Status representation

A. **`String` field** — flexible; no compile-time enforcement.
B. **`enum TaskStatus`** — closed set, compile-time safe, directly mappable to `VARCHAR` with
`@Enumerated(EnumType.STRING)`, usable in JPQL `GROUP BY status`.
C. **Sealed class hierarchy** — carries per-status behaviour; appropriate only when each
status variant requires distinct methods.

### Primary key type

I. **`Long` auto-increment** — simple, compact, single-node appropriate.
II. **`UUID`** — globally unique; necessary for distributed or replicated systems.

### Status transition enforcement

X. **Guarded transitions in the `Task` domain class** — a `transition(TaskStatus next)`
method rejects illegal state changes.
Y. **Freely settable on creation** — the AI agent supplies an initial status; the server
validates it is a known enum value, but enforces no transition table.

## Decision Outcome

**Chosen: Option 1 + Option B + Option I + Option Y.**

- **Standalone aggregate**: no child entity requirements exist; adding them pre-emptively
  violates YAGNI and complicates persistence without benefit.
- **`enum TaskStatus { TODO, IN_PROGRESS, DONE }`**: an enum is the idiomatic Java choice for
  a small, closed set of values with no per-variant behaviour. It is directly supported by JPA
  and enables a clean `GROUP BY status` in the summary query. A sealed class hierarchy adds
  indirection without benefit here.
- **`Long` auto-increment PK**: single-node deployment with no distributed ID requirement.
  `Long` keeps the schema compact; sequential IDs are acceptable because the MCP interface is
  not a public REST API (resource enumeration is not a concern in this context).
- **Freely settable status**: the AI agent creates tasks with an explicit status supplied at
  insertion time. No server-side workflow or transition table is in scope. Invalid status
  strings are rejected at the MCP boundary by JSON Schema validation (see ADR-005) before
  reaching the domain layer.

### Physical schema

Table name `task` (singular, per `data_modeling.md`):

| Column        | Type           | Constraints   |
| ------------- | -------------- | ------------- |
| `id`          | `BIGSERIAL`    | `PRIMARY KEY` |
| `title`       | `VARCHAR(255)` | `NOT NULL`    |
| `description` | `TEXT`         | nullable      |
| `status`      | `VARCHAR(50)`  | `NOT NULL`    |
| `created_at`  | `TIMESTAMPTZ`  | `NOT NULL`    |
| `updated_at`  | `TIMESTAMPTZ`  | `NOT NULL`    |

### Positive Consequences

- One table, one JPA entity, one enum — minimal complexity.
- `TaskStatus` enum values are directly usable in a Spring Data JPQL
  `SELECT t.status, COUNT(t) FROM TaskJpaEntity t GROUP BY t.status` query.
- The `Long` PK eliminates UUID generation overhead and keeps foreign keys compact.
- New status values require only one enum constant and one DB migration.

### Negative Consequences / Risks

- `Long` PKs are sequential and guessable; if tasks are exposed via a public REST API in the
  future, resource enumeration becomes a concern. Mitigation: use UUID PKs in that scenario
  (this ADR should be superseded).
- No transition guards mean the AI could insert logically inconsistent status values (e.g., a
  task completed before it was started); this is acceptable for AI-generated test data.
- If sub-tasks or tags are added, the single-aggregate decision must be revisited and will
  require a schema migration.

## Pros and Cons of the Options

### Option 1 — Standalone aggregate

- **Pro**: Simplest schema and JPA mapping.
- **Con**: Cannot model hierarchical task relationships without redesign.

### Option 2 — Task with child entities

- **Pro**: Extensible to sub-tasks, tags, or comments.
- **Con**: Adds join complexity and aggregate boundary management before any requirement exists.

### Option A — String status

- **Pro**: No migration needed when adding statuses.
- **Con**: No compile-time safety; `GROUP BY` operates on opaque strings.

### Option B — `enum TaskStatus`

- **Pro**: Compile-time safe; exhaustive switch in summary logic; JPA-native.
- **Con**: Adding a new status requires a code change and a DB migration.

### Option C — Sealed class hierarchy

- **Pro**: Can carry per-status behaviour.
- **Con**: No per-status behaviour is needed; over-engineering for this domain.

### Option I — `Long` PK

- **Pro**: Simple, fast, compact.
- **Con**: Sequential and guessable; unsuitable if IDs appear in public-facing URLs.

### Option II — UUID PK

- **Pro**: Globally unique; safe to expose publicly.
- **Con**: Larger index footprint; unnecessary for single-node deployment.

### Option X — Guarded transitions

- **Pro**: Prevents logically invalid state sequences.
- **Con**: Adds domain complexity for a use case where the AI is the sole creator and no
  workflow engine exists.

### Option Y — Freely settable on creation

- **Pro**: Simple; correct for bulk AI-generated test data insertion.
- **Con**: No server-side guard against logically inconsistent status values.

## Notes

Schema migrations are managed by Flyway (forward-only, `resources/db/migration/`). The
`spring.jpa.hibernate.ddl-auto=update` setting in `application.properties` is acceptable for
early development but must be replaced with Flyway-managed migrations before any shared or
production deployment.
