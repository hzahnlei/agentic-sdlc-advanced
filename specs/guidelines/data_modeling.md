# Data Modeling Guidelines

## Logical Model

- Align the model with domain language (DDD): aggregates, entities, value objects.
- An **aggregate** is a consistency boundary — enforce invariants within it, never across aggregates.
- **Value objects** are immutable and identified by their value, not an ID.
- **Entities** have identity that persists across state changes.
- Model behavior, not just data: place domain logic inside aggregates, not in services.
- Aggregates are **pure domain objects** — no I/O, no persistence, no external calls. Repositories (infra layer) handle load/save.
- Avoid anemic models (plain data bags with no behavior).
- Name types, fields, and relations using the ubiquitous language of the domain — no technical abbreviations.

## Physical Model

### Relational (SQL)

- Every table has a surrogate primary key (`id`); use UUIDs for distributed systems, auto-increment otherwise.
- Name tables as singular nouns (`order`, `customer`), columns as `snake_case`.
- Add a `created_at` / `updated_at` timestamp to every mutable table.
- Normalize to 3NF by default; denormalize only when read performance requires it and you can justify it.
- Declare foreign keys and let the DB enforce referential integrity.
- Index every foreign key and every column used in `WHERE`, `ORDER BY`, or `JOIN` predicates.
- Avoid `NULL` where possible — `NULL` means "unknown", not "empty" or "zero".
- Never store multiple values in a single column (no comma-separated lists).
- Migrations are forward-only; never modify existing migration files.

### Document Stores

- Embed data that is always read together and only written by one aggregate.
- Reference (by ID) data that is shared across aggregates or grows unbounded.
- Design documents around read patterns, not write patterns.
- Keep documents small — avoid unbounded arrays inside a document.
- Include a `schema_version` field for forward-compatible migrations.

### Key/Value Stores

- Design keys to be self-describing and hierarchical: `<namespace>:<entity>:<id>` (e.g., `session:user:42`).
- Set a TTL for all ephemeral data (sessions, caches, rate-limit counters).
- Never store relational data in a key/value store; use it for caching, sessions, and simple counters.
- Document the key schema in the codebase alongside the access code.
