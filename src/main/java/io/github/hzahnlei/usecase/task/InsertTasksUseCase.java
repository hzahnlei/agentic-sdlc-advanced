package io.github.hzahnlei.usecase.task;

import io.github.hzahnlei.domain.task.InvalidTaskException;
import io.github.hzahnlei.domain.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Validates and atomically persists a batch of new Tasks.
 * All records in a batch are inserted in a single transaction;
 * a validation failure in any item rejects the entire batch.
 */
@Service
public class InsertTasksUseCase {

    private final TaskRepository taskRepository;

    public InsertTasksUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public List<Task> execute(List<NewTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new InvalidTaskException(
                    "INVALID_INPUT",
                    "At least one task is required",
                    List.of());
        }

        for (int i = 0; i < tasks.size(); i++) {
            String title = tasks.get(i).title();
            if (title == null || title.isBlank()) {
                throw new InvalidTaskException(
                        "INVALID_INPUT",
                        "title must not be blank",
                        List.of(new InvalidTaskException.FieldError(
                                "[" + i + "].title", "must not be blank")));
            }
        }

        return taskRepository.insertAll(tasks);
    }
}
