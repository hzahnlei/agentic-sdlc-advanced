# ADR-002: Use HTTP+SSE Transport for MCP Rather Than Streamable HTTP

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-03-24    |
| **Status**        | Accepted      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

The MCP specification version 2025-06-18 defines **Streamable HTTP** as its primary transport.
In Streamable HTTP the server sends JSON-RPC 2.0 responses either as plain HTTP responses (for
non-streaming tool calls) or as Server-Sent Events streams (for streaming/progress). A single
POST endpoint handles both cases.

`spring-ai-starter-mcp-server` 1.0.4 — already locked in `pom.xml` — implements the
**HTTP+SSE** transport shape from the earlier MCP draft (pre-2025-06-18): a dedicated SSE
endpoint for the event stream (`GET /sse`) and a separate messages endpoint (`POST /message`).
Spring AI 1.0.4 does **not** implement Streamable HTTP as defined in the 2025-06-18 spec.

The project must choose which transport shape to target, accepting the consequent spec-
conformance and dependency trade-offs.

## Decision Drivers

- `spring-ai-starter-mcp-server` 1.0.4 is the locked dependency; changing the Spring AI BOM
  version is out of scope for this phase.
- Claude Desktop, Claude.ai, and most current MCP clients connect using the HTTP+SSE shape
  that Spring AI 1.0.4 exposes; Streamable HTTP is not yet widely required by clients in
  practice (Q1 2026).
- The four MCP tools (`mcp-schema-tasks`, `mcp-tasks`, `mcp-tasks-summary`, `mcp-help`) are
  all request/response interactions — no streaming or progress-event feature of Streamable
  HTTP is needed.
- Manually implementing Streamable HTTP on top of plain Spring MVC would add significant
  undifferentiated work and diverge from the library's managed lifecycle.

## Considered Options

1. **HTTP+SSE via Spring AI 1.0.4 auto-configuration** — use the transport the library
   provides, acknowledging it aligns with the pre-2025-06-18 MCP draft.
2. **Manual Streamable HTTP implementation** — implement the 2025-06-18 transport directly
   in Spring MVC controllers, bypassing Spring AI's MCP server support.
3. **Upgrade Spring AI to a version that implements Streamable HTTP** — adopt a Spring AI
   release that explicitly targets MCP 2025-06-18 when one becomes available.
4. **STDIO transport** — suitable only for local process-based MCP (Claude Desktop launching
   the JVM locally), not for a network-accessible server.

## Decision Outcome

**Chosen option: Option 1 — HTTP+SSE via Spring AI 1.0.4 auto-configuration**, because it is
the only transport the locked dependency supports without forking framework code, all target AI
clients support it, and none of the four tools require streaming. This ADR records the
divergence from the 2025-06-18 transport spec as a conscious, bounded technical debt item.

### Positive Consequences

- Zero transport code to write or maintain — the starter wires `GET /sse` and `POST /message`
  automatically.
- Compatible with Claude Desktop and target AI agents without custom client configuration.
- Full access to Spring AI's tool registration and lifecycle management.

### Negative Consequences / Risks

- The server does not conform to the MCP 2025-06-18 transport layer; the `Mcp-Version` header
  reflects the earlier transport shape.
- Future MCP clients that drop HTTP+SSE in favour of Streamable HTTP exclusively will require
  a transport migration (tracked as tech debt).
- The SSE endpoint holds a long-lived HTTP connection per connected client; capacity planning
  must account for this connection profile.

## Pros and Cons of the Options

### Option 1 — HTTP+SSE via Spring AI 1.0.4

- **Pro**: Zero implementation effort; fully managed lifecycle.
- **Pro**: Immediately compatible with target AI clients.
- **Con**: Does not implement MCP 2025-06-18 Streamable HTTP transport.

### Option 2 — Manual Streamable HTTP

- **Pro**: Strict spec conformance.
- **Con**: Requires implementing JSON-RPC 2.0 routing, SSE framing, session management, and
  the initialization handshake by hand — substantial undifferentiated work.
- **Con**: Bypasses Spring AI's tool registration mechanism, requiring a second manual
  implementation for tool dispatch.

### Option 3 — Upgrade Spring AI

- **Pro**: Delivers spec conformance via the library.
- **Con**: No Spring AI release with first-class Streamable HTTP support was available at the
  time of this decision.
- **Con**: BOM upgrades can introduce breaking changes in unrelated Spring AI features.

### Option 4 — STDIO transport

- **Pro**: Simplest possible implementation for local Claude Desktop integration.
- **Con**: Not suitable for a network server; requires the AI client to launch the JVM process
  locally.

## Notes

When Spring AI releases a version with first-class Streamable HTTP support aligned with MCP
2025-06-18, this ADR should be revisited and superseded. The transition is purely a transport
layer change; the tool implementations (see ADR-003) are transport-agnostic.
