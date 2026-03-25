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

public class TaskSchemaSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ScenarioState state;

    @Autowired
    private ObjectMapper objectMapper;

    @When("the AI Agent requests the Task schema at {string}")
    public void requestTaskSchema(String endpoint) {
        state.lastResponse = restTemplate.getForEntity("/v1/mcp/schema/tasks", String.class);
    }

    @Then("the response body is a valid JSON Schema object")
    public void responseIsValidJsonSchema() throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        assertThat(body).containsKey("type");
        assertThat(body).containsKey("properties");
    }

    @Then("the schema {string} array contains {string}")
    public void schemaArrayContains(String arrayName, String value) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        List<String> array = (List<String>) body.get(arrayName);
        assertThat(array).contains(value);
    }

    @Then("the schema {string} array does not contain {string}")
    public void schemaArrayDoesNotContain(String arrayName, String value) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        List<String> array = (List<String>) body.get(arrayName);
        if (array != null) {
            assertThat(array).doesNotContain(value);
        }
    }

    @Then("the schema properties include {string}")
    public void schemaPropertiesInclude(String propertyName) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        assertThat(properties).containsKey(propertyName);
    }

    @Then("the schema property {string} allows exactly the values {string}, {string}, {string}")
    public void schemaPropertyAllowsExactly(String property, String v1, String v2, String v3) throws Exception {
        Map<String, Object> body = objectMapper.readValue(
                state.lastResponse.getBody(), new TypeReference<>() {});
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) body.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> prop = (Map<String, Object>) properties.get(property);
        @SuppressWarnings("unchecked")
        List<String> enumValues = (List<String>) prop.get("enum");
        assertThat(enumValues).containsExactlyInAnyOrder(v1, v2, v3);
    }
}
