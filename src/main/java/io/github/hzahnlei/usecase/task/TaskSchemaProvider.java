package io.github.hzahnlei.usecase.task;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Builds and returns the JSON Schema document that describes the TaskInput shape.
 * Single source of truth for the insertion schema (ADR-005).
 */
@Service
public class TaskSchemaProvider {

    public Map<String, Object> getSchema() {
        return Map.of(
                "type", "object",
                "required", List.of("title", "status"),
                "properties", Map.of(
                        "title", Map.of(
                                "type", "string",
                                "description", "Short summary of the task",
                                "maxLength", 255),
                        "description", Map.of(
                                "type", "string",
                                "description", "Optional detailed description of the task"),
                        "status", Map.of(
                                "type", "string",
                                "enum", List.of("TODO", "IN_PROGRESS", "DONE"),
                                "description", "Current lifecycle status of the task"),
                        "dueDate", Map.of(
                                "type", "string",
                                "format", "date",
                                "description", "Optional deadline (ISO 8601 date, e.g. 2026-12-31)")));
    }
}
