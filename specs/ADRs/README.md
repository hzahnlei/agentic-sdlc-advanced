# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for this project.

ADRs follow the [MADR format](https://adr.github.io/madr/) (Markdown Architectural Decision Records).

## Index

| ID                                                                   | Title                                                                     | Status   |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------- | -------- |
| [ADR-001](ADR-001-structured_logging.md)                             | Use Structured Logging Instead of Plain Text Logging                      | Accepted |
| [ADR-002](ADR-002-mcp-transport-protocol.md)                         | Use HTTP+SSE Transport for MCP Rather Than Streamable HTTP                | Proposed |
| [ADR-003](ADR-003-mcp-tool-implementation-strategy.md)               | Use Programmatic `ToolRegistration` API for MCP Tools                     | Proposed |
| [ADR-004](ADR-004-task-aggregate-design.md)                          | Task as a Single-Aggregate Root with `enum` Status and `Long` PK          | Proposed |
| [ADR-005](ADR-005-json-schema-exposure-strategy.md)                  | Expose Task DB Schema via Programmatic JSON Schema Builder                | Proposed |
| [ADR-006](ADR-006-package-layer-architecture.md)                     | Enforce Clean Architecture with Four-Layer Packages and ArchUnit          | Proposed |

## Status values

`Proposed` → `Accepted` | `Rejected` | `Deprecated` → `Superseded by ADR-NNN`

## Creating a new ADR

1. Copy [`ADR-000-template.md`](ADR-000-template.md) to `ADR-NNN-short-title.md`.
2. Fill in all sections.
3. Set status to `Proposed` and open for discussion.
4. Update the index in this README.
