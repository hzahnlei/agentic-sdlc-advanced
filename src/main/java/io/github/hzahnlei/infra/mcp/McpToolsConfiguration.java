package io.github.hzahnlei.infra.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hzahnlei.usecase.task.GetTasksSummaryUseCase;
import io.github.hzahnlei.usecase.task.InsertTasksUseCase;
import io.github.hzahnlei.usecase.task.NewTask;
import io.github.hzahnlei.usecase.task.TaskSchemaProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Registers the four MCP tools with Spring AI so that they are advertised
 * over the HTTP+SSE transport (GET /sse, POST /message).
 *
 * Also exposes getToolDescriptors() for the REST help endpoint delegate
 * (McpToolsApiDelegateImpl) to build its HelpInfo response.
 */
@Configuration
public class McpToolsConfiguration {

    /**
     * Lightweight descriptor used internally and by the REST help endpoint.
     * Not to be confused with the generated ToolDescription API model.
     */
    public record ToolDescriptor(String name, String description, String method, String path) {}

    private static final List<ToolDescriptor> DESCRIPTORS = List.of(
            new ToolDescriptor(
                    "mcp-schema-tasks",
                    "Returns the JSON Schema describing the shape of a Task input object. "
                    + "Call this first to understand what fields are required before inserting tasks.",
                    "GET", "/v1/mcp/schema/tasks"),
            new ToolDescriptor(
                    "mcp-tasks",
                    "Accepts a JSON array of Task objects and persists them to the database. "
                    + "Use the schema from mcp-schema-tasks to construct valid payloads.",
                    "POST", "/v1/mcp/tasks"),
            new ToolDescriptor(
                    "mcp-tasks-summary",
                    "Returns task counts grouped by status (TODO, IN_PROGRESS, DONE) and total. "
                    + "Use this to validate that bulk insertion produced the expected data distribution.",
                    "GET", "/v1/mcp/tasks/summary"),
            new ToolDescriptor(
                    "mcp-help",
                    "Returns this list of available MCP tools and their descriptions.",
                    "GET", "/v1/mcp/help"));

    private static final String TASKS_INPUT_SCHEMA = """
            {
              "type": "object",
              "required": ["tasks"],
              "properties": {
                "tasks": {
                  "type": "array",
                  "minItems": 1,
                  "items": {
                    "type": "object",
                    "required": ["title", "status"],
                    "properties": {
                      "title":       { "type": "string" },
                      "description": { "type": "string" },
                      "status":      { "type": "string", "enum": ["TODO", "IN_PROGRESS", "DONE"] },
                      "dueDate":     { "type": "string", "format": "date" }
                    }
                  }
                }
              }
            }
            """;

    public List<ToolDescriptor> getToolDescriptors() {
        return DESCRIPTORS;
    }

    @Bean
    public List<ToolCallback> mcpToolCallbacks(
            TaskSchemaProvider schemaProvider,
            InsertTasksUseCase insertTasksUseCase,
            GetTasksSummaryUseCase summaryUseCase,
            ObjectMapper objectMapper) {
        return DESCRIPTORS.stream()
                .map(td -> buildCallback(td, schemaProvider, insertTasksUseCase, summaryUseCase, objectMapper))
                .toList();
    }

    private ToolCallback buildCallback(
            ToolDescriptor td,
            TaskSchemaProvider schemaProvider,
            InsertTasksUseCase insertTasksUseCase,
            GetTasksSummaryUseCase summaryUseCase,
            ObjectMapper objectMapper) {

        String inputSchema = "mcp-tasks".equals(td.name())
                ? TASKS_INPUT_SCHEMA
                : "{\"type\":\"object\",\"properties\":{}}";

        ToolDefinition definition = ToolDefinition.builder()
                .name(td.name())
                .description(td.description())
                .inputSchema(inputSchema)
                .build();

        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                return definition;
            }

            @Override
            public String call(String json) {
                try {
                    return switch (td.name()) {
                        case "mcp-schema-tasks" ->
                                objectMapper.writeValueAsString(schemaProvider.getSchema());
                        case "mcp-tasks" ->
                                insertTasks(json, insertTasksUseCase, objectMapper);
                        case "mcp-tasks-summary" ->
                                objectMapper.writeValueAsString(summaryUseCase.execute());
                        case "mcp-help" ->
                                objectMapper.writeValueAsString(Map.of("tools", DESCRIPTORS));
                        default -> "{}";
                    };
                } catch (JsonProcessingException e) {
                    return "{\"error\":\"serialization_error\",\"message\":\""
                            + e.getOriginalMessage() + "\"}";
                } catch (RuntimeException e) {
                    try {
                        return objectMapper.writeValueAsString(
                                Map.of("error", e.getMessage() != null ? e.getMessage() : "internal_error"));
                    } catch (JsonProcessingException ex) {
                        return "{\"error\":\"internal_error\"}";
                    }
                }
            }
        };
    }

    private static String insertTasks(
            String json,
            InsertTasksUseCase insertTasksUseCase,
            ObjectMapper objectMapper) throws JsonProcessingException {

        Map<String, Object> input = objectMapper.readValue(json, new TypeReference<>() {});
        Object rawTasks = input.get("tasks");
        String tasksJson = objectMapper.writeValueAsString(rawTasks);
        List<NewTask> tasks = objectMapper.readValue(tasksJson, new TypeReference<>() {});
        return objectMapper.writeValueAsString(insertTasksUseCase.execute(tasks));
    }
}
