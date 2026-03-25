package io.github.hzahnlei.infra.persistence;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.hzahnlei.domain.task.Task;
import io.github.hzahnlei.domain.task.TaskStatus;
import io.github.hzahnlei.usecase.task.NewTask;
import io.github.hzahnlei.usecase.task.TaskRepository;

@Component
public class TaskRepositoryAdapter implements TaskRepository {

	private final TaskJpaRepository jpaRepository;

	public TaskRepositoryAdapter(TaskJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public List<Task> insertAll(List<NewTask> newTasks) {
		List<TaskJpaEntity> entities = newTasks.stream()
				.map(this::toEntity)
				.toList();
		return jpaRepository.saveAll(entities).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public Map<TaskStatus, Long> countByStatus() {
		return jpaRepository.countGroupByStatus().stream()
				.collect(Collectors.toMap(
						row -> (TaskStatus) row[0],
						row -> (Long) row[1]));
	}

	private TaskJpaEntity toEntity(NewTask task) {
		TaskJpaEntity entity = new TaskJpaEntity();
		entity.setTitle(task.title());
		entity.setDescription(task.description());
		entity.setStatus(task.status());
		entity.setDueDate(task.dueDate());
		return entity;
	}

	private Task toDomain(TaskJpaEntity entity) {
		return new Task(
				entity.getId(),
				entity.getTitle(),
				entity.getDescription(),
				entity.getStatus(),
				entity.getDueDate(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}
}
