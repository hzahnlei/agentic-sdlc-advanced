package io.github.hzahnlei.infra.api;

import io.github.hzahnlei.infra.api.model.HelpInfo;
import io.github.hzahnlei.infra.api.model.TaskInput;
import io.github.hzahnlei.infra.api.model.TaskSummary;
import io.github.hzahnlei.infra.api.model.TaskSummaryCounts;
import io.github.hzahnlei.infra.api.model.ToolDescription;
import io.github.hzahnlei.infra.mcp.McpToolsConfiguration;
import io.github.hzahnlei.usecase.task.GetTasksSummaryUseCase;
import io.github.hzahnlei.usecase.task.InsertTasksUseCase;
import io.github.hzahnlei.usecase.task.NewTask;
import io.github.hzahnlei.usecase.task.TaskSchemaProvider;
import io.github.hzahnlei.domain.task.TaskStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class McpToolsApiDelegateImpl implements McpToolsApiDelegate {

    private final TaskSchemaProvider schemaProvider;
    private final InsertTasksUseCase insertTasksUseCase;
    private final GetTasksSummaryUseCase summaryUseCase;
    private final McpToolsConfiguration toolsConfiguration;
    private final IdempotencyService idempotencyService;

    public McpToolsApiDelegateImpl(
            TaskSchemaProvider schemaProvider,
            InsertTasksUseCase insertTasksUseCase,
            GetTasksSummaryUseCase summaryUseCase,
            McpToolsConfiguration toolsConfiguration,
            IdempotencyService idempotencyService) {
        this.schemaProvider = schemaProvider;
        this.insertTasksUseCase = insertTasksUseCase;
        this.summaryUseCase = summaryUseCase;
        this.toolsConfiguration = toolsConfiguration;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public ResponseEntity<Map<String, Object>> mcpSchemaTasks() {
        return ResponseEntity.ok(schemaProvider.getSchema());
    }

    @Override
    public ResponseEntity<List<io.github.hzahnlei.infra.api.model.Task>> mcpTasks(
            List<TaskInput> taskInput, UUID idempotencyKey) {

        if (idempotencyKey != null) {
            Optional<List<io.github.hzahnlei.infra.api.model.Task>> cached =
                    idempotencyService.get(idempotencyKey.toString());
            if (cached.isPresent()) {
                return ResponseEntity.status(201).body(cached.get());
            }
        }

        List<NewTask> newTasks = taskInput.stream()
                .map(input -> new NewTask(
                        input.getTitle(),
                        input.getDescription(),
                        TaskStatus.valueOf(input.getStatus().getValue()),
                        input.getDueDate()))
                .toList();

        List<io.github.hzahnlei.infra.api.model.Task> result =
                insertTasksUseCase.execute(newTasks).stream()
                        .map(this::toApiModel)
                        .toList();

        if (idempotencyKey != null) {
            idempotencyService.store(idempotencyKey.toString(), result);
        }

        return ResponseEntity.status(201).body(result);
    }

    @Override
    public ResponseEntity<TaskSummary> mcpTasksSummary() {
        io.github.hzahnlei.usecase.task.TaskSummary summary = summaryUseCase.execute();
        Map<TaskStatus, Long> counts = summary.counts();

        TaskSummaryCounts apiCounts = new TaskSummaryCounts()
                .TODO(counts.getOrDefault(TaskStatus.TODO, 0L).intValue())
                .IN_PROGRESS(counts.getOrDefault(TaskStatus.IN_PROGRESS, 0L).intValue())
                .DONE(counts.getOrDefault(TaskStatus.DONE, 0L).intValue());

        TaskSummary apiSummary = new TaskSummary()
                .counts(apiCounts)
                .total((int) summary.total());

        return ResponseEntity.ok(apiSummary);
    }

    @Override
    public ResponseEntity<HelpInfo> mcpHelp() {
        List<ToolDescription> tools = toolsConfiguration.getToolDescriptors().stream()
                .map(td -> new ToolDescription()
                        .name(td.name())
                        .description(td.description())
                        .method(ToolDescription.MethodEnum.fromValue(td.method()))
                        .path(td.path()))
                .toList();

        return ResponseEntity.ok(new HelpInfo().tools(tools));
    }

    private io.github.hzahnlei.infra.api.model.Task toApiModel(
            io.github.hzahnlei.domain.task.Task domainTask) {
        io.github.hzahnlei.infra.api.model.TaskStatus apiStatus =
                io.github.hzahnlei.infra.api.model.TaskStatus.fromValue(
                        domainTask.status().name());
        OffsetDateTime createdAt = domainTask.createdAt() != null
                ? OffsetDateTime.ofInstant(domainTask.createdAt(), ZoneOffset.UTC) : null;
        OffsetDateTime updatedAt = domainTask.updatedAt() != null
                ? OffsetDateTime.ofInstant(domainTask.updatedAt(), ZoneOffset.UTC) : null;
        return new io.github.hzahnlei.infra.api.model.Task(
                domainTask.id(),
                domainTask.title(),
                apiStatus,
                createdAt,
                updatedAt)
                .dueDate(domainTask.dueDate());
    }
}
