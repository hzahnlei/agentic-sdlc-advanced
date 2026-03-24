# Glossary

This glossary defines the **ubiquitous language** of this bounded context.

<!--
For the AI coding assistant:
- All class names, method names, field names, and variable names MUST use
  the exact terms from the "Code Name" column.
- Never invent synonyms. If "Task" is the term, do not use "Item", "Ticket",
  "Record", or "Entry" in code.
- The "Scope" column indicates where the term applies:
  D = Domain layer, A = Application/use-case layer, I = Infrastructure/API layer, ALL = everywhere.
-->

## Core Terms

| Term                | Code Name                                  | Definition                                                                                                                                                                                                                                                    | Scope |
| ------------------- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| Task                | `Task`                                     | The central aggregate root. Represents a unit of work with a lifecycle status. Mandatory fields: `title`, `status`. Optional: `description`. Server-assigned: `id`, `createdAt`, `updatedAt`. Not to be confused with `TaskInput` (insertion payload) or `TaskJpaEntity` (persistence mapping). | D     |
| Task Input          | `TaskInput`                                | Caller-supplied payload for creating a Task. Contains only `title`, `description` (optional), and `status`. Server-assigned fields (`id`, `createdAt`, `updatedAt`) are absent and must not be included by callers.                                           | I     |
| Task Summary        | `TaskSummary`                              | Aggregate read model returned by the summary tool. Holds the count of `Task` records for each `TaskStatus` value and the overall `total`. Read-only; never persisted.                                                                                         | A     |
| Task Repository     | `TaskRepository`                           | Output port interface (use-case layer) that defines the persistence contract for `Task` aggregates. Implemented by `TaskRepositoryAdapter` in the infrastructure layer. Never import the adapter from use-case or domain code.                                 | A     |
| Task Schema Provider| `TaskSchemaProvider`                       | Use-case service that builds and returns the JSON Schema document describing the `TaskInput` shape. The single source of truth for the insertion schema (see ADR-005).                                                                                         | A     |
| AI Agent            | — (actor; not a code class)                | An external AI system (e.g. Claude, GPT-4o) that connects to the MCP Server to inspect the schema, insert tasks in bulk, and validate results. The primary actor in all four use cases. Never model as a domain object.                                       | ALL   |
| MCP Server          | — (deployment unit; not a code class)      | The Spring Boot application that implements the Model Context Protocol server. Exposes four MCP Tools over HTTP+SSE transport. Distinct from the MCP protocol specification itself.                                                                            | I     |
| MCP Tool            | — (configuration concept; not a code class)| A named, discoverable capability of the MCP Server. Each tool has a unique name, a natural-language description (used by the AI Agent for tool selection), an input JSON Schema, and an HTTP endpoint. Registered programmatically in `McpToolsConfiguration`. | I     |

## Value Objects

Value objects have no identity — they are equal by value. List them here so the
AI generates them as immutable types, not mutable entities.

| Term             | Code Name                                              | Validation / Constraints                                                    |
| ---------------- | ------------------------------------------------------ | --------------------------------------------------------------------------- |
| Task Title       | `title` (String field on `Task` and `TaskInput`)       | Must not be blank. Maximum 255 characters. Stored as `VARCHAR(255) NOT NULL`. |
| Task Description | `description` (String field on `Task` and `TaskInput`) | Optional (nullable). No maximum length enforced at the domain layer. Stored as `TEXT`. |

## Enumerations

| Enum Name    | Values                          | Notes                                                                                                                                                           |
| ------------ | ------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `TaskStatus` | `TODO`, `IN_PROGRESS`, `DONE`   | Lifecycle state of a Task. Persisted as a VARCHAR string via `@Enumerated(EnumType.STRING)`. No server-side transition enforcement — tasks are created with an explicit status supplied by the AI Agent. All three values are valid as initial states. |
