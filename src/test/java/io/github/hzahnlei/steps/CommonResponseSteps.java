package io.github.hzahnlei.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonResponseSteps {

    @Autowired
    private ScenarioState state;

    @Then("the response status is {int}")
    public void responseStatusIs(int expectedStatus) {
        assertThat(state.lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }
}
