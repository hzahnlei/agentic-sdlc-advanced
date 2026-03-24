# UC005 Invoke MCP Tool Callbacks

| Field          | Value                                      |
| -------------- | ------------------------------------------ |
| **ID**         | UC005                                      |
| **Name**       | Invoke MCP Tool Callbacks                  |
| **Actor**      | MCP Client (AI Agent via Spring AI)        |
| **Layer**      | `infra/mcp` → `usecase`                   |
| **Feature**    | [UC005-invoke-mcp-tool-callbacks.feature](../features/UC005-invoke-mcp-tool-callbacks.feature) |

## Description

Spring AI's HTTP+SSE transport (ADR-002) receives a JSON-RPC `tools/call` message and dispatches
it to the matching `ToolCallback` bean registered in `McpToolsConfiguration`. Each callback
deserialises the input JSON, delegates to the appropriate use case, and returns a JSON string.

This use case covers the four registered tools:

| Tool name           | Delegates to                 |
| ------------------- | ---------------------------- |
| `mcp-schema-tasks`  | `TaskSchemaProvider`         |
| `mcp-tasks`         | `InsertTasksUseCase`         |
| `mcp-tasks-summary` | `GetTasksSummaryUseCase`     |
| `mcp-help`          | Static descriptor list       |

## Main Success Scenario

1. Spring AI dispatches a `tools/call` request with `name` and `arguments` (JSON object).
2. `McpToolsConfiguration` locates the `ToolCallback` whose `ToolDefinition.name()` matches.
3. The callback's `call(String json)` method is invoked with the serialised `arguments`.
4. The callback delegates to the corresponding use case and returns the JSON-serialised result.
5. Spring AI wraps the result in a JSON-RPC response and sends it to the client.

## Error Scenarios

| Condition                      | Outcome                              |
| ------------------------------ | ------------------------------------ |
| Serialisation failure          | `{"error":"Internal error"}` returned |
| `InsertTasksUseCase` rejects input | `InvalidTaskException` propagates; Spring AI returns a tool error |

## Notes

- The tool callbacks are transport-agnostic; they do not depend on HTTP.
- `mcp-tasks` input must be a JSON object with a `tasks` array (see input schema in `McpToolsConfiguration`).
- All tools are registered as a single `List<ToolCallback>` bean; Spring AI discovers them automatically.
