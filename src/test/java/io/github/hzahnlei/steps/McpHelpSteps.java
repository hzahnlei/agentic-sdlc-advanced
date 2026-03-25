package io.github.hzahnlei.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class McpHelpSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Autowired
    private ObjectMapper objectMapper;

    @When("the AI Agent requests the tool directory at {string}")
    public void requestToolDirectory(String endpoint) {
        state.lastResponse = restTemplate.getForEntity("/v1/mcp/help", String.class);
    }

    @Then("the response contains a tool named {string}")
    public void responseContainsToolNamed(String name) throws Exception {
        List<Map<String, Object>> tools = getTools();
        List<String> names = tools.stream().map(t -> (String) t.get("name")).toList();
        assertThat(names).contains(name);
    }

    @Then("every tool entry has a non-blank {string}")
    public void everyToolEntryHasNonBlank(String field) throws Exception {
        List<Map<String, Object>> tools = getTools();
        for (Map<String, Object> tool : tools) {
            Object value = tool.get(field);
            assertThat(value).as("field '%s' in tool '%s'", field, tool.get("name")).isNotNull();
            assertThat(value.toString()).as("field '%s' in tool '%s'", field, tool.get("name")).isNotBlank();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTools() throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        return (List<Map<String, Object>>) body.get("tools");
    }
}
