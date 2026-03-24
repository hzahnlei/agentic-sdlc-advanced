package io.github.hzahnlei.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskSummarySteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Autowired
    private ObjectMapper objectMapper;

    @When("the AI Agent requests the task summary at {string}")
    public void requestTaskSummary(String endpoint) {
        state.lastResponse = restTemplate.getForEntity("/v1/mcp/tasks/summary", String.class);
    }

    @Then("the summary count for {string} is {int}")
    public void summaryCountForIs(String status, int expected) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) body.get("counts");
        Object value = counts.get(status);
        assertThat(((Number) value).intValue()).isEqualTo(expected);
    }

    @Then("the summary total is {int}")
    public void summaryTotalIs(int expected) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        assertThat(((Number) body.get("total")).intValue()).isEqualTo(expected);
    }

    @Then("the summary total equals the sum of all per-status counts")
    public void summaryTotalEqualsSum() throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) body.get("counts");
        int sum = counts.values().stream()
                .mapToInt(v -> ((Number) v).intValue())
                .sum();
        int total = ((Number) body.get("total")).intValue();
        assertThat(total).isEqualTo(sum);
    }
}
