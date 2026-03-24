package io.github.hzahnlei.usecase.task;

import io.github.hzahnlei.domain.task.Task;
import io.github.hzahnlei.domain.task.TaskStatus;

import java.util.List;
import java.util.Map;

/**
 * Output port: persistence contract for the Task aggregate.
 * Implemented by TaskRepositoryAdapter in the infrastructure layer.
 * Use-case code must never import the adapter directly.
 */
public interface TaskRepository {

    /**
     * Persists the given new tasks and returns the created Task aggregates
     * with server-assigned id, createdAt, and updatedAt populated.
     */
    List<Task> insertAll(List<NewTask> newTasks);

    /**
     * Returns the count of persisted tasks for each TaskStatus value.
     * Statuses with zero tasks may be absent from the returned map.
     */
    Map<TaskStatus, Long> countByStatus();
}
