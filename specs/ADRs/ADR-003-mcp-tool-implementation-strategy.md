# ADR-003: Use Programmatic `ToolRegistration` API for MCP Tools

| Field             | Value         |
| ----------------- | ------------- |
| **Date**          | 2026-03-24    |
| **Status**        | Proposed      |
| **Deciders**      | Project owner |
| **Supersedes**    | —             |
| **Superseded by** | —             |

## Context and Problem Statement

The MCP server must expose four tools: `mcp-schema-tasks`, `mcp-tasks`, `mcp-tasks-summary`,
and `mcp-help`. An MCP tool registration requires: (1) a unique name and natural-language
description used by the AI model to decide when to call the tool; (2) a JSON Schema for the
input parameters; (3) a handler function that executes when the AI invokes the tool.

Spring AI 1.0.4 provides two mechanisms for registering MCP tools:

- **Declarative `@Tool` annotation** on Spring bean methods: the framework reflects on
  annotated methods, generates the input JSON Schema from Java parameter types, and registers
  the tool at startup.
- **Programmatic `ToolRegistration` / `FunctionCallback` API**: tools are constructed
  explicitly as `ToolRegistration` objects with a name, description, and JSON Schema, then
  registered in a `@Configuration` class.

## Decision Drivers

- The `java_coding.md` guideline states: "Avoid too much hidden magic, e.g. Java Annotation
  Processing. Better make things more explicit and visible."
- Tool descriptions shown to the AI model are critical for correct tool selection; they must
  be precise, developer-controlled literals — not inferred from Java method or parameter names.
- Input validation at the MCP boundary (security.md: validate at system boundaries) must be
  explicit and auditable.
- Constructor injection is mandatory (no `@Autowired` field injection); both approaches
  support this.
- Clean architecture (general_coding.md): tool handlers sit in `infra`, delegate to
  `usecase` — the wiring must be visible, not implied by annotation scanning.

## Considered Options

1. **`@Tool` annotation on Spring bean methods** — Spring AI reflects on annotated methods,
   derives input schema from Java types, registers tools at startup.
2. **Programmatic `ToolRegistration` in a dedicated `@Configuration` class** — each tool is
   constructed with explicit name, description, and input JSON Schema; handlers are method
   references to use-case services.
3. **Raw JSON-RPC handler** — bypass Spring AI's tool abstraction entirely; implement the
   `tools/call` JSON-RPC method manually in a Spring MVC controller.

## Decision Outcome

**Chosen option: Option 2 — Programmatic `ToolRegistration` in `McpToolsConfiguration`**,
because it makes every tool's name, description, and input schema an explicit, readable data
structure in one configuration class with no reflection-derived surprises. Tool descriptions
are literal strings authored by the developer, ensuring the AI model receives accurate
guidance. This aligns with the "avoid hidden magic" guideline and keeps the tool wiring
separate from business logic.

### Positive Consequences

- Tool names, descriptions, and input schemas are co-located in `McpToolsConfiguration` —
  easy to review in a pull request.
- Changing a tool description does not require touching the business logic class.
- No annotation processing; behaviour is fully traceable via constructor wiring.
- Input schema can be authored to match exactly what the AI needs (required vs. optional
  fields) — not constrained by what reflection can derive from Java types.
- Input validation can be inserted explicitly before calling the use-case layer.

### Negative Consequences / Risks

- Slightly more boilerplate per tool (explicit `ToolRegistration` builder) compared to a
  single `@Tool` annotation.
- Input schema must be kept in sync with the actual use-case parameter types manually; there
  is no compile-time check. Mitigation: a unit test (see ADR-005) validates the schema
  against the domain type.

## Pros and Cons of the Options

### Option 1 — `@Tool` annotation

- **Pro**: Minimal boilerplate; schema auto-generated from Java types.
- **Con**: Schema generation is reflection-based; output is opaque until runtime.
- **Con**: Tool descriptions are annotation string literals scattered across business/infra
  classes rather than centralized.
- **Con**: Violates the "avoid hidden magic" guideline.

### Option 2 — Programmatic `ToolRegistration`

- **Pro**: Explicit, visible, centralized; full control over schema and description wording.
- **Con**: More verbose; input schema maintained by hand.

### Option 3 — Raw JSON-RPC handler

- **Pro**: Zero Spring AI dependency on the tool layer.
- **Con**: Requires reimplementing JSON-RPC dispatch, error serialization, and schema
  advertisement — large undifferentiated effort.
- **Con**: Incompatible with Spring AI's SSE-based transport (see ADR-002).

## Notes

The `mcp-tasks` tool accepts a JSON array of Task objects. Its input schema must explicitly
declare required fields (`title`, `status`) and their types. Hand-authoring this schema in
the `ToolRegistration` builder ensures it is exact and reviewed, rather than inferred from
JPA entity annotations that carry persistence concerns, not API shape.

All four tools are registered as `@Bean` instances in
`io.github.hzahnlei.infra.mcp.McpToolsConfiguration`. Each handler delegates immediately to
a use-case service injected via constructor — no business logic in the configuration class.
