package io.github.hzahnlei.usecase.task;

import io.github.hzahnlei.domain.task.TaskStatus;

import java.util.Map;

/**
 * Read model returned by GetTasksSummaryUseCase.
 * Holds the count of Task records per TaskStatus and the overall total.
 * Read-only; never persisted.
 */
public record TaskSummary(Map<TaskStatus, Long> counts, long total) {
}
