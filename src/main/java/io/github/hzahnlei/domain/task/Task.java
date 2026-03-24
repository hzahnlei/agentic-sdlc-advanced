package io.github.hzahnlei.domain.task;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Aggregate root for the Task bounded context.
 * All fields except id, createdAt, updatedAt are caller-supplied.
 * Server-assigned fields are set by the persistence layer.
 */
public record Task(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt) {
}
