package io.github.hzahnlei.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.github.hzahnlei.infra.persistence.TaskJpaRepository;
import io.github.hzahnlei.infra.persistence.TaskJpaEntity;
import io.github.hzahnlei.domain.task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class CommonSteps {

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Autowired
    private ScenarioState state;

    @Before
    public void resetIdempotencyAndState() {
        state.pendingBatch = null;
        state.idempotencyKey = null;
        state.lastResponse = null;
        state.lastToolResponse = null;
    }

    @Given("the MCP server is running")
    public void theMcpServerIsRunning() {
        // Spring Boot test context is started; nothing extra needed
    }

    @Given("the task database is empty")
    public void theTaskDatabaseIsEmpty() {
        taskJpaRepository.deleteAll();
    }

    @Given("the following tasks exist in the database:")
    public void theFollowingTasksExistInTheDatabase(List<Map<String, String>> rows) {
        taskJpaRepository.deleteAll();
        List<TaskJpaEntity> entities = rows.stream().map(row -> {
            TaskJpaEntity e = new TaskJpaEntity();
            e.setTitle(row.get("title"));
            e.setDescription(row.getOrDefault("description", null));
            e.setStatus(TaskStatus.valueOf(row.get("status")));
            return e;
        }).toList();
        taskJpaRepository.saveAll(entities);
    }
}
