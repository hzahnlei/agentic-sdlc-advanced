package io.github.hzahnlei.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.hzahnlei.infra.persistence.TaskJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskInsertSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskJpaRepository taskJpaRepository;

    @Given("a batch of {int} valid tasks:")
    public void aBatchOfValidTasks(int count, List<Map<String, String>> rows) {
        state.pendingBatch = rows;
    }

    @Given("a batch containing one task with a blank title:")
    public void aBatchWithBlankTitle(List<Map<String, String>> rows) {
        state.pendingBatch = rows;
    }

    @Given("a batch containing one task with an invalid status:")
    public void aBatchWithInvalidStatus(List<Map<String, String>> rows) {
        state.pendingBatch = rows;
    }

    @Given("the request carries Idempotency-Key {string}")
    public void requestCarriesIdempotencyKey(String key) {
        state.idempotencyKey = key;
    }

    @When("the AI Agent submits the batch to {string}")
    public void submitBatch(String endpoint) throws Exception {
        List<Map<String, Object>> body = buildBody(state.pendingBatch);
        state.lastResponse = post("/v1/mcp/tasks", body);
    }

    @When("the AI Agent submits an empty array to {string}")
    public void submitEmptyArray(String endpoint) throws Exception {
        state.lastResponse = post("/v1/mcp/tasks", List.of());
    }

    @When("the AI Agent submits the identical batch again with the same Idempotency-Key")
    public void submitBatchAgain() throws Exception {
        List<Map<String, Object>> body = buildBody(state.pendingBatch);
        state.lastResponse = post("/v1/mcp/tasks", body);
    }

    @Then("the response contains {int} task objects")
    public void responseContainsTaskObjects(int expected) throws Exception {
        List<?> tasks = objectMapper.readValue(state.lastResponse.getBody(), List.class);
        assertThat(tasks).hasSize(expected);
    }

    @Then("each returned task has a non-null {string}")
    public void eachTaskHasNonNull(String field) throws Exception {
        List<Map<String, Object>> tasks = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        for (Map<String, Object> task : tasks) {
            assertThat(task.get(field)).as("field '%s' should not be null", field).isNotNull();
        }
    }

    @Then("the task titles match the submitted titles")
    public void taskTitlesMatch() throws Exception {
        List<Map<String, Object>> tasks = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        List<String> returnedTitles = tasks.stream()
                .map(t -> (String) t.get("title"))
                .toList();
        List<String> submittedTitles = state.pendingBatch.stream()
                .map(r -> r.get("title"))
                .toList();
        assertThat(returnedTitles).containsExactlyInAnyOrderElementsOf(submittedTitles);
    }

    @Then("the error code is {string}")
    public void errorCodeIs(String expected) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        assertThat(error.get("code")).isEqualTo(expected);
    }

    @Then("the error details reference field {string}")
    public void errorDetailsReferenceField(String field) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> details = (List<Map<String, String>>) error.get("details");
        assertThat(details).isNotNull().isNotEmpty();
        List<String> fields = details.stream().map(d -> d.get("field")).toList();
        assertThat(fields).contains(field);
    }

    @Then("no tasks are persisted in the database")
    public void noTasksPersistedInDatabase() {
        assertThat(taskJpaRepository.count()).isZero();
    }

    @Then("the database contains exactly {int} task records")
    public void databaseContainsExactly(int expected) {
        assertThat(taskJpaRepository.count()).isEqualTo(expected);
    }

    private org.springframework.http.ResponseEntity<String> post(String url, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (state.idempotencyKey != null) {
            headers.set("Idempotency-Key", state.idempotencyKey);
        }
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        return restTemplate.postForEntity(url, entity, String.class);
    }

    private List<Map<String, Object>> buildBody(List<Map<String, String>> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, String> row : rows) {
            Map<String, Object> task = new java.util.LinkedHashMap<>();
            task.put("title", row.get("title"));
            if (row.containsKey("description")) {
                task.put("description", row.get("description"));
            }
            task.put("status", row.get("status"));
            if (row.containsKey("dueDate")) {
                task.put("dueDate", row.get("dueDate"));
            }
            result.add(task);
        }
        return result;
    }
}
