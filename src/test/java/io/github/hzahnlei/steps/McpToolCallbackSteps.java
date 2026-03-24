package io.github.hzahnlei.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class McpToolCallbackSteps {

    @Autowired
    private List<ToolCallback> mcpToolCallbacks;

    @Autowired
    private ScenarioState state;

    @Autowired
    private ObjectMapper objectMapper;

    @When("the MCP tool {string} is invoked with {string}")
    public void toolInvokedWith(String name, String json) {
        state.lastToolResponse = findTool(name).call(json);
    }

    @When("the MCP tool {string} is invoked with:")
    public void toolInvokedWithDocstring(String name, String json) {
        state.lastToolResponse = findTool(name).call(json);
    }

    @Then("the tool response is valid JSON")
    public void toolResponseIsValidJson() throws Exception {
        objectMapper.readTree(state.lastToolResponse);
    }

    @Then("the tool response contains field {string} with value {string}")
    public void toolResponseContainsField(String field, String expected) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastToolResponse, new TypeReference<>() {});
        assertThat(body.get(field)).hasToString(expected);
    }

    @Then("the tool response is a JSON array of {int} element(s)")
    public void toolResponseIsArrayOf(int expectedSize) throws Exception {
        List<?> list = objectMapper.readValue(state.lastToolResponse, List.class);
        assertThat(list).hasSize(expectedSize);
    }

    @Then("each element in the tool response has a non-null {string}")
    public void eachElementHasNonNull(String field) throws Exception {
        List<Map<String, Object>> list = objectMapper.readValue(
                state.lastToolResponse, new TypeReference<>() {});
        for (Map<String, Object> element : list) {
            assertThat(element.get(field))
                    .as("field '%s' should not be null", field)
                    .isNotNull();
        }
    }

    @Then("the tool response summary count for {string} is {int}")
    public void toolResponseSummaryCountIs(String status, int expected) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastToolResponse, new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) body.get("counts");
        assertThat(((Number) counts.get(status)).intValue()).isEqualTo(expected);
    }

    @Then("the tool response lists tool names: {string}, {string}, {string}, {string}")
    public void toolResponseListsToolNames(String n1, String n2, String n3, String n4) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastToolResponse, new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tools = (List<Map<String, Object>>) body.get("tools");
        List<String> names = tools.stream().map(t -> (String) t.get("name")).toList();
        assertThat(names).contains(n1, n2, n3, n4);
    }

    private ToolCallback findTool(String name) {
        return mcpToolCallbacks.stream()
                .filter(tc -> name.equals(tc.getToolDefinition().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No MCP tool found: " + name));
    }
}
