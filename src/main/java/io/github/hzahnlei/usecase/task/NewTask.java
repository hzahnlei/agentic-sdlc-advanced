package io.github.hzahnlei.usecase.task;

import io.github.hzahnlei.domain.task.TaskStatus;

import java.time.LocalDate;

/**
 * Use-case-layer input for creating a single Task.
 * Contains only caller-supplied fields; server-assigned fields
 * (id, createdAt, updatedAt) are absent.
 */
public record NewTask(String title, String description, TaskStatus status, LocalDate dueDate) {
}
