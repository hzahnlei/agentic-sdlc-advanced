package io.github.hzahnlei.usecase.task;

import io.github.hzahnlei.domain.task.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * Queries task counts grouped by TaskStatus and assembles a TaskSummary read model.
 * All three status values are always present in the result (zero-filled if absent).
 */
@Service
public class GetTasksSummaryUseCase {

    private final TaskRepository taskRepository;

    public GetTasksSummaryUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskSummary execute() {
        Map<TaskStatus, Long> raw = taskRepository.countByStatus();

        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, raw.getOrDefault(status, 0L));
        }

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new TaskSummary(counts, total);
    }
}
